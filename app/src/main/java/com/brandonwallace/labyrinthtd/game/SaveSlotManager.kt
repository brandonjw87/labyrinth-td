package com.brandonwallace.labyrinthtd.game

import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SaveSlotSummary(
    val slotId: Int, // 1..6
    val label: String,
    val isOccupied: Boolean,
    val isActiveSlot: Boolean,
    val wave: Int,
    val ageNum: Int,
    val ageTitle: String,
    val ageBadge: String,
    val gold: Long,
    val bankBalance: Long,
    val towerCount: Int,
    val totalKills: Long,
    val lastSavedTimestamp: String,
    val savedAtMillis: Long = if (isOccupied) System.currentTimeMillis() else 0L
) {
    val hasData: Boolean get() = isOccupied
}

/**
 * MULTI-SLOT CAMPAIGN SAVE MANAGER (6 Save Slots + Seamless Migration of Existing Saves)
 * Ensures that any current game stored in SharedPreferences ("state_v1" / "state_backup_v1")
 * is automatically migrated into Slot 1 and upgraded into the 18-Age / 4-Path / 8-Tier system
 * without losing any player progress!
 */
object SaveSlotManager {
    const val SLOT_COUNT = 6
    const val MAX_SLOTS = SLOT_COUNT
    private const val KEY_ACTIVE_SLOT = "active_save_slot_v5"
    private const val KEY_MIGRATED_V5 = "migrated_legacy_to_slots_v5"
    private const val PREFIX_SLOT_DATA = "slot_state_data_"
    private const val PREFIX_SLOT_META = "slot_state_meta_"
    private const val PREFIX_SLOT_LABEL = "slot_custom_label_"

    fun getActiveSlotId(prefs: SharedPreferences): Int =
        prefs.getInt(KEY_ACTIVE_SLOT, 1).coerceIn(1, SLOT_COUNT)

    fun getActiveSlot(prefs: SharedPreferences): Int = getActiveSlotId(prefs)

    fun setActiveSlotId(prefs: SharedPreferences, slotId: Int) {
        prefs.edit().putInt(KEY_ACTIVE_SLOT, slotId.coerceIn(1, SLOT_COUNT)).apply()
    }

    fun setActiveSlot(prefs: SharedPreferences, slotId: Int) = setActiveSlotId(prefs, slotId)

    /**
     * Migrates any pre-existing save ("state_v1" or "state_backup_v1") into Slot 1
     * so current games seamlessly upgrade into the expanded 18-Age / 8-Tier architecture.
     */
    fun ensureLegacySaveMigrated(prefs: SharedPreferences) {
        val existingSlot1 = prefs.getString("${PREFIX_SLOT_DATA}1", null)
        val legacyPrimary = prefs.getString("state_v1", null)
        val legacyBackup = prefs.getString("state_backup_v1", null)
        val bestLegacy = if (!legacyPrimary.isNullOrBlank()) legacyPrimary else legacyBackup

        if (existingSlot1.isNullOrBlank() && !bestLegacy.isNullOrBlank()) {
            val meta = extractMetaFromSerializedState(bestLegacy)
            prefs.edit()
                .putString("${PREFIX_SLOT_DATA}1", bestLegacy)
                .putString("${PREFIX_SLOT_META}1", meta)
                .putString("${PREFIX_SLOT_LABEL}1", "Main Campaign (Upgraded)")
                .putBoolean(KEY_MIGRATED_V5, true)
                .apply()
        }
    }

    fun saveSlotData(
        prefs: SharedPreferences,
        slotId: Int,
        serializedState: String,
        customLabel: String? = null
    ) {
        val id = slotId.coerceIn(1, SLOT_COUNT)
        val meta = extractMetaFromSerializedState(serializedState)
        prefs.edit().apply {
            putString("$PREFIX_SLOT_DATA$id", serializedState)
            putString("$PREFIX_SLOT_META$id", meta)
            if (!customLabel.isNullOrBlank()) {
                putString("$PREFIX_SLOT_LABEL$id", customLabel)
            }
            // Also keep "state_v1" mirrored to the active slot for backward safety
            putString("state_v1", serializedState)
            putInt(KEY_ACTIVE_SLOT, id)
            apply()
        }
    }

    fun loadSlotData(prefs: SharedPreferences, slotId: Int): String? {
        ensureLegacySaveMigrated(prefs)
        val id = slotId.coerceIn(1, SLOT_COUNT)
        val slotData = prefs.getString("$PREFIX_SLOT_DATA$id", null)
        if (!slotData.isNullOrBlank()) return slotData
        if (id == 1) {
            return prefs.getString("state_v1", null) ?: prefs.getString("state_backup_v1", null)
        }
        return null
    }

    fun clearSlot(prefs: SharedPreferences, slotId: Int) {
        val id = slotId.coerceIn(1, SLOT_COUNT)
        prefs.edit()
            .remove("$PREFIX_SLOT_DATA$id")
            .remove("$PREFIX_SLOT_META$id")
            .putString("$PREFIX_SLOT_LABEL$id", "Campaign Slot $id")
            .apply()
    }

    fun saveToSlot(
        prefs: SharedPreferences,
        slotId: Int,
        serializedState: String,
        wave: Int = 1,
        ageNum: Int = 1,
        ageLevel: Int = 1,
        ageTitle: String = "",
        gold: Long = 0L,
        bankBalance: Long = 0L,
        towerCount: Int = 0,
        totalKills: Long = 0L,
        label: String? = null,
        customLabel: String? = null
    ) {
        saveSlotData(prefs, slotId, serializedState, customLabel ?: label)
    }

    fun loadRawFromSlot(prefs: SharedPreferences, slotId: Int): String? =
        loadSlotData(prefs, slotId)

    fun cloneSlot(prefs: SharedPreferences, fromSlot: Int, toSlot: Int): Boolean {
        val raw = loadSlotData(prefs, fromSlot) ?: return false
        saveSlotData(prefs, toSlot, raw, "Cloned from Slot $fromSlot")
        return true
    }

    fun listSlots(prefs: SharedPreferences): List<SaveSlotSummary> =
        listSlotSummaries(prefs, getActiveSlotId(prefs))

    fun listSlotSummaries(prefs: SharedPreferences, activeSlotId: Int): List<SaveSlotSummary> {
        ensureLegacySaveMigrated(prefs)
        return (1..SLOT_COUNT).map { id ->
            val rawData = prefs.getString("$PREFIX_SLOT_DATA$id", null)
            val rawMeta = prefs.getString("$PREFIX_SLOT_META$id", null)
            val label = prefs.getString("$PREFIX_SLOT_LABEL$id", null) ?: "Campaign Slot $id"
            if (rawData.isNullOrBlank()) {
                SaveSlotSummary(
                    slotId = id,
                    label = label,
                    isOccupied = false,
                    isActiveSlot = id == activeSlotId,
                    wave = 1,
                    ageNum = 1,
                    ageTitle = "Empty Slot",
                    ageBadge = "EMPTY",
                    gold = 0L,
                    bankBalance = 0L,
                    towerCount = 0,
                    totalKills = 0L,
                    lastSavedTimestamp = "Tap 'Save Here' or 'New Run'"
                )
            } else {
                val parsed = parseMetaString(rawMeta ?: extractMetaFromSerializedState(rawData))
                val phase = ClearancePhase.forAgeLevel(parsed.ageNum)
                SaveSlotSummary(
                    slotId = id,
                    label = label,
                    isOccupied = true,
                    isActiveSlot = id == activeSlotId,
                    wave = parsed.wave,
                    ageNum = parsed.ageNum,
                    ageTitle = phase.title,
                    ageBadge = phase.badge,
                    gold = parsed.gold,
                    bankBalance = parsed.bankBalance,
                    towerCount = parsed.towerCount,
                    totalKills = parsed.totalKills,
                    lastSavedTimestamp = parsed.timestamp
                )
            }
        }
    }

    private data class ParsedMeta(
        val wave: Int,
        val ageNum: Int,
        val gold: Long,
        val bankBalance: Long,
        val towerCount: Int,
        val totalKills: Long,
        val timestamp: String
    )

    private fun extractMetaFromSerializedState(serialized: String): String {
        val map = mutableMapOf<String, String>()
        for (entry in serialized.split(";")) {
            val idx = entry.indexOf('=')
            if (idx > 0) {
                map[entry.substring(0, idx)] = entry.substring(idx + 1)
            }
        }
        val wave = map["wave"]?.toIntOrNull() ?: 1
        val manualAge = map["manualAge"]?.toIntOrNull() ?: 1
        val naturalAge = ClearancePhase.naturalAgeForWave(wave)
        val effAge = maxOf(manualAge, naturalAge).coerceIn(1, EraEvolutionCatalog.MAX_AGE_NUM)
        val gold = map["gold"]?.toLongOrNull() ?: 650L
        val bank = map["bankBalance"]?.toLongOrNull() ?: 250L
        val kills = map["kills"]?.toLongOrNull() ?: 0L
        val towersStr = map["towers"].orEmpty()
        val towerCount = if (towersStr.isBlank()) 0 else towersStr.split("|").count { it.isNotBlank() }
        val sdf = SimpleDateFormat("MMM d, HH:mm", Locale.US)
        val ts = sdf.format(Date())
        return "w=$wave|a=$effAge|g=$gold|b=$bank|t=$towerCount|k=$kills|ts=$ts"
    }

    private fun parseMetaString(meta: String): ParsedMeta {
        val m = mutableMapOf<String, String>()
        for (part in meta.split("|")) {
            val idx = part.indexOf('=')
            if (idx > 0) {
                m[part.substring(0, idx)] = part.substring(idx + 1)
            }
        }
        return ParsedMeta(
            wave = m["w"]?.toIntOrNull() ?: 1,
            ageNum = m["a"]?.toIntOrNull() ?: 1,
            gold = m["g"]?.toLongOrNull() ?: 650L,
            bankBalance = m["b"]?.toLongOrNull() ?: 250L,
            towerCount = m["t"]?.toIntOrNull() ?: 0,
            totalKills = m["k"]?.toLongOrNull() ?: 0L,
            timestamp = m["ts"] ?: "Saved"
        )
    }
}

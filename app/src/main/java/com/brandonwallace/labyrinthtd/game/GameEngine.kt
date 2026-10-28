package com.brandonwallace.labyrinthtd.game

import android.content.SharedPreferences
import com.brandonwallace.labyrinthtd.audio.SoundEngine
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.random.Random

data class TowerInstance(
    val padId: Int,
    var type: TowerType,
    var x: Float = LabyrinthMap.buildPads.getOrNull(padId)?.x ?: 500f,
    var y: Float = LabyrinthMap.buildPads.getOrNull(padId)?.y ?: 500f,
    var terrainBonus: PadBonusType = LabyrinthMap.buildPads.getOrNull(padId)?.bonus ?: PadBonusType.STANDARD,
    var multiPassCount: Int = LabyrinthMap.buildPads.getOrNull(padId)?.multiPassRating ?: 2,
    var pathA: Int = 0,
    var pathB: Int = 0,
    var pathC: Int = 0,
    var pathD: Int = 0, // Path D: Era & Cosmic Overdrive line (8 tiers, Age-gated)
    var targeting: TargetingPriority = TargetingPriority.FIRST,
    var gear: TowerGearAttachment = TowerGearAttachment.NONE,
    var gearTier: Int = 1,
    var xp: Long = 0L,
    var ascensionStars: Int = 0,
    var doctrineCooldown: Float = 5.0f,
    var cooldown: Float = 0f,
    var turretAngleRad: Float = 0f,
    var recoilAnim: Float = 0f,
    var spinUpBonus: Float = 0f,
    var shotsFired: Int = 0,
    var totalDamageDealt: Double = 0.0,
    var totalKills: Int = 0,
    var totalReactionsTriggered: Int = 0,
    var investedGold: Long = 0L
) {
    val totalLevel: Int get() = 1 + pathA + pathB + pathC + pathD
    val maxSinglePath: Int get() = max(max(pathA, pathB), max(pathC, pathD))

    fun tierOf(pathIndex: Int): Int = when (pathIndex) {
        0 -> pathA
        1 -> pathB
        2 -> pathC
        else -> pathD
    }

    fun setTier(pathIndex: Int, value: Int) {
        when (pathIndex) {
            0 -> pathA = value
            1 -> pathB = value
            2 -> pathC = value
            else -> pathD = value
        }
    }

    /** Combined multiplier for reaching Tier 5 / 6 / 7 / 8 on any upgrade line. */
    val apexTierDamageMult: Float
        get() = when {
            maxSinglePath >= 8 -> 5.0f
            maxSinglePath >= 7 -> 3.2f
            maxSinglePath >= 6 -> 2.2f
            maxSinglePath >= 5 -> 1.65f
            else -> 1.0f
        }
    val veteranRank: VeteranRankSpec get() = TowerVeteranCatalog.rankForXp(xp)
    val canAscend: Boolean get() = ascensionStars < TowerAscensionCatalog.MAX_STARS && (totalLevel >= 6 || maxSinglePath >= 5 || type.isFusedApex)
}

data class ActiveEnemy(
    val id: Long,
    val type: EnemyType,
    var distanceOnPath: Float,
    var maxHp: Float,
    var hp: Float,
    var maxShield: Float,
    var shield: Float,
    var armor: Float,
    var baseSpeed: Float,
    val eliteAffix: EnemyEliteAffix = EnemyEliteAffix.NONE,
    val statusTimers: MutableMap<StatusEffect, Float> = mutableMapOf(),
    val statusStacks: MutableMap<StatusEffect, Int> = mutableMapOf(),
    var camorevealed: Boolean = false,
    var hitFlash: Float = 0f,
    var healPulseCooldown: Float = 2.0f,
    var rewindCooldown: Float = 0f
) {
    var x: Float = 0f
        private set
    var y: Float = 0f
        private set
    var dirX: Float = 1f
        private set
    var dirY: Float = 0f
        private set

    init {
        syncPosition()
    }

    fun syncPosition() {
        val pos = LabyrinthMap.positionAtDistance(distanceOnPath)
        val dir = LabyrinthMap.directionAtDistance(distanceOnPath)
        x = pos.x
        y = pos.y
        dirX = dir.x
        dirY = dir.y
    }

    val isCamoHidden: Boolean
        get() = type.isCamo && !camorevealed && !statusTimers.containsKey(StatusEffect.MARKED)
}

data class VisualProjectile(
    var x: Float,
    var y: Float,
    val startX: Float,
    val startY: Float,
    val targetX: Float,
    val targetY: Float,
    var progress: Float,
    val speed: Float,
    val colorArgb: Long,
    val radius: Float,
    val isBeam: Boolean = false,
    val isArc: Boolean = false,
    val visualStyle: Int = 0,
    val secondaryColorArgb: Long = 0xFFFFDF73L
)

data class VisualCallout(
    val text: String,
    val x: Float,
    var y: Float,
    val colorArgb: Long,
    var life: Float = 1.15f,
    val maxLife: Float = 1.15f,
    val isCombo: Boolean = false
)

data class VisualShockwave(
    val x: Float,
    val y: Float,
    val maxRadius: Float,
    val colorArgb: Long,
    var progress: Float = 0f,
    val visualStyle: Int = 0
)

data class SynergyLink(
    val fromPadId: Int,
    val toPadId: Int,
    val fromX: Float,
    val fromY: Float,
    val toX: Float,
    val toY: Float,
    val label: String,
    val colorArgb: Long,
    val isFusionReady: Boolean = false
)

data class FusionCandidate(
    val primaryPadId: Int,
    val partnerPadId: Int,
    val resultType: TowerType,
    val fusionCost: Long
)

data class UiSnapshot(
    val gold: Long,
    val bankBalance: Long,
    val bankVaultTier: Int,
    val bankVaultName: String,
    val bankWaveInterestPct: Double,
    val bankTickInterestPct: Double,
    val bankVaultCap: Long,
    val bankNextTierCost: Long?,
    val totalBankInterestEarned: Long,
    val lastBankInterestPayout: Long,
    val bankAutoDepositPct: Int,
    val bankOverdraftProtection: Boolean,
    val bondPrincipal: Long,
    val bondWavesLeft: Int,
    val techShards: Int,
    val wave: Int,
    val clearancePhase: ClearancePhase,
    val nextAgeAdvanceCost: Long?,
    val waveMutator: WaveMutator,
    val incomingThreatTip: String,
    val streamlinedUiMode: Boolean,
    val unlockAllFeatures: Boolean,
    val recommendedPadId: Int?,
    val smartActionLabel: String,
    val smartActionCost: Long,
    val enemiesAlive: Int,
    val coreShield: Int,
    val maxCoreShield: Int,
    val coreRebounds: Int,
    val flawlessStreak: Int,
    val totalKills: Long,
    val totalCombosTriggered: Long,
    val currentDps: Double,
    val gameSpeed: Float,
    val selectedPadId: Int?,
    val selectedPad: BuildPad?,
    val selectedTower: TowerInstance?,
    val selectedTowerAscendCost: Long?,
    val selectedGadgetSlotId: Int?,
    val selectedBuildType: TowerType,
    val buildFilterCategory: TowerCategory?,
    val fusionCandidate: FusionCandidate?,
    val weather: WeatherFront,
    val weatherTimerSec: Float,
    val showSynergyOverlay: Boolean,
    val showHeatmapOverlay: Boolean,
    val autoBuild: Boolean,
    val autoUpgrade: Boolean,
    val autoAbilities: Boolean,
    val autoWaveRush: Boolean,
    val abilityCooldowns: Map<CommanderAbility, Float>,
    val overclockActiveSec: Float,
    val techRanks: Map<String, Int>,
    val towerMasteryRanks: Map<TowerType, Int>,
    val recentReactionsLog: List<String>,
    // Cheat flags
    val cheatInfiniteGold: Boolean,
    val cheatOmniPath555: Boolean,
    val cheatHyperSpeed: Boolean,
    val cheatOneShotKill: Boolean,
    val cheatZeroCost: Boolean,
    val cheatGodCore: Boolean,
    val cheatFreezeEnemies: Boolean,
    // Optional Tutorial Mode state
    val isTutorialMode: Boolean,
    val tutorialStep: Int,
    val tutorialTitle: String,
    val tutorialInstruction: String,
    val isMuted: Boolean,
    val activeSaveSlotId: Int = 1,
    val lastEraEvolutionMessage: String = "",
    val activeEdicts: List<CivilizationEdictSpec> = emptyList()
)

class GameEngine(private val soundEngine: SoundEngine? = null) {
    companion object {
        const val PREFS_NAME = "labyrinth_td_save"
        const val SAVE_SCHEMA_VERSION = 5
        /** Oldest save schema that can be migrated forward (v4 = free-placement saves). */
        const val MIN_MIGRATABLE_SCHEMA = 4
        // Hidden compound interest per wave on unspent gold (Brandon's signature hidden mechanic)
        private const val HIDDEN_WAVE_INTEREST_RATE = 0.16769
        // Money no longer stops at 9 Trillion: saturating 64-bit ceiling of ~4.5 Quintillion.
        private const val MAX_SAFE_GOLD = CurrencyMath.MAX_TREASURY_GOLD

        /** Age required to buy Tier 6 / 7 / 8 on Paths A-C. */
        fun requiredAgeForTier(tier: Int): Int = when {
            tier <= 5 -> 1
            tier == 6 -> 7   // Gunpowder Age
            tier == 7 -> 10  // Atomic Age
            else -> 13       // Fusion Age
        }

        /** Age required for each tier of Path D (Era & Cosmic Overdrive). */
        fun requiredAgeForPathDTier(tier: Int): Int = when (tier) {
            1 -> 2
            2 -> 4
            3 -> 5
            4 -> 7
            5 -> 8
            6 -> 10
            7 -> 12
            else -> 14
        }

        /** Age required to forge each armory attachment (future gear unlocks with future Ages). */
        fun requiredAgeForGear(att: TowerGearAttachment): Int = when (att) {
            TowerGearAttachment.BESSEMER_STEAM_TURBINE -> 8
            TowerGearAttachment.TESLA_CAPACITOR_BANK -> 9
            TowerGearAttachment.DEPLETED_URANIUM_SABOT -> 10
            TowerGearAttachment.SILICON_RADAR_UPLINK -> 11
            TowerGearAttachment.CYBER_NANITE_INJECTOR -> 12
            TowerGearAttachment.FUSION_PLASMA_COIL -> 13
            TowerGearAttachment.GRAVITON_SINGULARITY_LENS -> 14
            TowerGearAttachment.DYSON_STELLAR_CONDUIT -> 15
            TowerGearAttachment.QUANTUM_ZERO_POINT_CELL -> 16
            TowerGearAttachment.OMEGA_CHRONO_CROWN -> 18
            else -> 1
        }

        /** Starfleet Age (XIV) unlocks Omni-Crosspathing across Paths A, B and C. */
        const val OMNI_CROSSPATH_AGE = 14

        fun romanNumeral(n: Int): String {
            val values = intArrayOf(10, 9, 5, 4, 1)
            val symbols = arrayOf("X", "IX", "V", "IV", "I")
            var num = n.coerceIn(1, 39)
            val sb = StringBuilder()
            for (i in values.indices) {
                while (num >= values[i]) {
                    sb.append(symbols[i])
                    num -= values[i]
                }
            }
            return sb.toString()
        }
    }

    private val lock = Any()

    // Core progression state
    var gold: Long = 650L
    var techShards: Int = 12
    var wave: Int = 1
    var civilizationAgeLevel: Int = 1 // 1=Dark Age ... 6=Golden Age ... 10=Atomic ... 14=Starfleet ... 18=Omega Age
    var activeSaveSlotId: Int = 1
    /** Last tower evolution message shown after an Age-Up (e.g. "12 towers evolved into Steam Gatling Pillbox..."). */
    var lastEraEvolutionMessage: String = ""
    var coreShield: Int = 100
    var maxCoreShield: Int = 100
    var coreRebounds: Int = 0
    var flawlessStreak: Int = 0
    var waveHadBreach: Boolean = false
    var totalKills: Long = 0L
    var totalCombosTriggered: Long = 0L

    // Royal Treasury Bank & Compound Interest Account
    var bankBalance: Long = 250L
    var bankVaultTier: Int = 1
    var totalBankInterestEarned: Long = 0L
    var lastBankInterestPayout: Long = 0L
    var bankAutoDepositPct: Int = 0 // 0, 25, or 50
    var bankOverdraftProtection: Boolean = true
    var bondPrincipal: Long = 0L
    var bondWavesLeft: Int = 0
    private var bankInterestTickTimer: Float = 0f

    // Progressive Learning Curve & Adaptive GUI state
    var streamlinedUiMode: Boolean = true
    var unlockAllFeatures: Boolean = false
    var recommendedPadId: Int? = 18

    // Simulation & UI selection (selectedPadId acts as the selected Tower ID; null = Free-Place Build Mode!)
    var gameSpeed: Float = 1.0f
    var selectedPadId: Int? = null
    var selectedGadgetSlotId: Int? = null
    var selectedBuildType: TowerType = TowerType.GATLING
    var buildFilterCategory: TowerCategory? = null
    var showSynergyOverlay: Boolean = false
    var showHeatmapOverlay: Boolean = false
    var showFloatingCallouts: Boolean = false // Keep map clean of floating text banners by default

    // AI Auto-Governor toggles
    var autoBuild: Boolean = false
    var autoUpgrade: Boolean = false
    var autoAbilities: Boolean = false
    var autoWaveRush: Boolean = false
    private var aiDecisionTimer: Float = 0f

    // Cheat flags ("KING'S CONSOLE")
    var cheatInfiniteGold: Boolean = false
    var cheatOmniPath555: Boolean = false
    var cheatHyperSpeed: Boolean = false
    var cheatOneShotKill: Boolean = false
    var cheatZeroCost: Boolean = false
    var cheatGodCore: Boolean = false
    var cheatFreezeEnemies: Boolean = false

    // Optional Tutorial Mode (ONLY entered when player explicitly requests it)
    var isTutorialMode: Boolean = false
    var tutorialStep: Int = 0
    private var savedEndlessSnapshot: String? = null

    // Entities on the map (NO pre-placed towers at start!)
    private var nextFreeTowerId: Int = 100
    val towers = mutableMapOf<Int, TowerInstance>()
    val trackGadgets = mutableMapOf<Int, TrackGadgetType>()
    val enemies = mutableListOf<ActiveEnemy>()
    val projectiles = mutableListOf<VisualProjectile>()
    val callouts = mutableListOf<VisualCallout>()
    val shockwaves = mutableListOf<VisualShockwave>()
    val synergyLinks = mutableListOf<SynergyLink>()
    val techRanks = mutableMapOf<String, Int>()
    val towerMasteryRanks = mutableMapOf<TowerType, Int>()
    val abilityCooldowns = mutableMapOf<CommanderAbility, Float>()
    val recentReactionsLog = ArrayDeque<String>(8)

    // Weather & Timers
    var currentWeather: WeatherFront = WeatherFront.CLEAR_OVERWATCH
    var weatherTimerSec: Float = 45f
    private var monsoonRainPulseTimer: Float = 0f
    var overclockActiveSec: Float = 0f
    private var mintIncomeTimer: Float = 0f

    // Wave spawning state
    private var nextEnemyId: Long = 1L
    private val spawnQueue = ArrayDeque<EnemyType>()
    private var spawnIntervalTimer: Float = 2.0f
    private var waveCooldownTimer: Float = 2.5f

    // Rolling DPS tracker
    private var damageThisWindow: Double = 0.0
    private var dpsWindowTimer: Float = 0f
    var rollingDps: Double = 0.0

    init {
        for (ability in CommanderAbility.entries) {
            abilityCooldowns[ability] = 0f
        }
        for (node in TechTreeCatalog.nodes) {
            techRanks[node.id] = 0
        }
        for (tType in TowerType.entries) {
            towerMasteryRanks[tType] = 0
        }
        // Start with a clean, untouched green lawn meadow (zero pre-placed towers!)
        towers.clear()
        rebuildSynergyLinks()
        recommendedPadId = computeRecommendedEmptyPadId()
        queueWaveEnemies(1)
    }

    fun effectiveAgeLevel(): Int =
        max(civilizationAgeLevel, ClearancePhase.naturalAgeForWave(wave)).coerceIn(1, ClearancePhase.entries.size)

    fun currentClearancePhase(): ClearancePhase =
        ClearancePhase.forAgeLevel(effectiveAgeLevel())

    fun nextAgeAdvanceCost(): Long? {
        val curAge = effectiveAgeLevel()
        if (curAge >= ClearancePhase.entries.size) return null
        val nextPhase = ClearancePhase.forAgeLevel(curAge + 1)
        return effectiveCost(nextPhase.advanceCostGold)
    }

    /**
     * Age of Empires 1-Tap Advancement:
     * Advances the player's settlement to the next Historical Age right on the map!
     */
    fun advanceCivilizationAge(): Boolean = synchronized(lock) {
        val curAge = effectiveAgeLevel()
        if (curAge >= ClearancePhase.entries.size) return false
        val nextAge = curAge + 1
        val nextPhase = ClearancePhase.forAgeLevel(nextAge)
        val cost = effectiveCost(nextPhase.advanceCostGold)
        if (!spendGoldWithOverdraft(cost)) return false

        val prevBonusHp = currentClearancePhase().bonusMaxCastleHp
        civilizationAgeLevel = nextAge
        val wallTechBonus = 50 * (techRanks["ARC_KEEP"] ?: 0)
        val hpGain = (nextPhase.bonusMaxCastleHp - prevBonusHp).coerceAtLeast(15)
        maxCoreShield = 100 + nextPhase.bonusMaxCastleHp + wallTechBonus
        coreShield = (coreShield + hpGain).coerceAtMost(maxCoreShield)

        // Newly unlocked buildings get pre-selected so the player sees what the Age unlocked
        val newlyUnlocked = when (nextAge) {
            2 -> TowerType.CRYO
            3 -> TowerType.PYRO
            4 -> TowerType.RAIL_CANNON
            else -> null
        }
        if (newlyUnlocked != null) selectedBuildType = newlyUnlocked
        recommendedPadId = computeRecommendedEmptyPadId()

        // AGE-UP UPGRADES EVERY TOWER: re-evaluate range coverage with the new era multipliers
        evolveAllTowersToCurrentEra()

        val keepPos = LabyrinthMap.waypoints.last()
        shockwaves.add(VisualShockwave(keepPos.x, keepPos.y, 260f, nextPhase.accentColor))
        for (t in towers.values) {
            shockwaves.add(VisualShockwave(t.x, t.y, 46f, nextPhase.accentColor))
        }
        recordReactionLog("TOWN CENTER: Advanced to ${nextPhase.badge} (${nextPhase.title})!")
        soundEngine?.play(SoundEngine.Sfx.FUSION_APEX)
        return true
    }

    /** Era evolution profile (name, damage, speed, range, pierce) for a tower type in the current Age. */
    fun eraProfileFor(type: TowerType): EraTowerEvolutionProfile =
        EraEvolutionCatalog.profileFor(type, effectiveAgeLevel())

    /** Multiplier applied to era evolution gains by the "Historical-to-Future Evolution" tech + edicts. */
    private fun eraScalingBoost(): Float {
        val techBoost = 0.20f * (techRanks["ARC_ERA"] ?: 0)
        val edictBoost = activeEdicts().sumOf { it.towerAgeUpgradeScalingBonus.toDouble() }.toFloat()
        return 1f + techBoost + edictBoost
    }

    /** Future & Cosmic Sciences tech branch: per-family damage multipliers. */
    private fun futureTechDamageMult(type: TowerType): Float {
        val rail = techRanks["FUT_RAIL"] ?: 0
        val photon = techRanks["FUT_PHOTON"] ?: 0
        val grav = techRanks["FUT_GRAV"] ?: 0
        val nano = techRanks["FUT_NANO"] ?: 0
        return when (type) {
            TowerType.RAIL_CANNON, TowerType.SNIPER, TowerType.GATLING,
            TowerType.LEVIATHAN_GAUSS_CITADEL, TowerType.TACHYON_RAILGUN -> 1f + 0.25f * rail
            TowerType.LASER_ARRAY, TowerType.PRISM, TowerType.DYSON_HELIOS_ARRAY,
            TowerType.QUANTUM_CHRONO_MATRIX, TowerType.ASTRAL_NEXUS -> 1f + 0.25f * photon
            TowerType.GRAVITY_WELL, TowerType.CHRONO, TowerType.OMEGA_SINGULARITY_CORE,
            TowerType.CHRONOS_OBSERVATORY -> 1f + 0.25f * grav
            TowerType.VENOM, TowerType.PYRO, TowerType.DRONE, TowerType.NANITE_SWARM_HIVE,
            TowerType.ALCHEMICAL_CATHEDRAL, TowerType.PLAGUEBRINGER_VULCAN -> 1f + 0.25f * nano
            else -> 1f
        }
    }

    /** Era damage multiplier with tech/edict amplification (amplifies only the gain above 1.0x). */
    private fun eraDamageMult(type: TowerType): Float {
        val raw = eraProfileFor(type).damageMult
        return 1f + (raw - 1f) * eraScalingBoost()
    }

    /**
     * All Civilization Edicts unlocked up to the current Age are always in force.
     * "Imperial Senate Decrees" tech amplifies their effect.
     */
    fun activeEdicts(): List<CivilizationEdictSpec> {
        val age = effectiveAgeLevel()
        return EraEvolutionCatalog.edicts.filter { it.unlockAgeNum <= age }
    }

    private fun edictPotency(): Float = 1f + 0.15f * (techRanks["ARC_EDICT"] ?: 0)

    private fun edictDamageMult(): Float =
        1f + activeEdicts().sumOf { it.globalDamageBonus.toDouble() }.toFloat() * edictPotency()

    private fun edictFireRateMult(): Float =
        1f + activeEdicts().sumOf { it.globalFireRateBonus.toDouble() }.toFloat() * edictPotency()

    private fun edictRangeMult(): Float =
        1f + activeEdicts().sumOf { it.globalRangeBonus.toDouble() }.toFloat() * edictPotency()

    private fun edictCritBonus(): Float =
        activeEdicts().sumOf { it.globalCritChanceBonus.toDouble() }.toFloat() * edictPotency()

    private fun edictPierceBonus(): Float =
        (activeEdicts().sumOf { it.globalArmorPierceBonus.toDouble() }.toFloat() * edictPotency()).coerceAtMost(0.60f)

    private fun edictKillGoldMult(): Float =
        1f + activeEdicts().sumOf { it.killGoldBonus.toDouble() }.toFloat() * edictPotency()

    private fun edictBankBonus(): Double =
        activeEdicts().sumOf { it.bankInterestBonus } * edictPotency()

    /**
     * Upgrades every placed tower into the current Age: refreshes crossfire coverage for the new
     * (larger) era range and records a clear summary of what the towers evolved into.
     */
    private fun evolveAllTowersToCurrentEra() {
        val age = effectiveAgeLevel()
        val evolvedNames = linkedSetOf<String>()
        for (t in towers.values) {
            t.multiPassCount = LabyrinthMap.countRoadPassesInRange(t.x, t.y, effectiveTowerRange(t))
            t.doctrineCooldown = min(t.doctrineCooldown, 1.5f)
            evolvedNames.add(EraEvolutionCatalog.profileFor(t.type, age).evolvedShortName)
        }
        rebuildSynergyLinks()
        lastEraEvolutionMessage = if (towers.isEmpty()) {
            "All future towers will be built as ${ClearancePhase.forAgeLevel(age).title} designs."
        } else {
            val sample = evolvedNames.take(3).joinToString(", ")
            "${towers.size} towers evolved: $sample${if (evolvedNames.size > 3) "…" else ""}"
        }
        recordReactionLog(lastEraEvolutionMessage)
    }

    fun stepSimulation(rawDt: Float) = synchronized(lock) {
        val dt = (rawDt.coerceIn(0.001f, 0.1f)) * gameSpeed
        if (cheatInfiniteGold && gold < 999_999_999_999_999_999L) {
            gold = 999_999_999_999_999_999L
        }

        // Sync maxCoreShield with current Age + Fortified Keep Walls tech + Bastion Keeps
        val wallTechBonus = 50 * (techRanks["ARC_KEEP"] ?: 0)
        val bastionBonus = towers.values.sumOf { t ->
            when {
                t.type == TowerType.AEGIS_ORBITAL_BASTION -> 250
                t.type == TowerType.SHIELD_GARRISON && t.pathA >= 5 -> 120
                t.type == TowerType.SHIELD_GARRISON && t.pathA >= 3 -> 50
                else -> 0
            }
        }
        val desiredMaxHp = 100 + currentClearancePhase().bonusMaxCastleHp + wallTechBonus + bastionBonus
        if (maxCoreShield < desiredMaxHp) {
            val diff = desiredMaxHp - maxCoreShield
            maxCoreShield = desiredMaxHp
            coreShield = (coreShield + diff).coerceAtMost(maxCoreShield)
        }

        // Update DPS window
        dpsWindowTimer += rawDt
        if (dpsWindowTimer >= 1.0f) {
            rollingDps = rollingDps * 0.4 + (damageThisWindow / dpsWindowTimer) * 0.6
            damageThisWindow = 0.0
            dpsWindowTimer = 0f
        }

        // Ability cooldowns & Rallying Horn Overclock
        for (ab in CommanderAbility.entries) {
            val cd = abilityCooldowns[ab] ?: 0f
            if (cd > 0f) abilityCooldowns[ab] = max(0f, cd - rawDt)
        }
        if (overclockActiveSec > 0f) {
            overclockActiveSec = max(0f, overclockActiveSec - rawDt)
        }

        // Weather cycle
        weatherTimerSec -= rawDt
        if (weatherTimerSec <= 0f) {
            val all = WeatherFront.entries
            currentWeather = all[(currentWeather.ordinal + 1) % all.size]
            weatherTimerSec = 42f
        }
        if (currentWeather == WeatherFront.MONSOON_SQUALL) {
            monsoonRainPulseTimer += dt
            if (monsoonRainPulseTimer >= 2.5f) {
                monsoonRainPulseTimer = 0f
                for (e in enemies) {
                    if (e.hp > 0f) applyStatus(e, StatusEffect.WET, 4.0f)
                }
            }
        }

        // Passive Gold from Royal Mints, Falconry Roosts, and Grand Wonders
        mintIncomeTimer += dt
        if (mintIncomeTimer >= 1.0f) {
            mintIncomeTimer -= 1.0f
            var passiveGold = 0L
            val mintTechMult = 1f + 0.25f * (techRanks["ECO_MINT"] ?: 0)
            for (t in towers.values) {
                val padBonus = t.terrainBonus.goldMult * mintTechMult
                when (t.type) {
                    TowerType.HUB -> {
                        val base = when (t.pathA) {
                            1, 2 -> 8L
                            3, 4 -> 22L
                            5 -> 65L
                            6 -> 180L
                            7 -> 600L
                            8 -> 2_500L
                            else -> 3L
                        }
                        passiveGold += (base * padBonus * (1 + t.ascensionStars * 0.25f)).roundToLong()
                    }
                    TowerType.ASTRAL_NEXUS -> {
                        passiveGold += (55L * padBonus * (1 + t.ascensionStars * 0.25f)).roundToLong()
                    }
                    TowerType.DYSON_HELIOS_ARRAY -> {
                        passiveGold += (80L * padBonus * (1 + t.ascensionStars * 0.25f)).roundToLong()
                    }
                    TowerType.AEGIS_ORBITAL_BASTION -> {
                        passiveGold += (150L * padBonus * (1 + t.ascensionStars * 0.25f)).roundToLong()
                    }
                    TowerType.SHIELD_GARRISON -> {
                        val base = when (t.pathC) {
                            4 -> 45L
                            5 -> 120L
                            6 -> 120L
                            7 -> 300L
                            8 -> 800L
                            else -> 0L
                        }
                        if (base > 0L) passiveGold += (base * padBonus * (1 + t.ascensionStars * 0.25f)).roundToLong()
                    }
                    TowerType.DRONE -> {
                        if (enemies.isNotEmpty()) {
                            val base = when {
                                t.pathB >= 8 -> 500L
                                t.pathB >= 7 -> 120L
                                t.pathB >= 4 -> 18L
                                else -> 0L
                            }
                            passiveGold += (base * padBonus).roundToLong()
                        }
                    }
                    else -> {}
                }
            }
            if (passiveGold > 0L) {
                addGold(passiveGold)
            }
        }

        // Live Intra-Wave Treasury Interest Tick (every 6 seconds of active combat!)
        if (bankBalance > 0L && (enemies.isNotEmpty() || spawnQueue.isNotEmpty())) {
            bankInterestTickTimer += dt
            if (bankInterestTickTimer >= 6.0f) {
                bankInterestTickTimer -= 6.0f
                val cap = effectiveBankVaultCap()
                if (bankBalance < cap) {
                    val tickGain = CurrencyMath.safeMultiply(bankBalance, effectiveBankLiveTickRate()).coerceAtLeast(1L)
                    val actualGain = min(tickGain, (cap - bankBalance).coerceAtLeast(0L))
                    if (actualGain > 0L) {
                        bankBalance = CurrencyMath.safeAdd(bankBalance, actualGain)
                        totalBankInterestEarned = CurrencyMath.safeAdd(totalBankInterestEarned, actualGain)
                        lastBankInterestPayout = actualGain
                    }
                }
            }
        }

        // Wave spawning & continuous wave streaming
        if (spawnQueue.isNotEmpty()) {
            spawnIntervalTimer -= dt
            if (spawnIntervalTimer <= 0f) {
                val nextType = spawnQueue.removeFirst()
                spawnEnemy(nextType, wave)
                val mut = WaveMutator.forWave(wave)
                val baseInterval = when {
                    nextType.isBoss -> 1.05f
                    nextType == EnemyType.SWARMLING -> 0.28f
                    else -> 0.45f
                }
                val earlyRelax = if (wave <= 3) 1.18f else 1.0f
                spawnIntervalTimer = (baseInterval * earlyRelax) / mut.countMult.coerceAtLeast(0.5f)
            }
        } else if (enemies.isEmpty()) {
            waveCooldownTimer -= dt
            if (waveCooldownTimer <= 0f || autoWaveRush) {
                advanceToNextWave(rushed = autoWaveRush)
            }
        } else if (autoWaveRush && spawnQueue.isEmpty() && enemies.size <= 4) {
            advanceToNextWave(rushed = true)
        }

        // AI Auto-Governor
        if (autoBuild || autoUpgrade || autoAbilities) {
            aiDecisionTimer += rawDt
            if (aiDecisionTimer >= 0.85f) {
                aiDecisionTimer = 0f
                runAiTacticianStep()
            }
        }

        // Sub-step enemies & towers at high speeds (8x / 12x) for 100% combat & movement precision
        val subSteps = if (dt > 0.055f) (dt / 0.045f).toInt().coerceIn(2, 6) else 1
        val subDt = dt / subSteps.toFloat()
        for (sIdx in 0 until subSteps) {
            stepEnemies(subDt)
            stepTowers(subDt)
        }

        // Update visual projectiles, callouts, shockwaves
        val pIter = projectiles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.progress += dt * p.speed
            p.x = p.startX + (p.targetX - p.startX) * p.progress.coerceIn(0f, 1f)
            p.y = p.startY + (p.targetY - p.startY) * p.progress.coerceIn(0f, 1f)
            if (p.progress >= 1f) pIter.remove()
        }

        val cIter = callouts.iterator()
        while (cIter.hasNext()) {
            val c = cIter.next()
            c.life -= rawDt
            c.y -= rawDt * 22f
            if (c.life <= 0f) cIter.remove()
        }

        val sIter = shockwaves.iterator()
        while (sIter.hasNext()) {
            val s = sIter.next()
            s.progress += rawDt * 2.4f
            if (s.progress >= 1f) sIter.remove()
        }
    }

    private fun stepEnemies(dt: Float) {
        val spawnedSplits = mutableListOf<ActiveEnemy>()
        val iter = enemies.iterator()
        val durMult = 1f + 0.15f * (techRanks["ELM_DUR"] ?: 0)
        val dotTechMult = 1f + 0.22f * (techRanks["ELM_DOT"] ?: 0)
        val slowTechBonus = 0.08f * (techRanks["ELM_SLOW"] ?: 0)

        while (iter.hasNext()) {
            val e = iter.next()
            if (e.hp <= 0f) {
                handleEnemyDeath(e, spawnedSplits)
                iter.remove()
                continue
            }
            if (e.hitFlash > 0f) e.hitFlash = max(0f, e.hitFlash - dt * 4f)
            if (e.rewindCooldown > 0f) e.rewindCooldown = max(0f, e.rewindCooldown - dt)

            // Tick status timers & DoTs
            val stIter = e.statusTimers.entries.iterator()
            var speedFactor = 1.0f
            while (stIter.hasNext()) {
                val entry = stIter.next()
                val st = entry.key
                val rem = entry.value - dt / durMult.coerceAtLeast(1f)
                if (rem <= 0f) {
                    stIter.remove()
                    e.statusStacks.remove(st)
                } else {
                    entry.setValue(rem)
                    when (st) {
                        StatusEffect.FROZEN -> speedFactor = 0f
                        StatusEffect.CHILLED -> speedFactor = min(speedFactor, (0.58f - slowTechBonus).coerceAtLeast(0.22f))
                        StatusEffect.WET -> speedFactor = min(speedFactor, (0.85f - slowTechBonus * 0.5f).coerceAtLeast(0.45f))
                        StatusEffect.OILED -> speedFactor = min(speedFactor, (0.72f - slowTechBonus * 0.6f).coerceAtLeast(0.32f))
                        StatusEffect.TIME_ANCHORED -> speedFactor = min(speedFactor, (0.65f - slowTechBonus * 0.7f).coerceAtLeast(0.28f))
                        StatusEffect.BURNING -> {
                            val stacks = (e.statusStacks[st] ?: 1).coerceAtMost(6)
                            val solarMult = if (currentWeather == WeatherFront.SOLAR_FLARE) 1.5f else 1.0f
                            val burnDmg = (18f * stacks + e.maxHp * 0.012f) * solarMult * dotTechMult * dt
                            inflictRawDamage(e, burnDmg, ignoreArmor = true, sourceTower = null)
                        }
                        StatusEffect.POISONED -> {
                            val stacks = (e.statusStacks[st] ?: 1).coerceAtMost(10)
                            val psnDmg = (15f * stacks + e.maxHp * 0.01f) * dotTechMult * dt
                            inflictRawDamage(e, psnDmg, ignoreArmor = true, sourceTower = null)
                        }
                        StatusEffect.BLEEDING -> {
                            val stacks = (e.statusStacks[st] ?: 1).coerceAtMost(6)
                            val bldDmg = (20f * stacks) * dotTechMult * dt
                            inflictRawDamage(e, bldDmg, ignoreArmor = false, sourceTower = null)
                        }
                        else -> {}
                    }
                }
            }

            if (cheatFreezeEnemies) {
                speedFactor = 0f
            }

            // Elite Affix regeneration (unless Burned, Poisoned, or Corroded)
            if (e.eliteAffix.regenPerSecRatio > 0f &&
                !e.statusTimers.containsKey(StatusEffect.BURNING) &&
                !e.statusTimers.containsKey(StatusEffect.POISONED) &&
                !e.statusTimers.containsKey(StatusEffect.CORRODED)
            ) {
                e.hp = min(e.maxHp, e.hp + e.maxHp * e.eliteAffix.regenPerSecRatio * dt)
            }

            // Shield regen if not cracked or shocked
            if (e.maxShield > 0f && e.shield < e.maxShield &&
                !e.statusTimers.containsKey(StatusEffect.CORRODED) &&
                !e.statusTimers.containsKey(StatusEffect.SHOCKED)
            ) {
                e.shield = min(e.maxShield, e.shield + e.maxShield * 0.03f * dt)
            }

            // Battlefield Monk Healer or Warlord Banner aura
            if ((e.type.healsNearby || e.eliteAffix == EnemyEliteAffix.WARLORD_BANNER) &&
                !e.statusTimers.containsKey(StatusEffect.CORRODED)
            ) {
                e.healPulseCooldown -= dt
                if (e.healPulseCooldown <= 0f) {
                    e.healPulseCooldown = 2.2f
                    val ex = e.x
                    val ey = e.y
                    for (ally in enemies) {
                        if (ally.hp > 0f && hypot(ally.x - ex, ally.y - ey) <= 110f) {
                            ally.hp = min(ally.maxHp, ally.hp + ally.maxHp * 0.05f)
                        }
                    }
                }
            }

            // Check On-Road Sapper Fortifications
            val gadgetMult = 1f + 0.25f * (techRanks["ARC_GADGET"] ?: 0)
            for ((slotId, gType) in trackGadgets) {
                val slot = LabyrinthMap.gadgetSlots.getOrNull(slotId) ?: continue
                if (kotlin.math.abs(e.distanceOnPath - slot.pathDistance) <= 28f) {
                    when (gType) {
                        TrackGadgetType.REVERSE_CONVEYOR -> {
                            speedFactor = -0.55f * gadgetMult * (1f - e.eliteAffix.knockbackResist * 0.5f)
                            applyStatus(e, StatusEffect.TIME_ANCHORED, 2.5f)
                        }
                        TrackGadgetType.ELEMENTAL_RUNE -> {
                            applyStatus(e, StatusEffect.WET, 3.5f)
                            applyStatus(e, StatusEffect.OILED, 3.5f)
                            applyStatus(e, StatusEffect.CORRODED, 3.5f)
                        }
                        TrackGadgetType.STASIS_SNARE -> {
                            e.camorevealed = true
                            e.shield = max(0f, e.shield - e.maxShield * 0.25f * dt * gadgetMult)
                            speedFactor = min(speedFactor, 0.32f)
                        }
                        TrackGadgetType.ALCHEMY_TRANSMUTER -> {
                            applyStatus(e, StatusEffect.MARKED, 4.0f)
                        }
                    }
                }
            }

            // Advance along The King's Valley Road
            e.distanceOnPath = (e.distanceOnPath + e.baseSpeed * speedFactor * dt).coerceAtLeast(0f)
            e.syncPosition()

            // Check death from DoTs
            if (e.hp <= 0f) {
                handleEnemyDeath(e, spawnedSplits)
                iter.remove()
                continue
            }

            // Check if enemy reached the Royal Castle Keep at the end of the road
            if (e.distanceOnPath >= LabyrinthMap.totalLength - 5f) {
                if (cheatGodCore) {
                    e.distanceOnPath = 0f
                    e.syncPosition()
                } else {
                    val breachDmg = when {
                        e.type.isBoss -> 30
                        e.eliteAffix != EnemyEliteAffix.NONE -> 15
                        else -> 10
                    }
                    coreShield -= breachDmg
                    waveHadBreach = true
                    flawlessStreak = 0
                    if (coreShield <= 0) {
                        triggerAegisCoreRebound()
                        break
                    } else {
                        // Repulse raider back along the road so towers can finish it off
                        e.distanceOnPath = LabyrinthMap.totalLength * 0.72f
                        e.syncPosition()
                    }
                }
            }
        }

        if (spawnedSplits.isNotEmpty()) {
            enemies.addAll(spawnedSplits)
        }
    }

    private fun finalizeDeadEnemies() {
        if (enemies.isEmpty()) return
        val spawnedSplits = mutableListOf<ActiveEnemy>()
        val iter = enemies.iterator()
        while (iter.hasNext()) {
            val e = iter.next()
            if (e.hp <= 0f) {
                handleEnemyDeath(e, spawnedSplits)
                iter.remove()
            }
        }
        if (spawnedSplits.isNotEmpty()) {
            enemies.addAll(spawnedSplits)
        }
    }

    private fun handleEnemyDeath(e: ActiveEnemy, spawnedSplits: MutableList<ActiveEnemy>) {
        totalKills++
        val mutBounty = WaveMutator.forWave(wave).bountyMult
        var bounty = ((e.type.goldReward + (wave * 2L)) * mutBounty * e.eliteAffix.goldAndXpMult).roundToLong()
        bounty = (bounty * (1f + 0.12f * (techRanks["ECO_BOUNTY"] ?: 0)) * edictKillGoldMult()).roundToLong()
        if (currentWeather == WeatherFront.AETHER_METEOR_SHOWER) {
            bounty = (bounty * 1.75f).roundToLong()
        }
        if (e.statusTimers.containsKey(StatusEffect.MARKED)) {
            bounty = (bounty * 1.35f).roundToLong()
        }
        val ex = e.x
        val ey = e.y
        for ((slotId, gType) in trackGadgets) {
            if (gType == TrackGadgetType.ALCHEMY_TRANSMUTER) {
                val slot = LabyrinthMap.gadgetSlots.getOrNull(slotId)
                if (slot != null && hypot(slot.x - ex, slot.y - ey) <= 65f) {
                    bounty *= 3L
                    break
                }
            }
        }
        val xpMultiplier = 1f + 0.35f * (techRanks["KIN_VET"] ?: 0)
        val killXp = ((e.type.goldReward * 4L + wave * 3L) * e.eliteAffix.goldAndXpMult * xpMultiplier).roundToLong()
        for (t in towers.values) {
            if (hypot(t.x - ex, t.y - ey) <= 230f) {
                t.xp += killXp
                if (t.type == TowerType.HUB && t.pathA >= 2) bounty = (bounty * 1.30f).roundToLong()
                if (t.type == TowerType.DRONE && t.pathB >= 1) bounty = (bounty * 1.35f).roundToLong()
                if (t.type == TowerType.ASTRAL_NEXUS) bounty = (bounty * 1.45f).roundToLong()
                if (t.gear == TowerGearAttachment.ROYAL_TAX_BANNER) bounty = (bounty * 1.25f).roundToLong()
            }
        }
        addGold(bounty)

        if (e.type.isBoss) {
            techShards += 3
            shockwaves.add(VisualShockwave(ex, ey, 140f, 0xFFC75B39))
            if (isTutorialMode && tutorialStep == 4) {
                tutorialStep = 5
            }
        }

        // Siege Trojan Wagon / Bombard Train deploys 3 Foot Raiders on destruction
        if (e.type.splitsOnDeath) {
            for (i in -1..1) {
                val mite = createEnemyInstance(
                    EnemyType.HYDRA_SPAWN,
                    wave,
                    startDist = (e.distanceOnPath + i * 14f).coerceIn(0f, LabyrinthMap.totalLength - 40f)
                )
                spawnedSplits.add(mite)
            }
        }

        // Apothecary Plague spread on death
        if (e.statusTimers.containsKey(StatusEffect.POISONED)) {
            for (other in enemies) {
                if (other.id != e.id && other.hp > 0f && hypot(other.x - ex, other.y - ey) <= 85f) {
                    applyStatus(other, StatusEffect.POISONED, 5.0f)
                }
            }
        }
    }

    /**
     * Royal Keep Garrison Rally: Instead of halting the game on a Game Over screen,
     * the Castle Keep sounds the Royal Horn, repulsing raiders backward and granting an emergency war chest.
     */
    private fun triggerAegisCoreRebound() {
        coreRebounds++
        coreShield = maxCoreShield
        val emergencyGold = 250L + wave * 45L
        addGold(emergencyGold)
        for (e in enemies) {
            if (e.hp <= 0f) continue
            e.distanceOnPath = (e.distanceOnPath * 0.55f).coerceAtLeast(0f)
            e.syncPosition()
            applyStatus(e, StatusEffect.FROZEN, 3.5f)
            applyStatus(e, StatusEffect.TIME_ANCHORED, 5.0f)
            inflictRawDamage(e, e.maxHp * 0.35f, ignoreArmor = true, sourceTower = null)
        }
        finalizeDeadEnemies()
        val corePos = LabyrinthMap.waypoints.last()
        shockwaves.add(VisualShockwave(corePos.x, corePos.y, 420f, 0xFFD4AF37))
        soundEngine?.play(SoundEngine.Sfx.CORE_SURGE)
    }

    /**
     * Computes the effective firing range of a placed tower including its natural terrain bonus,
     * veteran rank, ascension stars, equipped forge gear, mastery rank, and blacksmith tech.
     */
    fun effectiveTowerRange(t: TowerInstance): Float {
        val vet = t.veteranRank
        val gearTierScale = 1f + 0.25f * (t.gearTier - 1)
        val gearRangeMult = 1f + (t.gear.rangeMult - 1f) * gearTierScale
        val techRangeMult = 1f + 0.08f * (techRanks["KIN_RANGE"] ?: 0)
        val masteryMult = 1f + 0.05f * (towerMasteryRanks[t.type] ?: 0)
        val starMult = TowerAscensionCatalog.rangeMultiplierForStars(t.ascensionStars)
        val eraRangeMult = eraProfileFor(t.type).rangeMult
        val pathDRangeMult = 1f + 0.05f * t.pathD
        val futureRangeTech = if (t.type == TowerType.LASER_ARRAY || t.type == TowerType.PRISM || t.type.isFusedApex) {
            1f + 0.25f * (techRanks["FUT_PHOTON"] ?: 0)
        } else 1f
        var range = t.type.baseRange * t.terrainBonus.rangeMult * (1f + 0.06f * (t.totalLevel - 1).coerceAtMost(14)) *
            vet.rangeBonusMult * gearRangeMult * techRangeMult * masteryMult * starMult *
            eraRangeMult * edictRangeMult() * pathDRangeMult * futureRangeTech
        if (currentWeather == WeatherFront.CLEAR_OVERWATCH) range *= 1.15f
        if (t.type == TowerType.SNIPER && t.pathA >= 2) range += 120f
        if (t.type == TowerType.SNIPER && t.pathA >= 6) range += 160f
        if (t.type == TowerType.DRONE && t.pathA >= 3) range += 70f
        if (t.type == TowerType.RAIL_CANNON && t.pathA >= 2) range += 60f
        // Keep gameplay readable: ranges never exceed the full map width
        return range.coerceAtMost(LabyrinthMap.WORLD_WIDTH * 0.85f)
    }

    private fun stepTowers(dt: Float) {
        if (enemies.isEmpty()) return

        // Era evolution (per tower type) replaces the flat Age bonus so power is never double-counted.
        val agePhase = currentClearancePhase()
        val edictSpd = edictFireRateMult()
        val edictDmg = edictDamageMult()
        val omegaTech = techRanks["FUT_OMEGA"] ?: 0
        val globalSpdTech = (1f + 0.08f * (techRanks["ARC_SPD"] ?: 0)) * edictSpd * (1f + 0.15f * omegaTech)
        val kinDmgTech = (1f + 0.12f * (techRanks["KIN_DMG"] ?: 0))
        val elmDmgTech = (1f + 0.14f * (techRanks["ELM_DMG"] ?: 0))
        val otherDmgTech = 1f
        val apexTech = (1f + 0.18f * (techRanks["ARC_FUSE"] ?: 0))
        val globalDmgTech = edictDmg * (1f + 0.30f * omegaTech)
        val pathDTechRate = 0.20f * (techRanks["KIN_PATH_D"] ?: 0)
        val tier8TechMult = 1f + 0.25f * (techRanks["SIG_TIER8"] ?: 0)
        val omniTechRate = 0.10f * (techRanks["FUT_OMNI"] ?: 0)
        @Suppress("UNUSED_VARIABLE") val unusedAgePhase = agePhase
        val gearTechScale = 1f + 0.18f * (techRanks["KIN_GEAR"] ?: 0)
        val crossfireTechRate = 0.08f + 0.05f * (techRanks["ARC_CROSSFIRE"] ?: 0)
        val starTechScale = 1f + 0.15f * (techRanks["ARC_ASCEND"] ?: 0)
        val doctrineCdMult = (1f - 0.12f * (techRanks["SIG_DOCTRINE"] ?: 0)).coerceAtLeast(0.40f)
        val overclockMult = if (overclockActiveSec > 0f) 2.0f else 1.0f
        val cheatSpdMult = if (cheatHyperSpeed) 5.0f else 1.0f

        for (t in towers.values) {
            if (t.recoilAnim > 0f) t.recoilAnim = max(0f, t.recoilAnim - dt * 5f)
            val tx = t.x
            val ty = t.y
            val vet = t.veteranRank
            val gearScale = (1f + 0.30f * (t.gearTier - 1)) * gearTechScale
            val gearDmgMult = 1f + (t.gear.damageMult - 1f) * gearScale
            val gearSpdMult = 1f + (t.gear.fireRateMult - 1f) * gearScale
            val starDmgMult = 1f + (TowerAscensionCatalog.damageMultiplierForStars(t.ascensionStars) - 1f) * starTechScale
            val starSpdMult = TowerAscensionCatalog.fireRateMultiplierForStars(t.ascensionStars)
            val masteryDmgMult = 1f + 0.12f * (towerMasteryRanks[t.type] ?: 0)
            val crossfireDmgMult = 1f + (t.multiPassCount - 1).coerceAtLeast(0) * crossfireTechRate

            var range = effectiveTowerRange(t)
            val era = eraProfileFor(t.type)
            var fireRate = t.type.baseFireRate * t.terrainBonus.fireRateMult * globalSpdTech *
                vet.fireRateBonusMult * gearSpdMult * starSpdMult * overclockMult * cheatSpdMult * era.fireRateMult
            fireRate *= (1f + 0.11f * t.pathB + 0.05f * (t.pathA + t.pathC) + 0.07f * t.pathD)
            if (t.type == TowerType.GATLING && t.pathB >= 2) {
                fireRate *= (1f + t.spinUpBonus)
            }
            if (currentWeather == WeatherFront.GEOMAGNETIC_TEMPEST &&
                (t.type == TowerType.TESLA || t.type == TowerType.DRONE || t.type == TowerType.SNIPER || t.type == TowerType.TEMPEST_MAELSTROM)
            ) {
                fireRate *= 1.30f
            }

            var trueSight = t.terrainBonus.grantsTrueSight ||
                t.gear == TowerGearAttachment.SPYGLASS_CROWS_NEST ||
                (t.type == TowerType.SNIPER && t.pathB >= 2) ||
                (t.type == TowerType.PRISM && t.pathB >= 2) ||
                (t.type == TowerType.CRYO && t.pathB >= 3) ||
                (t.type == TowerType.TESLA && t.pathB >= 3) ||
                (t.type == TowerType.LASER_ARRAY && t.pathB >= 2) ||
                (t.type == TowerType.RAIL_CANNON && t.pathC >= 3) ||
                t.gear == TowerGearAttachment.SILICON_RADAR_UPLINK ||
                effectiveAgeLevel() >= 11 || // Information Age radar grants True Sight to every tower
                t.type.isFusedApex

            var neighborDmgMult = 1.0f
            var neighborReactionMult = t.terrainBonus.reactionMult
            val absorbedStatuses = mutableListOf<StatusEffect>()

            for (other in towers.values) {
                if (other.padId == t.padId) continue
                val dist = hypot(other.x - tx, other.y - ty)
                if (dist <= 220f) {
                    if (other.type == TowerType.CHRONO && other.pathB >= 1 || other.type == TowerType.CHRONOS_OBSERVATORY) {
                        fireRate *= if (other.pathB >= 5 || other.type.isFusedApex) 1.75f else if (other.pathB >= 4) 1.45f else 1.22f
                    }
                    if (other.type == TowerType.HUB) {
                        if (other.pathB >= 1) {
                            trueSight = true
                            range *= 1.18f
                        }
                        if (other.pathC >= 1) {
                            neighborReactionMult *= if (other.pathC >= 5) 2.0f else 1.35f
                        }
                    }
                    if (other.type == TowerType.PRISM && other.pathC >= 1 || other.type == TowerType.CHRONOS_OBSERVATORY) {
                        neighborDmgMult *= 1.22f
                        fireRate *= 1.18f
                    }
                    if (other.type == TowerType.DRONE && other.pathB >= 2 || other.type == TowerType.DAVINCI_SKY_FORTRESS) {
                        trueSight = true
                        neighborDmgMult *= 1.22f
                    }
                    if (other.type == TowerType.ASTRAL_NEXUS) {
                        trueSight = true
                        neighborDmgMult *= 1.45f
                        fireRate *= 1.30f
                    }
                    // New support towers & Mega-Wonders
                    if (other.type == TowerType.GRAVITY_WELL && other.pathB >= 1) {
                        neighborDmgMult *= when {
                            other.pathB >= 8 -> 2.5f
                            other.pathB >= 6 -> 1.9f
                            other.pathB >= 5 -> 1.55f
                            else -> 1.20f
                        }
                        if (other.pathB >= 3) fireRate *= if (other.pathB >= 6) 1.5f else 1.3f
                    }
                    if (other.type == TowerType.SHIELD_GARRISON && other.pathC >= 3) {
                        neighborDmgMult *= when {
                            other.pathC >= 8 -> 2.5f
                            other.pathC >= 7 -> 1.85f
                            other.pathC >= 6 -> 1.65f
                            else -> 1.25f
                        }
                    }
                    if (other.type == TowerType.LASER_ARRAY && other.pathC >= 4) {
                        fireRate *= if (other.pathC >= 8) 2.0f else 1.35f
                    }
                    if (other.type == TowerType.CHRONO && other.pathB >= 6) {
                        fireRate *= when {
                            other.pathB >= 8 -> 1.70f
                            other.pathB >= 7 -> 1.45f
                            else -> 1.25f
                        }
                    }
                    if (other.type == TowerType.HUB && other.pathB >= 6) {
                        neighborDmgMult *= if (other.pathB >= 8) 2.0f else if (other.pathB >= 7) 1.75f else 1.35f
                    }
                    if (other.type == TowerType.OMEGA_SINGULARITY_CORE) {
                        fireRate *= 2.0f
                    }
                    if (other.type == TowerType.AEGIS_ORBITAL_BASTION) {
                        trueSight = true
                        neighborDmgMult *= 1.80f
                    }
                    if (t.type == TowerType.PRISM || t.type == TowerType.ASTRAL_NEXUS || t.type == TowerType.CHRONOS_OBSERVATORY) {
                        other.type.primaryStatus?.let { absorbedStatuses.add(it) }
                    }
                }
            }

            // Passive Ice Keep Winter Aura (Path B >= 2)
            if (t.type == TowerType.CRYO && t.pathB >= 2) {
                for (e in enemies) {
                    if (e.hp > 0f && hypot(e.x - tx, e.y - ty) <= range) {
                        applyStatus(e, StatusEffect.CHILLED, 1.5f)
                        if (t.pathB >= 3) e.camorevealed = true
                    }
                }
            }

            // Tier-5 Doctrine Active Skill periodic trigger
            if (t.maxSinglePath >= 5 || t.type.isFusedApex) {
                t.doctrineCooldown -= dt
                if (t.doctrineCooldown <= 0f) {
                    t.doctrineCooldown = t.type.doctrineCooldownSec * doctrineCdMult
                    triggerTowerDoctrineSkill(t, range, neighborDmgMult * starDmgMult * vet.damageBonusMult)
                }
            }

            t.cooldown -= dt
            if (t.cooldown > 0f) continue

            val target = selectTarget(tx, ty, range, t.targeting, trueSight)
            if (target == null) {
                t.spinUpBonus = max(0f, t.spinUpBonus - dt * 0.5f)
                continue
            }

            t.cooldown = (1f / fireRate.coerceAtLeast(0.2f))
            t.turretAngleRad = atan2(target.y - ty, target.x - tx)
            t.recoilAnim = 1.0f
            t.shotsFired++
            if (t.type == TowerType.GATLING && t.pathB >= 2) {
                val cap = if (t.pathB >= 5) 2.2f else if (t.pathB >= 4) 1.6f else 0.8f
                t.spinUpBonus = min(cap, t.spinUpBonus + 0.08f)
            }

            var shotDamage = t.type.baseDamage * t.terrainBonus.damageMult * neighborDmgMult *
                vet.damageBonusMult * gearDmgMult * starDmgMult * masteryDmgMult * crossfireDmgMult
            shotDamage *= (1f + 0.28f * t.pathA + 0.16f * t.pathB + 0.20f * t.pathC + 0.24f * t.pathD)
            shotDamage *= t.apexTierDamageMult
            if (t.maxSinglePath >= 6) shotDamage *= tier8TechMult
            if (t.pathD > 0) shotDamage *= (1f + pathDTechRate * t.pathD)
            val activePathCount = (0..3).count { t.tierOf(it) > 0 }
            if (activePathCount > 0) shotDamage *= (1f + omniTechRate * activePathCount)
            // AGE EVOLUTION: every Age-Up multiplies the damage of every tower (Dark Age 1x -> Omega Age 400x)
            shotDamage *= eraDamageMult(t.type)
            shotDamage *= globalDmgTech
            shotDamage *= futureTechDamageMult(t.type)
            shotDamage *= when (t.type.category) {
                TowerCategory.KINETIC -> kinDmgTech
                TowerCategory.ELEMENTAL -> elmDmgTech
                else -> otherDmgTech
            }
            if (t.type.isFusedApex) shotDamage *= apexTech
            if (currentWeather == WeatherFront.SOLAR_FLARE &&
                (t.type == TowerType.PYRO || t.type == TowerType.PRISM || t.type == TowerType.MORTAR || t.type == TowerType.FROSTFIRE_PARADOX)
            ) {
                shotDamage *= 1.40f
            }

            val critChance = 0.10f + 0.05f * (techRanks["KIN_CRIT"] ?: 0) +
                vet.critBonus + t.gear.critChanceBonus * gearScale + edictCritBonus() +
                (if (currentWeather == WeatherFront.CLEAR_OVERWATCH) 0.12f else 0f) +
                (if (target.statusTimers.containsKey(StatusEffect.MARKED)) 0.25f else 0f)
            if (Random.nextFloat() < critChance) {
                val critMult = 1.75f + 0.25f * (techRanks["KIN_CRIT"] ?: 0) +
                    (if (target.statusTimers.containsKey(StatusEffect.MARKED)) 0.50f else 0f)
                shotDamage *= critMult
            }

            if (t.gear.goldPerHit > 0L) {
                addGold(t.gear.goldPerHit * t.gearTier)
            }

            executeTowerAttack(t, target, shotDamage, neighborReactionMult, absorbedStatuses)
        }

        // Immediately clean up any enemies killed by tower fire in this tick
        finalizeDeadEnemies()
    }

    /**
     * Unleashes a Tower's Tier-5 / Wonder Periodic Doctrine Skill!
     */
    private fun triggerTowerDoctrineSkill(t: TowerInstance, range: Float, dmgScale: Float) {
        val inRange = enemies.filter { it.hp > 0f && hypot(it.x - t.x, it.y - t.y) <= range * 1.25f }
        if (inRange.isEmpty()) return
        val burstDmg = t.type.baseDamage * 3.2f * dmgScale
        shockwaves.add(VisualShockwave(t.x, t.y, min(range, 210f), t.type.primaryColor))
        for (e in inRange.take(6)) {
            t.type.primaryStatus?.let { applyStatus(e, it, 4.5f) }
            t.type.secondaryStatus?.let { applyStatus(e, it, 4.0f) }
            val dealt = inflictRawDamage(e, burstDmg, ignoreArmor = true, sourceTower = t)
            t.totalDamageDealt += dealt
            damageThisWindow += dealt
        }
        if (t.type == TowerType.HUB || t.type == TowerType.ASTRAL_NEXUS) {
            addGold(45L + wave * 8L)
        }
    }

    private fun executeTowerAttack(
        t: TowerInstance,
        primaryTarget: ActiveEnemy,
        damage: Float,
        reactionMult: Float,
        absorbedStatuses: List<StatusEffect>
    ) {
        val isBeam = t.type == TowerType.PRISM || t.type == TowerType.LASER_ARRAY ||
            t.type == TowerType.DYSON_HELIOS_ARRAY || t.type == TowerType.FROSTFIRE_PARADOX ||
            t.type == TowerType.ASTRAL_NEXUS || t.type == TowerType.CHRONOS_OBSERVATORY ||
            (t.type == TowerType.PYRO && t.pathB >= 5)
        val isArc = t.type == TowerType.TESLA || t.type == TowerType.TEMPEST_MAELSTROM ||
            t.type == TowerType.QUANTUM_CHRONO_MATRIX

        val projStyle = when (t.type) {
            TowerType.GATLING, TowerType.COLOSSUS_SIEGEWORKS, TowerType.HUB -> 0 // 3D Faceted Boulder / Cannonball
            TowerType.SNIPER, TowerType.TACHYON_RAILGUN -> 1 // 3D Fletched Ballista Javelin / Lance
            TowerType.MORTAR, TowerType.GLACIER_BOMBARD, TowerType.PYRO, TowerType.ALCHEMICAL_CATHEDRAL -> 2 // 3D Lofted Siege Fireball / Bombard Shell
            TowerType.VENOM, TowerType.PLAGUEBRINGER_VULCAN, TowerType.CHRONO, TowerType.GRAVITY_WELL, TowerType.OMEGA_SINGULARITY_CORE -> 3 // 3D Alchemical Flask / Singularity Orb
            TowerType.TESLA, TowerType.TEMPEST_MAELSTROM, TowerType.QUANTUM_CHRONO_MATRIX -> 4 // 3D Jagged Multi-Strand Lightning
            TowerType.PRISM, TowerType.LASER_ARRAY, TowerType.DYSON_HELIOS_ARRAY, TowerType.FROSTFIRE_PARADOX, TowerType.ASTRAL_NEXUS, TowerType.CHRONOS_OBSERVATORY -> 5 // 3D Volumetric Energy Lance Beam
            TowerType.DRONE, TowerType.DAVINCI_SKY_FORTRESS, TowerType.NANITE_SWARM_HIVE, TowerType.SHIELD_GARRISON, TowerType.AEGIS_ORBITAL_BASTION -> 6 // 3D Finned Cruise Missile / Rocket
            TowerType.RAIL_CANNON, TowerType.LEVIATHAN_GAUSS_CITADEL -> 7 // 3D Gauss Sabot with Mach Rings
            TowerType.HYDRO, TowerType.CRYO -> 8 // 3D Crystalline Frost/Hydro Shard Volley
        }

        if (projectiles.size < 96) {
            projectiles.add(
                VisualProjectile(
                    x = t.x,
                    y = t.y,
                    startX = t.x,
                    startY = t.y,
                    targetX = primaryTarget.x,
                    targetY = primaryTarget.y,
                    progress = if (isBeam || isArc) 0.22f else 0f,
                    speed = when (projStyle) {
                        2 -> 3.4f // High parabolic siege mortar arc
                        1, 7 -> 5.4f // Fast javelin / railgun sabot
                        4, 5 -> 4.2f // Sustained 3D lightning / laser beam
                        else -> 4.4f
                    },
                    colorArgb = t.type.primaryColor,
                    radius = if (t.type.splashRadius > 70f) 7.8f else 5.4f,
                    isBeam = isBeam,
                    isArc = isArc,
                    visualStyle = projStyle,
                    secondaryColorArgb = t.type.secondaryColor
                )
            )
        }

        if (t.shotsFired % 3 == 1) {
            val sfx = when (t.type) {
                TowerType.GATLING, TowerType.PLAGUEBRINGER_VULCAN, TowerType.COLOSSUS_SIEGEWORKS -> SoundEngine.Sfx.GATLING
                TowerType.CRYO, TowerType.GLACIER_BOMBARD -> SoundEngine.Sfx.CRYO
                TowerType.PYRO, TowerType.FROSTFIRE_PARADOX, TowerType.ALCHEMICAL_CATHEDRAL -> SoundEngine.Sfx.PYRO
                TowerType.TESLA, TowerType.TEMPEST_MAELSTROM -> SoundEngine.Sfx.TESLA
                TowerType.VENOM -> SoundEngine.Sfx.VENOM
                TowerType.SNIPER, TowerType.TACHYON_RAILGUN, TowerType.DAVINCI_SKY_FORTRESS -> SoundEngine.Sfx.SNIPER
                TowerType.MORTAR -> SoundEngine.Sfx.MORTAR
                TowerType.PRISM, TowerType.ASTRAL_NEXUS, TowerType.CHRONOS_OBSERVATORY -> SoundEngine.Sfx.PRISM
                TowerType.CHRONO -> SoundEngine.Sfx.CHRONO
                TowerType.DRONE -> SoundEngine.Sfx.DRONE
                TowerType.HYDRO -> SoundEngine.Sfx.HYDRO
                TowerType.HUB -> SoundEngine.Sfx.HUB_MINT
                TowerType.RAIL_CANNON, TowerType.LEVIATHAN_GAUSS_CITADEL -> SoundEngine.Sfx.SNIPER
                TowerType.LASER_ARRAY, TowerType.DYSON_HELIOS_ARRAY -> SoundEngine.Sfx.PRISM
                TowerType.QUANTUM_CHRONO_MATRIX -> SoundEngine.Sfx.TESLA
                TowerType.GRAVITY_WELL, TowerType.OMEGA_SINGULARITY_CORE -> SoundEngine.Sfx.CHRONO
                TowerType.SHIELD_GARRISON, TowerType.AEGIS_ORBITAL_BASTION -> SoundEngine.Sfx.MORTAR
                TowerType.NANITE_SWARM_HIVE -> SoundEngine.Sfx.DRONE
            }
            soundEngine?.play(sfx)
        }

        val hitTargets = mutableListOf<ActiveEnemy>()
        hitTargets.add(primaryTarget)

        val splashTechMult = 1f + 0.12f * (techRanks["SIG_SPLASH"] ?: 0)
        val splashRad = t.type.splashRadius * (1f + 0.12f * t.pathB + 0.10f * t.pathC) * splashTechMult
        if (splashRad > 0f) {
            for (other in enemies) {
                if (other.hp > 0f && other.id != primaryTarget.id &&
                    hypot(other.x - primaryTarget.x, other.y - primaryTarget.y) <= splashRad
                ) {
                    hitTargets.add(other)
                }
            }
            if (shockwaves.size < 48) {
                shockwaves.add(
                    VisualShockwave(
                        x = primaryTarget.x,
                        y = primaryTarget.y,
                        maxRadius = splashRad.coerceIn(28f, 130f),
                        colorArgb = t.type.primaryColor,
                        visualStyle = projStyle
                    )
                )
            }
        } else if (shockwaves.size < 48 && t.shotsFired % 2 == 0) {
            shockwaves.add(
                VisualShockwave(
                    x = primaryTarget.x,
                    y = primaryTarget.y,
                    maxRadius = 22f,
                    colorArgb = t.type.primaryColor,
                    visualStyle = projStyle
                )
            )
        }

        val bonusTechChains = (techRanks["ELM_CHAIN"] ?: 0) / 2
        val baseBounces = when {
            t.type == TowerType.TEMPEST_MAELSTROM -> 10
            t.type == TowerType.TESLA -> 3 + t.pathA * 2 + (if (currentWeather == WeatherFront.GEOMAGNETIC_TEMPEST) 3 else 0) +
                (if (primaryTarget.statusTimers.containsKey(StatusEffect.WET)) 4 else 0)
            t.type == TowerType.GATLING && t.pathC >= 1 -> t.pathC + 1
            t.type == TowerType.PRISM && t.pathA >= 3 -> 3
            else -> 0
        }
        val maxBounces = baseBounces + t.gear.extraBounces * t.gearTier + (if (baseBounces > 0) bonusTechChains else 0)
        if (maxBounces > 0) {
            var last = primaryTarget
            for (b in 0 until maxBounces) {
                val next = enemies.firstOrNull { cand ->
                    cand.hp > 0f && cand.id != last.id && hitTargets.none { it.id == cand.id } &&
                        hypot(cand.x - last.x, cand.y - last.y) <= 180f
                } ?: break
                if (projectiles.size < 96) {
                    projectiles.add(
                        VisualProjectile(
                            x = last.x,
                            y = last.y,
                            startX = last.x,
                            startY = last.y,
                            targetX = next.x,
                            targetY = next.y,
                            progress = 0.25f,
                            speed = 5.2f,
                            colorArgb = t.type.secondaryColor,
                            radius = 4.2f,
                            isArc = true,
                            visualStyle = 4,
                            secondaryColorArgb = t.type.primaryColor
                        )
                    )
                }
                hitTargets.add(next)
                last = next
            }
        }

        val xpTechMult = 1f + 0.35f * (techRanks["KIN_VET"] ?: 0)
        val knockbackTechMult = 1f + 0.25f * (techRanks["SIG_KNOCK"] ?: 0)

        for ((idx, enemy) in hitTargets.withIndex()) {
            if (enemy.hp <= 0f) continue
            val falloff = if (idx == 0) 1.0f else 0.78f
            var finalDmg = damage * falloff

            val triggeredCombo = evaluateAndTriggerCombo(t, enemy, reactionMult)
            if (triggeredCombo != null) {
                val elmComboTech = 1f + 0.20f * (techRanks["ELM_COMBO"] ?: 0)
                val weatherComboMult = if (currentWeather == WeatherFront.AETHER_METEOR_SHOWER) 1.45f else 1.0f
                val overclockCombo = if (overclockActiveSec > 0f) 1.5f else 1.0f
                finalDmg *= triggeredCombo.bonusDamageMult * reactionMult * elmComboTech * weatherComboMult * overclockCombo
                t.totalReactionsTriggered++
                totalCombosTriggered++
                t.xp += (18L * xpTechMult).roundToLong()
                if (idx == 0) {
                    recordReactionLog("W$wave: ${t.type.shortName} -> ${triggeredCombo.title} on ${enemy.type.displayName}!")
                }
                if (isTutorialMode && tutorialStep == 1) {
                    tutorialStep = 2
                }
            }

            t.type.primaryStatus?.let { st ->
                if (st == StatusEffect.CHILLED && enemy.statusTimers.containsKey(StatusEffect.CHILLED) &&
                    (t.pathA >= 2 || enemy.statusTimers.containsKey(StatusEffect.WET))
                ) {
                    applyStatus(enemy, StatusEffect.FROZEN, 2.8f)
                } else {
                    applyStatus(enemy, st, 4.2f)
                }
            }
            if (t.totalLevel >= 3 || t.type.isFusedApex) {
                t.type.secondaryStatus?.let { applyStatus(enemy, it, 3.8f) }
            }
            t.gear.bonusStatus?.let { applyStatus(enemy, it, 3.8f) }
            for (absSt in absorbedStatuses) {
                applyStatus(enemy, absSt, 3.5f)
            }

            // Knockback & Bell-Chime Rewind
            val kbFactor = (1f - enemy.eliteAffix.knockbackResist).coerceAtLeast(0.15f) * knockbackTechMult
            if ((t.type == TowerType.CHRONO && t.pathA >= 2 && t.shotsFired % 3 == 0) ||
                t.type == TowerType.TACHYON_RAILGUN || t.type == TowerType.CHRONOS_OBSERVATORY
            ) {
                if (enemy.rewindCooldown <= 0f) {
                    val rewindUnits = (if (t.pathA >= 3 || t.type.isFusedApex) 75f else 42f) * kbFactor
                    enemy.distanceOnPath = (enemy.distanceOnPath - rewindUnits).coerceAtLeast(0f)
                    enemy.syncPosition()
                    enemy.rewindCooldown = 2.5f
                }
            }
            if ((t.type == TowerType.MORTAR && t.pathA >= 2) ||
                (t.type == TowerType.HYDRO && t.pathA >= 2) ||
                t.type == TowerType.TEMPEST_MAELSTROM ||
                t.type == TowerType.COLOSSUS_SIEGEWORKS
            ) {
                enemy.distanceOnPath = (enemy.distanceOnPath - 16f * kbFactor).coerceAtLeast(0f)
                enemy.syncPosition()
            }

            val hpRatio = enemy.hp / enemy.maxHp.coerceAtLeast(1f)
            if (((t.type == TowerType.SNIPER && t.pathB >= 3) || t.type == TowerType.TACHYON_RAILGUN || t.type == TowerType.DAVINCI_SKY_FORTRESS) &&
                hpRatio <= 0.18f && (!enemy.type.isBoss || t.pathB >= 5 || t.type.isFusedApex)
            ) {
                finalDmg = max(finalDmg, enemy.hp + enemy.shield + 500f)
            }

            val ignoreArmor = (t.type == TowerType.PRISM && t.pathB >= 1) ||
                (t.type == TowerType.GATLING && t.pathA >= 5) ||
                t.type.isFusedApex

            val wasAlive = enemy.hp > 0f
            val dealt = inflictRawDamage(enemy, finalDmg, ignoreArmor = ignoreArmor, sourceTower = t)
            t.totalDamageDealt += dealt
            damageThisWindow += dealt
            t.xp += ((dealt * 0.08).toLong().coerceAtLeast(2L) * xpTechMult).roundToLong()
            if (wasAlive && enemy.hp <= 0f) {
                t.totalKills++
            }
        }
    }

    private fun evaluateAndTriggerCombo(
        t: TowerInstance,
        e: ActiveEnemy,
        reactionMult: Float
    ): ComboReaction? {
        val statuses = e.statusTimers.keys
        if (statuses.isEmpty() && !t.type.isFusedApex) return null

        when (t.type) {
            TowerType.FROSTFIRE_PARADOX -> return ComboReaction.THERMAL_SHOCK
            TowerType.TEMPEST_MAELSTROM -> return ComboReaction.SUPERCONDUCT
            TowerType.GLACIER_BOMBARD -> return ComboReaction.SHATTER
            TowerType.PLAGUEBRINGER_VULCAN, TowerType.COLOSSUS_SIEGEWORKS -> if (statuses.contains(StatusEffect.CORRODED)) return ComboReaction.HEMORRHAGE
            TowerType.TACHYON_RAILGUN, TowerType.CHRONOS_OBSERVATORY -> return ComboReaction.CHRONO_COMPRESSION
            TowerType.ALCHEMICAL_CATHEDRAL -> return ComboReaction.NAPALM_IGNITION
            TowerType.DAVINCI_SKY_FORTRESS -> return ComboReaction.HEMORRHAGE
            else -> {}
        }

        if (statuses.contains(StatusEffect.FROZEN) &&
            (t.type == TowerType.MORTAR || t.type == TowerType.SNIPER)
        ) {
            e.statusTimers.remove(StatusEffect.FROZEN)
            return ComboReaction.SHATTER
        }

        if ((statuses.contains(StatusEffect.FROZEN) && t.type == TowerType.PYRO) ||
            (statuses.contains(StatusEffect.BURNING) && t.type == TowerType.CRYO)
        ) {
            e.armor = (e.armor * 0.5f).coerceAtLeast(0f)
            return ComboReaction.THERMAL_SHOCK
        }

        if (statuses.contains(StatusEffect.WET) && t.type == TowerType.TESLA) {
            return ComboReaction.SUPERCONDUCT
        }

        if (statuses.contains(StatusEffect.WET) && t.type == TowerType.CRYO) {
            applyStatus(e, StatusEffect.FROZEN, 4.0f)
            return ComboReaction.FLASH_FREEZE
        }

        if (statuses.contains(StatusEffect.OILED) && (t.type == TowerType.PYRO || t.type == TowerType.TESLA)) {
            applyStatus(e, StatusEffect.BURNING, 6.0f)
            return ComboReaction.NAPALM_IGNITION
        }

        if (statuses.contains(StatusEffect.POISONED) && (t.type == TowerType.PYRO || t.type == TowerType.TESLA)) {
            return ComboReaction.BIO_DETONATION
        }

        if (statuses.contains(StatusEffect.BLEEDING) &&
            (statuses.contains(StatusEffect.CORRODED) || statuses.contains(StatusEffect.MARKED)) &&
            t.type.category == TowerCategory.KINETIC
        ) {
            return ComboReaction.HEMORRHAGE
        }

        if (statuses.contains(StatusEffect.TIME_ANCHORED) &&
            (t.type == TowerType.MORTAR || t.type == TowerType.HYDRO || t.type == TowerType.DRONE)
        ) {
            return ComboReaction.CHRONO_COMPRESSION
        }

        if (t.type == TowerType.PRISM && statuses.size >= 2) {
            return ComboReaction.PRISM_REFRACTION
        }

        return null
    }

    private fun inflictRawDamage(
        e: ActiveEnemy,
        rawAmount: Float,
        ignoreArmor: Boolean,
        sourceTower: TowerInstance?
    ): Float {
        if (cheatOneShotKill && sourceTower != null) {
            val total = e.hp + e.shield
            e.shield = 0f
            e.hp = 0f
            return total
        }

        var dmg = rawAmount.coerceAtLeast(1f)
        if (e.statusTimers.containsKey(StatusEffect.CORRODED)) {
            dmg *= 1.35f
        }
        if (e.type.weakTo != null && e.statusTimers.containsKey(e.type.weakTo)) {
            dmg *= 1.30f
        }
        if (e.type.isBoss || e.type == EnemyType.IRONCLAD || e.type == EnemyType.BOMBARD_TRAIN) {
            dmg *= (1f + 0.20f * (techRanks["SIG_BOSS"] ?: 0))
        }

        if (e.shield > 0f && !ignoreArmor) {
            val shieldTechMult = 1f + 0.30f * (techRanks["SIG_SHIELD"] ?: 0)
            val shieldDmg = (if (sourceTower?.type == TowerType.TESLA) dmg * 2.0f else dmg) * shieldTechMult
            if (e.shield >= shieldDmg) {
                e.shield -= shieldDmg
                e.hitFlash = 1.0f
                return shieldDmg
            } else {
                val leftover = shieldDmg - e.shield
                e.shield = 0f
                dmg = leftover
            }
        }

        val pierceTech = 0.10f * (techRanks["KIN_PIERCE"] ?: 0)
        val gearPierce = (sourceTower?.gear?.armorPierceBonus ?: 0f) * (sourceTower?.gearTier ?: 1) * 0.65f
        val eraPierce = sourceTower?.let { eraProfileFor(it.type).armorPierceBonus } ?: 0f
        val effectiveArmor = if (ignoreArmor) 0f else {
            val baseArm = if (e.statusTimers.containsKey(StatusEffect.CORRODED)) e.armor * 0.5f else e.armor
            (baseArm - pierceTech - gearPierce - eraPierce - edictPierceBonus()).coerceIn(0f, 0.78f)
        }
        val hullDmg = dmg * (1f - effectiveArmor)
        e.hp -= hullDmg
        e.hitFlash = 1.0f
        return hullDmg
    }

    private fun applyStatus(e: ActiveEnemy, status: StatusEffect, durationSec: Float) {
        val cur = e.statusTimers[status] ?: 0f
        e.statusTimers[status] = max(cur, durationSec)
        e.statusStacks[status] = ((e.statusStacks[status] ?: 0) + 1).coerceAtMost(10)
        if (status == StatusEffect.MARKED) {
            e.camorevealed = true
        }
    }

    private fun selectTarget(
        x: Float,
        y: Float,
        range: Float,
        priority: TargetingPriority,
        trueSight: Boolean
    ): ActiveEnemy? {
        val candidates = enemies.filter { e ->
            e.hp > 0f && (!e.isCamoHidden || trueSight) && hypot(e.x - x, e.y - y) <= range
        }
        if (candidates.isEmpty()) return null
        return when (priority) {
            TargetingPriority.FIRST -> candidates.maxByOrNull { it.distanceOnPath }
            TargetingPriority.LAST -> candidates.minByOrNull { it.distanceOnPath }
            TargetingPriority.STRONGEST -> candidates.maxByOrNull { it.hp + it.shield }
            TargetingPriority.WEAKEST -> candidates.minByOrNull { it.hp + it.shield }
            TargetingPriority.CLUSTERED -> candidates.maxByOrNull { c ->
                enemies.count { o -> o.hp > 0f && hypot(o.x - c.x, o.y - c.y) <= 75f }
            }
            TargetingPriority.PRIMED -> candidates.maxByOrNull { c ->
                c.statusTimers.size * 1000f + c.distanceOnPath
            }
            TargetingPriority.ARMORED -> candidates.maxByOrNull { c ->
                (c.armor * 2500f) + c.shield + c.distanceOnPath * 0.1f
            }
            TargetingPriority.BOSS_HUNTER -> candidates.maxByOrNull { c ->
                (if (c.type.isBoss) 500_000f else 0f) + (if (c.eliteAffix != EnemyEliteAffix.NONE) 50_000f else 0f) + c.hp
            }
        }
    }

    // =========================================================================
    // ROYAL TREASURY BANK & COMPOUND INTEREST ACCOUNT (UNBOUNDED 64-BIT VAULT)
    // =========================================================================

    fun effectiveBankWaveInterestRate(): Double {
        val base = BankCatalog.specForTier(bankVaultTier).waveInterestRate
        val techBonus = 0.035 * (techRanks["ECO_BANK"] ?: 0)
        val hubCount = towers.values.count {
            it.type == TowerType.HUB || it.type == TowerType.ASTRAL_NEXUS ||
                it.type == TowerType.DYSON_HELIOS_ARRAY || it.type == TowerType.AEGIS_ORBITAL_BASTION
        }
        val hubBonus = 0.018 * hubCount
        val ageBonus = currentClearancePhase().bonusBankInterestRate
        val edictBonus = edictBankBonus()
        // No artificial 75% ceiling! Compound interest scales freely across all 18 Ages & 15 Vault Tiers.
        return (base + techBonus + hubBonus + ageBonus + edictBonus).coerceAtLeast(0.05)
    }

    fun effectiveBankLiveTickRate(): Double {
        val base = BankCatalog.specForTier(bankVaultTier).liveTickRate
        val techBonus = 0.006 * (techRanks["ECO_BANK"] ?: 0)
        val edictBonus = edictBankBonus() * 0.15
        // No artificial 9% ceiling!
        return (base + techBonus + edictBonus).coerceAtLeast(0.01)
    }

    fun effectiveBankVaultCap(): Long {
        // Vault capacity is completely uncapped up to the 64-bit saturating ceiling (4.50 Quintillion Gold)
        return CurrencyMath.MAX_TREASURY_GOLD
    }

    fun depositToBank(amount: Long): Boolean = synchronized(lock) {
        val cap = effectiveBankVaultCap()
        val room = (cap - bankBalance).coerceAtLeast(0L)
        val actual = min(amount.coerceAtLeast(0L), min(gold, room))
        if (actual <= 0L) return false
        gold = (gold - actual).coerceAtLeast(0L)
        bankBalance = CurrencyMath.safeAdd(bankBalance, actual)
        soundEngine?.play(SoundEngine.Sfx.HUB_MINT)
        return true
    }

    fun depositPercentToBank(percent: Int): Boolean = synchronized(lock) {
        if (gold <= 0L) return false
        val target = CurrencyMath.safePercent(gold, percent.coerceIn(1, 100)).coerceAtLeast(1L)
        return depositToBank(target)
    }

    fun withdrawFromBank(amount: Long): Boolean = synchronized(lock) {
        val actual = min(amount.coerceAtLeast(0L), bankBalance)
        if (actual <= 0L) return false
        bankBalance = (bankBalance - actual).coerceAtLeast(0L)
        gold = CurrencyMath.safeAdd(gold, actual)
        soundEngine?.play(SoundEngine.Sfx.HUB_MINT)
        return true
    }

    fun withdrawPercentFromBank(percent: Int): Boolean = synchronized(lock) {
        if (bankBalance <= 0L) return false
        val target = CurrencyMath.safePercent(bankBalance, percent.coerceIn(1, 100)).coerceAtLeast(1L)
        return withdrawFromBank(target)
    }

    fun upgradeBankVault(): Boolean = synchronized(lock) {
        if (bankVaultTier >= BankCatalog.tiers.size) return false
        val nextSpec = BankCatalog.specForTier(bankVaultTier + 1)
        val cost = effectiveCost(nextSpec.upgradeCostGold)
        if (!spendGoldWithOverdraft(cost)) return false
        bankVaultTier++
        soundEngine?.play(SoundEngine.Sfx.UPGRADE)
        return true
    }

    fun cycleBankAutoDepositPct() = synchronized(lock) {
        bankAutoDepositPct = when (bankAutoDepositPct) {
            0 -> 25
            25 -> 50
            else -> 0
        }
    }

    fun toggleBankOverdraftProtection() = synchronized(lock) {
        bankOverdraftProtection = !bankOverdraftProtection
    }

    fun issueHighYieldBond(amount: Long): Boolean = synchronized(lock) {
        if (bondWavesLeft > 0) return false
        val maxAvail = availableSpendableGold()
        val invest = min(amount.coerceAtLeast(100L), maxAvail)
        if (invest < 100L || !spendGoldWithOverdraft(invest)) return false
        bondPrincipal = invest
        bondWavesLeft = 3
        soundEngine?.play(SoundEngine.Sfx.HUB_MINT)
        return true
    }

    private fun spendGoldWithOverdraft(cost: Long): Boolean {
        if (cost <= 0L) return true
        if (gold >= cost) {
            gold -= cost
            return true
        }
        if (bankOverdraftProtection && CurrencyMath.safeAdd(gold, bankBalance) >= cost) {
            val shortfall = cost - gold
            gold = 0L
            bankBalance = (bankBalance - shortfall).coerceAtLeast(0L)
            return true
        }
        return false
    }

    private fun availableSpendableGold(): Long =
        if (bankOverdraftProtection) CurrencyMath.safeAdd(gold, bankBalance) else gold

    // =========================================================================
    // FREE-PLACEMENT SPATIAL BUILDING, TOWER ARMORY GEAR & ASCENSION PRESTIGE
    // =========================================================================

    fun toggleStreamlinedUiMode() = synchronized(lock) {
        streamlinedUiMode = !streamlinedUiMode
    }

    fun toggleUnlockAllFeatures() = synchronized(lock) {
        unlockAllFeatures = !unlockAllFeatures
    }

    fun computeRecommendedEmptyPadId(): Int? {
        val existingCoords = towers.values.map { Vec2(it.x, it.y) }
        val emptyPads = LabyrinthMap.buildPads.filter { pad ->
            !towers.containsKey(pad.id) &&
                existingCoords.none { hypot(it.x - pad.x, it.y - pad.y) < LabyrinthMap.MIN_TOWER_SPACING }
        }
        if (emptyPads.isEmpty()) return null
        return emptyPads.maxByOrNull { pad ->
            val nearExisting = existingCoords.any { hypot(it.x - pad.x, it.y - pad.y) <= 215f }
            val score = pad.multiPassRating * 100 +
                (if (nearExisting) 65 else 0) +
                (if (pad.bonus != PadBonusType.STANDARD) 25 else 0) -
                (pad.id / 2)
            score
        }?.id
    }

    private fun computeSmartRecommendation(): Pair<String, Long> {
        val ageNum = effectiveAgeLevel()
        for (t in towers.values) {
            val cand = findFusionCandidateLocked(t.padId)
            if (cand != null) {
                return "🏛️ Fuse Wonder: ${cand.resultType.shortName}" to cand.fusionCost
            }
        }
        val targetTowerCount = (3 + wave).coerceAtMost(32)
        val recPadId = computeRecommendedEmptyPadId()
        if (towers.size < targetTowerCount && recPadId != null) {
            val recType = chooseSmartTowerTypeForPad(recPadId)
            val cost = effectiveCost(recType.baseCost)
            return "🔨 Place ${recType.ageShortName(ageNum)}" to cost
        }
        val upgCandidate = towers.values
            .filter { !it.type.isFusedApex && (0..3).any { p -> canUpgradePath(it, p) } }
            .minByOrNull { it.totalLevel }
        if (upgCandidate != null) {
            val pathIdx = (0..3).firstOrNull { canUpgradePath(upgCandidate, it) } ?: 0
            val curTier = upgCandidate.tierOf(pathIdx)
            val spec = upgCandidate.type.paths.getOrNull(pathIdx)?.tiers?.getOrNull(curTier)
            if (spec != null) {
                val cost = effectiveCost(spec.cost)
                return "⬆️ Upgrade ${upgCandidate.type.ageShortName(ageNum)}" to cost
            }
        }
        return "🏛️ Deposit 25% Gold" to 0L
    }

    private fun chooseSmartTowerTypeForPad(padId: Int): TowerType {
        val pad = LabyrinthMap.buildPads[padId]
        val phase = currentClearancePhase()
        val allowedIds = if (unlockAllFeatures) {
            TowerType.buildableBaseTowers.map { it.id }.toSet()
        } else {
            phase.unlockedTowerIds
        }
        val allowedTowers = TowerType.buildableBaseTowers.filter { allowedIds.contains(it.id) }
            .ifEmpty { listOf(TowerType.GATLING, TowerType.SNIPER, TowerType.MORTAR) }

        val neighborTypes = towers.values.filter {
            hypot(it.x - pad.x, it.y - pad.y) <= 215f
        }.map { it.type }.toSet()

        val candidates = listOfNotNull(
            if (towers.values.none { it.type == TowerType.MORTAR }) TowerType.MORTAR else null,
            if (towers.values.none { it.type == TowerType.GATLING }) TowerType.GATLING else null,
            if (towers.values.none { it.type == TowerType.SNIPER }) TowerType.SNIPER else null,
            if (neighborTypes.contains(TowerType.MORTAR) && !neighborTypes.contains(TowerType.CRYO)) TowerType.CRYO else null,
            if (neighborTypes.contains(TowerType.CRYO) && !neighborTypes.contains(TowerType.PYRO)) TowerType.PYRO else null,
            if (neighborTypes.contains(TowerType.HYDRO) && !neighborTypes.contains(TowerType.TESLA)) TowerType.TESLA else null,
            if (towers.values.none { it.type == TowerType.RAIL_CANNON }) TowerType.RAIL_CANNON else null,
            if (towers.values.none { it.type == TowerType.LASER_ARRAY }) TowerType.LASER_ARRAY else null,
            if (towers.values.none { it.type == TowerType.GRAVITY_WELL }) TowerType.GRAVITY_WELL else null,
            if (towers.values.none { it.type == TowerType.SHIELD_GARRISON }) TowerType.SHIELD_GARRISON else null,
            if (towers.values.none { it.type == TowerType.CRYO }) TowerType.CRYO else null,
            if (towers.values.none { it.type == TowerType.HUB }) TowerType.HUB else null,
            if (towers.values.none { it.type == TowerType.PYRO }) TowerType.PYRO else null
        )

        return candidates.firstOrNull { allowedTowers.contains(it) }
            ?: allowedTowers[(towers.size + wave) % allowedTowers.size]
    }

    fun executeSmartRecommendedAction(): Boolean = synchronized(lock) {
        for (t in towers.values.toList()) {
            val cand = findFusionCandidateLocked(t.padId)
            if (cand != null && availableSpendableGold() >= cand.fusionCost) {
                selectedPadId = t.padId
                return fuseSelectedTower()
            }
        }
        val targetTowerCount = (3 + wave).coerceAtMost(32)
        val recPadId = computeRecommendedEmptyPadId()
        if (towers.size < targetTowerCount && recPadId != null) {
            val recType = chooseSmartTowerTypeForPad(recPadId)
            val cost = effectiveCost(recType.baseCost)
            if (availableSpendableGold() >= cost) {
                val pad = LabyrinthMap.buildPads[recPadId]
                val ok = buildTowerAtGrass(pad.x, pad.y, recType)
                recommendedPadId = computeRecommendedEmptyPadId()
                return ok
            }
        }
        val upgCandidate = towers.values
            .filter { !it.type.isFusedApex && (0..3).any { p -> canUpgradePath(it, p) } }
            .minByOrNull { it.totalLevel }
        if (upgCandidate != null) {
            val pathIdx = (0..3).firstOrNull { canUpgradePath(upgCandidate, it) } ?: 0
            val curTier = upgCandidate.tierOf(pathIdx)
            val spec = upgCandidate.type.paths.getOrNull(pathIdx)?.tiers?.getOrNull(curTier)
            if (spec != null && availableSpendableGold() >= effectiveCost(spec.cost)) {
                selectedPadId = upgCandidate.padId
                return upgradeSelectedTower(pathIdx)
            }
        }
        return depositPercentToBank(25)
    }

    private fun buildIncomingThreatTip(w: Int): String = ""

    /**
     * Handles a direct tap on the meadow canvas at world coordinates (worldX, worldY):
     * 1. If the tap hits an existing tower, selects that tower to open its compact Upgrade & Armory bar.
     * 2. If a tower was already selected and the player taps open grass, deselects it to return to Build Mode.
     * 3. If in Build Mode (no tower selected), free-places the active [selectedBuildType] right on the grass!
     */
    fun handleMeadowTap(worldX: Float, worldY: Float): Boolean = synchronized(lock) {
        // 1. Check if tapping an existing placed tower
        val hitTower = towers.values
            .filter { hypot(it.x - worldX, it.y - worldY) <= 32f }
            .minByOrNull { hypot(it.x - worldX, it.y - worldY) }
        if (hitTower != null) {
            selectedPadId = if (selectedPadId == hitTower.padId) null else hitTower.padId
            selectedGadgetSlotId = null
            return true
        }

        // 2. If currently inspecting a tower or gadget slot, tapping open grass cleanly closes the inspector
        if (selectedPadId != null || selectedGadgetSlotId != null) {
            selectedPadId = null
            selectedGadgetSlotId = null
            return true
        }

        // 3. Otherwise, free-place the selected building right at (worldX, worldY) on the grass!
        return buildTowerAtGrass(worldX, worldY, selectedBuildType)
    }

    /**
     * Free-places a tower of [type] at (or nudged to the nearest valid grass near) (rawX, rawY).
     */
    fun buildTowerAtGrass(rawX: Float, rawY: Float, type: TowerType = selectedBuildType): Boolean = synchronized(lock) {
        val existingCoords = towers.values.map { Vec2(it.x, it.y) }
        val validSpot = LabyrinthMap.resolveFreePlacementPoint(rawX, rawY, existingCoords) ?: return false
        val cost = effectiveCost(type.baseCost)
        if (!spendGoldWithOverdraft(cost)) return false

        val tempInstance = TowerInstance(padId = 0, type = type, x = validSpot.x, y = validSpot.y)
        val site = LabyrinthMap.evaluateFreePlacementSite(validSpot.x, validSpot.y, effectiveTowerRange(tempInstance))
        while (towers.containsKey(nextFreeTowerId)) {
            nextFreeTowerId++
        }
        val newId = nextFreeTowerId++
        towers[newId] = TowerInstance(
            padId = newId,
            type = type,
            x = validSpot.x,
            y = validSpot.y,
            terrainBonus = site.terrainBonus,
            multiPassCount = site.multiPassRating,
            investedGold = cost
        )
        shockwaves.add(VisualShockwave(validSpot.x, validSpot.y, 48f, type.primaryColor))
        rebuildSynergyLinks()
        recommendedPadId = computeRecommendedEmptyPadId()
        soundEngine?.play(SoundEngine.Sfx.UPGRADE)
        if (isTutorialMode && tutorialStep == 0) {
            tutorialStep = 1
        }
        return true
    }

    fun selectPad(padId: Int?) = synchronized(lock) {
        selectedPadId = padId
        if (padId != null) selectedGadgetSlotId = null
    }

    fun selectGadgetSlot(slotId: Int?) = synchronized(lock) {
        selectedGadgetSlotId = slotId
        if (slotId != null) selectedPadId = null
    }

    fun setBuildTowerType(type: TowerType) = synchronized(lock) {
        selectedBuildType = type
        selectedPadId = null
        selectedGadgetSlotId = null
    }

    fun setCategoryFilter(cat: TowerCategory?) = synchronized(lock) {
        buildFilterCategory = cat
    }

    fun effectiveCost(rawCost: Long): Long {
        if (cheatZeroCost || cheatInfiniteGold) return 0L
        val discount = (0.06f * (techRanks["ECO_BUILD"] ?: 0)).coerceAtMost(0.45f)
        return (rawCost * (1f - discount)).roundToLong().coerceAtLeast(1L)
    }

    fun buildTowerOnSelectedPad(type: TowerType = selectedBuildType): Boolean = synchronized(lock) {
        val padId = selectedPadId ?: return false
        if (towers.containsKey(padId)) return false
        val pad = LabyrinthMap.buildPads.getOrNull(padId)
        val wx = pad?.x ?: 500f
        val wy = pad?.y ?: 500f
        val existingCoords = towers.values.map { Vec2(it.x, it.y) }
        val spot = LabyrinthMap.resolveFreePlacementPoint(wx, wy, existingCoords) ?: Vec2(wx, wy)
        val cost = effectiveCost(type.baseCost)
        if (!spendGoldWithOverdraft(cost)) return false
        val site = LabyrinthMap.evaluateFreePlacementSite(spot.x, spot.y, type.baseRange)
        towers[padId] = TowerInstance(
            padId = padId,
            type = type,
            x = spot.x,
            y = spot.y,
            terrainBonus = pad?.bonus ?: site.terrainBonus,
            multiPassCount = pad?.multiPassRating ?: site.multiPassRating,
            investedGold = cost
        )
        rebuildSynergyLinks()
        recommendedPadId = computeRecommendedEmptyPadId()
        soundEngine?.play(SoundEngine.Sfx.UPGRADE)
        if (isTutorialMode && tutorialStep == 0) {
            tutorialStep = 1
        }
        return true
    }

    fun canUpgradePath(t: TowerInstance, pathIndex: Int): Boolean {
        if (t.type.isFusedApex) return false
        if (pathIndex !in 0..3) return false
        val currentTier = t.tierOf(pathIndex)
        if (currentTier >= 8) return false
        if (cheatOmniPath555 || unlockAllFeatures) return true

        val nextTier = currentTier + 1
        val age = effectiveAgeLevel()

        // Path D (index 3: Era & Cosmic Overdrive) is always combinable with any A/B/C build, gated by Age
        if (pathIndex == 3) {
            return age >= requiredAgeForPathDTier(nextTier)
        }

        // Paths A, B, C (0..2): check era unlock for Tiers 6, 7, 8
        if (age < requiredAgeForTier(nextTier)) return false

        // Starfleet Age (Age 14+) unlocks full 8-8-8-8 Omni-Crosspathing on all towers!
        if (age >= OMNI_CROSSPATH_AGE) return true

        val tiersABC = listOf(t.pathA, t.pathB, t.pathC).toMutableList().also { it[pathIndex] = nextTier }
        val nonZeroCount = tiersABC.count { it > 0 }
        // Allow up to 2 active A/B/C paths before Golden Age, or all 3 with tertiary up to T2 from Golden Age (6+)
        if (nonZeroCount > 2 && age < 6) return false
        val deepPaths = tiersABC.count { it > 4 }      // Only 1 primary path > Tier 4 before Age 14
        val secondaryPaths = tiersABC.count { it > 2 } // Up to 2 paths > Tier 2 from Castle Age (3+), 1 before
        val maxSecondaryAllowed = if (age >= 3) 2 else 1
        return deepPaths <= 1 && secondaryPaths <= maxSecondaryAllowed
    }

    fun upgradeSelectedTower(pathIndex: Int): Boolean = synchronized(lock) {
        val padId = selectedPadId ?: return false
        val t = towers[padId] ?: return false
        if (!canUpgradePath(t, pathIndex)) return false
        val currentTier = t.tierOf(pathIndex)
        val spec = t.type.paths.getOrNull(pathIndex)?.tiers?.getOrNull(currentTier) ?: return false
        val cost = effectiveCost(spec.cost)
        if (!spendGoldWithOverdraft(cost)) return false
        t.investedGold = CurrencyMath.safeAdd(t.investedGold, cost)
        t.setTier(pathIndex, currentTier + 1)
        t.multiPassCount = LabyrinthMap.countRoadPassesInRange(t.x, t.y, effectiveTowerRange(t))
        rebuildSynergyLinks()
        soundEngine?.play(SoundEngine.Sfx.UPGRADE)
        if (isTutorialMode && tutorialStep == 2) {
            tutorialStep = 3
        }
        return true
    }

    /**
     * Equips or upgrades a Forged Armory Attachment on the selected tower (up to Tier V / MAX_FORGE_TIER = 5)!
     */
    fun equipOrUpgradeSelectedTowerGear(attachment: TowerGearAttachment): Boolean = synchronized(lock) {
        val padId = selectedPadId ?: return false
        val t = towers[padId] ?: return false
        if (attachment == TowerGearAttachment.NONE) {
            t.gear = TowerGearAttachment.NONE
            t.gearTier = 1
            return true
        }
        if (t.gear == attachment) {
            if (t.gearTier >= TowerGearAttachment.MAX_FORGE_TIER) return false
            val upgCost = effectiveCost((attachment.baseCost * (t.gearTier + 1) * 0.85f).roundToLong())
            if (!spendGoldWithOverdraft(upgCost)) return false
            t.gearTier++
            t.investedGold = CurrencyMath.safeAdd(t.investedGold, upgCost)
        } else {
            val equipCost = effectiveCost(attachment.baseCost)
            if (!spendGoldWithOverdraft(equipCost)) return false
            t.gear = attachment
            t.gearTier = 1
            t.investedGold = CurrencyMath.safeAdd(t.investedGold, equipCost)
        }
        t.multiPassCount = LabyrinthMap.countRoadPassesInRange(t.x, t.y, effectiveTowerRange(t))
        soundEngine?.play(SoundEngine.Sfx.UPGRADE)
        return true
    }

    /**
     * Ascends / Prestiges a high-level tower (totalLevel >= 6 or Tier 5) to the next Star (★1..★25)!
     */
    fun ascendSelectedTower(): Boolean = synchronized(lock) {
        val padId = selectedPadId ?: return false
        val t = towers[padId] ?: return false
        if (!t.canAscend) return false
        val ascendDiscount = (0.08f * (techRanks["ARC_ASCEND"] ?: 0)).coerceAtMost(0.55f)
        val rawCost = TowerAscensionCatalog.ascensionCostGold(t.type.baseCost, t.ascensionStars)
        val cost = effectiveCost((rawCost * (1f - ascendDiscount)).roundToLong())
        if (!spendGoldWithOverdraft(cost)) return false
        t.ascensionStars++
        t.investedGold = CurrencyMath.safeAdd(t.investedGold, cost)
        shockwaves.add(VisualShockwave(t.x, t.y, 135f, 0xFFD4AF37))
        soundEngine?.play(SoundEngine.Sfx.FUSION_APEX)
        return true
    }

    /**
     * Upgrades the permanent Guild Mastery Rank for an entire [TowerType] (+12% Dmg & +5% Range per rank, up to Rank 25).
     */
    fun upgradeTowerTypeMastery(type: TowerType): Boolean = synchronized(lock) {
        val curRank = towerMasteryRanks[type] ?: 0
        if (curRank >= 25) return false
        val goldCost = effectiveCost(250L * (curRank + 1))
        if (!spendGoldWithOverdraft(goldCost)) return false
        towerMasteryRanks[type] = curRank + 1
        soundEngine?.play(SoundEngine.Sfx.UPGRADE)
        return true
    }

    fun cycleSelectedTowerTargeting() = synchronized(lock) {
        val padId = selectedPadId ?: return
        val t = towers[padId] ?: return
        val all = TargetingPriority.entries
        t.targeting = all[(t.targeting.ordinal + 1) % all.size]
    }

    fun sellSelectedTower(): Boolean = synchronized(lock) {
        val padId = selectedPadId ?: return false
        val removed = towers.remove(padId) ?: return false
        val refundRatio = (0.75f + 0.04f * (techRanks["ECO_SELL"] ?: 0)).coerceAtMost(0.95f)
        val refund = CurrencyMath.safeMultiply(removed.investedGold, refundRatio.toDouble()).coerceAtLeast(removed.type.baseCost / 2)
        addGold(refund)
        selectedPadId = null
        rebuildSynergyLinks()
        recommendedPadId = computeRecommendedEmptyPadId()
        return true
    }

    private fun findFusionCandidateLocked(padId: Int?): FusionCandidate? {
        val id = padId ?: return null
        val t = towers[id] ?: return null
        if (t.type.isFusedApex || t.totalLevel < 4) return null
        for (other in towers.values) {
            if (other.padId == id || other.type.isFusedApex || other.totalLevel < 4) continue
            if (hypot(other.x - t.x, other.y - t.y) <= 220f) {
                val apex = TowerType.findFusionResult(t.type, other.type)
                if (apex != null) {
                    val fuseDiscount = (0.08f * (techRanks["ARC_FUSE"] ?: 0)).coerceAtMost(0.60f)
                    val cost = effectiveCost((apex.baseCost * (1f - fuseDiscount)).roundToLong())
                    return FusionCandidate(id, other.padId, apex, cost)
                }
            }
        }
        return null
    }

    fun fuseSelectedTower(): Boolean = synchronized(lock) {
        val cand = findFusionCandidateLocked(selectedPadId) ?: return false
        if (!spendGoldWithOverdraft(cand.fusionCost)) return false
        val primary = towers[cand.primaryPadId] ?: return false
        val partner = towers.remove(cand.partnerPadId)
        primary.type = cand.resultType
        primary.pathA = 8
        primary.pathB = 8
        primary.pathC = 8
        primary.pathD = 8
        primary.xp += (partner?.xp ?: 0L) + 2500L
        primary.ascensionStars = max(primary.ascensionStars, partner?.ascensionStars ?: 0)
        primary.investedGold = CurrencyMath.safeAdd(
            CurrencyMath.safeAdd(primary.investedGold, partner?.investedGold ?: 0L),
            cand.fusionCost
        )

        shockwaves.add(VisualShockwave(primary.x, primary.y, 220f, cand.resultType.primaryColor))
        recordReactionLog("GRAND WONDER: Built ${cand.resultType.displayName}!")
        rebuildSynergyLinks()
        recommendedPadId = computeRecommendedEmptyPadId()
        soundEngine?.play(SoundEngine.Sfx.FUSION_APEX)
        return true
    }

    fun deployTrackGadget(slotId: Int, gadgetType: TrackGadgetType): Boolean = synchronized(lock) {
        if (slotId !in LabyrinthMap.gadgetSlots.indices) return false
        val discount = (0.08f * (techRanks["ARC_GADGET"] ?: 0)).coerceAtMost(0.60f)
        val cost = effectiveCost((gadgetType.cost * (1f - discount)).roundToLong())
        if (!spendGoldWithOverdraft(cost)) return false
        trackGadgets[slotId] = gadgetType
        val slot = LabyrinthMap.gadgetSlots[slotId]
        shockwaves.add(VisualShockwave(slot.x, slot.y, 90f, gadgetType.colorArgb))
        soundEngine?.play(SoundEngine.Sfx.GADGET_DEPLOY)
        return true
    }

    fun activateCommanderAbility(ability: CommanderAbility): Boolean = synchronized(lock) {
        val cd = abilityCooldowns[ability] ?: 0f
        if (cd > 0f && !cheatZeroCost) return false
        abilityCooldowns[ability] = if (cheatZeroCost) 1.5f else ability.cooldownSec

        when (ability) {
            CommanderAbility.ORBITAL_ION_STRIKE -> {
                val sorted = enemies.filter { it.hp > 0f }.sortedByDescending { it.hp + it.shield }.take(8)
                for (e in sorted) {
                    applyStatus(e, StatusEffect.BURNING, 6.0f)
                    applyStatus(e, StatusEffect.SHOCKED, 5.0f)
                    applyStatus(e, StatusEffect.CORRODED, 6.0f)
                    val strikeDmg = 280f + wave * 65f + e.maxHp * 0.22f
                    inflictRawDamage(e, strikeDmg, ignoreArmor = true, sourceTower = null)
                    shockwaves.add(VisualShockwave(e.x, e.y, 115f, 0xFFC75B39))
                }
                finalizeDeadEnemies()
                soundEngine?.play(SoundEngine.Sfx.MORTAR)
            }
            CommanderAbility.ABSOLUTE_ZERO_NOVA -> {
                for (e in enemies) {
                    if (e.hp <= 0f) continue
                    applyStatus(e, StatusEffect.WET, 6.0f)
                    applyStatus(e, StatusEffect.FROZEN, 4.8f)
                    e.camorevealed = true
                }
                shockwaves.add(VisualShockwave(500f, 680f, 520f, 0xFF8FBCCF))
                soundEngine?.play(SoundEngine.Sfx.CRYO)
            }
            CommanderAbility.HYPER_OVERCLOCK -> {
                overclockActiveSec = 9.5f
                soundEngine?.play(SoundEngine.Sfx.UPGRADE)
            }
            CommanderAbility.GOLDEN_TRANSMUTATION -> {
                val windfall = (220L + wave * 55L) * effectiveAgeLevel()
                addGold(windfall)
                techShards += 2
                for (e in enemies) {
                    if (e.hp > 0f) applyStatus(e, StatusEffect.MARKED, 8.0f)
                }
                soundEngine?.play(SoundEngine.Sfx.HUB_MINT)
            }
        }
        if (isTutorialMode && tutorialStep == 3) {
            tutorialStep = 4
        }
        return true
    }

    fun rushNextWave() = synchronized(lock) {
        advanceToNextWave(rushed = true)
        if (isTutorialMode && tutorialStep == 3) {
            tutorialStep = 4
        }
    }

    private fun advanceToNextWave(rushed: Boolean) {
        val prevPhase = currentClearancePhase()
        if (!waveHadBreach) {
            flawlessStreak++
        }
        waveHadBreach = false

        // 1. Hidden compound interest on unspent wallet gold (Brandon's signature hidden mechanic)
        if (gold > 0L) {
            val interest = CurrencyMath.safeMultiply(gold, HIDDEN_WAVE_INTEREST_RATE)
            gold = CurrencyMath.safeAdd(gold, interest)
        }

        // 2. Visible Royal Treasury Compound Interest Dividend (unbounded saturating 64-bit vault!)
        if (bankBalance > 0L) {
            val cap = effectiveBankVaultCap()
            val rate = effectiveBankWaveInterestRate()
            val rawDividend = CurrencyMath.safeMultiply(bankBalance, rate).coerceAtLeast(1L)
            val newBal = CurrencyMath.safeAdd(bankBalance, rawDividend).coerceAtMost(cap)
            val actualEarned = (newBal - bankBalance).coerceAtLeast(0L)
            if (actualEarned > 0L) {
                bankBalance = newBal
                totalBankInterestEarned = CurrencyMath.safeAdd(totalBankInterestEarned, actualEarned)
                lastBankInterestPayout = actualEarned
            }
        }

        // 3. Mature 3-Wave Royal Bond if active
        if (bondWavesLeft > 0) {
            bondWavesLeft--
            if (bondWavesLeft == 0 && bondPrincipal > 0L) {
                val payout = CurrencyMath.safeMultiply(bondPrincipal, 2.0)
                bondPrincipal = 0L
                gold = CurrencyMath.safeAdd(gold, payout)
            }
        }

        // 4. Wave tithe + Royal Mint bonus + Rush bonus
        val ecoWaveMult = 1f + 0.15f * (techRanks["ECO_WAVE"] ?: 0)
        var waveGold = ((95L + wave * 24L) * ecoWaveMult * effectiveAgeLevel()).roundToLong()
        for (t in towers.values) {
            if (t.type == TowerType.HUB) {
                waveGold += when (t.pathA) {
                    1, 2 -> 35L
                    3, 4 -> 90L
                    5 -> 250L
                    6 -> 750L
                    7 -> 2_200L
                    8 -> 8_000L
                    else -> 15L
                }
            }
        }
        if (rushed) {
            waveGold = CurrencyMath.safeMultiply(waveGold, 1.35)
            techShards += 1
        }
        addGold(waveGold)
        if (wave % 2 == 0) techShards += 1

        // Regenerate Castle Keep walls from University Architecture tech
        val keepRegen = 2 * (techRanks["ARC_KEEP"] ?: 0)
        if (keepRegen > 0) {
            coreShield = (coreShield + keepRegen).coerceAtMost(maxCoreShield)
        }

        wave++
        val naturalAge = ClearancePhase.naturalAgeForWave(wave)
        if (naturalAge > civilizationAgeLevel) {
            civilizationAgeLevel = naturalAge
            evolveAllTowersToCurrentEra()
        }
        val newPhase = currentClearancePhase()
        if (newPhase != prevPhase) {
            val wallTechBonus = 50 * (techRanks["ARC_KEEP"] ?: 0)
            maxCoreShield = 100 + newPhase.bonusMaxCastleHp + wallTechBonus
            coreShield = (coreShield + 20).coerceAtMost(maxCoreShield)
        }
        if (wave >= 13 && wave % 3 == 1) {
            val all = WeatherFront.entries
            currentWeather = all[(currentWeather.ordinal + 1) % all.size]
            weatherTimerSec = 42f
        }
        recommendedPadId = computeRecommendedEmptyPadId()
        queueWaveEnemies(wave)
        waveCooldownTimer = 2.0f
        soundEngine?.play(SoundEngine.Sfx.WAVE_START)
    }

    private fun queueWaveEnemies(w: Int) {
        val mut = WaveMutator.forWave(w)
        val baseCount = when {
            w == 1 -> 8
            w == 2 -> 11
            w == 3 -> 13
            else -> (13 + w * 2).coerceAtMost(80)
        }
        val count = (baseCount * mut.countMult).toInt().coerceAtLeast(6)

        for (i in 0 until count) {
            val isBossSlot = i == count - 1 && ((w >= 6 && (w % 3 == 0 || w % 4 == 0)) || isTutorialMode)
            val type = when {
                isBossSlot && w >= 40 -> EnemyType.OMEGA_ARCHITECT_BOSS
                isBossSlot && w >= 28 -> EnemyType.STARFLEET_MOTHERSHIP_BOSS
                isBossSlot && w >= 18 -> EnemyType.INDUSTRIAL_DREADNOUGHT_BOSS
                isBossSlot -> EnemyType.LEVIATHAN_BOSS
                w <= 2 -> if (i % 3 == 0) EnemyType.SWARMLING else EnemyType.STRIDER
                w == 3 -> when {
                    i % 4 == 0 -> EnemyType.SPRINTER
                    i % 3 == 0 -> EnemyType.ARBALESTER
                    i % 2 == 0 -> EnemyType.SWARMLING
                    else -> EnemyType.STRIDER
                }
                w in 4..7 -> when {
                    i % 6 == 5 && w >= 6 -> EnemyType.SHIELDER
                    i % 5 == 4 && w >= 5 -> EnemyType.SAPPER_PETARD
                    i % 4 == 3 -> EnemyType.IRONCLAD
                    i % 3 == 2 -> EnemyType.SPRINTER
                    i % 2 == 1 -> EnemyType.ARBALESTER
                    else -> EnemyType.STRIDER
                }
                w in 8..13 -> when {
                    i % 8 == 7 && w >= 11 -> EnemyType.CATAPHRACT
                    i % 7 == 6 && w >= 10 -> EnemyType.SKY_WYVERN
                    i % 6 == 5 && w >= 9 -> EnemyType.NANITE_HEALER
                    i % 5 == 4 -> EnemyType.SHADOW_CAMO
                    i % 4 == 3 -> EnemyType.SHIELDER
                    i % 3 == 2 -> EnemyType.IRONCLAD
                    i % 2 == 1 -> EnemyType.SPRINTER
                    else -> EnemyType.ARBALESTER
                }
                w in 14..19 -> when (i % 10) {
                    9 -> EnemyType.STEAM_LANDSHIP
                    8 -> EnemyType.DIESEL_MECH_WALKER
                    7 -> EnemyType.IRONCLAD_ZEPPELIN
                    6 -> EnemyType.MUSKET_DRAGOON
                    5 -> EnemyType.BOMBARD_TRAIN
                    4 -> EnemyType.FROST_GOLEM
                    3 -> EnemyType.CATAPHRACT
                    2 -> EnemyType.SKY_WYVERN
                    1 -> EnemyType.SHADOW_CAMO
                    else -> EnemyType.HYDRA_SPLITTER
                }
                w in 20..27 -> when (i % 10) {
                    9 -> EnemyType.QUANTUM_PHASE_WRAITH
                    8 -> EnemyType.NANITE_CARRIER
                    7 -> EnemyType.CYBER_CENTURION
                    6 -> EnemyType.STEALTH_NIGHTHAWK
                    5 -> EnemyType.ATOMIC_GOLIATH
                    4 -> EnemyType.STEAM_LANDSHIP
                    3 -> EnemyType.DIESEL_MECH_WALKER
                    2 -> EnemyType.IRONCLAD_ZEPPELIN
                    1 -> EnemyType.BOMBARD_TRAIN
                    else -> EnemyType.MUSKET_DRAGOON
                }
                w in 28..36 -> when (i % 10) {
                    9 -> EnemyType.CHRONO_PARADOX_DRIFTER
                    8 -> EnemyType.DYSON_HARVESTER_TITAN
                    7 -> EnemyType.PLASMA_HOVER_TANK
                    6 -> EnemyType.STARFLEET_CORVETTE
                    5 -> EnemyType.QUANTUM_PHASE_WRAITH
                    4 -> EnemyType.NANITE_CARRIER
                    3 -> EnemyType.CYBER_CENTURION
                    2 -> EnemyType.STEALTH_NIGHTHAWK
                    1 -> EnemyType.ATOMIC_GOLIATH
                    else -> EnemyType.DIESEL_MECH_WALKER
                }
                else -> when (i % 10) {
                    9 -> EnemyType.CHRONO_PARADOX_DRIFTER
                    8 -> EnemyType.DYSON_HARVESTER_TITAN
                    7 -> EnemyType.PLASMA_HOVER_TANK
                    6 -> EnemyType.STARFLEET_CORVETTE
                    5 -> EnemyType.QUANTUM_PHASE_WRAITH
                    4 -> EnemyType.NANITE_CARRIER
                    3 -> EnemyType.CYBER_CENTURION
                    2 -> EnemyType.STEALTH_NIGHTHAWK
                    1 -> EnemyType.ATOMIC_GOLIATH
                    else -> EnemyType.STEAM_LANDSHIP
                }
            }
            spawnQueue.addLast(type)
        }
    }

    private fun spawnEnemy(type: EnemyType, w: Int) {
        enemies.add(createEnemyInstance(type, w, startDist = 0f))
    }

    private fun createEnemyInstance(type: EnemyType, w: Int, startDist: Float): ActiveEnemy {
        val mut = WaveMutator.forWave(w)
        val earlyCurveFactor = when (w) {
            1 -> 0.72f
            2 -> 0.84f
            3 -> 0.94f
            4 -> 1.02f
            else -> 1.12f
        }
        val earlySpeedFactor = when (w) {
            1 -> 0.86f
            2 -> 0.92f
            3 -> 0.97f
            else -> 1.04f
        }

        // Determine if this raider spawns with a Veteran Elite Affix (from Wave 4+)
        val affix = if (w >= 4 && type != EnemyType.HYDRA_SPAWN && Random.nextFloat() < min(0.45f, (w - 3) * 0.032f)) {
            val pool = EnemyEliteAffix.entries.filter { it != EnemyEliteAffix.NONE }
            pool[(nextEnemyId.toInt() + w) % pool.size]
        } else {
            EnemyEliteAffix.NONE
        }

        val scale = 1.18f * earlyCurveFactor * mut.hpMult * affix.hpMult * (1.0f + w * 0.26f) * (1.078f.pow((w - 1).coerceAtMost(140)))
        val maxHp = type.baseHp * scale
        val maxShield = maxHp * (type.shieldRatio + affix.bonusShieldRatio)
        val waveArmorBonus = if (w >= 4) (w * 0.003f).coerceAtMost(0.10f) else 0f
        return ActiveEnemy(
            id = nextEnemyId++,
            type = type,
            distanceOnPath = startDist,
            maxHp = maxHp,
            hp = maxHp,
            maxShield = maxShield,
            shield = maxShield,
            armor = (type.armor + affix.bonusArmor + waveArmorBonus).coerceIn(0f, 0.86f),
            baseSpeed = type.baseSpeed * earlySpeedFactor * mut.speedMult * affix.speedMult * (1f + min(0.55f, w * 0.010f)),
            eliteAffix = affix
        )
    }

    fun upgradeTechNode(nodeId: String): Boolean = synchronized(lock) {
        val spec = TechTreeCatalog.nodes.firstOrNull { it.id == nodeId } ?: return false
        val cur = techRanks[nodeId] ?: 0
        if (cur >= spec.maxRank) return false
        val cost = if (cheatZeroCost) 0 else spec.shardCost
        if (techShards < cost) return false
        techShards -= cost
        techRanks[nodeId] = cur + 1
        soundEngine?.play(SoundEngine.Sfx.UPGRADE)
        return true
    }

    // =========================================================================
    // OPTIONAL INTERACTIVE TUTORIAL LEVEL ("HOW TO PLAY")
    // =========================================================================

    fun startOptionalTutorialLevel() = synchronized(lock) {
        if (!isTutorialMode) {
            savedEndlessSnapshot = serializeStateToString()
        }
        isTutorialMode = true
        tutorialStep = 0
        towers.clear()
        trackGadgets.clear()
        enemies.clear()
        spawnQueue.clear()
        gold = 1200L
        techShards = 12
        wave = 1
        civilizationAgeLevel = 1
        coreShield = 100
        selectedPadId = null
        selectedBuildType = TowerType.GATLING
        queueWaveEnemies(1)
    }

    fun exitOptionalTutorialLevel() = synchronized(lock) {
        if (!isTutorialMode) return
        isTutorialMode = false
        tutorialStep = 0
        val snap = savedEndlessSnapshot
        if (snap != null) {
            deserializeStateFromString(snap)
            savedEndlessSnapshot = null
        }
        addGold(350L)
    }

    private fun currentTutorialPrompt(): Pair<String, String> {
        if (!isTutorialMode) return "" to ""
        return when (tutorialStep) {
            0 -> "Step 1/5: Free-Place on the Lawn" to
                "Select a building card at the bottom and tap anywhere on the green grass beside the road to construct it!"
            1 -> "Step 2/5: Advance Ages or Combine Elements" to
                "Tap ▲ Age Up to advance to the Feudal Age (and evolve all towers!), or place complementary towers near each other!"
            2 -> "Step 3/5: 4 Upgrade Paths, Armory & Ascension" to
                "Tap any placed tower on the grass to upgrade Paths A/B/C/D up to Tier 8, equip Forge Gear, or Ascend ★ Stars!"
            3 -> "Step 4/5: Royal Treasury & Next Wave" to
                "Tap 🏛️ Bank in the top bar to earn uncapped compound interest on spare Gold, or tap +W to rush the next wave!"
            else -> "Step 5/5: Training Complete!" to
                "Tap 'Exit' to return to your campaign with +350 Bonus Gold."
        }
    }

    // =========================================================================
    // KING'S CHEAT CONSOLE ACTIONS
    // =========================================================================

    fun cheatAddGold(amount: Long) {
        synchronized(lock) {
            addGold(amount)
            soundEngine?.play(SoundEngine.Sfx.CHEAT_ACTIVATE)
        }
    }

    fun cheatMaxBankVaultAndFill() {
        synchronized(lock) {
            bankVaultTier = BankCatalog.tiers.size
            bankBalance = CurrencyMath.safeAdd(bankBalance, 1_000_000_000_000_000L) // +1.00 Quadrillion Gold
            soundEngine?.play(SoundEngine.Sfx.CHEAT_ACTIVATE)
        }
    }

    fun cheatAdvanceAge() {
        synchronized(lock) {
            if (civilizationAgeLevel < ClearancePhase.entries.size) {
                civilizationAgeLevel = (effectiveAgeLevel() + 1).coerceAtMost(ClearancePhase.entries.size)
            } else {
                civilizationAgeLevel = 1
            }
            val phase = currentClearancePhase()
            val wallTechBonus = 50 * (techRanks["ARC_KEEP"] ?: 0)
            maxCoreShield = 100 + phase.bonusMaxCastleHp + wallTechBonus
            coreShield = maxCoreShield
            evolveAllTowersToCurrentEra()
            soundEngine?.play(SoundEngine.Sfx.FUSION_APEX)
        }
    }

    fun cheatAddShards(amount: Int) {
        synchronized(lock) {
            techShards = (techShards + amount).coerceAtMost(999_999_999)
            soundEngine?.play(SoundEngine.Sfx.CHEAT_ACTIVATE)
        }
    }

    fun cheatMaxAllTech() {
        synchronized(lock) {
            for (node in TechTreeCatalog.nodes) {
                techRanks[node.id] = node.maxRank
            }
            for (tType in TowerType.entries) {
                towerMasteryRanks[tType] = 25
            }
            soundEngine?.play(SoundEngine.Sfx.CHEAT_ACTIVATE)
        }
    }

    fun cheatMaxSelectedTowerOmni() {
        synchronized(lock) {
            val padId = selectedPadId ?: return
            val t = towers[padId] ?: return
            t.pathA = 8
            t.pathB = 8
            t.pathC = 8
            t.pathD = 8
            t.xp = 250_000_000L
            t.ascensionStars = TowerAscensionCatalog.MAX_STARS
            t.gear = TowerGearAttachment.OMEGA_CHRONO_CROWN
            t.gearTier = TowerGearAttachment.MAX_FORGE_TIER
            t.multiPassCount = LabyrinthMap.countRoadPassesInRange(t.x, t.y, effectiveTowerRange(t))
            rebuildSynergyLinks()
            soundEngine?.play(SoundEngine.Sfx.CHEAT_ACTIVATE)
        }
    }

    fun cheatMaxAllTowersOmni() {
        synchronized(lock) {
            for (t in towers.values) {
                t.pathA = 8
                t.pathB = 8
                t.pathC = 8
                t.pathD = 8
                t.xp = 250_000_000L
                t.ascensionStars = TowerAscensionCatalog.MAX_STARS
                if (t.gear == TowerGearAttachment.NONE) {
                    t.gear = TowerGearAttachment.OMEGA_CHRONO_CROWN
                }
                t.gearTier = TowerGearAttachment.MAX_FORGE_TIER
                t.multiPassCount = LabyrinthMap.countRoadPassesInRange(t.x, t.y, effectiveTowerRange(t))
            }
            rebuildSynergyLinks()
            soundEngine?.play(SoundEngine.Sfx.CHEAT_ACTIVATE)
        }
    }

    fun cheatTransformSelectedToApex(apexType: TowerType) {
        synchronized(lock) {
            val padId = selectedPadId
            if (padId != null && towers.containsKey(padId)) {
                val t = towers[padId]!!
                t.type = apexType
                t.pathA = 8
                t.pathB = 8
                t.pathC = 8
                t.pathD = 8
                shockwaves.add(VisualShockwave(t.x, t.y, 180f, apexType.primaryColor))
            } else {
                val recPad = LabyrinthMap.buildPads.getOrNull(computeRecommendedEmptyPadId() ?: 18)
                val wx = recPad?.x ?: 500f
                val wy = recPad?.y ?: 578f
                val spot = LabyrinthMap.resolveFreePlacementPoint(wx, wy, towers.values.map { Vec2(it.x, it.y) }) ?: Vec2(wx, wy)
                while (towers.containsKey(nextFreeTowerId)) nextFreeTowerId++
                val newId = nextFreeTowerId++
                towers[newId] = TowerInstance(
                    padId = newId,
                    type = apexType,
                    x = spot.x,
                    y = spot.y,
                    pathA = 8,
                    pathB = 8,
                    pathC = 8,
                    pathD = 8,
                    investedGold = apexType.baseCost
                )
                shockwaves.add(VisualShockwave(spot.x, spot.y, 180f, apexType.primaryColor))
            }
            rebuildSynergyLinks()
            soundEngine?.play(SoundEngine.Sfx.FUSION_APEX)
        }
    }

    fun cheatDeployAllTrackGadgets() {
        synchronized(lock) {
            val types = TrackGadgetType.entries
            for (slot in LabyrinthMap.gadgetSlots) {
                trackGadgets[slot.id] = types[slot.id % types.size]
            }
            soundEngine?.play(SoundEngine.Sfx.GADGET_DEPLOY)
        }
    }

    fun cheatJumpWaves(delta: Int) {
        synchronized(lock) {
            wave = (wave + delta).coerceAtLeast(1)
            val natAge = ClearancePhase.naturalAgeForWave(wave)
            if (natAge > civilizationAgeLevel) {
                civilizationAgeLevel = natAge
                evolveAllTowersToCurrentEra()
            }
            spawnQueue.clear()
            queueWaveEnemies(wave)
            soundEngine?.play(SoundEngine.Sfx.CHEAT_ACTIVATE)
        }
    }

    fun cheatSpawnTestBoss() {
        synchronized(lock) {
            val bossType = when {
                wave >= 40 -> EnemyType.OMEGA_ARCHITECT_BOSS
                wave >= 28 -> EnemyType.STARFLEET_MOTHERSHIP_BOSS
                wave >= 18 -> EnemyType.INDUSTRIAL_DREADNOUGHT_BOSS
                else -> EnemyType.LEVIATHAN_BOSS
            }
            spawnEnemy(bossType, wave + 5)
            soundEngine?.play(SoundEngine.Sfx.WAVE_START)
        }
    }

    fun cheatNukeAllEnemies() {
        synchronized(lock) {
            val copy = enemies.toList()
            val splits = mutableListOf<ActiveEnemy>()
            enemies.clear()
            for (e in copy) {
                e.hp = 0f
                handleEnemyDeath(e, splits)
            }
            shockwaves.add(VisualShockwave(500f, 680f, 680f, 0xFFC75B39))
            soundEngine?.play(SoundEngine.Sfx.CORE_SURGE)
        }
    }

    fun cheatCycleWeather() {
        synchronized(lock) {
            val all = WeatherFront.entries
            currentWeather = all[(currentWeather.ordinal + 1) % all.size]
            weatherTimerSec = 60f
        }
    }

    // =========================================================================
    // AI AUTO-GOVERNOR & SYNERGY BUILDER
    // =========================================================================

    private fun runAiTacticianStep() {
        if (autoAbilities && enemies.size >= 5) {
            for (ab in CommanderAbility.entries) {
                if ((abilityCooldowns[ab] ?: 0f) <= 0f) {
                    activateCommanderAbility(ab)
                    break
                }
            }
        }
        if (autoBuild && towers.size < 36) {
            val emptyPadId = computeRecommendedEmptyPadId()
            if (emptyPadId != null) {
                val pad = LabyrinthMap.buildPads[emptyPadId]
                val chosen = chooseSmartTowerTypeForPad(emptyPadId)
                buildTowerAtGrass(pad.x, pad.y, chosen)
            }
        }
        if (autoUpgrade && towers.isNotEmpty()) {
            for (t in towers.values.toList()) {
                val cand = findFusionCandidateLocked(t.padId)
                if (cand != null && availableSpendableGold() >= cand.fusionCost) {
                    val prev = selectedPadId
                    selectedPadId = t.padId
                    fuseSelectedTower()
                    selectedPadId = prev
                    return
                }
            }
            val upgradable = towers.values.shuffled().firstOrNull { t ->
                !t.type.isFusedApex && (0..3).any { p -> canUpgradePath(t, p) }
            }
            if (upgradable != null) {
                val pathIdx = (0..3).firstOrNull { canUpgradePath(upgradable, it) } ?: 0
                val prevSel = selectedPadId
                selectedPadId = upgradable.padId
                upgradeSelectedTower(pathIdx)
                selectedPadId = prevSel
            }
        }
    }

    fun rebuildSynergyLinks() = synchronized(lock) {
        synergyLinks.clear()
        val list = towers.values.toList()
        for (i in 0 until list.size) {
            for (j in i + 1 until list.size) {
                val a = list[i]
                val b = list[j]
                val d = hypot(a.x - b.x, a.y - b.y)
                if (d > 220f) continue

                val apex = TowerType.findFusionResult(a.type, b.type)
                if (apex != null && a.totalLevel >= 4 && b.totalLevel >= 4) {
                    synergyLinks.add(SynergyLink(a.padId, b.padId, a.x, a.y, b.x, b.y, "WONDER: ${apex.shortName}", 0xFFD4AF37, isFusionReady = true))
                } else if (apex != null) {
                    synergyLinks.add(SynergyLink(a.padId, b.padId, a.x, a.y, b.x, b.y, "COMBO PAIR", 0xFF8FBCCF, isFusionReady = false))
                } else if (a.type == TowerType.HUB || b.type == TowerType.HUB ||
                    a.type == TowerType.CHRONO || b.type == TowerType.CHRONO ||
                    a.type == TowerType.PRISM || b.type == TowerType.PRISM ||
                    a.type == TowerType.SHIELD_GARRISON || b.type == TowerType.SHIELD_GARRISON ||
                    a.type.isFusedApex || b.type.isFusedApex
                ) {
                    synergyLinks.add(SynergyLink(a.padId, b.padId, a.x, a.y, b.x, b.y, "GUILD LINK", 0xFFD4AF37, isFusionReady = false))
                }
            }
        }
    }

    private fun addGold(amount: Long) {
        if (amount <= 0L) return
        if (bankAutoDepositPct > 0) {
            val cap = effectiveBankVaultCap()
            val room = (cap - bankBalance).coerceAtLeast(0L)
            val sweep = min(CurrencyMath.safePercent(amount, bankAutoDepositPct), room)
            val walletGain = (amount - sweep).coerceAtLeast(0L)
            bankBalance = CurrencyMath.safeAdd(bankBalance, sweep)
            gold = CurrencyMath.safeAdd(gold, walletGain)
        } else {
            gold = CurrencyMath.safeAdd(gold, amount)
        }
    }

    private fun addCallout(text: String, x: Float, y: Float, colorArgb: Long, isCombo: Boolean = false) {
        if (!showFloatingCallouts) return
        if (callouts.size > 16) callouts.removeFirst()
        callouts.add(VisualCallout(text, x, y, colorArgb, isCombo = isCombo))
    }

    private fun recordReactionLog(entry: String) {
        if (recentReactionsLog.size >= 6) recentReactionsLog.removeFirst()
        recentReactionsLog.addLast(entry)
    }

    fun captureUiSnapshot(): UiSnapshot = synchronized(lock) {
        val selTower = selectedPadId?.let { towers[it] }
        val selPad = selTower?.let { t ->
            BuildPad(t.padId, t.x, t.y, t.terrainBonus, t.multiPassCount)
        } ?: selectedPadId?.let { LabyrinthMap.buildPads.getOrNull(it) }
        val (tutTitle, tutInstr) = currentTutorialPrompt()
        val vaultSpec = BankCatalog.specForTier(bankVaultTier)
        val nextVaultCost = if (bankVaultTier < BankCatalog.tiers.size) {
            effectiveCost(BankCatalog.specForTier(bankVaultTier + 1).upgradeCostGold)
        } else null
        val recPad = computeRecommendedEmptyPadId()
        recommendedPadId = recPad
        val (smartLbl, smartCost) = computeSmartRecommendation()
        val phase = currentClearancePhase()
        val ascendCost = if (selTower != null && selTower.canAscend) {
            val disc = (0.08f * (techRanks["ARC_ASCEND"] ?: 0)).coerceAtMost(0.55f)
            val raw = TowerAscensionCatalog.ascensionCostGold(selTower.type.baseCost, selTower.ascensionStars)
            effectiveCost((raw * (1f - disc)).roundToLong())
        } else null

        UiSnapshot(
            gold = gold,
            bankBalance = bankBalance,
            bankVaultTier = bankVaultTier,
            bankVaultName = vaultSpec.name,
            bankWaveInterestPct = effectiveBankWaveInterestRate() * 100.0,
            bankTickInterestPct = effectiveBankLiveTickRate() * 100.0,
            bankVaultCap = effectiveBankVaultCap(),
            bankNextTierCost = nextVaultCost,
            totalBankInterestEarned = totalBankInterestEarned,
            lastBankInterestPayout = lastBankInterestPayout,
            bankAutoDepositPct = bankAutoDepositPct,
            bankOverdraftProtection = bankOverdraftProtection,
            bondPrincipal = bondPrincipal,
            bondWavesLeft = bondWavesLeft,
            techShards = techShards,
            wave = wave,
            clearancePhase = phase,
            nextAgeAdvanceCost = nextAgeAdvanceCost(),
            waveMutator = WaveMutator.forWave(wave),
            incomingThreatTip = buildIncomingThreatTip(wave),
            streamlinedUiMode = streamlinedUiMode,
            unlockAllFeatures = unlockAllFeatures || cheatOmniPath555 || cheatInfiniteGold,
            recommendedPadId = recPad,
            smartActionLabel = smartLbl,
            smartActionCost = smartCost,
            enemiesAlive = enemies.size + spawnQueue.size,
            coreShield = coreShield,
            maxCoreShield = maxCoreShield,
            coreRebounds = coreRebounds,
            flawlessStreak = flawlessStreak,
            totalKills = totalKills,
            totalCombosTriggered = totalCombosTriggered,
            currentDps = rollingDps,
            gameSpeed = gameSpeed,
            selectedPadId = selectedPadId,
            selectedPad = selPad,
            selectedTower = selTower?.copy(),
            selectedTowerAscendCost = ascendCost,
            selectedGadgetSlotId = selectedGadgetSlotId,
            selectedBuildType = selectedBuildType,
            buildFilterCategory = buildFilterCategory,
            fusionCandidate = findFusionCandidateLocked(selectedPadId),
            weather = currentWeather,
            weatherTimerSec = weatherTimerSec,
            showSynergyOverlay = showSynergyOverlay,
            showHeatmapOverlay = showHeatmapOverlay,
            autoBuild = autoBuild,
            autoUpgrade = autoUpgrade,
            autoAbilities = autoAbilities,
            autoWaveRush = autoWaveRush,
            abilityCooldowns = abilityCooldowns.toMap(),
            overclockActiveSec = overclockActiveSec,
            techRanks = techRanks.toMap(),
            towerMasteryRanks = towerMasteryRanks.toMap(),
            recentReactionsLog = recentReactionsLog.toList(),
            cheatInfiniteGold = cheatInfiniteGold,
            cheatOmniPath555 = cheatOmniPath555,
            cheatHyperSpeed = cheatHyperSpeed,
            cheatOneShotKill = cheatOneShotKill,
            cheatZeroCost = cheatZeroCost,
            cheatGodCore = cheatGodCore,
            cheatFreezeEnemies = cheatFreezeEnemies,
            isTutorialMode = isTutorialMode,
            tutorialStep = tutorialStep,
            tutorialTitle = tutTitle,
            tutorialInstruction = tutInstr,
            isMuted = soundEngine?.isMuted ?: false,
            activeSaveSlotId = activeSaveSlotId,
            lastEraEvolutionMessage = lastEraEvolutionMessage,
            activeEdicts = activeEdicts()
        )
    }

    // =========================================================================
    // SAVE & LOAD WITH 6 CAMPAIGN SAVE SLOTS & SEAMLESS LEGACY MIGRATION (v1..v5)
    // =========================================================================

    fun serializeStateToString(): String = synchronized(lock) {
        val sb = StringBuilder()
        sb.append("v=$SAVE_SCHEMA_VERSION;")
        sb.append("gold=$gold;bank=$bankBalance;btier=$bankVaultTier;bearn=$totalBankInterestEarned;")
        sb.append("bauto=$bankAutoDepositPct;bover=$bankOverdraftProtection;bondp=$bondPrincipal;bondw=$bondWavesLeft;")
        sb.append("simple=$streamlinedUiMode;unlockall=$unlockAllFeatures;age=$civilizationAgeLevel;")
        sb.append("shards=$techShards;wave=$wave;kills=$totalKills;combos=$totalCombosTriggered;")
        val towerStr = towers.values.joinToString("|") { t ->
            "${t.padId},${t.type.id},${t.pathA},${t.pathB},${t.pathC},${t.targeting.name},${t.investedGold},${t.x.toInt()},${t.y.toInt()},${t.gear.id},${t.gearTier},${t.xp},${t.ascensionStars},${t.pathD}"
        }
        sb.append("towers=$towerStr;")
        val gadgetStr = trackGadgets.entries.joinToString("|") { "${it.key},${it.value.id}" }
        sb.append("gadgets=$gadgetStr;")
        val techStr = techRanks.entries.joinToString("|") { "${it.key},${it.value}" }
        sb.append("tech=$techStr;")
        val masteryStr = towerMasteryRanks.entries.joinToString("|") { "${it.key.id},${it.value}" }
        sb.append("mastery=$masteryStr")
        return sb.toString()
    }

    /**
     * Deserializes any save from schema v1..v5 into the expanded 18-Age / 4-Path / 8-Tier engine
     * WITHOUT wiping existing campaigns when upgrading!
     */
    fun deserializeStateFromString(raw: String) = synchronized(lock) {
        if (raw.isBlank()) return
        val map = raw.split(";").mapNotNull { part ->
            val idx = part.indexOf('=')
            if (idx > 0) part.substring(0, idx) to part.substring(idx + 1) else null
        }.toMap()
        if (map.isEmpty()) return

        gold = (map["gold"]?.toLongOrNull() ?: gold).coerceIn(0L, MAX_SAFE_GOLD)
        bankBalance = (map["bank"]?.toLongOrNull() ?: bankBalance).coerceIn(0L, MAX_SAFE_GOLD)
        bankVaultTier = (map["btier"]?.toIntOrNull() ?: bankVaultTier).coerceIn(1, BankCatalog.tiers.size)
        totalBankInterestEarned = (map["bearn"]?.toLongOrNull() ?: totalBankInterestEarned).coerceIn(0L, MAX_SAFE_GOLD)
        bankAutoDepositPct = map["bauto"]?.toIntOrNull() ?: bankAutoDepositPct
        bankOverdraftProtection = map["bover"]?.toBooleanStrictOrNull() ?: bankOverdraftProtection
        bondPrincipal = (map["bondp"]?.toLongOrNull() ?: bondPrincipal).coerceIn(0L, MAX_SAFE_GOLD)
        bondWavesLeft = map["bondw"]?.toIntOrNull() ?: bondWavesLeft
        streamlinedUiMode = map["simple"]?.toBooleanStrictOrNull() ?: streamlinedUiMode
        unlockAllFeatures = map["unlockall"]?.toBooleanStrictOrNull() ?: unlockAllFeatures
        techShards = map["shards"]?.toIntOrNull() ?: techShards
        wave = map["wave"]?.toIntOrNull()?.coerceAtLeast(1) ?: wave
        civilizationAgeLevel = (map["age"]?.toIntOrNull() ?: ClearancePhase.naturalAgeForWave(wave)).coerceIn(1, ClearancePhase.entries.size)
        totalKills = map["kills"]?.toLongOrNull() ?: totalKills
        totalCombosTriggered = map["combos"]?.toLongOrNull() ?: totalCombosTriggered

        towers.clear()
        val towerStr = map["towers"].orEmpty()
        if (towerStr.isNotBlank()) {
            for (item in towerStr.split("|")) {
                val p = item.split(",")
                if (p.size >= 7) {
                    val padId = p[0].toIntOrNull() ?: continue
                    val tType = TowerType.entries.firstOrNull { it.id == p[1] } ?: continue
                    val pA = (p[2].toIntOrNull() ?: 0).coerceIn(0, 8)
                    val pB = (p[3].toIntOrNull() ?: 0).coerceIn(0, 8)
                    val pC = (p[4].toIntOrNull() ?: 0).coerceIn(0, 8)
                    val prio = TargetingPriority.entries.firstOrNull { it.name == p[5] } ?: TargetingPriority.FIRST
                    val inv = p[6].toLongOrNull() ?: tType.baseCost
                    // Support both v4/v5 free-placement coordinates AND legacy v1..v3 pad-indexed saves!
                    val fallbackPad = LabyrinthMap.buildPads.getOrNull(padId % LabyrinthMap.buildPads.size)
                    val wx = p.getOrNull(7)?.toFloatOrNull() ?: (fallbackPad?.x ?: 500f)
                    val wy = p.getOrNull(8)?.toFloatOrNull() ?: (fallbackPad?.y ?: 500f)
                    val gear = p.getOrNull(9)?.let { gid -> TowerGearAttachment.entries.firstOrNull { it.id == gid } } ?: TowerGearAttachment.NONE
                    val gTier = (p.getOrNull(10)?.toIntOrNull() ?: 1).coerceIn(1, TowerGearAttachment.MAX_FORGE_TIER)
                    val xpVal = p.getOrNull(11)?.toLongOrNull() ?: 0L
                    val stars = (p.getOrNull(12)?.toIntOrNull() ?: 0).coerceIn(0, TowerAscensionCatalog.MAX_STARS)
                    val pD = (p.getOrNull(13)?.toIntOrNull() ?: if (tType.isFusedApex) 8 else 0).coerceIn(0, 8)
                    val site = LabyrinthMap.evaluateFreePlacementSite(wx, wy, tType.baseRange)
                    towers[padId] = TowerInstance(
                        padId = padId,
                        type = tType,
                        x = wx,
                        y = wy,
                        terrainBonus = site.terrainBonus,
                        multiPassCount = site.multiPassRating,
                        pathA = if (tType.isFusedApex) 8 else pA,
                        pathB = if (tType.isFusedApex) 8 else pB,
                        pathC = if (tType.isFusedApex) 8 else pC,
                        pathD = pD,
                        targeting = prio,
                        gear = gear,
                        gearTier = gTier,
                        xp = xpVal,
                        ascensionStars = stars,
                        investedGold = inv
                    )
                    if (padId >= nextFreeTowerId) nextFreeTowerId = padId + 1
                }
            }
        }

        val gadgetStr = map["gadgets"].orEmpty()
        if (gadgetStr.isNotBlank()) {
            trackGadgets.clear()
            for (item in gadgetStr.split("|")) {
                val p = item.split(",")
                if (p.size >= 2) {
                    val slotId = p[0].toIntOrNull() ?: continue
                    val gType = TrackGadgetType.entries.firstOrNull { it.id == p[1] } ?: continue
                    if (slotId in LabyrinthMap.gadgetSlots.indices) {
                        trackGadgets[slotId] = gType
                    }
                }
            }
        }

        val techStr = map["tech"].orEmpty()
        if (techStr.isNotBlank()) {
            for (item in techStr.split("|")) {
                val p = item.split(",")
                if (p.size >= 2) {
                    val r = p[1].toIntOrNull() ?: 0
                    techRanks[p[0]] = r
                }
            }
        }

        val masteryStr = map["mastery"].orEmpty()
        if (masteryStr.isNotBlank()) {
            for (item in masteryStr.split("|")) {
                val p = item.split(",")
                if (p.size >= 2) {
                    val tType = TowerType.entries.firstOrNull { it.id == p[0] } ?: continue
                    val r = p[1].toIntOrNull() ?: 0
                    towerMasteryRanks[tType] = r
                }
            }
        }

        val wallTechBonus = 50 * (techRanks["ARC_KEEP"] ?: 0)
        maxCoreShield = 100 + currentClearancePhase().bonusMaxCastleHp + wallTechBonus
        coreShield = coreShield.coerceIn(1, maxCoreShield)
        selectedPadId = null
        evolveAllTowersToCurrentEra()
        recommendedPadId = computeRecommendedEmptyPadId()
        spawnQueue.clear()
        enemies.clear()
        queueWaveEnemies(wave)
    }

    fun getSaveSlots(prefs: SharedPreferences): List<SaveSlotSummary> = synchronized(lock) {
        SaveSlotManager.ensureLegacySaveMigrated(prefs)
        return SaveSlotManager.listSlots(prefs)
    }

    fun saveToSlot(prefs: SharedPreferences, slotId: Int, customLabel: String? = null): Boolean = synchronized(lock) {
        if (isTutorialMode) return false
        val targetSlot = slotId.coerceIn(1, SaveSlotManager.MAX_SLOTS)
        activeSaveSlotId = targetSlot
        val data = serializeStateToString()
        val phase = currentClearancePhase()
        SaveSlotManager.saveToSlot(
            prefs = prefs,
            slotId = targetSlot,
            serializedState = data,
            wave = wave,
            ageLevel = effectiveAgeLevel(),
            ageTitle = "${phase.badge} (${phase.title})",
            gold = gold,
            bankBalance = bankBalance,
            towerCount = towers.size,
            totalKills = totalKills,
            customLabel = customLabel
        )
        return true
    }

    fun loadFromSlot(prefs: SharedPreferences, slotId: Int): Boolean = synchronized(lock) {
        SaveSlotManager.ensureLegacySaveMigrated(prefs)
        val targetSlot = slotId.coerceIn(1, SaveSlotManager.MAX_SLOTS)
        val raw = SaveSlotManager.loadRawFromSlot(prefs, targetSlot) ?: return false
        try {
            deserializeStateFromString(raw)
            activeSaveSlotId = targetSlot
            SaveSlotManager.setActiveSlot(prefs, targetSlot)
            soundEngine?.play(SoundEngine.Sfx.UPGRADE)
            return true
        } catch (_: Exception) {
            return false
        }
    }

    fun cloneCurrentToSlot(prefs: SharedPreferences, targetSlotId: Int): Boolean = synchronized(lock) {
        val target = targetSlotId.coerceIn(1, SaveSlotManager.MAX_SLOTS)
        save(prefs)
        return SaveSlotManager.cloneSlot(prefs, activeSaveSlotId, target)
    }

    fun startNewCampaignInSlot(prefs: SharedPreferences, slotId: Int, customLabel: String? = null): Boolean = synchronized(lock) {
        val target = slotId.coerceIn(1, SaveSlotManager.MAX_SLOTS)
        activeSaveSlotId = target
        isTutorialMode = false
        tutorialStep = 0
        gold = 650L
        bankBalance = 250L
        bankVaultTier = 1
        totalBankInterestEarned = 0L
        lastBankInterestPayout = 0L
        bondPrincipal = 0L
        bondWavesLeft = 0
        techShards = 12
        wave = 1
        civilizationAgeLevel = 1
        coreShield = 100
        maxCoreShield = 100
        totalKills = 0L
        totalCombosTriggered = 0L
        towers.clear()
        trackGadgets.clear()
        for (node in TechTreeCatalog.nodes) techRanks[node.id] = 0
        for (tType in TowerType.entries) towerMasteryRanks[tType] = 0
        selectedPadId = null
        selectedGadgetSlotId = null
        enemies.clear()
        spawnQueue.clear()
        evolveAllTowersToCurrentEra()
        recommendedPadId = computeRecommendedEmptyPadId()
        queueWaveEnemies(1)
        saveToSlot(prefs, target, customLabel ?: "Campaign $target")
        soundEngine?.play(SoundEngine.Sfx.WAVE_START)
        return true
    }

    fun save(prefs: SharedPreferences): Unit = synchronized(lock) {
        if (isTutorialMode) return@synchronized
        SaveSlotManager.ensureLegacySaveMigrated(prefs)
        saveToSlot(prefs, activeSaveSlotId)
    }

    fun load(prefs: SharedPreferences) = synchronized(lock) {
        SaveSlotManager.ensureLegacySaveMigrated(prefs)
        val slotId = SaveSlotManager.getActiveSlot(prefs)
        activeSaveSlotId = slotId
        val slotRaw = SaveSlotManager.loadRawFromSlot(prefs, slotId)
        val primary = slotRaw ?: prefs.getString("state_v1", null)
        val backup = prefs.getString("state_backup_v1", null)
        try {
            if (!primary.isNullOrBlank()) {
                deserializeStateFromString(primary)
            } else if (!backup.isNullOrBlank()) {
                deserializeStateFromString(backup)
            }
        } catch (_: Exception) {
            if (!backup.isNullOrBlank()) {
                try {
                    deserializeStateFromString(backup)
                } catch (_: Exception) {
                }
            }
        }
    }

    fun toggleMute(): Boolean {
        val se = soundEngine ?: return false
        se.isMuted = !se.isMuted
        return se.isMuted
    }

    fun <T> executeLockedRenderRead(block: (GameEngine) -> T): T = synchronized(lock) {
        block(this)
    }
}

package com.brandonwallace.labyrinthtd.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.content.Context
import com.brandonwallace.labyrinthtd.game.BankCatalog
import com.brandonwallace.labyrinthtd.game.ClearancePhase
import com.brandonwallace.labyrinthtd.game.ComboReaction
import com.brandonwallace.labyrinthtd.game.CommanderAbility
import com.brandonwallace.labyrinthtd.game.CurrencyMath
import com.brandonwallace.labyrinthtd.game.EnemyEliteAffix
import com.brandonwallace.labyrinthtd.game.EnemyType
import com.brandonwallace.labyrinthtd.game.EraEvolutionCatalog
import com.brandonwallace.labyrinthtd.game.GameEngine
import com.brandonwallace.labyrinthtd.game.HistoricalCodexAndBlueprints
import com.brandonwallace.labyrinthtd.game.TechTreeCatalog
import com.brandonwallace.labyrinthtd.game.TowerAscensionCatalog
import com.brandonwallace.labyrinthtd.game.TowerGearAttachment
import com.brandonwallace.labyrinthtd.game.TowerInstance
import com.brandonwallace.labyrinthtd.game.TowerType
import com.brandonwallace.labyrinthtd.game.TowerVeteranCatalog
import com.brandonwallace.labyrinthtd.game.UiSnapshot
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

// Warm Age of Empires Wood, Stone, Brass & Parchment Palette (Zero Neon)
private val WoodDark = Color(0xEE1F1711)
private val WoodPanel = Color(0xFF2C2118)
private val WoodCard = Color(0xFF382A1F)
private val StoneCard = Color(0xFF433D36)
private val BrassBorder = Color(0xFF8C6F43)
private val GoldAccent = Color(0xFFD4AF37)
private val GoldBright = Color(0xFFE6C65A)
private val ParchmentText = Color(0xFFF5EBD6)
private val MutedParchment = Color(0xFFC7B89B)
private val MeadowGreen = Color(0xFF4E8C40)
private val MeadowGreenBg = Color(0xFF243B1E)
private val TerracottaRed = Color(0xFFC75B39)
private val TerracottaBg = Color(0xFF422118)

private enum class ModalType {
    NONE,
    COMMAND_MENU,
    SAVE_SLOTS,
    TOWN_CENTER_AGE,
    BANK_VAULT,
    TOWER_ARMORY_FORGE,
    USER_GUIDE,
    CHEAT_MENU,
    TECH_TREE,
    AUTO_TACTICIAN
}

fun formatNumber(n: Long): String = CurrencyMath.formatGold(n)

private fun towerIcon(type: TowerType, ageNum: Int): String = when (type) {
    TowerType.GATLING -> if (ageNum <= 1) "🪨" else if (ageNum in 2..6) "🏹" else "🔫"
    TowerType.SNIPER -> if (ageNum <= 3) "🏹" else "🎯"
    TowerType.MORTAR -> "☄️"
    TowerType.HYDRO -> "💧"
    TowerType.CRYO -> "❄️"
    TowerType.HUB -> "🏛️"
    TowerType.PYRO -> "🔥"
    TowerType.VENOM -> "🧪"
    TowerType.DRONE -> "🦅"
    TowerType.TESLA -> "⚡"
    TowerType.PRISM -> "☀️"
    TowerType.CHRONO -> "⏳"
    TowerType.RAIL_CANNON -> "🚄"
    TowerType.LASER_ARRAY -> "🔦"
    TowerType.GRAVITY_WELL -> "🌀"
    TowerType.SHIELD_GARRISON -> "🛡️"
    TowerType.DYSON_HELIOS_ARRAY -> "🌞"
    TowerType.LEVIATHAN_GAUSS_CITADEL -> "🛰️"
    TowerType.OMEGA_SINGULARITY_CORE -> "🌌"
    TowerType.AEGIS_ORBITAL_BASTION -> "🏰"
    TowerType.QUANTUM_CHRONO_MATRIX -> "⚛️"
    TowerType.NANITE_SWARM_HIVE -> "🐝"
    else -> "👑"
}

@Composable
fun LabyrinthHudOverlay(
    snap: UiSnapshot,
    engine: GameEngine,
    onResetCamera: () -> Unit,
    onReplayIntro: () -> Unit = {},
    onAction: () -> Unit
) {
    var activeModal by remember { mutableStateOf(ModalType.NONE) }
    val context = LocalContext.current
    val spendableGold = if (snap.bankOverdraftProtection) {
        CurrencyMath.safeAdd(snap.gold, snap.bankBalance)
    } else {
        snap.gold
    }
    val ageNum = snap.clearancePhase.phaseNum

    Box(modifier = Modifier.fillMaxSize()) {
        // =====================================================================
        // 1. ENHANCED 2-ROW 2D TOP HUD BAR (Zero Crunched or Hidden Buttons!)
        // =====================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(WoodDark)
                .border(width = 1.dp, color = BrassBorder)
                .statusBarsPadding()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // ROW 1: 4 Balanced Primary Empire Readouts (Age Up, Gold, Bank Vault, Keep & Wave Rush)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1) Age Pill + 1-Tap Direct Age Up
                val nextCost = snap.nextAgeAdvanceCost
                val canAdvanceAge = nextCost != null && spendableGold >= nextCost
                Box(
                    modifier = Modifier
                        .weight(1.28f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (canAdvanceAge) MeadowGreenBg else WoodCard)
                        .border(
                            1.dp,
                            if (canAdvanceAge) GoldBright else BrassBorder,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable {
                            if (canAdvanceAge) {
                                engine.advanceCivilizationAge()
                                onAction()
                            } else {
                                activeModal = ModalType.TOWN_CENTER_AGE
                            }
                        }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val ageLabel = if (nextCost != null) {
                        "🏰 ${snap.clearancePhase.title} ▲${formatNumber(nextCost)}"
                    } else {
                        "🏰 ${snap.clearancePhase.title} ★MAX"
                    }
                    Text(
                        text = ageLabel,
                        color = if (canAdvanceAge) GoldBright else ParchmentText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // 2) Gold Treasury Counter Box
                val goldStr = if (snap.cheatInfiniteGold) "🪙 ∞ Gold" else "🪙 ${formatNumber(snap.gold)}"
                Box(
                    modifier = Modifier
                        .weight(0.92f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(WoodCard)
                        .border(1.dp, GoldAccent, RoundedCornerShape(6.dp))
                        .clickable { activeModal = ModalType.BANK_VAULT }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = goldStr,
                        color = GoldBright,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Serif,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // 3) Royal Treasury Bank Pill
                Box(
                    modifier = Modifier
                        .weight(1.05f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(WoodCard)
                        .border(1.dp, GoldAccent, RoundedCornerShape(6.dp))
                        .clickable { activeModal = ModalType.BANK_VAULT }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🏛️ ${formatNumber(snap.bankBalance)} (+${snap.bankWaveInterestPct.roundToInt()}%)",
                        color = GoldAccent,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // 4) Keep HP & Wave (Tap to rush next wave for +35% Gold)
                Box(
                    modifier = Modifier
                        .weight(0.95f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(WoodCard)
                        .border(1.dp, BrassBorder, RoundedCornerShape(6.dp))
                        .clickable {
                            engine.rushNextWave()
                            onAction()
                        }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🛡️${snap.coreShield} • W${snap.wave}⏩",
                        color = ParchmentText,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // ROW 2: Permanent Gold Creator Crest + Uncrunched Quick-Command Toolbar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Permanent 3D Embossed Metallic Gold Creator Crest (Tap to replay 3D 4K Intro)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFF18120A))
                        .border(1.dp, GoldAccent, RoundedCornerShape(5.dp))
                        .clickable { onReplayIntro() }
                        .padding(horizontal = 4.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "★ CREATED BY: BRANDON J. WALLACE ★",
                        color = Color(0xFFFFDF73),
                        fontSize = 8.0.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Serif,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // Game Speed Cycle (1x -> 2x -> 4x -> 8x -> 12x)
                val speedLbl = when {
                    snap.gameSpeed >= 10.5f -> "⏩12x"
                    snap.gameSpeed >= 7.0f -> "⏩8x"
                    snap.gameSpeed >= 3.5f -> "⏩4x"
                    snap.gameSpeed >= 1.8f -> "⏩2x"
                    else -> "⏩1x"
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(WoodCard)
                        .border(1.dp, BrassBorder, RoundedCornerShape(5.dp))
                        .clickable {
                            engine.gameSpeed = when {
                                snap.gameSpeed < 1.5f -> 2.0f
                                snap.gameSpeed < 3.0f -> 4.0f
                                snap.gameSpeed < 6.0f -> 8.0f
                                snap.gameSpeed < 10.0f -> 12.0f
                                else -> 1.0f
                            }
                            onAction()
                        }
                        .padding(horizontal = 5.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = speedLbl,
                        color = GoldBright,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                // Blacksmith & University Tech Tree Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(WoodCard)
                        .border(1.dp, BrassBorder, RoundedCornerShape(5.dp))
                        .clickable { activeModal = ModalType.TECH_TREE }
                        .padding(horizontal = 5.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "⚒️${snap.techShards}",
                        color = ParchmentText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                // Royal Auto-Governor Quick Button
                val anyAuto = snap.autoBuild || snap.autoUpgrade || snap.autoAbilities || snap.autoWaveRush
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(if (anyAuto) MeadowGreenBg else WoodCard)
                        .border(1.dp, if (anyAuto) GoldBright else BrassBorder, RoundedCornerShape(5.dp))
                        .clickable { activeModal = ModalType.AUTO_TACTICIAN }
                        .padding(horizontal = 5.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "🤖Auto",
                        color = if (anyAuto) GoldBright else ParchmentText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                // 6-Slot Campaign Save Manager Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(WoodCard)
                        .border(1.dp, GoldAccent, RoundedCornerShape(5.dp))
                        .clickable { activeModal = ModalType.SAVE_SLOTS }
                        .padding(horizontal = 5.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "💾S${snap.activeSaveSlotId}",
                        color = GoldBright,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                // Command Menu Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(WoodCard)
                        .border(1.dp, GoldAccent, RoundedCornerShape(5.dp))
                        .clickable { activeModal = ModalType.COMMAND_MENU }
                        .padding(horizontal = 5.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "☰Menu",
                        color = ParchmentText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }

        // =====================================================================
        // 2. ULTRA-CLEAN BOTTOM FREE-PLACEMENT DOCK (Single Row in Build Mode!)
        // =====================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(WoodDark)
                .border(width = 1.dp, color = BrassBorder)
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            if (snap.selectedTower != null) {
                SelectedTowerDock(
                    snap = snap,
                    tower = snap.selectedTower,
                    ageNum = ageNum,
                    spendableGold = spendableGold,
                    engine = engine,
                    onOpenArmory = { activeModal = ModalType.TOWER_ARMORY_FORGE },
                    onAction = onAction
                )
            } else {
                FreePlaceBuildDock(
                    snap = snap,
                    ageNum = ageNum,
                    spendableGold = spendableGold,
                    engine = engine,
                    onOpenTownCenter = { activeModal = ModalType.TOWN_CENTER_AGE },
                    onAction = onAction
                )
            }
        }

        // =====================================================================
        // 3. MODALS (Town Center, Save Slots, Bank, Armory Forge, Tech Tree, Menu, Cheats)
        // =====================================================================
        when (activeModal) {
            ModalType.COMMAND_MENU -> CommandMenuDialog(
                snap = snap,
                engine = engine,
                onResetCamera = onResetCamera,
                onReplayIntro = {
                    activeModal = ModalType.NONE
                    onReplayIntro()
                },
                onOpenModal = { activeModal = it },
                onAction = onAction,
                onDismiss = { activeModal = ModalType.NONE }
            )
            ModalType.SAVE_SLOTS -> SaveSlotsDialog(
                snap = snap,
                engine = engine,
                context = context,
                onAction = onAction,
                onDismiss = { activeModal = ModalType.NONE }
            )
            ModalType.TOWN_CENTER_AGE -> TownCenterAgeDialog(
                snap = snap,
                spendableGold = spendableGold,
                engine = engine,
                onOpenTechTree = { activeModal = ModalType.TECH_TREE },
                onAction = onAction,
                onDismiss = { activeModal = ModalType.NONE }
            )
            ModalType.BANK_VAULT -> BankVaultDialog(
                snap = snap,
                engine = engine,
                onAction = onAction,
                onDismiss = { activeModal = ModalType.NONE }
            )
            ModalType.TOWER_ARMORY_FORGE -> TowerArmoryForgeDialog(
                snap = snap,
                spendableGold = spendableGold,
                engine = engine,
                onAction = onAction,
                onDismiss = { activeModal = ModalType.NONE }
            )
            ModalType.USER_GUIDE -> UserGuideDialog(
                snap = snap,
                engine = engine,
                onAction = onAction,
                onDismiss = { activeModal = ModalType.NONE }
            )
            ModalType.CHEAT_MENU -> CheatMenuDialog(
                snap = snap,
                engine = engine,
                onAction = onAction,
                onDismiss = { activeModal = ModalType.NONE }
            )
            ModalType.TECH_TREE -> TechTreeDialog(
                snap = snap,
                spendableGold = spendableGold,
                engine = engine,
                onAction = onAction,
                onDismiss = { activeModal = ModalType.NONE }
            )
            ModalType.AUTO_TACTICIAN -> AutoTacticianDialog(
                snap = snap,
                engine = engine,
                onAction = onAction,
                onDismiss = { activeModal = ModalType.NONE }
            )
            ModalType.NONE -> {}
        }
    }
}

/**
 * Single-row bottom building bar:
 * Select a building card, then tap ANYWHERE on the green lawn grass to place it!
 */
@Composable
private fun FreePlaceBuildDock(
    snap: UiSnapshot,
    ageNum: Int,
    spendableGold: Long,
    engine: GameEngine,
    onOpenTownCenter: () -> Unit,
    onAction: () -> Unit
) {
    val unlockedIds = snap.clearancePhase.unlockedTowerIds
    val visibleTowers = TowerType.buildableBaseTowers.filter { t ->
        snap.unlockAllFeatures || unlockedIds.contains(t.id)
    }
    val nextCost = snap.nextAgeAdvanceCost

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (visibleTowers.size <= 3 && !snap.unlockAllFeatures) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (tType in visibleTowers) {
                    val cost = engine.effectiveCost(tType.baseCost)
                    val canAfford = spendableGold >= cost
                    val isSelected = snap.selectedBuildType == tType
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(7.dp))
                            .background(if (isSelected) StoneCard else WoodCard)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) GoldBright else if (canAfford) BrassBorder else Color(0xFF4A3B2C),
                                shape = RoundedCornerShape(7.dp)
                            )
                            .clickable {
                                engine.setBuildTowerType(tType)
                                onAction()
                            }
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Column {
                            Text(
                                text = "${towerIcon(tType, ageNum)} ${tType.ageShortName(ageNum)}",
                                color = ParchmentText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (cost == 0L) "FREE • Tap Grass" else "🪙 ${formatNumber(cost)}G",
                                color = if (canAfford) GoldBright else TerracottaRed,
                                fontSize = 9.0.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        } else {
            // Scrollable Tower Cards on the Left so the Pinned Town Center / Age-Up Card on the Right NEVER disappears!
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (tType in visibleTowers) {
                    val cost = engine.effectiveCost(tType.baseCost)
                    val canAfford = spendableGold >= cost
                    val isSelected = snap.selectedBuildType == tType
                    Box(
                        modifier = Modifier
                            .width(105.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(if (isSelected) StoneCard else WoodCard)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) GoldBright else if (canAfford) BrassBorder else Color(0xFF4A3B2C),
                                shape = RoundedCornerShape(7.dp)
                            )
                            .clickable {
                                engine.setBuildTowerType(tType)
                                onAction()
                            }
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Column {
                            Text(
                                text = "${towerIcon(tType, ageNum)} ${tType.ageShortName(ageNum)}",
                                color = ParchmentText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (cost == 0L) "FREE" else "🪙 ${formatNumber(cost)}G",
                                color = if (canAfford) GoldBright else TerracottaRed,
                                fontSize = 9.0.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Pinned Right-Side Town Center / Age-Up Card (Always visible on screen in every Age!)
        val canAdvance = nextCost != null && spendableGold >= nextCost
        val nextAgeTitle = if (nextCost != null) ClearancePhase.forAgeLevel(ageNum + 1).title else "Town Center"
        Box(
            modifier = Modifier
                .width(104.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(if (canAdvance) MeadowGreenBg else WoodCard)
                .border(1.5.dp, if (canAdvance) GoldBright else GoldAccent, RoundedCornerShape(7.dp))
                .clickable {
                    if (canAdvance) {
                        engine.advanceCivilizationAge()
                        onAction()
                    } else {
                        onOpenTownCenter()
                    }
                }
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Column {
                Text(
                    text = "🏰 $nextAgeTitle",
                    color = GoldBright,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (nextCost != null) "▲ ${formatNumber(nextCost)}G Age Up" else "👑 Edicts & Lore",
                    color = if (canAdvance) ParchmentText else MutedParchment,
                    fontSize = 9.0.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SelectedTowerDock(
    snap: UiSnapshot,
    tower: TowerInstance,
    ageNum: Int,
    spendableGold: Long,
    engine: GameEngine,
    onOpenArmory: () -> Unit,
    onAction: () -> Unit
) {
    val vet = tower.veteranRank
    val eraProfile = engine.eraProfileFor(tower.type)
    val starBadge = if (tower.ascensionStars > 0) " ★${tower.ascensionStars}" else ""

    // HEADER ROW A: Full Tower Name & Combat Stats on Left + Clean Close Button on Right (Never Crunched!)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 6.dp)) {
            Text(
                text = "${towerIcon(tower.type, ageNum)} ${tower.type.ageDisplayName(ageNum)} Lv.${tower.totalLevel}$starBadge",
                color = GoldBright,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Era ${String.format("%.1f", eraProfile.damageMult)}x Dmg • ${vet.shortBadge} ${vet.title} • ${tower.terrainBonus.badge} (${tower.multiPassCount}x) • Kills: ${tower.totalKills}",
                color = MutedParchment,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        WoodButton(
            label = "✕ Close",
            bgColor = WoodCard,
            borderColor = BrassBorder,
            textColor = ParchmentText
        ) {
            engine.selectPad(null)
            onAction()
        }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // HEADER ROW B: Balanced Equal-Weight Command Strip (Armory, Target, Ascend, Wonder, Sell — All Visible!)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1) Armory Forge Gear Attachment button
        val gearLabel = if (tower.gear == TowerGearAttachment.NONE) {
            "⚔️ Armory"
        } else {
            "${tower.gear.icon} ${tower.gear.shortTag} T${tower.gearTier}"
        }
        WoodButton(
            label = gearLabel,
            bgColor = WoodCard,
            borderColor = GoldAccent,
            textColor = GoldBright,
            modifier = Modifier.weight(1.05f),
            onClick = onOpenArmory
        )

        // 2) Targeting Priority Cycle button
        WoodButton(
            label = "🎯 ${tower.targeting.shortTag}",
            bgColor = WoodCard,
            borderColor = BrassBorder,
            textColor = ParchmentText,
            modifier = Modifier.weight(0.95f)
        ) {
            engine.cycleSelectedTowerTargeting()
            onAction()
        }

        // 3) Ascension / Prestige Star button (Always visible!)
        val ascendCost = snap.selectedTowerAscendCost
        if (ascendCost != null) {
            val canAscend = spendableGold >= ascendCost
            WoodButton(
                label = "★${tower.ascensionStars + 1} (${formatNumber(ascendCost)}G)",
                bgColor = if (canAscend) MeadowGreenBg else WoodCard,
                borderColor = GoldBright,
                textColor = GoldBright,
                modifier = Modifier.weight(1.1f)
            ) {
                engine.ascendSelectedTower()
                onAction()
            }
        } else {
            WoodButton(
                label = "★Ascend (Lv6+)",
                bgColor = WoodCard,
                borderColor = BrassBorder,
                textColor = MutedParchment,
                modifier = Modifier.weight(1.05f),
                onClick = onOpenArmory
            )
        }

        // 4) Grand Wonder Fusion button when adjacent Lv4+ partner is ready
        val fusion = snap.fusionCandidate
        if (fusion != null) {
            val canFuse = spendableGold >= fusion.fusionCost
            WoodButton(
                label = "🏛️ ${formatNumber(fusion.fusionCost)}G",
                bgColor = if (canFuse) MeadowGreenBg else WoodCard,
                borderColor = GoldBright,
                textColor = GoldBright,
                modifier = Modifier.weight(1.05f)
            ) {
                engine.fuseSelectedTower()
                onAction()
            }
        }

        // 5) Sell / Salvage button
        val refund = CurrencyMath.safeMultiply(tower.investedGold, 0.75).coerceAtLeast(tower.type.baseCost / 2)
        WoodButton(
            label = "🪙+${formatNumber(refund)}",
            bgColor = TerracottaBg,
            borderColor = TerracottaRed,
            textColor = ParchmentText,
            modifier = Modifier.weight(0.95f)
        ) {
            engine.sellSelectedTower()
            onAction()
        }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // ROW C: 4 Upgrade Paths (Path A, Path B, Path C, Path D: Era & Cosmic Overdrive) with 8-Pip Progress Bar!
    if (tower.type.isFusedApex) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(WoodCard)
                .border(1.dp, GoldAccent, RoundedCornerShape(6.dp))
                .padding(7.dp)
        ) {
            Text(
                text = "👑 ${tower.type.ageDisplayName(ageNum)} (Skill: ${tower.type.doctrineSkillTitle}): ${tower.type.roleSummary}",
                color = GoldBright,
                fontSize = 10.sp
            )
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            for (pathIdx in 0..3) {
                val pathSpec = tower.type.paths.getOrNull(pathIdx) ?: continue
                val curTier = tower.tierOf(pathIdx)
                val nextSpec = pathSpec.tiers.getOrNull(curTier)
                val canUpg = engine.canUpgradePath(tower, pathIdx)
                val cost = nextSpec?.let { engine.effectiveCost(it.cost) } ?: 0L
                val affordable = canUpg && nextSpec != null && spendableGold >= cost

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(7.dp))
                        .background(if (affordable) StoneCard else WoodCard)
                        .border(
                            1.dp,
                            if (curTier >= 8) GoldBright else if (affordable) GoldAccent else BrassBorder,
                            RoundedCornerShape(7.dp)
                        )
                        .clickable(enabled = affordable) {
                            engine.upgradeSelectedTower(pathIdx)
                            onAction()
                        }
                        .padding(horizontal = 6.dp, vertical = 5.dp)
                ) {
                    Column {
                        val pathLetter = when (pathIdx) {
                            0 -> "A"
                            1 -> "B"
                            2 -> "C"
                            else -> "D:Era"
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Path $pathLetter",
                                color = if (pathIdx == 3) GoldAccent else MutedParchment,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "$curTier/8",
                                color = if (curTier > 0) GoldBright else MutedParchment,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                        // Visual 8-pip tier progress bar
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(1.5.dp)
                        ) {
                            for (pip in 1..8) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(3.dp)
                                        .background(
                                            when {
                                                pip <= curTier && pip >= 6 -> Color(0xFF40C4FF)
                                                pip <= curTier -> GoldBright
                                                else -> Color(0xFF221A14)
                                            }
                                        )
                                )
                            }
                        }
                        Text(
                            text = nextSpec?.name ?: "★ OMEGA T8",
                            color = ParchmentText,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val reqAge = if (pathIdx == 3) {
                            GameEngine.requiredAgeForPathDTier(curTier + 1)
                        } else {
                            GameEngine.requiredAgeForTier(curTier + 1)
                        }
                        val costLabel = when {
                            curTier >= 8 -> "★ OMEGA MAX"
                            !canUpg && ageNum < reqAge -> "Req Age ${GameEngine.romanNumeral(reqAge)}"
                            !canUpg -> "Req Age XIV"
                            cost == 0L -> "FREE"
                            else -> "🪙 ${formatNumber(cost)}G"
                        }
                        Text(
                            text = costLabel,
                            color = if (curTier >= 8 || affordable) GoldBright else TerracottaRed,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// TOWER ARMORY FORGE, ASCENSION STARS (★1..★25) & GUILD MASTERY DIALOG
// =============================================================================

@Composable
private fun TowerArmoryForgeDialog(
    snap: UiSnapshot,
    spendableGold: Long,
    engine: GameEngine,
    onAction: () -> Unit,
    onDismiss: () -> Unit
) {
    val tower = snap.selectedTower
    if (tower == null) {
        onDismiss()
        return
    }
    val vet = tower.veteranRank
    val nextVet = TowerVeteranCatalog.ranks.getOrNull(vet.rank + 1)
    val masteryRank = snap.towerMasteryRanks[tower.type] ?: 0
    val masteryCost = engine.effectiveCost(250L * (masteryRank + 1))

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 580.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, GoldAccent, RoundedCornerShape(12.dp)),
            color = WoodPanel
        ) {
            Column(
                modifier = Modifier
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚔️ TOWER ARMORY & FORGE MASTERY",
                        color = GoldBright,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Serif
                    )
                    WoodButton("✕", WoodCard, BrassBorder, ParchmentText, onClick = onDismiss)
                }

                // 1) Veteran Rank, Combat XP, Ascension Stars & Guild Mastery Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(WoodCard)
                        .border(1.dp, GoldAccent, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "${tower.type.ageDisplayName(snap.clearancePhase.phaseNum)} • ${vet.title} (${vet.shortBadge})",
                            color = GoldBright,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        val xpLine = if (nextVet != null) {
                            "Combat XP: ${formatNumber(tower.xp)} / ${formatNumber(nextVet.minXp)} • Vet Bonus: +${((vet.damageBonusMult - 1f) * 100).roundToInt()}% Dmg, +${((vet.fireRateBonusMult - 1f) * 100).roundToInt()}% Spd"
                        } else {
                            "Combat XP: ${formatNumber(tower.xp)} (MAX OMEGA VETERAN V20)"
                        }
                        Text(xpLine, color = ParchmentText, fontSize = 10.sp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val ascendCost = snap.selectedTowerAscendCost
                            val ascendLbl = when {
                                tower.ascensionStars >= TowerAscensionCatalog.MAX_STARS -> "★${TowerAscensionCatalog.MAX_STARS} MAX ASCENDED"
                                ascendCost != null -> "★ Ascend Star ${tower.ascensionStars + 1} (${formatNumber(ascendCost)}G)"
                                else -> "★ Ascend (Req Lv.6+)"
                            }
                            WoodButton(
                                label = ascendLbl,
                                bgColor = if (ascendCost != null && spendableGold >= ascendCost) MeadowGreenBg else StoneCard,
                                borderColor = GoldBright,
                                textColor = GoldBright,
                                modifier = Modifier.weight(1f)
                            ) {
                                engine.ascendSelectedTower()
                                onAction()
                            }

                            val canMastery = masteryRank < 25 && spendableGold >= masteryCost
                            WoodButton(
                                label = if (masteryRank >= 25) "Guild Mastery MAX" else "Guild Mastery Lv.$masteryRank (${formatNumber(masteryCost)}G)",
                                bgColor = if (canMastery) MeadowGreenBg else StoneCard,
                                borderColor = GoldAccent,
                                textColor = ParchmentText,
                                modifier = Modifier.weight(1f)
                            ) {
                                engine.upgradeTowerTypeMastery(tower.type)
                                onAction()
                            }
                        }
                    }
                }

                Text(
                    text = "⚒️ Socket & Upgrade 20 Forged Siege & Cosmic Relics (Tier I -> Tier V):",
                    color = GoldAccent,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )

                for (att in TowerGearAttachment.equippableList) {
                    val isEquipped = tower.gear == att
                    val maxTier = TowerGearAttachment.MAX_FORGE_TIER
                    val cost = if (isEquipped) {
                        if (tower.gearTier >= maxTier) 0L else engine.effectiveCost((att.baseCost * (tower.gearTier + 1) * 0.85f).toLong())
                    } else {
                        engine.effectiveCost(att.baseCost)
                    }
                    val canBuy = (!isEquipped || tower.gearTier < maxTier) && spendableGold >= cost

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isEquipped) MeadowGreenBg else WoodCard)
                            .border(1.dp, if (isEquipped) GoldBright else BrassBorder, RoundedCornerShape(8.dp))
                            .clickable(enabled = canBuy) {
                                engine.equipOrUpgradeSelectedTowerGear(att)
                                onAction()
                            }
                            .padding(9.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            val titleTag = if (isEquipped) "${att.icon} ${att.title} (Equipped Tier ${tower.gearTier}/$maxTier)" else "${att.icon} ${att.title}"
                            Text(titleTag, color = GoldBright, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            Text(att.summary, color = ParchmentText, fontSize = 9.5.sp)
                        }
                        val btnText = when {
                            isEquipped && tower.gearTier >= maxTier -> "MAX T$maxTier"
                            isEquipped -> "▲ T${tower.gearTier + 1} (${formatNumber(cost)}G)"
                            else -> "Equip (${formatNumber(cost)}G)"
                        }
                        Text(
                            text = btnText,
                            color = if (canBuy || (isEquipped && tower.gearTier >= maxTier)) GoldBright else TerracottaRed,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// 6-SLOT CAMPAIGN SAVE MANAGER DIALOG (WITH AUTOMATIC LEGACY MIGRATION)
// =============================================================================

@Composable
private fun SaveSlotsDialog(
    snap: UiSnapshot,
    engine: GameEngine,
    context: Context,
    onAction: () -> Unit,
    onDismiss: () -> Unit
) {
    val prefs = remember { context.getSharedPreferences(GameEngine.PREFS_NAME, Context.MODE_PRIVATE) }
    var refreshTick by remember { mutableIntStateOf(0) }
    val slots = remember(refreshTick, snap.wave, snap.gold, snap.activeSaveSlotId) {
        engine.getSaveSlots(prefs)
    }
    val dateFmt = remember { SimpleDateFormat("MMM d, h:mm a", Locale.US) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 600.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, GoldAccent, RoundedCornerShape(12.dp)),
            color = WoodPanel
        ) {
            Column(
                modifier = Modifier
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💾 CAMPAIGN SAVE SLOTS (SLOTS 1–6)",
                        color = GoldBright,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Serif
                    )
                    WoodButton("✕ Close", WoodCard, BrassBorder, ParchmentText, onClick = onDismiss)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MeadowGreenBg)
                        .border(1.dp, GoldAccent, RoundedCornerShape(8.dp))
                        .padding(9.dp)
                ) {
                    Text(
                        text = "✓ Active Slot: Slot ${snap.activeSaveSlotId} • Existing saves automatically update into this 18-Age / 4-Path / 8-Tier expansion!",
                        color = GoldBright,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                for (slot in slots) {
                    val isActive = slot.slotId == snap.activeSaveSlotId
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isActive) StoneCard else WoodCard)
                            .border(
                                width = if (isActive) 2.dp else 1.dp,
                                color = if (isActive) GoldBright else BrassBorder,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${if (isActive) "▶ " else ""}Slot ${slot.slotId}: ${slot.label}",
                                    color = GoldBright,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                val timeTxt = if (slot.hasData && slot.savedAtMillis > 0L) {
                                    dateFmt.format(Date(slot.savedAtMillis))
                                } else {
                                    "Empty Slot"
                                }
                                Text(timeTxt, color = MutedParchment, fontSize = 9.5.sp)
                            }

                            if (slot.hasData) {
                                Text(
                                    text = "🏰 ${slot.ageTitle} • Wave ${slot.wave} • 🪙 ${formatNumber(slot.gold)} (+🏛️ ${formatNumber(slot.bankBalance)}) • Towers: ${slot.towerCount} • Kills: ${formatNumber(slot.totalKills)}",
                                    color = ParchmentText,
                                    fontSize = 10.sp
                                )
                            } else {
                                Text(
                                    text = "No campaign saved in this slot yet — Save current game, Clone, or Start a New Run!",
                                    color = MutedParchment,
                                    fontSize = 10.sp
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                WoodButton(
                                    label = "💾 Save Here",
                                    bgColor = MeadowGreenBg,
                                    borderColor = GoldAccent,
                                    textColor = GoldBright,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    engine.saveToSlot(prefs, slot.slotId)
                                    refreshTick++
                                    onAction()
                                }

                                if (slot.hasData) {
                                    WoodButton(
                                        label = "📂 Load Slot",
                                        bgColor = WoodCard,
                                        borderColor = GoldBright,
                                        textColor = GoldBright,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        engine.loadFromSlot(prefs, slot.slotId)
                                        refreshTick++
                                        onAction()
                                        onDismiss()
                                    }
                                }

                                if (!isActive) {
                                    WoodButton(
                                        label = "📋 Clone Here",
                                        bgColor = WoodCard,
                                        borderColor = BrassBorder,
                                        textColor = ParchmentText,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        engine.cloneCurrentToSlot(prefs, slot.slotId)
                                        refreshTick++
                                        onAction()
                                    }
                                }

                                WoodButton(
                                    label = "🌱 New Run",
                                    bgColor = TerracottaBg,
                                    borderColor = TerracottaRed,
                                    textColor = ParchmentText,
                                    modifier = Modifier.weight(0.9f)
                                ) {
                                    engine.startNewCampaignInSlot(prefs, slot.slotId)
                                    refreshTick++
                                    onAction()
                                    onDismiss()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// TOWN CENTER 18-AGE ADVANCEMENT & TOWER EVOLUTION DIALOG
// =============================================================================

@Composable
private fun TownCenterAgeDialog(
    snap: UiSnapshot,
    spendableGold: Long,
    engine: GameEngine,
    onOpenTechTree: () -> Unit,
    onAction: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 600.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, GoldAccent, RoundedCornerShape(12.dp)),
            color = WoodPanel
        ) {
            Column(
                modifier = Modifier
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🏰 TOWN CENTER — 18 AGES OF HISTORY & FUTURE",
                        color = GoldBright,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Serif
                    )
                    WoodButton("✕", WoodCard, BrassBorder, ParchmentText, onClick = onDismiss)
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (snap.lastEraEvolutionMessage.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(7.dp))
                            .background(WoodCard)
                            .border(1.dp, GoldAccent, RoundedCornerShape(7.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "⚡ Tower Era Evolution: ${snap.lastEraEvolutionMessage}",
                            color = GoldBright,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                val nextCost = snap.nextAgeAdvanceCost
                if (nextCost != null) {
                    val nextPhase = ClearancePhase.forAgeLevel(snap.clearancePhase.phaseNum + 1)
                    val nextEdict = EraEvolutionCatalog.edicts.firstOrNull { it.unlockAgeNum == nextPhase.phaseNum }
                    val canAfford = spendableGold >= nextCost
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (canAfford) MeadowGreenBg else WoodCard)
                            .border(1.5.dp, if (canAfford) GoldBright else BrassBorder, RoundedCornerShape(8.dp))
                            .clickable(enabled = canAfford) {
                                engine.advanceCivilizationAge()
                                onAction()
                                onDismiss()
                            }
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "▲ ADVANCE TO ${nextPhase.badge}: ${nextPhase.title.uppercase()} (🪙 ${formatNumber(nextCost)} Gold)",
                                color = GoldBright,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Serif
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Directly upgrades ALL placed & future towers to ${nextPhase.title} architecture, boosts global damage to ${String.format("%.1f", nextPhase.damageBonusMult)}x, Fire Rate to ${String.format("%.2f", nextPhase.fireRateBonusMult)}x, Keep +${nextPhase.bonusMaxCastleHp} HP${if (nextEdict != null) ", and enacts '${nextEdict.title}' (${nextEdict.summary})" else ""}!",
                                color = ParchmentText,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                for (phase in ClearancePhase.entries) {
                    val isReached = phase.phaseNum <= snap.clearancePhase.phaseNum
                    val edict = EraEvolutionCatalog.edicts.firstOrNull { it.unlockAgeNum == phase.phaseNum }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(if (isReached) WoodCard else Color(0xFF221912))
                            .border(1.dp, if (isReached) GoldAccent else BrassBorder, RoundedCornerShape(7.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${if (isReached) "✓" else "🔒"} ${phase.badge}: ${phase.title} (${phase.waveRangeLabel})",
                                color = if (isReached) GoldBright else MutedParchment,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tower Evolution: ${String.format("%.1f", phase.damageBonusMult)}x Dmg • ${String.format("%.2f", phase.fireRateBonusMult)}x Spd • Keep: +${phase.bonusMaxCastleHp} HP",
                                color = ParchmentText,
                                fontSize = 9.5.sp
                            )
                            if (edict != null) {
                                Text(
                                    text = "📜 Edict (${edict.title}): ${edict.summary}",
                                    color = GoldAccent,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    WoodButton(
                        label = "⚒️ Tech Tree (${snap.techShards} Relics)",
                        bgColor = WoodCard,
                        borderColor = GoldAccent,
                        textColor = GoldBright,
                        onClick = onOpenTechTree
                    )
                    WoodButton(
                        label = if (snap.unlockAllFeatures) "🔓 All Buildings: ON" else "🔒 Show All Buildings",
                        bgColor = WoodCard,
                        borderColor = BrassBorder,
                        textColor = ParchmentText
                    ) {
                        engine.toggleUnlockAllFeatures()
                        onAction()
                    }
                }
            }
        }
    }
}

// =============================================================================
// SINGLE ROYAL COMMAND MENU DIALOG
// =============================================================================

@Composable
private fun CommandMenuDialog(
    snap: UiSnapshot,
    engine: GameEngine,
    onResetCamera: () -> Unit,
    onReplayIntro: () -> Unit,
    onOpenModal: (ModalType) -> Unit,
    onAction: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 600.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, GoldAccent, RoundedCornerShape(12.dp)),
            color = WoodPanel
        ) {
            Column(
                modifier = Modifier
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🏰 ROYAL COMMAND & SETTINGS",
                        color = GoldBright,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Serif
                    )
                    WoodButton("✕ Close", WoodCard, BrassBorder, ParchmentText, onClick = onDismiss)
                }

                Text(
                    text = "📯 Royal Siege Decrees (Tap to Unleash)",
                    color = GoldAccent,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (ab in CommanderAbility.entries) {
                        val cd = snap.abilityCooldowns[ab] ?: 0f
                        val ready = cd <= 0f
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(7.dp))
                                .background(if (ready) MeadowGreenBg else WoodCard)
                                .border(1.dp, if (ready) GoldBright else BrassBorder, RoundedCornerShape(7.dp))
                                .clickable(enabled = ready) {
                                    engine.activateCommanderAbility(ab)
                                    onAction()
                                    onDismiss()
                                }
                                .padding(vertical = 7.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (ready) ab.shortName else "${cd.roundToInt()}s",
                                color = if (ready) GoldBright else MutedParchment,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }

                MenuRowCard(
                    title = "💾 Campaign Save Slots (Slot ${snap.activeSaveSlotId} Active)",
                    subtitle = "Save, Load, Clone, or Start New Campaigns across 6 Slots (with seamless legacy migration)"
                ) {
                    onOpenModal(ModalType.SAVE_SLOTS)
                }

                MenuRowCard(
                    title = "🏰 Town Center & 18-Age Evolution",
                    subtitle = "Advance from Dark Age (I) past Golden Age (VI) to Omega Age (XVIII) • Currently ${snap.clearancePhase.badge} (${snap.clearancePhase.title})"
                ) {
                    onOpenModal(ModalType.TOWN_CENTER_AGE)
                }

                MenuRowCard(
                    title = "🏛️ Royal Treasury & Unbounded Interest Bank",
                    subtitle = "Balance: ${formatNumber(snap.bankBalance)}G • Earning +${snap.bankWaveInterestPct.roundToInt()}% per wave (No Money Max Cap!)"
                ) {
                    onOpenModal(ModalType.BANK_VAULT)
                }

                MenuRowCard(
                    title = "⚒️ Blacksmith, Future Sciences & Guilds (${snap.techShards} Relics)",
                    subtitle = "40 Historical & Cosmic Technologies + 16 Per-Building Guild Mastery Ranks"
                ) {
                    onOpenModal(ModalType.TECH_TREE)
                }

                MenuRowCard(
                    title = "🤖 Royal Auto-Governor (AI Automation)",
                    subtitle = "Toggle automated free-placement building, 4-path upgrades, decrees, and wave streaming"
                ) {
                    onOpenModal(ModalType.AUTO_TACTICIAN)
                }

                MenuRowCard(
                    title = "📖 Royal Encyclopedia & Chronicles",
                    subtitle = "18 Ages, 32 Towers & Grand Wonders, 20 Forge Gear Items, 10 Combos & 32 Raider Dossiers"
                ) {
                    onOpenModal(ModalType.USER_GUIDE)
                }

                MenuRowCard(
                    title = "👑 King's Cheat Console (God Mode)",
                    subtitle = "Infinite Gold, Instant Age Up, 8-8-8-8 ★25 Omega Towers & Spawn 16 Grand Wonders"
                ) {
                    onOpenModal(ModalType.CHEAT_MENU)
                }

                MenuRowCard(
                    title = "🎬 Replay 4K Animated Intro",
                    subtitle = "Watch the 4K-style era evolution cinematic — Created By: Brandon J. Wallace"
                ) {
                    onReplayIntro()
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    WoodButton(
                        label = "🎯 Reset View",
                        bgColor = WoodCard,
                        borderColor = BrassBorder,
                        textColor = ParchmentText,
                        modifier = Modifier.weight(1f)
                    ) {
                        onResetCamera()
                        onDismiss()
                    }
                    WoodButton(
                        label = if (snap.showSynergyOverlay) "🔗 Links: ON" else "🔗 Links: OFF",
                        bgColor = WoodCard,
                        borderColor = BrassBorder,
                        textColor = ParchmentText,
                        modifier = Modifier.weight(1f)
                    ) {
                        engine.showSynergyOverlay = !snap.showSynergyOverlay
                        onAction()
                    }
                    WoodButton(
                        label = if (snap.isMuted) "🔇 Muted" else "🔊 Sound ON",
                        bgColor = WoodCard,
                        borderColor = BrassBorder,
                        textColor = ParchmentText,
                        modifier = Modifier.weight(1f)
                    ) {
                        engine.toggleMute()
                        onAction()
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuRowCard(title: String, subtitle: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(WoodCard)
            .border(1.dp, BrassBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Column {
            Text(title, color = GoldBright, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
            Text(subtitle, color = ParchmentText, fontSize = 10.sp)
        }
    }
}

// =============================================================================
// ROYAL TREASURY & COMPOUND INTEREST BANK DIALOG (UNBOUNDED 64-BIT VAULT)
// =============================================================================

@Composable
private fun BankVaultDialog(
    snap: UiSnapshot,
    engine: GameEngine,
    onAction: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, GoldAccent, RoundedCornerShape(12.dp)),
            color = WoodPanel
        ) {
            Column(
                modifier = Modifier
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🏛️ ROYAL TREASURY & INTEREST ACCOUNT",
                        color = GoldBright,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Serif
                    )
                    WoodButton("✕", WoodCard, BrassBorder, ParchmentText, onClick = onDismiss)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(WoodCard)
                        .border(1.dp, GoldAccent, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("${snap.bankVaultName} (Tier ${snap.bankVaultTier}/${BankCatalog.tiers.size})", color = GoldBright, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Treasury Balance: 🪙 ${formatNumber(snap.bankBalance)} Gold (UNLIMITED VAULT CAP)",
                            color = ParchmentText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Wallet Gold: 🪙 ${formatNumber(snap.gold)} • Total Interest Earned: +${formatNumber(snap.totalBankInterestEarned)}G",
                            color = MutedParchment,
                            fontSize = 10.5.sp
                        )
                        Text(
                            text = "Uncapped Compound Dividend: +${String.format("%.1f", snap.bankWaveInterestPct)}% per Wave AND +${String.format("%.1f", snap.bankTickInterestPct)}% every 6s of combat!",
                            color = GoldAccent,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text("Deposit Wallet Gold into Treasury:", color = ParchmentText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    WoodButton("+25% Gold", MeadowGreenBg, MeadowGreen, GoldBright, Modifier.weight(1f)) {
                        engine.depositPercentToBank(25)
                        onAction()
                    }
                    WoodButton("+50% Gold", MeadowGreenBg, MeadowGreen, GoldBright, Modifier.weight(1f)) {
                        engine.depositPercentToBank(50)
                        onAction()
                    }
                    WoodButton("Deposit MAX", MeadowGreenBg, GoldAccent, GoldBright, Modifier.weight(1f)) {
                        engine.depositPercentToBank(100)
                        onAction()
                    }
                }

                Text("Withdraw Treasury Gold to Wallet:", color = ParchmentText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    WoodButton("-25% Vault", WoodCard, BrassBorder, ParchmentText, Modifier.weight(1f)) {
                        engine.withdrawPercentFromBank(25)
                        onAction()
                    }
                    WoodButton("-50% Vault", WoodCard, BrassBorder, ParchmentText, Modifier.weight(1f)) {
                        engine.withdrawPercentFromBank(50)
                        onAction()
                    }
                    WoodButton("Withdraw ALL", WoodCard, GoldAccent, GoldBright, Modifier.weight(1f)) {
                        engine.withdrawPercentFromBank(100)
                        onAction()
                    }
                }

                val nextCost = snap.bankNextTierCost
                if (nextCost != null) {
                    val nextTierSpec = BankCatalog.specForTier(snap.bankVaultTier + 1)
                    WoodButton(
                        label = "⬆️ Upgrade to ${nextTierSpec.name} (${formatNumber(nextCost)}G)",
                        bgColor = MeadowGreenBg,
                        borderColor = GoldBright,
                        textColor = GoldBright,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        engine.upgradeBankVault()
                        onAction()
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    WoodButton(
                        label = "Auto-Save: ${snap.bankAutoDepositPct}%",
                        bgColor = WoodCard,
                        borderColor = BrassBorder,
                        textColor = ParchmentText,
                        modifier = Modifier.weight(1f)
                    ) {
                        engine.cycleBankAutoDepositPct()
                        onAction()
                    }
                    WoodButton(
                        label = if (snap.bankOverdraftProtection) "Auto-Pay Builds: ON" else "Auto-Pay Builds: OFF",
                        bgColor = WoodCard,
                        borderColor = BrassBorder,
                        textColor = ParchmentText,
                        modifier = Modifier.weight(1f)
                    ) {
                        engine.toggleBankOverdraftProtection()
                        onAction()
                    }
                }

                val bondLabel = if (snap.bondWavesLeft > 0) {
                    "📜 Active Royal Bond: ${formatNumber(CurrencyMath.safeMultiply(snap.bondPrincipal, 2.0))}G in ${snap.bondWavesLeft} Wave(s)"
                } else {
                    "📜 Issue 3-Wave Royal Bond (Invest 250G -> 500G Payout)"
                }
                WoodButton(
                    label = bondLabel,
                    bgColor = WoodCard,
                    borderColor = GoldAccent,
                    textColor = GoldBright,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    engine.issueHighYieldBond(250L)
                    onAction()
                }
            }
        }
    }
}

// =============================================================================
// 40-TECH BLACKSMITH, UNIVERSITY, FUTURE SCIENCES & GUILD MASTERY DIALOG
// =============================================================================

@Composable
private fun TechTreeDialog(
    snap: UiSnapshot,
    spendableGold: Long,
    engine: GameEngine,
    onAction: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedBranchIdx by remember { mutableIntStateOf(0) }
    val branches = listOf("Blacksmith", "Siegecraft", "Alchemy", "University", "Treasury", "Future & Cosmic", "Guild Mastery")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 590.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, GoldAccent, RoundedCornerShape(12.dp)),
            color = WoodPanel
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚒️ UNIVERSITY, FUTURE TECH & GUILDS (${snap.techShards} Relics)",
                        color = GoldBright,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Serif
                    )
                    WoodButton("✕", WoodCard, BrassBorder, ParchmentText, onClick = onDismiss)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    branches.forEachIndexed { idx, bName ->
                        WoodButton(
                            label = bName,
                            bgColor = if (selectedBranchIdx == idx) StoneCard else WoodCard,
                            borderColor = if (selectedBranchIdx == idx) GoldBright else BrassBorder,
                            textColor = if (selectedBranchIdx == idx) GoldBright else ParchmentText
                        ) {
                            selectedBranchIdx = idx
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (selectedBranchIdx == 6) {
                        // Per-Tower Guild Mastery Ranks (up to Rank 25)
                        for (tType in TowerType.buildableBaseTowers) {
                            val rank = snap.towerMasteryRanks[tType] ?: 0
                            val cost = engine.effectiveCost(250L * (rank + 1))
                            val canBuy = rank < 25 && spendableGold >= cost
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(WoodCard)
                                    .border(1.dp, if (canBuy) GoldAccent else BrassBorder, RoundedCornerShape(8.dp))
                                    .clickable(enabled = canBuy) {
                                        engine.upgradeTowerTypeMastery(tType)
                                        onAction()
                                    }
                                    .padding(9.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        text = "${towerIcon(tType, snap.clearancePhase.phaseNum)} ${tType.ageDisplayName(snap.clearancePhase.phaseNum)} Mastery ($rank/25)",
                                        color = GoldBright,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Permanent +${rank * 12}% Damage & +${rank * 5}% Range to all ${tType.shortName}s.",
                                        color = ParchmentText,
                                        fontSize = 10.sp
                                    )
                                }
                                Text(
                                    text = if (rank >= 25) "MAX" else "🪙 ${formatNumber(cost)}G",
                                    color = if (canBuy) GoldBright else MutedParchment,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        val targetBranch = branches[selectedBranchIdx]
                        for (node in TechTreeCatalog.nodes.filter { it.branch == targetBranch }) {
                            val rank = snap.techRanks[node.id] ?: 0
                            val canBuy = rank < node.maxRank && (snap.techShards >= node.shardCost || snap.cheatZeroCost)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(WoodCard)
                                    .border(1.dp, if (canBuy) GoldAccent else BrassBorder, RoundedCornerShape(8.dp))
                                    .clickable(enabled = canBuy) {
                                        engine.upgradeTechNode(node.id)
                                        onAction()
                                    }
                                    .padding(9.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        text = "${node.title} ($rank/${node.maxRank})",
                                        color = GoldBright,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(node.summary, color = ParchmentText, fontSize = 10.sp)
                                }
                                Text(
                                    text = if (rank >= node.maxRank) "MAX" else "${node.shardCost} Relics",
                                    color = if (canBuy) GoldBright else MutedParchment,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// ROYAL AUTO-GOVERNOR DIALOG
// =============================================================================

@Composable
private fun AutoTacticianDialog(
    snap: UiSnapshot,
    engine: GameEngine,
    onAction: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, GoldAccent, RoundedCornerShape(12.dp)),
            color = WoodPanel
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🤖 ROYAL AUTO-GOVERNOR",
                        color = GoldBright,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Serif
                    )
                    WoodButton("✕", WoodCard, BrassBorder, ParchmentText, onClick = onDismiss)
                }

                ToggleRow("Auto-Place Buildings on Grass", "Automatically free-places fortifications along multi-pass road bends.", snap.autoBuild) {
                    engine.autoBuild = !snap.autoBuild
                    onAction()
                }
                ToggleRow("Auto-Upgrade 4 Paths & Fuse Wonders", "Upgrades paths A/B/C/D up to Tier 8 and combines Lv4+ pairs into Grand Wonders.", snap.autoUpgrade) {
                    engine.autoUpgrade = !snap.autoUpgrade
                    onAction()
                }
                ToggleRow("Auto-Unleash Royal Decrees", "Sounds the Rally Horn and fires Siege Salvos when raiders mass.", snap.autoAbilities) {
                    engine.autoAbilities = !snap.autoAbilities
                    onAction()
                }
                ToggleRow("Non-Stop Wave Streaming", "Calls the next wave automatically for +35% bonus Gold & Relics.", snap.autoWaveRush) {
                    engine.autoWaveRush = !snap.autoWaveRush
                    onAction()
                }
            }
        }
    }
}

// =============================================================================
// ROYAL ENCYCLOPEDIA & 18-AGE CHRONICLES DIALOG
// =============================================================================

@Composable
private fun UserGuideDialog(
    snap: UiSnapshot,
    engine: GameEngine,
    onAction: () -> Unit,
    onDismiss: () -> Unit
) {
    var tab by remember { mutableIntStateOf(0) }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 590.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, GoldAccent, RoundedCornerShape(12.dp)),
            color = WoodPanel
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📖 ROYAL ENCYCLOPEDIA & CHRONICLES",
                        color = GoldBright,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Serif
                    )
                    WoodButton("✕ Close", WoodCard, BrassBorder, ParchmentText, onClick = onDismiss)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    val tabs = listOf("Mechanics", "18 Ages", "32 Towers", "20 Gear", "Combos", "32 Raiders")
                    tabs.forEachIndexed { idx, title ->
                        WoodButton(
                            label = title,
                            bgColor = if (tab == idx) StoneCard else WoodCard,
                            borderColor = if (tab == idx) GoldBright else BrassBorder,
                            textColor = if (tab == idx) GoldBright else ParchmentText
                        ) {
                            tab = idx
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    when (tab) {
                        0 -> {
                            GuideCard(
                                "1. Free-Placement on the Lawn Grass",
                                "Select any building card at the bottom and tap anywhere on the open green grass to construct it! Placing near trees grants Woodland Overlook (+28% Range & Camo Sight), near boulders grants Stone Quarry (+32% Damage), and near road bends grants Crossfire damage bonuses."
                            )
                            GuideCard(
                                "2. 18 Ages of History & Beyond (Dark Age -> Golden Age -> Omega Age)",
                                "Advance through 18 Ages from Dark Age (I) past Golden Age (VI) into Industrial (VIII), Atomic (X), Cybernetic (XII), Starfleet (XIV), Dyson (XV), Quantum (XVI), Dimensional (XVII), and Omega Age (XVIII)! Advancing Ages directly evolves & upgrades all your placed and future towers (up to 400x damage & 3.6x fire rate) and enacts permanent Civilization Edicts."
                            )
                            GuideCard(
                                "3. 4 Upgrade Paths × 8 Tiers (Path A, B, C & Path D: Era Overdrive)",
                                "Every base tower has 4 upgrade paths (Paths A, B, C plus Path D: Era & Cosmic Overdrive) and each path extends all the way to Tier 8! Reaching Starfleet Age (XIV) unlocks full 8-8-8-8 Omni-Crosspathing on every tower."
                            )
                            GuideCard(
                                "4. 6 Save Slots, Unbounded Treasury & 16 Grand Wonders",
                                "Use the 💾 Save Slots menu to manage 6 independent campaigns (existing saves automatically migrate into Slot 1). Bank Vaults have zero artificial caps (scaling into Quadrillions & Quintillions of Gold), and 16 Grand Wonders can be fused from Level-4+ pairs!"
                            )
                        }
                        1 -> {
                            for (chron in HistoricalCodexAndBlueprints.eraChronicles) {
                                GuideCard(
                                    "${chron.romanBadge}: ${chron.eraTitle} (${chron.historicalYearSpan})",
                                    "Materials: ${chron.masonryAndMaterials}\nMilestone: ${chron.architecturalMilestone}\nDoctrine: ${chron.militaryDoctrineSummary}\n${chron.chronicleLore}"
                                )
                            }
                        }
                        2 -> {
                            for (t in TowerType.entries) {
                                GuideCard(
                                    "${towerIcon(t, snap.clearancePhase.phaseNum)} ${t.displayName} → ${t.ageDisplayName(18)} (${formatNumber(t.baseCost)}G)",
                                    "Skill: ${t.doctrineSkillTitle}\n${t.roleSummary}"
                                )
                            }
                        }
                        3 -> {
                            for (g in TowerGearAttachment.equippableList) {
                                GuideCard(
                                    "${g.icon} ${g.title} (${formatNumber(g.baseCost)}G • Upgradeable Tier I–V)",
                                    g.summary
                                )
                            }
                        }
                        4 -> {
                            for (c in ComboReaction.entries) {
                                GuideCard(
                                    "⚔️ ${c.title} (${c.bonusDamageMult}x Damage)",
                                    "${c.formula}\n${c.description}"
                                )
                            }
                        }
                        else -> {
                            for (e in EnemyType.entries) {
                                GuideCard(
                                    "${e.displayName} (HP: ${formatNumber(e.baseHp.toLong())} • Gold: ${formatNumber(e.goldReward)})",
                                    "${e.summary}\nCounter: ${e.counterTip}"
                                )
                            }
                            for (af in EnemyEliteAffix.entries.filter { it != EnemyEliteAffix.NONE }) {
                                GuideCard(
                                    "Elite Affix: ${af.title} (${af.badge})",
                                    "HP: ${(af.hpMult * 100).roundToInt()}% • Speed: ${(af.speedMult * 100).roundToInt()}% • Gold/XP Reward: ${(af.goldAndXpMult * 100).roundToInt()}%"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GuideCard(title: String, body: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(7.dp))
            .background(WoodCard)
            .border(1.dp, BrassBorder, RoundedCornerShape(7.dp))
            .padding(9.dp)
    ) {
        Column {
            Text(title, color = GoldBright, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(body, color = ParchmentText, fontSize = 10.sp, lineHeight = 14.sp)
        }
    }
}

// =============================================================================
// KING'S CHEAT CONSOLE ("GOD MODE") DIALOG
// =============================================================================

@Composable
private fun CheatMenuDialog(
    snap: UiSnapshot,
    engine: GameEngine,
    onAction: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 590.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, GoldBright, RoundedCornerShape(12.dp)),
            color = WoodPanel
        ) {
            Column(
                modifier = Modifier
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👑 KING'S CHEAT CONSOLE",
                        color = GoldBright,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Serif
                    )
                    WoodButton("✕ Close", WoodCard, BrassBorder, ParchmentText, onClick = onDismiss)
                }

                ToggleRow("Infinite Cosmic Gold (999Qa)", "Never run out of Gold.", snap.cheatInfiniteGold) {
                    engine.cheatInfiniteGold = !snap.cheatInfiniteGold
                    onAction()
                }
                ToggleRow("Masterwork 8-8-8-8 Omni-Crosspathing", "Removes path & Age gates so every building can reach 8-8-8-8.", snap.cheatOmniPath555) {
                    engine.cheatOmniPath555 = !snap.cheatOmniPath555
                    onAction()
                }
                ToggleRow("5.0x Siege Reload Speed", "All fortifications fire 5x faster.", snap.cheatHyperSpeed) {
                    engine.cheatHyperSpeed = !snap.cheatHyperSpeed
                    onAction()
                }
                ToggleRow("One-Hit Siege Kills", "Every projectile or beam instantly defeats any raider.", snap.cheatOneShotKill) {
                    engine.cheatOneShotKill = !snap.cheatOneShotKill
                    onAction()
                }
                ToggleRow("Impenetrable Castle Keep", "Castle Keep takes 0 breach damage.", snap.cheatGodCore) {
                    engine.cheatGodCore = !snap.cheatGodCore
                    onAction()
                }
                ToggleRow("Freeze All Raiders", "Halts all enemy movement on the road.", snap.cheatFreezeEnemies) {
                    engine.cheatFreezeEnemies = !snap.cheatFreezeEnemies
                    onAction()
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    WoodButton("+10M Gold", MeadowGreenBg, GoldAccent, GoldBright, Modifier.weight(1f)) {
                        engine.cheatAddGold(10_000_000L)
                        onAction()
                    }
                    WoodButton("+1T Gold", MeadowGreenBg, GoldAccent, GoldBright, Modifier.weight(1f)) {
                        engine.cheatAddGold(1_000_000_000_000L)
                        onAction()
                    }
                    WoodButton("Max Vault XV", MeadowGreenBg, GoldAccent, GoldBright, Modifier.weight(1f)) {
                        engine.cheatMaxBankVaultAndFill()
                        onAction()
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    WoodButton("🏰 Advance Age", MeadowGreenBg, GoldBright, GoldBright, Modifier.weight(1f)) {
                        engine.cheatAdvanceAge()
                        onAction()
                    }
                    WoodButton("Max Selected 8-8-8-8", WoodCard, GoldAccent, GoldBright, Modifier.weight(1f)) {
                        engine.cheatMaxSelectedTowerOmni()
                        onAction()
                    }
                    WoodButton("Max ALL 8-8-8-8", WoodCard, GoldAccent, GoldBright, Modifier.weight(1f)) {
                        engine.cheatMaxAllTowersOmni()
                        onAction()
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    WoodButton("Max 40 Techs", WoodCard, GoldAccent, GoldBright, Modifier.weight(1f)) {
                        engine.cheatMaxAllTech()
                        onAction()
                    }
                    WoodButton("☄️ Smite Wave", TerracottaBg, TerracottaRed, ParchmentText, Modifier.weight(1f)) {
                        engine.cheatNukeAllEnemies()
                        onAction()
                    }
                    WoodButton("👑 Spawn Boss", TerracottaBg, TerracottaRed, ParchmentText, Modifier.weight(1f)) {
                        engine.cheatSpawnTestBoss()
                        onAction()
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    WoodButton("⏩ +5 Waves", WoodCard, BrassBorder, ParchmentText, Modifier.weight(1f)) {
                        engine.cheatJumpWaves(5)
                        onAction()
                    }
                    WoodButton("⏩ +15 Waves", WoodCard, BrassBorder, ParchmentText, Modifier.weight(1f)) {
                        engine.cheatJumpWaves(15)
                        onAction()
                    }
                }

                Text("🏛️ Spawn Any of the 16 Grand Wonders on the Grass:", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    for (apex in TowerType.fusedApexTowers) {
                        WoodButton(
                            label = "👑 ${apex.shortName}",
                            bgColor = WoodCard,
                            borderColor = GoldBright,
                            textColor = GoldBright
                        ) {
                            engine.cheatTransformSelectedToApex(apex)
                            onAction()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (checked) MeadowGreenBg else WoodCard)
            .border(1.dp, if (checked) GoldBright else BrassBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onToggle)
            .padding(9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(title, color = ParchmentText, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = MutedParchment, fontSize = 9.5.sp)
        }
        Text(
            text = if (checked) "ON" else "OFF",
            color = if (checked) GoldBright else TerracottaRed,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun WoodButton(
    label: String,
    bgColor: Color,
    borderColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

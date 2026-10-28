package com.brandonwallace.labyrinthtd

import com.brandonwallace.labyrinthtd.game.ClearancePhase
import com.brandonwallace.labyrinthtd.game.CommanderAbility
import com.brandonwallace.labyrinthtd.game.CurrencyMath
import com.brandonwallace.labyrinthtd.game.EnemyEliteAffix
import com.brandonwallace.labyrinthtd.game.EnemyType
import com.brandonwallace.labyrinthtd.game.EraEvolutionCatalog
import com.brandonwallace.labyrinthtd.game.GameEngine
import com.brandonwallace.labyrinthtd.game.HistoricalCodexAndBlueprints
import com.brandonwallace.labyrinthtd.game.LabyrinthMap
import com.brandonwallace.labyrinthtd.game.SaveSlotManager
import com.brandonwallace.labyrinthtd.game.TechTreeCatalog
import com.brandonwallace.labyrinthtd.game.TowerAscensionCatalog
import com.brandonwallace.labyrinthtd.game.TowerGearAttachment
import com.brandonwallace.labyrinthtd.game.TowerType
import com.brandonwallace.labyrinthtd.game.TowerVeteranCatalog
import com.brandonwallace.labyrinthtd.game.TrackGadgetType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LabyrinthTdTest {

    @Test
    fun ouroborosMap_isLongWindingAndSupportsFreePlacement() {
        assertTrue("Track should exceed 8,000 units", LabyrinthMap.totalLength > 8000f)
        assertTrue("Meadow should contain deterministic trees, rocks, and wildflowers", LabyrinthMap.meadowProps.isNotEmpty())
        val freePoint = LabyrinthMap.resolveFreePlacementPoint(950f, 520f, emptyList())
        assertNotNull("Open grass near road should be valid for free tower placement", freePoint)
        val siteEval = LabyrinthMap.evaluateFreePlacementSite(freePoint!!.x, freePoint.y, 240f)
        assertTrue("Road passes in range should be at least 1", siteEval.multiPassRating >= 1)
    }

    @Test
    fun towerCatalog_has16BaseAnd16FusedApexWith4PathsAnd8Tiers() {
        assertEquals(16, TowerType.buildableBaseTowers.size)
        assertEquals(16, TowerType.fusedApexTowers.size)
        for (base in TowerType.buildableBaseTowers) {
            assertEquals("Each base tower must have 4 upgrade paths (A, B, C, D)", 4, base.paths.size)
            assertTrue("Each base tower must have a Tier-5+ Doctrine Skill", base.doctrineSkillTitle.isNotEmpty())
            for (p in base.paths) {
                assertEquals("Each upgrade path must have 8 tiers (1..8)", 8, p.tiers.size)
            }
        }
        assertEquals(20, TowerGearAttachment.entries.size)
        assertEquals(21, TowerVeteranCatalog.ranks.size)
        assertEquals(18, ClearancePhase.entries.size)
        assertEquals(40, TechTreeCatalog.nodes.size)
        assertEquals(32, EnemyType.entries.size)
        assertTrue(EnemyEliteAffix.entries.size >= 13)
        assertEquals(18, HistoricalCodexAndBlueprints.chronicles.size)
        assertTrue(HistoricalCodexAndBlueprints.blueprints.isNotEmpty())
        assertEquals(32, HistoricalCodexAndBlueprints.raiderDossiers.size)
    }

    @Test
    fun startsWithZeroPreplacedTowers_andSupportsFreePlacementAnywhereOnGrass() {
        val engine = GameEngine(null)
        assertEquals("Game must start with zero pre-placed towers on clean grass", 0, engine.towers.size)

        assertTrue(
            "Free-placing a Rock Watchtower on valid grass should succeed",
            engine.buildTowerAtGrass(950f, 520f, TowerType.GATLING)
        )
        assertEquals(1, engine.towers.size)
        val placed = engine.towers.values.first()
        assertEquals(TowerType.GATLING, placed.type)
        assertNull("Rapid free-placement keeps build mode active until a placed tower is tapped", engine.selectedPadId)

        assertTrue(engine.handleMeadowTap(placed.x, placed.y))
        assertEquals(placed.padId, engine.selectedPadId)
    }

    @Test
    fun buildAnd4Path8TierCrosspathUpgrades_respectStandardAndOmniCheatRules() {
        val engine = GameEngine(null)
        engine.cheatAddGold(500_000_000L)
        assertTrue(engine.buildTowerAtGrass(950f, 520f, TowerType.GATLING))
        val placed = engine.towers.values.first()
        engine.selectPad(placed.padId)

        // In Age I (Dark Age), Path A can upgrade to Tier 5 and Path B to Tier 2
        for (i in 1..5) {
            assertTrue("Should upgrade Path A to tier $i in Age I", engine.upgradeSelectedTower(0))
        }
        assertEquals(5, placed.pathA)
        assertFalse("Tier 6 requires advancing Civilization Age", engine.upgradeSelectedTower(0))

        assertTrue(engine.upgradeSelectedTower(1))
        assertTrue(engine.upgradeSelectedTower(1))
        assertEquals(2, placed.pathB)
        assertFalse("Path B tier 3 and Path C tier 1 are locked in Age I", engine.upgradeSelectedTower(1))
        assertFalse(engine.upgradeSelectedTower(2))

        // Advance Ages into the Future (up to Age XIII) so Tiers 6, 7, 8 and Path D unlock!
        for (age in 2..13) {
            assertTrue(engine.advanceCivilizationAge())
        }
        assertTrue("Tier 6 unlocked in Future Ages", engine.upgradeSelectedTower(0))
        assertTrue("Tier 7 unlocked in Future Ages", engine.upgradeSelectedTower(0))
        assertTrue("Tier 8 unlocked in Future Ages", engine.upgradeSelectedTower(0))
        assertEquals(8, placed.pathA)

        // Upgrade Path D (Era & Cosmic Overdrive)
        assertTrue(engine.upgradeSelectedTower(3))
        assertTrue(engine.upgradeSelectedTower(3))
        assertEquals(2, placed.pathD)

        // Enable Cheat Omni-Path 8-8-8-8
        engine.cheatOmniPath555 = true
        assertTrue(engine.upgradeSelectedTower(1))
        assertTrue(engine.upgradeSelectedTower(2))
        assertTrue(engine.upgradeSelectedTower(3))
    }

    @Test
    fun armoryGearAscensionAndGuildMastery_scaleTowerPowerToTier5AndStar25() {
        val engine = GameEngine(null)
        engine.cheatAddGold(50_000_000L)
        assertTrue(engine.buildTowerAtGrass(950f, 520f, TowerType.GATLING))
        val tower = engine.towers.values.first()
        engine.selectPad(tower.padId)

        // Upgrade to Lv.6+ (Path A = 5, Path B = 1) so Ascension is unlocked
        for (i in 1..5) assertTrue(engine.upgradeSelectedTower(0))
        assertTrue(engine.upgradeSelectedTower(1))

        val rangeBefore = engine.effectiveTowerRange(tower)

        // Equip & upgrade Armory Gear (Tier I -> V)
        for (tier in 1..TowerGearAttachment.MAX_FORGE_TIER) {
            assertTrue(engine.equipOrUpgradeSelectedTowerGear(TowerGearAttachment.BODKIN_QUIVER))
            assertEquals(tier, tower.gearTier)
        }

        // Ascend tower to ★5
        for (star in 1..5) {
            assertTrue(engine.ascendSelectedTower())
        }
        assertEquals(5, tower.ascensionStars)
        assertTrue(TowerAscensionCatalog.damageMultiplierForStars(tower.ascensionStars) > 1.0f)

        // Upgrade Guild Mastery for GATLING
        assertTrue(engine.upgradeTowerTypeMastery(TowerType.GATLING))
        assertEquals(1, engine.towerMasteryRanks[TowerType.GATLING])

        val rangeAfter = engine.effectiveTowerRange(tower)
        assertTrue("Effective range should increase from Ascension + Mastery", rangeAfter > rangeBefore)
    }

    @Test
    fun towerFusionCrucible_combinesNearbyFreePlacedLevel4TowersIntoApexWonder() {
        val engine = GameEngine(null)
        engine.cheatAddGold(2_000_000L)

        assertTrue(engine.buildTowerAtGrass(950f, 520f, TowerType.CRYO))
        val cryoTower = engine.towers.values.first { it.type == TowerType.CRYO }
        engine.selectPad(cryoTower.padId)
        for (i in 1..4) assertTrue(engine.upgradeSelectedTower(0))

        assertTrue(engine.buildTowerAtGrass(1040f, 520f, TowerType.PYRO))
        val pyroTower = engine.towers.values.first { it.type == TowerType.PYRO }
        engine.selectPad(pyroTower.padId)
        for (i in 1..4) assertTrue(engine.upgradeSelectedTower(0))

        val snapBefore = engine.captureUiSnapshot()
        assertNotNull("Fusion candidate should be detected for nearby Lv4+ Cryo & Pyro", snapBefore.fusionCandidate)
        assertEquals(TowerType.FROSTFIRE_PARADOX, snapBefore.fusionCandidate?.resultType)

        assertTrue("Fusion should succeed", engine.fuseSelectedTower())
        assertEquals(1, engine.towers.size)
        assertEquals(TowerType.FROSTFIRE_PARADOX, engine.towers.values.first().type)
    }

    @Test
    fun trackGadgetsAndAbilities_deployAndTriggerCleanly() {
        val engine = GameEngine(null)
        engine.cheatAddGold(500_000L)
        assertTrue(engine.deployTrackGadget(0, TrackGadgetType.REVERSE_CONVEYOR))
        assertTrue(engine.deployTrackGadget(1, TrackGadgetType.ELEMENTAL_RUNE))
        assertEquals(2, engine.trackGadgets.size)

        for (ab in CommanderAbility.entries) {
            assertTrue(engine.activateCommanderAbility(ab))
        }
    }

    @Test
    fun optionalTutorialMode_preservesAndRestoresEndlessProgress() {
        val engine = GameEngine(null)
        engine.cheatAddGold(5_000L)
        engine.buildTowerAtGrass(950f, 520f, TowerType.PRISM)
        val mainGoldBefore = engine.gold
        val mainTowersBefore = engine.towers.size

        engine.startOptionalTutorialLevel()
        assertTrue(engine.isTutorialMode)
        assertEquals(0, engine.towers.size)

        engine.exitOptionalTutorialLevel()
        assertFalse(engine.isTutorialMode)
        assertEquals(mainTowersBefore, engine.towers.size)
        assertEquals(mainGoldBefore + 350L, engine.gold)
    }

    @Test
    fun simulationStep_spawnsEnemiesLevelsVeteranXpAndCompoundsInterest() {
        val engine = GameEngine(null)
        assertTrue(engine.buildTowerAtGrass(200f, 150f, TowerType.GATLING))
        assertTrue(engine.buildTowerAtGrass(300f, 150f, TowerType.SNIPER))

        engine.gameSpeed = 4.0f
        for (i in 0 until 80) {
            engine.stepSimulation(0.05f)
        }
        assertTrue(
            "Enemies should have spawned and/or been slain by free-placed towers",
            engine.enemies.isNotEmpty() || engine.totalKills > 0L
        )
        assertTrue(
            "Free-placed towers should have dealt damage and earned Veteran XP",
            engine.towers.values.sumOf { it.totalDamageDealt } > 0.0 &&
                engine.towers.values.sumOf { it.xp } > 0L
        )

        val goldBeforeWave = engine.gold
        engine.rushNextWave()
        assertTrue("Gold should increase from wave stipend + hidden 16.769% interest", engine.gold > goldBeforeWave)
    }

    @Test
    fun unboundedTreasuryAndBank_haveNoMaxMoneyLimitsAndCompoundIntoQuintillions() {
        val engine = GameEngine(null)
        assertEquals("Initial seeded bank balance should be 250G", 250L, engine.bankBalance)

        // Verify CurrencyMath saturating arithmetic and formatting up to Quintillions
        val trillion = 1_500_000_000_000L
        val quadrillion = 250_000_000_000_000_000L
        assertEquals("1.50T", CurrencyMath.formatGold(trillion))
        assertEquals("250.00Qa", CurrencyMath.formatGold(quadrillion))
        assertEquals(CurrencyMath.MAX_TREASURY_GOLD, CurrencyMath.addGold(CurrencyMath.MAX_TREASURY_GOLD, 999_999_999L))

        // Grant 500 Trillion Gold and deposit 100% into the Bank Vault — verify zero artificial cap!
        engine.cheatAddGold(500_000_000_000_000L)
        assertTrue(engine.depositPercentToBank(100))
        assertTrue("Bank Vault must hold 500+ Trillion without being capped at 15,000G", engine.bankBalance >= 500_000_000_000_000L)

        // Rush wave and verify uncapped wave interest compounds on 500 Trillion
        val bankBefore = engine.bankBalance
        engine.rushNextWave()
        assertTrue("Bank Vault interest must compound with no upper ceiling", engine.bankBalance > bankBefore + 10_000_000_000_000L)
    }

    @Test
    fun eighteenAgesProgression_evolvesAndUpgradesTowersThroughOmegaAge() {
        val engine = GameEngine(null)
        engine.cheatAddGold(500_000_000_000_000L)

        // Place a starter Watchtower in Age I (Dark Age)
        assertTrue(engine.buildTowerAtGrass(950f, 520f, TowerType.GATLING))
        val tower = engine.towers.values.first()
        assertEquals(1, engine.civilizationAgeLevel)
        val age1Range = engine.effectiveTowerRange(tower)
        val age1Title = EraEvolutionCatalog.evolvedTowerTitle(tower.type, 1)

        // Advance from Age I all the way to Age XVIII (Omega Age)!
        for (targetAge in 2..18) {
            assertTrue("Should advance to Age $targetAge", engine.advanceCivilizationAge())
        }
        val snapOmega = engine.captureUiSnapshot()
        assertEquals("Should now be in Age XVIII (Omega Age)", 18, snapOmega.clearancePhase.phaseNum)
        assertEquals("All 16 base buildings should be unlocked in Omega Age", 16, snapOmega.clearancePhase.unlockedTowerIds.size)
        assertEquals("Civilization Age Level should be 18", 18, engine.civilizationAgeLevel)
        assertEquals("18 Civilization Edicts should be enacted", 18, snapOmega.activeEdicts.size)

        val age18Range = engine.effectiveTowerRange(tower)
        val age18Title = EraEvolutionCatalog.evolvedTowerTitle(tower.type, 18)
        assertTrue("Age XVIII tower range must exceed Age I range", age18Range > age1Range * 1.4f)
        assertFalse("Age XVIII tower title ($age18Title) must evolve from Age I ($age1Title)", age1Title == age18Title)
    }

    @Test
    fun legacySaveSerialization_restoresAndPreservesTowersAndEraEvolution() {
        val engine1 = GameEngine(null)
        engine1.cheatAddGold(10_000_000L)
        for (age in 2..7) engine1.advanceCivilizationAge() // Advance to Age VII (Enlightenment Age)
        assertTrue(engine1.buildTowerAtGrass(950f, 520f, TowerType.GATLING))
        val t1 = engine1.towers.values.first()
        engine1.selectPad(t1.padId)
        for (i in 1..6) assertTrue(engine1.upgradeSelectedTower(0)) // Tier 6 on Path A
        assertTrue(engine1.upgradeSelectedTower(3)) // Tier 1 on Path D

        val payload = engine1.serializeStateToString()
        assertEquals(6, SaveSlotManager.MAX_SLOTS)

        val engine2 = GameEngine(null)
        engine2.deserializeStateFromString(payload)
        assertEquals(7, engine2.civilizationAgeLevel)
        assertEquals(1, engine2.towers.size)
        val restoredTower = engine2.towers.values.first()
        assertEquals(6, restoredTower.pathA)
        assertEquals(1, restoredTower.pathD)
    }
}

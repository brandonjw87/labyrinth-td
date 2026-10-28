package com.brandonwallace.labyrinthtd.game

import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * UNBOUNDED CURRENCY & SATURATING TREASURY ARITHMETIC
 * Fixes all artificial money max limits and prevents 64-bit integer overflow
 * across all 18 Historical-to-Cosmic Ages.
 */
object CurrencyMath {
    const val MAX_TREASURY_GOLD: Long = 4_500_000_000_000_000_000L // 4.50 Quintillion (safe headroom below Long.MAX_VALUE)

    fun safeAdd(a: Long, b: Long): Long {
        if (b <= 0L) return max(0L, a + b)
        val headroom = MAX_TREASURY_GOLD - a
        return if (b >= headroom) MAX_TREASURY_GOLD else a + b
    }

    fun addGold(a: Long, b: Long): Long = safeAdd(a, b)

    fun safeMultiply(base: Long, factor: Double): Long {
        if (base <= 0L || factor <= 0.0) return 0L
        val prod = base.toDouble() * factor
        if (prod.isNaN() || prod.isInfinite() || prod >= MAX_TREASURY_GOLD.toDouble()) {
            return MAX_TREASURY_GOLD
        }
        return prod.roundToLong().coerceIn(0L, MAX_TREASURY_GOLD)
    }

    fun safePercent(base: Long, pct: Double): Long = safeMultiply(base, pct)

    fun safePercent(base: Long, percent: Int): Long =
        safeMultiply(base, percent.coerceIn(0, 100) / 100.0)

    fun formatGold(amount: Long): String {
        val absVal = kotlin.math.abs(amount.toDouble())
        val sign = if (amount < 0L) "-" else ""
        return when {
            absVal >= 1_000_000_000_000_000_000.0 -> String.format("%s%.2fQi", sign, absVal / 1_000_000_000_000_000_000.0)
            absVal >= 1_000_000_000_000_000.0 -> String.format("%s%.2fQa", sign, absVal / 1_000_000_000_000_000.0)
            absVal >= 1_000_000_000_000.0 -> String.format("%s%.2fT", sign, absVal / 1_000_000_000_000.0)
            absVal >= 1_000_000_000.0 -> String.format("%s%.2fB", sign, absVal / 1_000_000_000.0)
            absVal >= 1_000_000.0 -> String.format("%s%.2fM", sign, absVal / 1_000_000.0)
            absVal >= 10_000.0 -> String.format("%s%.1fK", sign, absVal / 1_000.0)
            else -> amount.toString()
        }
    }
}

/**
 * Visual & Structural Era Architecture Theme for Towers, Keep, and Roadway
 */
enum class EraArchitectureTheme(
    val id: String,
    val label: String,
    val plinthPrimaryArgb: Long,
    val plinthTrimArgb: Long,
    val roadSurfaceArgb: Long,
    val roadBorderArgb: Long,
    val hasMetallicRivets: Boolean,
    val hasEnergyConduits: Boolean,
    val hasCosmicHalo: Boolean
) {
    PRIMITIVE_TIMBER(
        id = "PRIMITIVE_TIMBER",
        label = "Timber & Fieldstone",
        plinthPrimaryArgb = 0xFF70685E,
        plinthTrimArgb = 0xFF5A4028,
        roadSurfaceArgb = 0xFF8C7355,
        roadBorderArgb = 0xFF594631,
        hasMetallicRivets = false,
        hasEnergyConduits = false,
        hasCosmicHalo = false
    ),
    FEUDAL_MASONRY(
        id = "FEUDAL_MASONRY",
        label = "Cut Stone & Oak",
        plinthPrimaryArgb = 0xFF7A746B,
        plinthTrimArgb = 0xFF8A683B,
        roadSurfaceArgb = 0xFF948066,
        roadBorderArgb = 0xFF63513B,
        hasMetallicRivets = false,
        hasEnergyConduits = false,
        hasCosmicHalo = false
    ),
    CASTLE_LIMESTONE(
        id = "CASTLE_LIMESTONE",
        label = "Fortified Limestone",
        plinthPrimaryArgb = 0xFF848078,
        plinthTrimArgb = 0xFFA68644,
        roadSurfaceArgb = 0xFF8E8578,
        roadBorderArgb = 0xFF5C554B,
        hasMetallicRivets = false,
        hasEnergyConduits = false,
        hasCosmicHalo = false
    ),
    IMPERIAL_GILDED(
        id = "IMPERIAL_GILDED",
        label = "Imperial Marble & Brass",
        plinthPrimaryArgb = 0xFF918A7E,
        plinthTrimArgb = 0xFFD4AF37,
        roadSurfaceArgb = 0xFF9B9284,
        roadBorderArgb = 0xFF78684A,
        hasMetallicRivets = true,
        hasEnergyConduits = false,
        hasCosmicHalo = false
    ),
    GUNPOWDER_STARFORT(
        id = "GUNPOWDER_STARFORT",
        label = "Bastion Brick & Bronze",
        plinthPrimaryArgb = 0xFF6E655F,
        plinthTrimArgb = 0xFFB87333,
        roadSurfaceArgb = 0xFF78726B,
        roadBorderArgb = 0xFF4F4A45,
        hasMetallicRivets = true,
        hasEnergyConduits = false,
        hasCosmicHalo = false
    ),
    INDUSTRIAL_BESSEMER(
        id = "INDUSTRIAL_BESSEMER",
        label = "Riveted Iron & Steam",
        plinthPrimaryArgb = 0xFF545B61,
        plinthTrimArgb = 0xFFC87D3B,
        roadSurfaceArgb = 0xFF5E6368,
        roadBorderArgb = 0xFF3B3F42,
        hasMetallicRivets = true,
        hasEnergyConduits = false,
        hasCosmicHalo = false
    ),
    ATOMIC_BUNKER(
        id = "ATOMIC_BUNKER",
        label = "Reinforced Ferro-Concrete",
        plinthPrimaryArgb = 0xFF4B5358,
        plinthTrimArgb = 0xFF68B078,
        roadSurfaceArgb = 0xFF4E5459,
        roadBorderArgb = 0xFF2F3438,
        hasMetallicRivets = true,
        hasEnergyConduits = true,
        hasCosmicHalo = false
    ),
    CYBER_TITANIUM(
        id = "CYBER_TITANIUM",
        label = "Titanium Hex-Alloy",
        plinthPrimaryArgb = 0xFF37434F,
        plinthTrimArgb = 0xFF48A8D8,
        roadSurfaceArgb = 0xFF3E4852,
        roadBorderArgb = 0xFF28323B,
        hasMetallicRivets = true,
        hasEnergyConduits = true,
        hasCosmicHalo = false
    ),
    ORBITAL_FUSION(
        id = "ORBITAL_FUSION",
        label = "Starfleet Composite",
        plinthPrimaryArgb = 0xFF2E3A48,
        plinthTrimArgb = 0xFF64C8F0,
        roadSurfaceArgb = 0xFF384452,
        roadBorderArgb = 0xFF222D38,
        hasMetallicRivets = true,
        hasEnergyConduits = true,
        hasCosmicHalo = true
    ),
    DYSON_QUANTUM(
        id = "DYSON_QUANTUM",
        label = "Dyson Star-Forged Lattice",
        plinthPrimaryArgb = 0xFF322E45,
        plinthTrimArgb = 0xFFE6C65A,
        roadSurfaceArgb = 0xFF3B374F,
        roadBorderArgb = 0xFF242033,
        hasMetallicRivets = true,
        hasEnergyConduits = true,
        hasCosmicHalo = true
    ),
    OMEGA_TRANSCENDENT(
        id = "OMEGA_TRANSCENDENT",
        label = "Omega Reality Citadel",
        plinthPrimaryArgb = 0xFF282238,
        plinthTrimArgb = 0xFFF5D76E,
        roadSurfaceArgb = 0xFF342E47,
        roadBorderArgb = 0xFF1D1829,
        hasMetallicRivets = true,
        hasEnergyConduits = true,
        hasCosmicHalo = true
    );

    val foundationFillArgb: Long get() = plinthPrimaryArgb
    val foundationRimArgb: Long get() = plinthTrimArgb
    val turretAccentArgb: Long get() = plinthTrimArgb
    val keepTrimArgb: Long get() = plinthTrimArgb
    val keepRoofArgb: Long get() = plinthPrimaryArgb
}

/**
 * Per-Age Tower Evolution Snapshot
 * Defines how a specific TowerType evolves in name, combat multipliers, and special passive
 * at a given Civilization Age (1..18).
 */
data class EraTowerEvolutionProfile(
    val ageNum: Int,
    val evolvedDisplayName: String,
    val evolvedShortName: String,
    val eraSubtitle: String,
    val damageMult: Float,
    val fireRateMult: Float,
    val rangeMult: Float,
    val armorPierceBonus: Float,
    val splashRadiusMult: Float,
    val projectileSpeedMult: Float,
    val eraSpecialTraitTitle: String,
    val eraSpecialTraitSummary: String,
    val architectureTheme: EraArchitectureTheme
)

/**
 * Civilization Empire Edicts (18 Edicts — 1 unlocked per Civilization Age!)
 * Players can toggle any unlocked Edict active (all unlocked Edicts can be active simultaneously!).
 */
data class CivilizationEdictSpec(
    val id: String,
    val unlockAgeNum: Int,
    val title: String,
    val eraName: String,
    val icon: String,
    val summary: String,
    val globalDamageBonus: Float = 0f,
    val globalFireRateBonus: Float = 0f,
    val globalRangeBonus: Float = 0f,
    val globalCritChanceBonus: Float = 0f,
    val globalArmorPierceBonus: Float = 0f,
    val killGoldBonus: Float = 0f,
    val bankInterestBonus: Double = 0.0,
    val towerAgeUpgradeScalingBonus: Float = 0f
)

/**
 * COMPLETE 18-AGE HISTORICAL TO FAR-FUTURE EVOLUTION CATALOG
 * Advancing your Civilization Age directly upgrades every tower's name, architecture,
 * weapon physics, damage, attack speed, range, and armor penetration!
 */
object EraEvolutionCatalog {

    const val MAX_AGE_NUM = 18

    fun architectureForAge(ageNum: Int): EraArchitectureTheme = when (ageNum.coerceIn(1, MAX_AGE_NUM)) {
        1 -> EraArchitectureTheme.PRIMITIVE_TIMBER
        2 -> EraArchitectureTheme.FEUDAL_MASONRY
        3 -> EraArchitectureTheme.CASTLE_LIMESTONE
        4, 5, 6 -> EraArchitectureTheme.IMPERIAL_GILDED
        7 -> EraArchitectureTheme.GUNPOWDER_STARFORT
        8, 9 -> EraArchitectureTheme.INDUSTRIAL_BESSEMER
        10 -> EraArchitectureTheme.ATOMIC_BUNKER
        11, 12 -> EraArchitectureTheme.CYBER_TITANIUM
        13, 14 -> EraArchitectureTheme.ORBITAL_FUSION
        15, 16 -> EraArchitectureTheme.DYSON_QUANTUM
        else -> EraArchitectureTheme.OMEGA_TRANSCENDENT
    }

    /**
     * 18 Empire Edicts unlocked progressively from Age I (Dark Age) to Age XVIII (Omega Age).
     */
    val edicts: List<CivilizationEdictSpec> = listOf(
        CivilizationEdictSpec(
            id = "EDICT_1_MILITIA_WATCH",
            unlockAgeNum = 1,
            title = "Militia Watch Charter",
            eraName = "Dark Age",
            icon = "🪵",
            summary = "+8% Range and +10% Damage to all basic fortifications.",
            globalDamageBonus = 0.10f,
            globalRangeBonus = 0.08f
        ),
        CivilizationEdictSpec(
            id = "EDICT_2_FEUDAL_TITHE",
            unlockAgeNum = 2,
            title = "Feudal Manor Tithe",
            eraName = "Feudal Age",
            icon = "🌾",
            summary = "+15% Enemy Kill Gold Bounty and +1.5% Bank Wave Interest.",
            killGoldBonus = 0.15f,
            bankInterestBonus = 0.015
        ),
        CivilizationEdictSpec(
            id = "EDICT_3_CASTLE_GARRISON",
            unlockAgeNum = 3,
            title = "Castellan Siege Drills",
            eraName = "Castle Age",
            icon = "🏰",
            summary = "+12% Attack Speed and +10% Armor Penetration for all towers.",
            globalFireRateBonus = 0.12f,
            globalArmorPierceBonus = 0.10f
        ),
        CivilizationEdictSpec(
            id = "EDICT_4_IMPERIAL_ARSENAL",
            unlockAgeNum = 4,
            title = "Imperial Gunpowder Monopoly",
            eraName = "Imperial Age",
            icon = "💣",
            summary = "+20% All Tower Damage and +8% Critical Strike Chance.",
            globalDamageBonus = 0.20f,
            globalCritChanceBonus = 0.08f
        ),
        CivilizationEdictSpec(
            id = "EDICT_5_WONDER_COMMISSIONS",
            unlockAgeNum = 5,
            title = "Grand Architect Commissions",
            eraName = "Wonder Age",
            icon = "🏛️",
            summary = "+15% Range and +20% Era Evolution scaling on all towers.",
            globalRangeBonus = 0.15f,
            towerAgeUpgradeScalingBonus = 0.20f
        ),
        CivilizationEdictSpec(
            id = "EDICT_6_GOLDEN_BULLION",
            unlockAgeNum = 6,
            title = "Sovereign Bullion Standard",
            eraName = "Golden Age",
            icon = "👑",
            summary = "+30% Kill Gold Bounty and +3.0% Bank Wave Compound Interest.",
            killGoldBonus = 0.30f,
            bankInterestBonus = 0.03
        ),
        CivilizationEdictSpec(
            id = "EDICT_7_STARFORT_BALLISTICS",
            unlockAgeNum = 7,
            title = "Vauban Star-Fort Ballistics",
            eraName = "Gunpowder Age",
            icon = "📐",
            summary = "+25% Tower Damage, +12% Range, and +12% Armor Penetration.",
            globalDamageBonus = 0.25f,
            globalRangeBonus = 0.12f,
            globalArmorPierceBonus = 0.12f
        ),
        CivilizationEdictSpec(
            id = "EDICT_8_BESSEMER_FORGES",
            unlockAgeNum = 8,
            title = "Bessemer Steam Assembly",
            eraName = "Industrial Age",
            icon = "⚙️",
            summary = "+22% Attack Speed across all towers and +25% Kill Gold.",
            globalFireRateBonus = 0.22f,
            killGoldBonus = 0.25f
        ),
        CivilizationEdictSpec(
            id = "EDICT_9_DYNAMO_GRID",
            unlockAgeNum = 9,
            title = "High-Voltage Dynamo Grid",
            eraName = "Machine Age",
            icon = "⚡",
            summary = "+35% All Tower Damage and +10% Crit Chance.",
            globalDamageBonus = 0.35f,
            globalCritChanceBonus = 0.10f
        ),
        CivilizationEdictSpec(
            id = "EDICT_10_MANHATTAN_CORE",
            unlockAgeNum = 10,
            title = "Atomic Fission Enrichment",
            eraName = "Atomic Age",
            icon = "☢️",
            summary = "+50% All Tower Damage, +15% Armor Penetration, and +30% Era Scaling.",
            globalDamageBonus = 0.50f,
            globalArmorPierceBonus = 0.15f,
            towerAgeUpgradeScalingBonus = 0.30f
        ),
        CivilizationEdictSpec(
            id = "EDICT_11_SILICON_TELEMETRY",
            unlockAgeNum = 11,
            title = "Silicon Fire-Control Radar",
            eraName = "Information Age",
            icon = "📡",
            summary = "+25% Range, +15% Crit Chance, and +4.0% Bank Wave Interest.",
            globalRangeBonus = 0.25f,
            globalCritChanceBonus = 0.15f,
            bankInterestBonus = 0.04
        ),
        CivilizationEdictSpec(
            id = "EDICT_12_NEURAL_OVERCLOCK",
            unlockAgeNum = 12,
            title = "Cybernetic Neural Uplinks",
            eraName = "Cybernetic Age",
            icon = "🧠",
            summary = "+35% Attack Speed and +60% All Tower Damage.",
            globalDamageBonus = 0.60f,
            globalFireRateBonus = 0.35f
        ),
        CivilizationEdictSpec(
            id = "EDICT_13_TOKAMAK_IGNITION",
            unlockAgeNum = 13,
            title = "Magnetic Fusion Ignition",
            eraName = "Fusion Age",
            icon = "☀️",
            summary = "+85% All Tower Damage and +50% Kill Gold Bounty.",
            globalDamageBonus = 0.85f,
            killGoldBonus = 0.50f
        ),
        CivilizationEdictSpec(
            id = "EDICT_14_STARFLEET_ORBITAL",
            unlockAgeNum = 14,
            title = "Starfleet Orbital Overwatch",
            eraName = "Starfleet Age",
            icon = "🚀",
            summary = "+35% Range, +40% Attack Speed, and unlocks 4-Path Omni-Crosspathing!",
            globalRangeBonus = 0.35f,
            globalFireRateBonus = 0.40f,
            towerAgeUpgradeScalingBonus = 0.45f
        ),
        CivilizationEdictSpec(
            id = "EDICT_15_DYSON_SWARM",
            unlockAgeNum = 15,
            title = "Dyson Megastructure Beam",
            eraName = "Dyson Age",
            icon = "🪐",
            summary = "+120% All Tower Damage and +6.0% Bank Wave Interest.",
            globalDamageBonus = 1.20f,
            bankInterestBonus = 0.06
        ),
        CivilizationEdictSpec(
            id = "EDICT_16_QUANTUM_ENTANGLE",
            unlockAgeNum = 16,
            title = "Quantum Entanglement Matrix",
            eraName = "Quantum Age",
            icon = "⚛️",
            summary = "+160% All Tower Damage, +25% Crit Chance, and +25% Armor Pierce.",
            globalDamageBonus = 1.60f,
            globalCritChanceBonus = 0.25f,
            globalArmorPierceBonus = 0.25f
        ),
        CivilizationEdictSpec(
            id = "EDICT_17_CHRONO_CONTINUUM",
            unlockAgeNum = 17,
            title = "Dimensional Rift Continuum",
            eraName = "Dimensional Age",
            icon = "🌀",
            summary = "+220% All Tower Damage, +60% Attack Speed, and +100% Kill Gold.",
            globalDamageBonus = 2.20f,
            globalFireRateBonus = 0.60f,
            killGoldBonus = 1.00f
        ),
        CivilizationEdictSpec(
            id = "EDICT_18_OMEGA_SOVEREIGN",
            unlockAgeNum = 18,
            title = "Omega Omniversal Decree",
            eraName = "Omega Age",
            icon = "♾️",
            summary = "+350% All Tower Damage, +80% Speed, +50% Range, and +10% Bank Interest!",
            globalDamageBonus = 3.50f,
            globalFireRateBonus = 0.80f,
            globalRangeBonus = 0.50f,
            bankInterestBonus = 0.10,
            towerAgeUpgradeScalingBonus = 1.00f
        )
    )

    /**
     * 18-Age Name Progression for every base & wonder tower.
     * Index 0 = Age I (Dark Age) .. Index 17 = Age XVIII (Omega Age).
     */
    private val displayNamesByTowerId: Map<String, List<String>> = mapOf(
        "GATLING" to listOf(
            "Rock Watchtower",            // Age 1: Dark
            "Arrow Watchtower",           // Age 2: Feudal
            "Crossbow Bastion",           // Age 3: Castle
            "Repeating Arbalest Keep",    // Age 4: Imperial
            "Culverin Volley Tower",      // Age 5: Wonder
            "Sovereign Musket Redoubt",   // Age 6: Golden
            "Puckle Flintlock Battery",   // Age 7: Gunpowder
            "Steam Gatling Pillbox",      // Age 8: Industrial
            "Maxim Autocannon Bunker",    // Age 9: Machine
            "Vulcan Rotary Turret",       // Age 10: Atomic
            "Phalanx CIWS Radar Array",   // Age 11: Information
            "Gauss Chaingun Spire",       // Age 12: Cybernetic
            "Plasma Pulse Repeater",      // Age 13: Fusion
            "Starfleet Mass Repeater",    // Age 14: Starfleet
            "Antimatter Shredder Spire",  // Age 15: Dyson
            "Tachyon Hyper-Gatling",      // Age 16: Quantum
            "Chrono-Volley Rift Matrix",  // Age 17: Dimensional
            "Omega Omni-Barrage Citadel"  // Age 18: Omega
        ),
        "SNIPER" to listOf(
            "Wooden Ballista",            // Age 1
            "Longbow Outpost",            // Age 2
            "Heavy Arbalest Keep",        // Age 3
            "Imperial Arquebus Tower",    // Age 4
            "Grand Basilica Sharpshooter",// Age 5
            "Royal Rifled Culverin",      // Age 6
            "Flintlock Marksman Bastion", // Age 7
            "Breech-Loaded Rail Rifle",   // Age 8
            "Anti-Armor 88mm Flak Tower", // Age 9
            "Depleted-Uranium Sniper",    // Age 10
            "Laser-Guided Rail Marksman", // Age 11
            "Cybernetic Gauss Assassin",  // Age 12
            "Magnetic Coilgun Spire",     // Age 13
            "Orbital Lance Targeting Hub",// Age 14
            "Relativistic Star-Lance",    // Age 15
            "Quantum Phase Disruptor",    // Age 16
            "Dimensional Singularity Gun",// Age 17
            "Omega Horizon Piercer"       // Age 18
        ),
        "MORTAR" to listOf(
            "Stone Catapult",             // Age 1
            "Mangonel Siege Post",        // Age 2
            "Counterweight Trebuchet",    // Age 3
            "Royal Siege Bombard",        // Age 4
            "Grand Monolith Mortar",      // Age 5
            "Gilded Sovereign Howitzer",  // Age 6
            "Vauban Siege Mortar",        // Age 7
            "Bessemer Railway Bertha",    // Age 8
            "Long-Tom Siege Artillery",   // Age 9
            "Atomic Heavy Howitzer",      // Age 10
            "Tomahawk Cruise Silo",       // Age 11
            "Thermobaric Nano-Mortar",    // Age 12
            "Plasma Nova Artillery",      // Age 13
            "Orbital Kinetic Drop Pod",   // Age 14
            "Antimatter Star-Bombard",    // Age 15
            "Zero-Point Collapse Mortar", // Age 16
            "Void-Rift Cataclysm Silo",   // Age 17
            "Omega Supernova Trebuchet"   // Age 18
        ),
        "HYDRO" to listOf(
            "Log Sluice Gate",            // Age 1
            "River Water Mill",           // Age 2
            "Moat Floodgate Keep",        // Age 3
            "Imperial Aqueduct Sluice",   // Age 4
            "Grand Fountain Cascade",     // Age 5
            "Sovereign Tidal Conduit",    // Age 6
            "Pressurized Canal Cannon",   // Age 7
            "Steam Hydro-Turbine",        // Age 8
            "High-Pressure Water Cannon", // Age 9
            "Heavy-Water Reactor Sluice", // Age 10
            "Sonic Hydro-Cavitator",      // Age 11
            "Cryo-Fluid Nano-Jet",        // Age 12
            "Magneto-Hydrodynamic Spire", // Age 13
            "Europa Sub-Glacial Geyser",  // Age 14
            "Stellar Hydrogen Torrent",   // Age 15
            "Quantum Superfluid Vortex",  // Age 16
            "Tidal Chrono-Maelstrom",     // Age 17
            "Omega Abyssal Deluge"        // Age 18
        ),
        "CRYO" to listOf(
            "Snowdrift Cairn",            // Age 1
            "Northern Ice Keep",          // Age 2
            "Glacier Watchtower",         // Age 3
            "Permafrost Bastion",         // Age 4
            "Winter Palace Citadel",      // Age 5
            "Sovereign Boreal Spire",     // Age 6
            "Saltpeter Hail Battery",     // Age 7
            "Ammonia Vapor Chiller",      // Age 8
            "Liquid-Nitrogen Cryo-Tower", // Age 9
            "Endothermic Fission Spire",  // Age 10
            "Peltier Solid-State Freezer",// Age 11
            "Laser-Cooled Cryo-Matrix",   // Age 12
            "Zero-Kelvin Stasis Emitter", // Age 13
            "Deep-Space Cryo-Beacon",     // Age 14
            "Bose-Einstein Condensate",   // Age 15
            "Quantum Entropy Arrestor",   // Age 16
            "Time-Freeze Singularity",    // Age 17
            "Omega Absolute-Zero Monolith"// Age 18
        ),
        "PYRO" to listOf(
            "Torch Brazier Post",         // Age 1
            "Pitch Cauldron Tower",       // Age 2
            "Boiling Pitch Bastion",      // Age 3
            "Byzantine Greek Fire Keep",  // Age 4
            "Dragonfire Alchemical Spire",// Age 5
            "Sovereign Sun-Forge Keep",   // Age 6
            "Congreve Rocket Battery",    // Age 7
            "Coal-Fired Blast Furnace",   // Age 8
            "Napalm Flamethrower Bunker", // Age 9
            "Thermonuclear Igniter Core", // Age 10
            "Plasma-Torch Incinerator",   // Age 11
            "Thermite Nano-Swarm Vent",   // Age 12
            "Solar-Corona Fusion Torch",  // Age 13
            "Starfire Orbital Igniter",   // Age 14
            "Dyson Prominence Ejector",   // Age 15
            "Quark-Gluon Firestorm",      // Age 16
            "Dimensional Hellfire Rift",  // Age 17
            "Omega Primordial Star-Forge" // Age 18
        ),
        "TESLA" to listOf(
            "Copper Lodestone Post",      // Age 1
            "Iron Lightning Rod",         // Age 2
            "Alchemist Spark Spire",      // Age 3
            "Imperial Storm Spire",       // Age 4
            "Grand Tempest Obelisk",      // Age 5
            "Sovereign Voltaic Tower",    // Age 6
            "Leyden Jar Arc Battery",     // Age 7
            "Westinghouse Dynamo Coil",   // Age 8
            "Wardenclyffe Tesla Tower",   // Age 9
            "Cyclotron Particle Arc",     // Age 10
            "EMP Pulse Capacitor Array",  // Age 11
            "Superconducting Arc Nexus",  // Age 12
            "Ionosphere Lightning Uplink",// Age 13
            "Magnetar Pulse Emitter",     // Age 14
            "Dyson Megavolt Conduit",     // Age 15
            "Quantum Electron Tunnel",    // Age 16
            "Void-Storm Astral Conduit",  // Age 17
            "Omega Fulmination Throne"    // Age 18
        ),
        "VENOM" to listOf(
            "Herbalist Hut",              // Age 1
            "Swamp Blight Post",          // Age 2
            "Plague Apothecary",          // Age 3
            "Royal Alchemist Vitriol",    // Age 4
            "Grand Miasma Conservatory",  // Age 5
            "Sovereign Elixir Crucible",  // Age 6
            "Sulfuric Acid Mortar",       // Age 7
            "Chemical Smog Distillery",   // Age 8
            "Mustard-Gas Dispersal Bunker",// Age 9
            "Radioactive Fallout Spire",  // Age 10
            "Bio-Engineered Viral Vat",   // Age 11
            "Corrosive Grey-Goo Dispenser",// Age 12
            "Molecular Acid Atomizer",    // Age 13
            "Xeno-Spore Bio-Containment", // Age 14
            "Antimatter Corrosion Field", // Age 15
            "Quantum Bond Dissolver",     // Age 16
            "Entropic Decay Obelisk",     // Age 17
            "Omega Oblivion Crucible"     // Age 18
        ),
        "PRISM" to listOf(
            "Polished Bronze Mirror",     // Age 1
            "Beacon Signal Tower",        // Age 2
            "Archimedes Burning Glass",   // Age 3
            "Sun Mirror Obelisk",         // Age 4
            "Grand Solarium Cathedral",   // Age 5
            "SovereignHelios Monument",   // Age 6
            "Fresnel Lighthouse Lens",    // Age 7
            "Carbon-Arc Searchlight",     // Age 8
            "Focused Ruby Maser Tower",   // Age 9
            "Excimer UV Laser Turret",    // Age 10
            "Fiber-Optic Phased Array",   // Age 11
            "X-Ray Free-Electron Laser",  // Age 12
            "Gamma-Ray Coherent Emitter", // Age 13
            "Orbital Solar Mirror Uplink",// Age 14
            "Dyson Sphere Nicol-Beam",    // Age 15
            "Quantum Photon Condenser",   // Age 16
            "Hyper-Luminous Quasar Spire",// Age 17
            "Omega Lux-Eternal Spire"     // Age 18
        ),
        "CHRONO" to listOf(
            "Standing Stone Sundial",     // Age 1
            "Monastery Bell Tower",       // Age 2
            "Water-Clock Belfry",         // Age 3
            "Town Clock & Sundial",       // Age 4
            "Astronomical Horologium",    // Age 5
            "Sovereign Pendulum Spire",   // Age 6
            "Marine Chronometer Keep",    // Age 7
            "Steam Governor Clockwork",   // Age 8
            "Quartz Oscillating Beacon",  // Age 9
            "Cesium Atomic Clock Spire",  // Age 10
            "Relativistic Lorentz Pylon", // Age 11
            "Tachyon Temporal Anchor",    // Age 12
            "Graviton Time-Dilation Well",// Age 13
            "Event-Horizon Stasis Dome",  // Age 14
            "Tipler Cylinder Chronos",    // Age 15
            "Quantum Closed-Timelike Loop",// Age 16
            "Multiverse Timeline Splicer",// Age 17
            "Omega Eternal Ouroboros"     // Age 18
        ),
        "DRONE" to listOf(
            "Hawk Hunting Perch",         // Age 1
            "Falconry Roost",             // Age 2
            "Royal Eagle Eyrie",          // Age 3
            "Da Vinci Ornithopter Roost", // Age 4
            "Imperial War-Kite Hangar",   // Age 5
            "SovereignGalleon Air-Dock",  // Age 6
            "Montgolfier Balloon Tether", // Age 7
            "Steam Dirigible Mooring",    // Age 8
            "Biplane Squadron Aerodrome", // Age 9
            "Supersonic Jet Interceptor", // Age 10
            "Predator UAV Drone Command", // Age 11
            "Autonomous Hunter-Swarm Hub",// Age 12
            "Orbital Strike Fighter Bay", // Age 13
            "Starfleet Carrier Deck",     // Age 14
            "Dyson Von-Neumann Swarm",    // Age 15
            "Quantum Phase-Interceptor",  // Age 16
            "Dimensional Valkyrie Nexus", // Age 17
            "Omega Seraphim Armada"       // Age 18
        ),
        "HUB" to listOf(
            "Barter Trading Post",        // Age 1
            "Village Market Stall",       // Age 2
            "Merchant Guildhall",         // Age 3
            "Royal Mint & Market",        // Age 4
            "Grand Bourse Exchange",      // Age 5
            "Sovereign Gold Treasury",    // Age 6
            "Chartered East-India House", // Age 7
            "Industrial Stock Exchange",  // Age 8
            "Central Reserve Clearinghouse",// Age 9
            "Global Bullion Depository",  // Age 10
            "High-Frequency Fiber Bourse",// Age 11
            "Quantum Crypto-Mint Node",   // Age 12
            "Fusion Matter Synthesizer",  // Age 13
            "Interplanetary Trade Hub",   // Age 14
            "Dyson Energy-to-Gold Forge", // Age 15
            "Zero-Point Replicator Mint", // Age 16
            "Multiverse Commerce Nexus",  // Age 17
            "Omega Infinite Cornucopia"   // Age 18
        ),
        "RAIL_CANNON" to listOf(
            "Heavy Sling Outpost",        // Age 1
            "Springald Bolt Thrower",     // Age 2
            "Forged Iron Ribault",        // Age 3
            "Great Bronze Dardanelles",   // Age 4
            "Basilisk Royal Cannon",      // Age 5
            "Sovereign Demi-Cannon",      // Age 6
            "Armstrong Rifled Breechloader",// Age 7
            "Krupp Steel Siege Gun",      // Age 8
            "Dora Railway Super-Cannon",  // Age 9
            "Atomic Annie Cannon",        // Age 10
            "Electromagnetic Rail Prototype",// Age 11
            "Hyper-Velocity Sabot Railgun",// Age 12
            "Plasma-Armature Mass Driver",// Age 13
            "Orbital MAC Defense Cannon", // Age 14
            "Dyson Relativistic Driver",  // Age 15
            "Tachyon Linear Accelerator", // Age 16
            "Singularity Bore Cannon",    // Age 17
            "Omega God-Slayer Railgun"    // Age 18
        ),
        "LASER_ARRAY" to listOf(
            "Fire-Arrow Signal Post",     // Age 1
            "Oil-Lamp Watch Reflector",   // Age 2
            "Parabolic Copper Mirror",    // Age 3
            "Alchemical Lens Tower",      // Age 4
            "Venetian Crystal Focusing Spire",// Age 5
            "Sovereign Solar Astrolabe",  // Age 6
            "Heliograph Mirror Telegraph",// Age 7
            "Electric Carbon Spotlight",  // Age 8
            "Infrared Heat-Ray Projector",// Age 9
            "CO2 Industrial Gas Laser",   // Age 10
            "Solid-State Nd:YAG Laser",   // Age 11
            "Megawatt Chemical Laser",    // Age 12
            "Free-Electron FEL Turret",   // Age 13
            "Starfleet Phaser Battery",   // Age 14
            "Stellar Corona Maser Lance", // Age 15
            "Planck-Scale Graser Beam",   // Age 16
            "Vacuum-Decay Ray Emitter",   // Age 17
            "Omega Genesis Beam Array"    // Age 18
        ),
        "GRAVITY_WELL" to listOf(
            "Mud Quagmire Pit",           // Age 1
            "Tar & Pitch Bog",            // Age 2
            "Magnetite Lodestone Pillar", // Age 3
            "Alchemical Lodestone Well",  // Age 4
            "Newtonian Orrery Spire",     // Age 5
            "Sovereign Gyroscope Keep",   // Age 6
            "Magnetic Compass Observatory",// Age 7
            "Electromagnetic Crane Magnet",// Age 8
            "High-Flux Solenoid Trap",    // Age 9
            "Centrifuge High-G Projector",// Age 10
            "Superconducting Mag-Well",   // Age 11
            "Artificial Gravity Generator",// Age 12
            "Graviton Tractor Emitter",   // Age 13
            "Micro-Singularity Containment",// Age 14
            "Neutron-Star Mass Anchor",   // Age 15
            "Black-Hole Event Horizon",   // Age 16
            "Dark-Matter Well Projector", // Age 17
            "Omega Great Attractor"       // Age 18
        ),
        "SHIELD_GARRISON" to listOf(
            "Palisade Shield Post",       // Age 1
            "Timber Blockhouse",          // Age 2
            "Pavise Crossbow Redoubt",    // Age 3
            "Imperial Bastion Hornwork",  // Age 4
            "Citadel Star-Ravelin",       // Age 5
            "Sovereign Crown Redoubt",    // Age 6
            "Casemate Ironclad Fort",     // Age 7
            "Armored Cupola Turret",      // Age 8
            "Maginot Ferro-Casemate",     // Age 9
            "Lead-Lined Fallout Bastion", // Age 10
            "Trophy Active-Defense Dome", // Age 11
            "Hard-Light Holographic Aegis",// Age 12
            "Magnetosphere Deflector Hub",// Age 13
            "Starfleet Ray-Shield Pylon", // Age 14
            "Planetary Dyson Shield Grid",// Age 15
            "Quantum Phase-Shift Bastion",// Age 16
            "Dimensional Ward Citadel",   // Age 17
            "Omega Invulnerable Bastion"  // Age 18
        )
    )

    private val shortNamesByTowerId: Map<String, List<String>> = mapOf(
        "GATLING" to listOf(
            "Rock Tower", "Arrow Keep", "Crossbow", "Arbalest", "Culverin", "Musket Fort",
            "Flintlock", "Steam Gatling", "Autocannon", "Vulcan Gun", "Phalanx CIWS", "Gauss Gun",
            "Plasma Gun", "Mass Driver", "Antimatter", "Tachyon Gun", "Chrono-Gun", "Omega Gatling"
        ),
        "SNIPER" to listOf(
            "Ballista", "Longbow", "Arbalest", "Arquebus", "Sharpshooter", "Rifled Gun",
            "Marksman", "Rail Rifle", "88mm Flak", "DU Sniper", "Laser Sniper", "Gauss Sniper",
            "Coilgun", "Orbital Lance", "Star-Lance", "Phase Sniper", "Rift Sniper", "Omega Lance"
        ),
        "MORTAR" to listOf(
            "Catapult", "Mangonel", "Trebuchet", "Bombard", "Monolith", "Howitzer",
            "Siege Mortar", "Rail Bertha", "Artillery", "Atom Mortar", "Cruise Silo", "Nano-Mortar",
            "Plasma Lob", "Kinetic Pod", "Star-Bombard", "Zero-Mortar", "Void Silo", "Omega Mortar"
        ),
        "HYDRO" to listOf(
            "Log Sluice", "Water Mill", "Floodgate", "Aqueduct", "Cascade", "Tidal Mill",
            "Canal Jet", "Steam Pump", "Water Cannon", "Heavy-Water", "Cavitator", "Cryo-Jet",
            "MHD Spire", "Geyser Hub", "H2 Torrent", "Superfluid", "Maelstrom", "Omega Deluge"
        ),
        "CRYO" to listOf(
            "Snow Cairn", "Ice Keep", "Glacier", "Permafrost", "Winter Keep", "Boreal Spire",
            "Hail Fort", "NH3 Chiller", "LN2 Tower", "Fission Cryo", "Peltier Cryo", "Laser Cryo",
            "0K Stasis", "Deep-Cryo", "BEC Spire", "Quantum Cryo", "Time-Freeze", "Omega Zero"
        ),
        "PYRO" to listOf(
            "Brazier", "Pitch Pot", "Boiling Pitch", "Greek Fire", "Dragonfire", "Sun-Forge",
            "Rocket Fort", "Blast Furnace", "Flamethrower", "Thermo Core", "Plasma Torch", "Thermite",
            "Corona Torch", "Starfire", "Prominence", "Quark Fire", "Hellfire", "Omega Forge"
        ),
        "TESLA" to listOf(
            "Lodestone", "Lightning Rod", "Spark Spire", "Storm Spire", "Tempest", "Voltaic",
            "Leyden Arc", "Dynamo Coil", "Tesla Tower", "Cyclotron", "EMP Array", "Super-Arc",
            "Ion Uplink", "Magnetar", "Dyson Volt", "Quantum Arc", "Void Storm", "Omega Bolt"
        ),
        "VENOM" to listOf(
            "Herb Hut", "Blight Post", "Apothecary", "Vitriol Lab", "Miasma", "Elixir Vat",
            "Acid Mortar", "Chem Smog", "Gas Bunker", "Rad Spire", "Viral Vat", "Nanite Goo",
            "Atom Acid", "Xeno-Spore", "Anti-Acid", "Bond Melter", "Entropy Spire", "Omega Blight"
        ),
        "PRISM" to listOf(
            "Bronze Mirror", "Beacon", "Burn Glass", "Sun Obelisk", "Solarium", "Helios",
            "Fresnel Lens", "Searchlight", "Ruby Maser", "UV Laser", "Phased Array", "X-Ray Laser",
            "Gamma Beam", "Solar Uplink", "Nicol-Beam", "Photon Core", "Quasar Beam", "Omega Lux"
        ),
        "CHRONO" to listOf(
            "Stone Dial", "Bell Tower", "Water Clock", "Sundial", "Horologium", "Pendulum",
            "Chronometer", "Steam Clock", "Quartz Spire", "Atomic Clock", "Lorentz Pylon", "Tachyon Bell",
            "Gravity Well", "Stasis Dome", "Tipler Spire", "Time Loop", "Timeline Hub", "Omega Clock"
        ),
        "DRONE" to listOf(
            "Hawk Perch", "Falcon Roost", "Eagle Eyrie", "Ornithopter", "War-Kite", "Air-Dock",
            "Balloon Fort", "Dirigible", "Aerodrome", "Jet Hangar", "UAV Command", "Drone Swarm",
            "Strike Bay", "Star Carrier", "Dyson Swarm", "Phase Wing", "Valkyrie Hub", "Omega Fleet"
        ),
        "HUB" to listOf(
            "Trade Post", "Market", "Guildhall", "Royal Mint", "Grand Bourse", "Gold Vault",
            "Trade House", "Stock Bourse", "Fed Reserve", "Bullion Fort", "Fiber Bourse", "Crypto-Mint",
            "Matter Synth", "Star Trade", "Dyson Mint", "Replicator", "Multi-Trade", "Omega Mint"
        ),
        "RAIL_CANNON" to listOf(
            "Heavy Sling", "Springald", "Ribault", "Dardanelles", "Basilisk", "Demi-Cannon",
            "Breech Gun", "Krupp Gun", "Dora Railgun", "Atomic Gun", "EM Railgun", "Sabot Rail",
            "Mass Driver", "Orbital MAC", "Dyson Driver", "Tachyon Rail", "Bore Cannon", "Omega Rail"
        ),
        "LASER_ARRAY" to listOf(
            "Fire Signal", "Lamp Mirror", "Copper Lens", "Alchemy Lens", "Crystal Lens", "Astrolabe",
            "Heliograph", "Arc Light", "Heat-Ray", "CO2 Laser", "Nd:YAG Laser", "Chem Laser",
            "FEL Laser", "Phaser Array", "Maser Lance", "Graser Beam", "Vacuum Ray", "Omega Beam"
        ),
        "GRAVITY_WELL" to listOf(
            "Quagmire", "Tar Bog", "Magnetite", "Lodestone", "Orrery", "Gyroscope",
            "Compass Hub", "EM Magnet", "Solenoid", "Centrifuge", "Mag-Well", "Grav-Gen",
            "Tractor Beam", "Micro-Hole", "Neutron Well", "Black Hole", "Dark Matter", "Omega Well"
        ),
        "SHIELD_GARRISON" to listOf(
            "Palisade", "Blockhouse", "Pavise Fort", "Hornwork", "Star-Ravelin", "Crown Fort",
            "Ironclad", "Cupola Fort", "Maginot", "Lead Bunker", "Trophy Dome", "Hard-Light",
            "Deflector", "Ray-Shield", "Dyson Grid", "Phase Fort", "Ward Citadel", "Omega Aegis"
        )
    )

    private val eraSubtitles: List<String> = listOf(
        "Age I: Dark Age Timber & Stone",
        "Age II: Feudal Manor & Iron",
        "Age III: Castle Limestone & Alchemy",
        "Age IV: Imperial Gunpowder & Brass",
        "Age V: Grand Renaissance Wonder",
        "Age VI: Golden Sovereign Dynasty",
        "Age VII: Enlightenment Star-Fort",
        "Age VIII: Industrial Steam & Steel",
        "Age IX: Machine Dynamo & Diesel",
        "Age X: Atomic Fission & Isotope",
        "Age XI: Silicon Radar & Guided",
        "Age XII: Cybernetic Neural & Nano",
        "Age XIII: Magnetic Fusion & Plasma",
        "Age XIV: Starfleet Orbital Uplink",
        "Age XV: Dyson Stellar Megastructure",
        "Age XVI: Quantum Zero-Point Matrix",
        "Age XVII: Dimensional Chrono-Rift",
        "Age XVIII: Omega Omniversal Apex"
    )

    private val eraSpecialTraits: List<Pair<String, String>> = listOf(
        "Rough-Hewn Masonry" to "Standard Dark Age garrison timber & stone.",
        "Iron-Shod Reinforcements" to "+15% Projectile speed and +18% Age Damage.",
        "Castle Murder-Holes" to "+5% Armor Pierce and +40% Age Damage.",
        "Saltpeter Gunpowder" to "+10% Splash radius and +75% Age Damage.",
        "Grand Guild Masterwork" to "+12% Range and +120% Age Damage.",
        "Gilded Crown Ordnance" to "+15% Armor Pierce and +185% Age Damage.",
        "Rifled Barrel Grooves" to "+20% Range, +18% Armor Pierce, and 3.8x Age Damage.",
        "Bessemer Steam Overpressure" to "+28% Fire Rate and 5.2x Age Damage.",
        "Autoloading Magazine Feed" to "+40% Fire Rate and 7.4x Age Damage.",
        "Enriched Uranium Cores" to "+30% Armor Pierce and 11.0x Age Damage.",
        "Silicon Fire-Control Lock" to "+35% Range, True Sight, and 16.5x Age Damage.",
        "Nanite Self-Sharpening" to "+40% Armor Pierce and 25.0x Age Damage.",
        "Superheated Plasma Sheath" to "Attacks ignite Plasma Burn; 38.0x Age Damage.",
        "Orbital Telemetry Uplink" to "+50% Range, +80% Speed, and 60.0x Age Damage.",
        "Stellar Antimatter Payload" to "+25% Splash Radius and 95.0x Age Damage.",
        "Quantum Tunneling Rounds" to "Ignores 65% Armor & Shields; 150x Age Damage.",
        "Dimensional Phase Volley" to "Strikes across timelines; 240x Age Damage.",
        "Omega Singularity Ordinance" to " Ultimate 400x Age Damage & +150% Attack Speed!"
    )

    /**
     * Computes the exact Era Evolution Profile for any [towerType] at [ageNum] (1..18).
     * Advancing your Civilization Age automatically evolves every tower using this profile!
     */
    fun profileFor(towerType: TowerType, ageNum: Int): EraTowerEvolutionProfile {
        val clampedAge = ageNum.coerceIn(1, MAX_AGE_NUM)
        val idx = clampedAge - 1

        val evolvedDisplay = displayNamesByTowerId[towerType.id]?.getOrNull(idx)
            ?: if (clampedAge >= 7) "${eraShortPrefix(clampedAge)} ${towerType.displayName}" else towerType.displayName
        val evolvedShort = shortNamesByTowerId[towerType.id]?.getOrNull(idx)
            ?: towerType.shortName

        // Exponential Era Damage Scaling so advancing into Industrial, Atomic, Cyber, Dyson & Omega Ages
        // dramatically upgrades every placed and newly built tower!
        val dmgMult = when (clampedAge) {
            1 -> 1.00f
            2 -> 1.22f
            3 -> 1.48f
            4 -> 1.82f
            5 -> 2.28f
            6 -> 2.90f
            7 -> 3.85f
            8 -> 5.25f
            9 -> 7.40f
            10 -> 11.0f
            11 -> 16.5f
            12 -> 25.0f
            13 -> 38.0f
            14 -> 60.0f
            15 -> 95.0f
            16 -> 150.0f
            17 -> 240.0f
            else -> 400.0f
        }

        val spdMult = when {
            clampedAge <= 6 -> 1.0f + (clampedAge - 1) * 0.10f
            clampedAge <= 12 -> 1.50f + (clampedAge - 6) * 0.14f
            else -> 2.34f + (clampedAge - 12) * 0.20f
        }

        val rngMult = 1.0f + (clampedAge - 1) * 0.035f
        val pierceBonus = ((clampedAge - 1) * 0.035f).coerceAtMost(0.65f)
        val splashMult = 1.0f + (clampedAge - 1) * 0.025f
        val projSpdMult = 1.0f + (clampedAge - 1) * 0.08f
        val (traitTitle, traitDesc) = eraSpecialTraits[idx]

        return EraTowerEvolutionProfile(
            ageNum = clampedAge,
            evolvedDisplayName = evolvedDisplay,
            evolvedShortName = evolvedShort,
            eraSubtitle = eraSubtitles[idx],
            damageMult = dmgMult,
            fireRateMult = spdMult,
            rangeMult = rngMult,
            armorPierceBonus = pierceBonus,
            splashRadiusMult = splashMult,
            projectileSpeedMult = projSpdMult,
            eraSpecialTraitTitle = traitTitle,
            eraSpecialTraitSummary = traitDesc,
            architectureTheme = architectureForAge(clampedAge)
        )
    }

    fun themeForAge(ageNum: Int): EraArchitectureTheme = architectureForAge(ageNum)

    fun evolvedTowerTitle(towerType: TowerType, ageNum: Int): String =
        profileFor(towerType, ageNum).evolvedDisplayName

    private fun eraShortPrefix(ageNum: Int): String = when (ageNum) {
        7 -> "Bastion"
        8 -> "Steam"
        9 -> "Dynamo"
        10 -> "Atomic"
        11 -> "Silicon"
        12 -> "Cyber"
        13 -> "Fusion"
        14 -> "Starfleet"
        15 -> "Dyson"
        16 -> "Quantum"
        17 -> "Chrono"
        else -> "Omega"
    }
}

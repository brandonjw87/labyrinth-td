package com.brandonwallace.labyrinthtd.game

import kotlin.math.pow

enum class TowerCategory(val label: String, val colorArgb: Long) {
    KINETIC("Fortifications", 0xFFD4AF37),
    ELEMENTAL("Alchemical", 0xFF5C9E57),
    ARCANE_TECH("Imperial & Future Tech", 0xFF7E9BB5),
    SUPPORT("Guild & Economy", 0xFFC89D54),
    APEX_HYBRID("Grand Wonders", 0xFFC75B39)
}

enum class TargetingPriority(val label: String, val shortTag: String) {
    FIRST("First (Closest to Keep)", "FIRST"),
    LAST("Last (Newly Entered)", "LAST"),
    STRONGEST("Strongest (Highest HP)", "STRONG"),
    WEAKEST("Weakest (Finish Off)", "WEAK"),
    CLUSTERED("Clustered (Best Splash)", "GROUP"),
    PRIMED("Status-Primed (Combo Ready)", "COMBO"),
    ARMORED("Armored & Shielded First", "ARMOR"),
    BOSS_HUNTER("Siege Boss Priority", "BOSS")
}

enum class StatusEffect(
    val label: String,
    val badge: String,
    val colorArgb: Long,
    val description: String
) {
    WET("Drenched", "WET", 0xFF4F8FB8, "Slows march by 15%; conducts lightning (+Superconduct) and freezes twice as fast."),
    CHILLED("Chilled", "CHL", 0xFF7FAFC4, "Slows movement speed by 35%; stacks into solid Frozen state."),
    FROZEN("Frozen Solid", "FRZ", 0xFF9AC4D8, "Immobilizes target completely; vulnerable to 3.5x Catapult/Ballista Shatter."),
    BURNING("Burning Pitch", "BRN", 0xFFD46A34, "Searing pitch fire over time; ignites Tar Oil and triggers Thermal Shock."),
    OILED("Tar-Slicked", "TAR", 0xFF7A5C43, "Slows by 25% and erupts into Greek Fire when hit by Flame or Lightning."),
    SHOCKED("Storm-Struck", "SHK", 0xFFD9B44A, "Staggers movement and arcs chain lightning to nearby raiders."),
    POISONED("Plague-Poisoned", "PSN", 0xFF6B9E48, "Stacking apothecary toxin DoT that spreads on death."),
    BLEEDING("Bleeding", "BLD", 0xFFB84A39, "Heavy stone, bolt & rail lacerations that deal bonus damage while moving."),
    CORRODED("Armor Cracked", "CRK", 0xFFA68B3C, "Sunders 50% armor and increases all incoming damage by +35%."),
    MARKED("Scout Marked", "MRK", 0xFFC75B39, "Takes +50% Critical Damage and is executed below 18% HP."),
    TIME_ANCHORED("Bell-Slowed", "SLW", 0xFF8C7AA8, "Slowed by Sundial/Chrono chimes; pushed back on the road and takes +45% splash damage.")
}

enum class ComboReaction(
    val title: String,
    val formula: String,
    val description: String,
    val bonusDamageMult: Float,
    val colorArgb: Long
) {
    SHATTER(
        title = "BOULDER SHATTER",
        formula = "FROZEN + Stone/Bolt/Rail (Catapult / Ballista / Railgun)",
        description = "Heavy boulder, bolt, or rail impact shatters frozen armor for 3.5x burst damage!",
        bonusDamageMult = 3.5f,
        colorArgb = 0xFFAECAD8
    ),
    SUPERCONDUCT(
        title = "STORM CONDUCTOR",
        formula = "WET + SHOCKED (Water Mill + Storm Spire)",
        description = "Water super-charges lightning: +4 chain jumps and 2.5x arc damage!",
        bonusDamageMult = 2.5f,
        colorArgb = 0xFFE0C068
    ),
    NAPALM_IGNITION(
        title = "GREEK FIRE IGNITION",
        formula = "TAR-SLICKED + BURNING (Apothecary + Boiling Pitch)",
        description = "Ignites volatile tar slick into Greek Fire dealing 2.8x burst + 6% Max HP burn!",
        bonusDamageMult = 2.8f,
        colorArgb = 0xFFD96B38
    ),
    FLASH_FREEZE(
        title = "WINTER FLASH-FREEZE",
        formula = "WET + CHILLED (Water Mill + Ice Keep)",
        description = "Instantly encases drenched foes in solid ice and extends freeze duration by +100%!",
        bonusDamageMult = 2.0f,
        colorArgb = 0xFF8FBCCF
    ),
    THERMAL_SHOCK(
        title = "THERMAL ARMOR CRACK",
        formula = "FROZEN + BURNING (Ice Keep + Boiling Pitch / Laser)",
        description = "Extreme temperature shift cracks armor permanently (-50% armor) and deals 3.2x burst!",
        bonusDamageMult = 3.2f,
        colorArgb = 0xFFD98A6C
    ),
    BIO_DETONATION(
        title = "ALCHEMICAL BLAST",
        formula = "POISONED + BURNING/SHOCKED (Apothecary + Pitch/Storm)",
        description = "Detonates plague vapors into a violent alchemical blast across nearby enemies!",
        bonusDamageMult = 2.6f,
        colorArgb = 0xFF7FA852
    ),
    HEMORRHAGE(
        title = "SIEGE BREACH",
        formula = "BLEEDING + MARKED/CRACKED (Rock Tower/Railgun + Ballista/Apothecary)",
        description = "Sundered armor gives way to 2.7x critical siege damage!",
        bonusDamageMult = 2.7f,
        colorArgb = 0xFFC75B39
    ),
    PRISM_REFRACTION(
        title = "SOLAR CONVERGENCE",
        formula = "Sun Obelisk / Laser Array + Adjacent Elemental Building",
        description = "Sun Obelisk & Laser Array absorb neighbor's element, focusing multi-status beams!",
        bonusDamageMult = 2.3f,
        colorArgb = 0xFFD4AF37
    ),
    CHRONO_COMPRESSION(
        title = "GRAVITON CRUSH",
        formula = "BELL-SLOWED + Splash (Sundial/Gravity Well + Catapult/Water Mill)",
        description = "Slowed formations take 2.9x concentrated siege splash and get knocked back!",
        bonusDamageMult = 2.9f,
        colorArgb = 0xFFA290BD
    ),
    OVERCLOCK_TRIAD(
        title = "IMPERIAL GUILD ACCORD",
        formula = "Royal Mint + Sundial/Aegis + Offensive Tower",
        description = "Guild supply line grants +35% Fire Rate, +35% Crit Damage, and +25% Kill Gold!",
        bonusDamageMult = 2.2f,
        colorArgb = 0xFFD4AF37
    )
}

/**
 * LAYER: TOWER ARMORY & FORGE ATTACHMENTS (20 Attachments across Medieval -> Future Ages, Upgradeable Tier I -> Tier V)
 */
enum class TowerGearAttachment(
    val id: String,
    val title: String,
    val shortTag: String,
    val icon: String,
    val baseCost: Long,
    val damageMult: Float,
    val fireRateMult: Float,
    val rangeMult: Float,
    val critChanceBonus: Float,
    val armorPierceBonus: Float,
    val extraBounces: Int,
    val goldPerHit: Long,
    val bonusStatus: StatusEffect?,
    val summary: String
) {
    NONE(
        id = "NONE",
        title = "No Forge Gear",
        shortTag = "None",
        icon = "⚙️",
        baseCost = 0L,
        damageMult = 1.0f,
        fireRateMult = 1.0f,
        rangeMult = 1.0f,
        critChanceBonus = 0f,
        armorPierceBonus = 0f,
        extraBounces = 0,
        goldPerHit = 0L,
        bonusStatus = null,
        summary = "Standard garrison equipment. Tap to forge a specialized attachment."
    ),
    BODKIN_QUIVER(
        id = "BODKIN_QUIVER",
        title = "Tempered Bodkin Tips",
        shortTag = "Bodkin",
        icon = "🗡️",
        baseCost = 140L,
        damageMult = 1.22f,
        fireRateMult = 1.05f,
        rangeMult = 1.0f,
        critChanceBonus = 0.14f,
        armorPierceBonus = 0.30f,
        extraBounces = 0,
        goldPerHit = 0L,
        bonusStatus = StatusEffect.BLEEDING,
        summary = "+22% Damage, +14% Crit Chance, ignores 30% Armor, and inflicts Bleeding."
    ),
    GREEK_FIRE_BRAZIER(
        id = "GREEK_FIRE_BRAZIER",
        title = "Greek Fire Brazier",
        shortTag = "FirePot",
        icon = "🔥",
        baseCost = 165L,
        damageMult = 1.25f,
        fireRateMult = 1.0f,
        rangeMult = 1.05f,
        critChanceBonus = 0.05f,
        armorPierceBonus = 0.10f,
        extraBounces = 0,
        goldPerHit = 0L,
        bonusStatus = StatusEffect.BURNING,
        summary = "+25% Damage and imbues all attacks with Burning Greek Fire."
    ),
    FROST_RUNESTONE(
        id = "FROST_RUNESTONE",
        title = "Northern Frost Stone",
        shortTag = "FrostGem",
        icon = "❄️",
        baseCost = 160L,
        damageMult = 1.15f,
        fireRateMult = 1.05f,
        rangeMult = 1.10f,
        critChanceBonus = 0f,
        armorPierceBonus = 0f,
        extraBounces = 0,
        goldPerHit = 0L,
        bonusStatus = StatusEffect.CHILLED,
        summary = "+15% Damage, +10% Range, and imbues all shots with Northern Chill."
    ),
    CLOCKWORK_WINCH(
        id = "CLOCKWORK_WINCH",
        title = "Clockwork Winch Gear",
        shortTag = "Winch",
        icon = "⚙️",
        baseCost = 175L,
        damageMult = 1.08f,
        fireRateMult = 1.32f,
        rangeMult = 1.0f,
        critChanceBonus = 0.06f,
        armorPierceBonus = 0f,
        extraBounces = 0,
        goldPerHit = 0L,
        bonusStatus = null,
        summary = "Precision brass gears increase Attack Speed by +32% and Damage by +8%."
    ),
    SPYGLASS_CROWS_NEST(
        id = "SPYGLASS_CROWS_NEST",
        title = "Lookout Crow's Nest",
        shortTag = "Spyglass",
        icon = "🔭",
        baseCost = 150L,
        damageMult = 1.12f,
        fireRateMult = 1.0f,
        rangeMult = 1.28f,
        critChanceBonus = 0.15f,
        armorPierceBonus = 0.15f,
        extraBounces = 0,
        goldPerHit = 0L,
        bonusStatus = StatusEffect.MARKED,
        summary = "+28% Range, True Sight vs Stealth, +15% Crit, and applies Marked."
    ),
    VITRIOL_ALEMBIC(
        id = "VITRIOL_ALEMBIC",
        title = "Alchemist Vitriol Vials",
        shortTag = "Vitriol",
        icon = "🧪",
        baseCost = 180L,
        damageMult = 1.18f,
        fireRateMult = 1.08f,
        rangeMult = 1.0f,
        critChanceBonus = 0f,
        armorPierceBonus = 0.45f,
        extraBounces = 0,
        goldPerHit = 0L,
        bonusStatus = StatusEffect.CORRODED,
        summary = "Ignores 45% Armor and coats projectiles in armor-cracking Vitriol Acid."
    ),
    ROYAL_TAX_BANNER(
        id = "ROYAL_TAX_BANNER",
        title = "Gilded Tax Standard",
        shortTag = "TaxFlag",
        icon = "🪙",
        baseCost = 190L,
        damageMult = 1.12f,
        fireRateMult = 1.10f,
        rangeMult = 1.12f,
        critChanceBonus = 0.08f,
        armorPierceBonus = 0f,
        extraBounces = 0,
        goldPerHit = 2L,
        bonusStatus = null,
        summary = "Mints +2 Gold on every single hit (scales with Forge Tier!) +12% Dmg/Range."
    ),
    RICOCHET_SLING(
        id = "RICOCHET_SLING",
        title = "Chain-Shot Ricochet Rig",
        shortTag = "ChainShot",
        icon = "⛓️",
        baseCost = 210L,
        damageMult = 1.15f,
        fireRateMult = 1.05f,
        rangeMult = 1.08f,
        critChanceBonus = 0.06f,
        armorPierceBonus = 0.10f,
        extraBounces = 2,
        goldPerHit = 0L,
        bonusStatus = StatusEffect.BLEEDING,
        summary = "Every attack ricochets to +2 additional nearby enemies across road bends!"
    ),
    IMPERIAL_CROWN_SEAL(
        id = "IMPERIAL_CROWN_SEAL",
        title = "Sovereign Crown Seal",
        shortTag = "CrownSeal",
        icon = "👑",
        baseCost = 320L,
        damageMult = 1.38f,
        fireRateMult = 1.24f,
        rangeMult = 1.20f,
        critChanceBonus = 0.18f,
        armorPierceBonus = 0.25f,
        extraBounces = 1,
        goldPerHit = 1L,
        bonusStatus = StatusEffect.MARKED,
        summary = "Masterwork Seal: +38% Dmg, +24% Speed, +20% Range, +1 Bounce & +1G/hit!"
    ),
    BESSEMER_STEAM_TURBINE(
        id = "BESSEMER_STEAM_TURBINE",
        title = "Industrial Steam Turbine",
        shortTag = "Turbine",
        icon = "🚂",
        baseCost = 480L,
        damageMult = 1.45f,
        fireRateMult = 1.42f,
        rangeMult = 1.12f,
        critChanceBonus = 0.10f,
        armorPierceBonus = 0.20f,
        extraBounces = 0,
        goldPerHit = 2L,
        bonusStatus = StatusEffect.BURNING,
        summary = "Industrial Age dynamo: +45% Dmg, +42% Attack Speed, and +2G/hit."
    ),
    TESLA_CAPACITOR_BANK(
        id = "TESLA_CAPACITOR_BANK",
        title = "High-Voltage Arc Capacitor",
        shortTag = "ArcCap",
        icon = "⚡",
        baseCost = 560L,
        damageMult = 1.52f,
        fireRateMult = 1.28f,
        rangeMult = 1.18f,
        critChanceBonus = 0.16f,
        armorPierceBonus = 0.25f,
        extraBounces = 3,
        goldPerHit = 0L,
        bonusStatus = StatusEffect.SHOCKED,
        summary = "+52% Dmg, +3 Chain Arc Bounces, and inflicts Storm-Struck on every hit!"
    ),
    DEPLETED_URANIUM_SABOT(
        id = "DEPLETED_URANIUM_SABOT",
        title = "Atomic Sabot Core",
        shortTag = "AtomCore",
        icon = "☢️",
        baseCost = 820L,
        damageMult = 1.75f,
        fireRateMult = 1.20f,
        rangeMult = 1.25f,
        critChanceBonus = 0.22f,
        armorPierceBonus = 0.55f,
        extraBounces = 1,
        goldPerHit = 0L,
        bonusStatus = StatusEffect.CORRODED,
        summary = "Atomic Age core: +75% Dmg, ignores 55% Armor, and +22% Crit Chance."
    ),
    SILICON_RADAR_UPLINK(
        id = "SILICON_RADAR_UPLINK",
        title = "Phased-Array Radar Lock",
        shortTag = "Radar",
        icon = "📡",
        baseCost = 1_100L,
        damageMult = 1.65f,
        fireRateMult = 1.35f,
        rangeMult = 1.45f,
        critChanceBonus = 0.28f,
        armorPierceBonus = 0.35f,
        extraBounces = 1,
        goldPerHit = 3L,
        bonusStatus = StatusEffect.MARKED,
        summary = "Information Age telemetry: +45% Range, +65% Dmg, True Sight & +3G/hit."
    ),
    CYBER_NANITE_INJECTOR(
        id = "CYBER_NANITE_INJECTOR",
        title = "Cybernetic Nanite Swarm",
        shortTag = "Nanites",
        icon = "🧬",
        baseCost = 1_650L,
        damageMult = 1.95f,
        fireRateMult = 1.45f,
        rangeMult = 1.22f,
        critChanceBonus = 0.20f,
        armorPierceBonus = 0.50f,
        extraBounces = 2,
        goldPerHit = 4L,
        bonusStatus = StatusEffect.POISONED,
        summary = "Cybernetic Age module: +95% Dmg, +45% Speed, +2 Bounces & +4G/hit."
    ),
    FUSION_PLASMA_COIL(
        id = "FUSION_PLASMA_COIL",
        title = "Tokamak Plasma Injector",
        shortTag = "Plasma",
        icon = "☀️",
        baseCost = 2_400L,
        damageMult = 2.30f,
        fireRateMult = 1.50f,
        rangeMult = 1.30f,
        critChanceBonus = 0.25f,
        armorPierceBonus = 0.60f,
        extraBounces = 2,
        goldPerHit = 5L,
        bonusStatus = StatusEffect.BURNING,
        summary = "Fusion Age core: +130% Dmg, +50% Speed, 60% Armor Pierce & +5G/hit."
    ),
    GRAVITON_SINGULARITY_LENS(
        id = "GRAVITON_SINGULARITY_LENS",
        title = "Starfleet Graviton Lens",
        shortTag = "Graviton",
        icon = "🌀",
        baseCost = 3_800L,
        damageMult = 2.75f,
        fireRateMult = 1.55f,
        rangeMult = 1.40f,
        critChanceBonus = 0.30f,
        armorPierceBonus = 0.65f,
        extraBounces = 3,
        goldPerHit = 6L,
        bonusStatus = StatusEffect.TIME_ANCHORED,
        summary = "Starfleet Age lens: +175% Dmg, +40% Range, +3 Bounces & slows targets."
    ),
    DYSON_STELLAR_CONDUIT(
        id = "DYSON_STELLAR_CONDUIT",
        title = "Dyson Stellar Harvester",
        shortTag = "DysonCore",
        icon = "🪐",
        baseCost = 6_200L,
        damageMult = 3.40f,
        fireRateMult = 1.70f,
        rangeMult = 1.50f,
        critChanceBonus = 0.35f,
        armorPierceBonus = 0.75f,
        extraBounces = 4,
        goldPerHit = 12L,
        bonusStatus = StatusEffect.MARKED,
        summary = "Dyson Age relic: +240% Dmg, +70% Speed, +4 Bounces & +12G/hit!"
    ),
    QUANTUM_ZERO_POINT_CELL(
        id = "QUANTUM_ZERO_POINT_CELL",
        title = "Quantum Zero-Point Cell",
        shortTag = "ZeroPt",
        icon = "⚛️",
        baseCost = 10_500L,
        damageMult = 4.50f,
        fireRateMult = 1.90f,
        rangeMult = 1.60f,
        critChanceBonus = 0.42f,
        armorPierceBonus = 0.85f,
        extraBounces = 5,
        goldPerHit = 20L,
        bonusStatus = StatusEffect.SHOCKED,
        summary = "Quantum Age cell: +350% Dmg, +90% Speed, +5 Bounces & +20G/hit!"
    ),
    OMEGA_CHRONO_CROWN(
        id = "OMEGA_CHRONO_CROWN",
        title = "Omega Omniversal Crown",
        shortTag = "Omega",
        icon = "♾️",
        baseCost = 18_000L,
        damageMult = 6.50f,
        fireRateMult = 2.25f,
        rangeMult = 1.80f,
        critChanceBonus = 0.55f,
        armorPierceBonus = 1.00f,
        extraBounces = 6,
        goldPerHit = 45L,
        bonusStatus = StatusEffect.TIME_ANCHORED,
        summary = "Omega Age apex module: +550% Dmg, +125% Speed, 100% Pierce, +6 Bounces & +45G/hit!"
    );

    companion object {
        const val MAX_FORGE_TIER = 5
        val equippableList: List<TowerGearAttachment> = entries.filter { it != NONE }
    }
}

/**
 * LAYER: TOWER VETERAN RANKS (20 Combat Experience Ranks from Levy R0 to Omega Sovereign V20)
 */
data class VeteranRankSpec(
    val rank: Int,
    val title: String,
    val shortBadge: String,
    val minXp: Long,
    val damageBonusMult: Float,
    val fireRateBonusMult: Float,
    val rangeBonusMult: Float,
    val critBonus: Float
)

object TowerVeteranCatalog {
    val ranks: List<VeteranRankSpec> = listOf(
        VeteranRankSpec(0, "Levy Garrison", "R0", 0L, 1.00f, 1.00f, 1.00f, 0.00f),
        VeteranRankSpec(1, "Watchman Regular", "V1", 250L, 1.06f, 1.03f, 1.02f, 0.02f),
        VeteranRankSpec(2, "Seasoned Guard", "V2", 750L, 1.12f, 1.06f, 1.04f, 0.04f),
        VeteranRankSpec(3, "Veteran Man-at-Arms", "V3", 1_800L, 1.20f, 1.10f, 1.06f, 0.06f),
        VeteranRankSpec(4, "Elite Bastion Crew", "V4", 4_000L, 1.30f, 1.14f, 1.08f, 0.08f),
        VeteranRankSpec(5, "Master Siege Battery", "V5", 8_500L, 1.42f, 1.18f, 1.10f, 0.10f),
        VeteranRankSpec(6, "Grandmaster Keep", "V6", 18_000L, 1.56f, 1.23f, 1.13f, 0.12f),
        VeteranRankSpec(7, "Royal Champion", "V7", 36_000L, 1.72f, 1.28f, 1.16f, 0.15f),
        VeteranRankSpec(8, "Imperial Vanguard", "V8", 75_000L, 1.90f, 1.34f, 1.20f, 0.18f),
        VeteranRankSpec(9, "Legendary Citadel", "V9", 150_000L, 2.15f, 1.42f, 1.24f, 0.22f),
        VeteranRankSpec(10, "Immortal Sovereign", "V10", 300_000L, 2.50f, 1.50f, 1.30f, 0.28f),
        VeteranRankSpec(11, "Industrial Ironclad", "V11", 600_000L, 2.95f, 1.58f, 1.34f, 0.30f),
        VeteranRankSpec(12, "Dynamo Commander", "V12", 1_200_000L, 3.50f, 1.66f, 1.38f, 0.32f),
        VeteranRankSpec(13, "Atomic Sentinel", "V13", 2_500_000L, 4.20f, 1.75f, 1.42f, 0.35f),
        VeteranRankSpec(14, "Cybernetic Archon", "V14", 5_000_000L, 5.10f, 1.85f, 1.46f, 0.38f),
        VeteranRankSpec(15, "Fusion Paragon", "V15", 10_000_000L, 6.25f, 1.96f, 1.50f, 0.40f),
        VeteranRankSpec(16, "Starfleet Admiral", "V16", 22_000_000L, 7.75f, 2.08f, 1.55f, 0.43f),
        VeteranRankSpec(17, "Dyson Overseer", "V17", 50_000_000L, 9.60f, 2.22f, 1.60f, 0.46f),
        VeteranRankSpec(18, "Quantum Ascendant", "V18", 120_000_000L, 12.0f, 2.38f, 1.66f, 0.50f),
        VeteranRankSpec(19, "Dimensional Titan", "V19", 300_000_000L, 15.5f, 2.55f, 1.72f, 0.55f),
        VeteranRankSpec(20, "Omega Eternal", "V20", 800_000_000L, 20.0f, 2.80f, 1.80f, 0.60f)
    )

    fun rankForXp(xp: Long): VeteranRankSpec {
        var matched = ranks.first()
        for (spec in ranks) {
            if (xp >= spec.minXp) matched = spec else break
        }
        return matched
    }
}

/**
 * LAYER: TOWER ASCENSION / PRESTIGE STARS (★1 to ★25)
 * Once a tower reaches totalLevel >= 6 (or Tier 5+ on any path), the player can Ascend it
 * up to ★25 for exponential multi-era scaling!
 */
object TowerAscensionCatalog {
    const val MAX_STARS = 25

    fun ascensionCostGold(baseTowerCost: Long, currentStars: Int): Long {
        val nextStar = (currentStars + 1).coerceIn(1, MAX_STARS)
        val raw = baseTowerCost.toDouble() * 3.2 * (1.62.pow(nextStar - 1))
        return CurrencyMath.safeMultiply(1L, raw).coerceAtLeast(100L)
    }

    fun damageMultiplierForStars(stars: Int): Float {
        if (stars <= 0) return 1.0f
        return (1.42.pow(stars.coerceIn(0, MAX_STARS))).toFloat()
    }

    fun fireRateMultiplierForStars(stars: Int): Float {
        if (stars <= 0) return 1.0f
        return 1.0f + 0.11f * stars.coerceIn(0, MAX_STARS)
    }

    fun rangeMultiplierForStars(stars: Int): Float {
        if (stars <= 0) return 1.0f
        return 1.0f + 0.04f * stars.coerceIn(0, MAX_STARS)
    }
}

data class UpgradeTierSpec(
    val tier: Int,
    val name: String,
    val summary: String,
    val cost: Long
)

data class UpgradePathSpec(
    val pathIndex: Int, // 0 = Path A, 1 = Path B, 2 = Path C, 3 = Path D (Future & Cosmic Line!)
    val pathTitle: String,
    val pathTheme: String,
    val tiers: List<UpgradeTierSpec>
)

/**
 * Helper that appends Tiers 6, 7, and 8 (Industrial/Atomic -> Cyber/Starfleet -> Dyson/Omega)
 * to any 5-tier path so EVERY single upgrade line goes 3 tiers farther (8 Tiers per Path!),
 * and creates the 4th Future Overdrive Upgrade Line (Path D, 8 tiers) for every tower!
 */
private fun extendPathTo8Tiers(
    pathIndex: Int,
    pathTitle: String,
    pathTheme: String,
    baseFive: List<UpgradeTierSpec>,
    t6Name: String,
    t6Desc: String,
    t7Name: String,
    t7Desc: String,
    t8Name: String,
    t8Desc: String
): UpgradePathSpec {
    val t5Cost = baseFive.lastOrNull()?.cost ?: 1500L
    val t6Cost = (t5Cost * 2.2).toLong()
    val t7Cost = (t5Cost * 5.0).toLong()
    val t8Cost = (t5Cost * 12.0).toLong()
    return UpgradePathSpec(
        pathIndex = pathIndex,
        pathTitle = pathTitle,
        pathTheme = pathTheme,
        tiers = baseFive + listOf(
            UpgradeTierSpec(6, t6Name, t6Desc, t6Cost),
            UpgradeTierSpec(7, t7Name, t7Desc, t7Cost),
            UpgradeTierSpec(8, t8Name, t8Desc, t8Cost)
        )
    )
}

private fun buildFuturePathD(
    towerShortName: String,
    baseCost: Long,
    theme: String
): UpgradePathSpec {
    val c1 = (baseCost * 1.15).toLong()
    return UpgradePathSpec(
        pathIndex = 3,
        pathTitle = "Path D: Era & Cosmic Overdrive",
        pathTheme = theme,
        tiers = listOf(
            UpgradeTierSpec(1, "Draftsman Blueprint", "+25% damage, +12% range & +15% Era Evolution bonus.", c1),
            UpgradeTierSpec(2, "Steel Gear-Train", "+28% attack speed and ignores +20% enemy armor.", c1 * 2),
            UpgradeTierSpec(3, "Steam Dynamo Core", "Every 4th attack unleashes an Era shockwave (+65% Dmg).", c1 * 4),
            UpgradeTierSpec(4, "Atomic Isotope Cell", "+80% damage, +20% crit chance, and +30% status duration.", c1 * 8),
            UpgradeTierSpec(5, "CYBERNETIC UPLINK", "Links with all towers in range for +35% speed & Smart Ricochet!", c1 * 17),
            UpgradeTierSpec(6, "STARFLEET ORBITAL SYNC", "Calls down orbital precision strikes every 5 shots (+150% Dmg)!", c1 * 38),
            UpgradeTierSpec(7, "DYSON STELLAR CONDUIT", "Channels stellar plasma: 2.5x damage and pierces all shields!", c1 * 85),
            UpgradeTierSpec(8, "OMEGA TRANSCENDENT $towerShortName", "Ultimate Tier-8 Apex: 4.5x Damage, +100% Speed & Reality Shatter!", c1 * 200)
        )
    )
}

enum class TowerType(
    val id: String,
    val displayName: String,
    val shortName: String,
    val category: TowerCategory,
    val baseCost: Long,
    val baseDamage: Float,
    val baseRange: Float,
    val baseFireRate: Float, // attacks per second
    val splashRadius: Float,
    val primaryColor: Long,
    val secondaryColor: Long,
    val roleSummary: String,
    val primaryStatus: StatusEffect?,
    val secondaryStatus: StatusEffect?,
    val doctrineSkillTitle: String = "Garrison Overdrive",
    val doctrineCooldownSec: Float = 9.0f,
    val isFusedApex: Boolean = false,
    val fusionRecipe: Pair<String, String>? = null,
    val paths: List<UpgradePathSpec>
) {
    GATLING(
        id = "GATLING",
        displayName = "Rock Watchtower",
        shortName = "Rock Tower",
        category = TowerCategory.KINETIC,
        baseCost = 100L,
        baseDamage = 20f,
        baseRange = 180f,
        baseFireRate = 3.8f,
        splashRadius = 0f,
        primaryColor = 0xFF8C8275,
        secondaryColor = 0xFFD4B483,
        roleSummary = "Rapid-fire watchtower. Evolves across 18 Ages from Rock Watchtower -> Crossbow -> Steam Gatling -> Phalanx CIWS -> Gauss Spire -> Omega Barrage Citadel.",
        primaryStatus = StatusEffect.BLEEDING,
        secondaryStatus = StatusEffect.CORRODED,
        doctrineSkillTitle = "Broadside Hyper-Volley",
        doctrineCooldownSec = 8.0f,
        paths = listOf(
            extendPathTo8Tiers(
                0, "Path A: Heavy Masonry & Sabot", "Armor Sunder & Bleed",
                listOf(
                    UpgradeTierSpec(1, "Flint Sling Stones", "Sharp stones inflict Bleeding on hit.", 80),
                    UpgradeTierSpec(2, "Iron-Tipped Quarrels", "+35% damage and ignores 30% armor.", 160),
                    UpgradeTierSpec(3, "Armor-Cracking Shot", "Cracks enemy armor (-50% armor) after 5 hits.", 320),
                    UpgradeTierSpec(4, "Culverin Volley", "Triggers 2.7x Siege Breach on cracked targets.", 650),
                    UpgradeTierSpec(5, "IMPERIAL CANNON KEEP", "Twin iron cannons shred 100% armor & 3x bleed crits!", 1400)
                ),
                "Bessemer Tungsten Shells", "+90% damage and permanent armor sunder on hit.",
                "Depleted-Uranium Chaingun", "2.4x damage and +35% bonus boss hull shredding.",
                "OMEGA SINGULARITY SHREDDER", "Tier 8 Apex: 4.0x damage, 100% True Pierce & instant Bleed rupture!"
            ),
            extendPathTo8Tiers(
                1, "Path B: Rapid Garrison & Rotary", "Fast Volley Speed",
                listOf(
                    UpgradeTierSpec(1, "Extra Lookouts", "+25% attack speed.", 75),
                    UpgradeTierSpec(2, "Repeating Winch", "Ramps fire rate up to +80% while firing.", 150),
                    UpgradeTierSpec(3, "Crossbow Battlements", "+50% base fire rate & +25% range.", 310),
                    UpgradeTierSpec(4, "Clockwork Loader", "Spin-up reaches +160% attack speed.", 620),
                    UpgradeTierSpec(5, "ROYAL GATLING BASTION", "Non-stop hail of iron shot down the road!", 1350)
                ),
                "Steam-Driven 6-Barrel", "Spin-up reaches +240% fire rate with zero cooldown.",
                "Cybernetic Phalanx CIWS", "3,000 RPM radar-locked stream intercepts all threats.",
                "OMEGA TACHYON GATLING", "Tier 8 Apex: Instantaneous relativistic hail across the valley!"
            ),
            extendPathTo8Tiers(
                2, "Path C: Skipping Stones & Chain", "Multi-Raider Ricochet",
                listOf(
                    UpgradeTierSpec(1, "Split Boulders", "Stones bounce to 1 nearby enemy.", 85),
                    UpgradeTierSpec(2, "Iron Shrapnel", "Bounces to 2 enemies with 85% damage.", 170),
                    UpgradeTierSpec(3, "Road Skipper", "Ricochets jump across parallel switchback lanes.", 340),
                    UpgradeTierSpec(4, "Chain Shot", "Bounces to 4 enemies and applies Bleeding to all.", 680),
                    UpgradeTierSpec(5, "FORTRESS VOLLEY KEEP", "Every shot chains through 7 enemies!", 1450)
                ),
                "Flak Shrapnel Matrix", "Ricochets to +9 enemies and bursts in a 45-unit flak cloud.",
                "Smart-Guided Gauss Flechettes", "Ricochets seek out highest-HP targets across 12 jumps.",
                "OMEGA OMNI-RICOCHET", "Tier 8 Apex: Every round chains through 16 targets for 120% damage per jump!"
            ),
            buildFuturePathD("GATLING", 100L, "Era Evolution & Orbital Synchrony")
        )
    ),

    SNIPER(
        id = "SNIPER",
        displayName = "Wooden Ballista",
        shortName = "Ballista",
        category = TowerCategory.KINETIC,
        baseCost = 140L,
        baseDamage = 135f,
        baseRange = 360f,
        baseFireRate = 0.75f,
        splashRadius = 0f,
        primaryColor = 0xFF9C6B3B,
        secondaryColor = 0xFFD9C29C,
        roleSummary = "Long-range precision marksman. Evolves from Wooden Ballista -> Longbow -> Arbalest -> Rail Rifle -> Gauss Assassin -> Omega Horizon Piercer.",
        primaryStatus = StatusEffect.MARKED,
        secondaryStatus = StatusEffect.BLEEDING,
        doctrineSkillTitle = "King's Skewer Lance",
        doctrineCooldownSec = 9.5f,
        paths = listOf(
            extendPathTo8Tiers(
                0, "Path A: Siege Arbalest & Rail", "Long Range & Boss Piercing",
                listOf(
                    UpgradeTierSpec(1, "Bodkin Bolts", "+45% base damage and ignores 50% armor.", 125),
                    UpgradeTierSpec(2, "High Watchtower", "+120 range (covers most of the meadow!).", 250),
                    UpgradeTierSpec(3, "Skewer Bolt", "Bolts pierce through 5 enemies in a line.", 490),
                    UpgradeTierSpec(4, "Shatter Lance", "Triggers 3.5x Shatter on Frozen & +100% Boss damage.", 980),
                    UpgradeTierSpec(5, "ROYAL CULVERIN CITADEL", "Map-wide cannon lance pierces all foes for 4x crit!", 1950)
                ),
                "Supersonic Sabot Barrel", "+160 range and +120% critical strike multiplier.",
                "Orbital Magnetic Coilgun", "Pierces entire road columns and deals +250% Boss damage.",
                "OMEGA HORIZON LANCE", "Tier 8 Apex: Full-map relativistic beam pierces every enemy on screen!"
            ),
            extendPathTo8Tiers(
                1, "Path B: Watchman's Mark & Execute", "Crit Support & Execute",
                listOf(
                    UpgradeTierSpec(1, "Scout Banner", "Applies Marked (+50% crit damage from all towers).", 120),
                    UpgradeTierSpec(2, "Torchlight Lookout", "Reveals Stealth raiders and +30% crit chance.", 240),
                    UpgradeTierSpec(3, "Finishing Bolt", "Instantly executes wounded raiders below 18% HP.", 480),
                    UpgradeTierSpec(4, "King's Bounty", "Marked enemies drop +50% bonus Gold on death.", 950),
                    UpgradeTierSpec(5, "LORD EXECUTIONER", "Executes bosses below 12% HP & marks whole waves!", 1900)
                ),
                "Satellite Target Designator", "Marked targets take +90% crit damage and drop 2x Gold.",
                "Quantum Weakness Scanner", "Executes non-bosses below 28% HP and bosses below 18% HP.",
                "OMEGA REAPER PROTOCOL", "Tier 8 Apex: Executes any enemy below 35% HP (Bosses 22%) with 3x Gold!"
            ),
            extendPathTo8Tiers(
                2, "Path C: Repeating Longbows", "Rapid Arrow Volley",
                listOf(
                    UpgradeTierSpec(1, "Double Windlass", "+40% attack speed.", 115),
                    UpgradeTierSpec(2, "Splinter Bolts", "Bolts burst into wood & iron splinters (55 splash).", 235),
                    UpgradeTierSpec(3, "Twin Bow Arms", "Fires 2 rapid bolts per cycle and causes Bleeding.", 470),
                    UpgradeTierSpec(4, "Ricochet Broadheads", "Critical hits bounce to 3 high-HP targets.", 940),
                    UpgradeTierSpec(5, "IMPERIAL RIFLE CORPS", "Fires heavy armor-piercing rounds at rapid speed!", 1920)
                ),
                "Semi-Auto Gauss Magazine", "Doubles fire rate and bounces to 5 high-HP targets.",
                "Hypersonic Burst Volleys", "Fires 4-round rail bursts that trigger Siege Breach.",
                "OMEGA PHANTOM BARRAGE", "Tier 8 Apex: Continuous 6-bolt critical volleys across the map!"
            ),
            buildFuturePathD("SNIPER", 140L, "Telemetry Lock & Antimatter Rounds")
        )
    ),

    MORTAR(
        id = "MORTAR",
        displayName = "Stone Catapult",
        shortName = "Catapult",
        category = TowerCategory.KINETIC,
        baseCost = 155L,
        baseDamage = 92f,
        baseRange = 265f,
        baseFireRate = 0.82f,
        splashRadius = 98f,
        primaryColor = 0xFF7A6855,
        secondaryColor = 0xFFB8AFA6,
        roleSummary = "Heavy area siege artillery. Evolves from Stone Catapult -> Trebuchet -> Railway Bertha -> Cruise Missile Silo -> Antimatter Bombard -> Omega Supernova.",
        primaryStatus = StatusEffect.BLEEDING,
        secondaryStatus = StatusEffect.CORRODED,
        doctrineSkillTitle = "Grand Trebuchet Barrage",
        doctrineCooldownSec = 10.0f,
        paths = listOf(
            extendPathTo8Tiers(
                0, "Path A: Counterweight Trebuchet", "Knockback & Stun",
                listOf(
                    UpgradeTierSpec(1, "Quarried Boulders", "+35% damage and +25% crush radius.", 120),
                    UpgradeTierSpec(2, "Rolling Impact", "Knocks enemies backward 18 units along the road.", 245),
                    UpgradeTierSpec(3, "Earth Tremor", "Stuns hit enemies for 0.8s and cracks armor.", 485),
                    UpgradeTierSpec(4, "Double Counterweight", "Each boulder creates 2 secondary shockwaves.", 960),
                    UpgradeTierSpec(5, "GRAND WAR TREBUCHET", "Colossal boulders knock whole waves back and stun!", 1900)
                ),
                "Railway Siege Bertha", "+60% blast radius and knocks enemies back 35 units.",
                "Tectonic Kinetic Driver", "Stuns all hit enemies for 1.6s and cracks 80% armor.",
                "OMEGA SUPERNOVA TREBUCHET", "Tier 8 Apex: Screen-shaking cataclysm knocks bosses back & deals 4.5x dmg!"
            ),
            extendPathTo8Tiers(
                1, "Path B: Scatter Grapeshot & MIRV", "Wide Area Bombardment",
                listOf(
                    UpgradeTierSpec(1, "Winch Crew", "+30% fire rate.", 115),
                    UpgradeTierSpec(2, "Rubble Basket", "Splits into 4 stone clusters on impact.", 230),
                    UpgradeTierSpec(3, "Caltrop Scatter", "Leaves iron caltrops on the road after impact.", 460),
                    UpgradeTierSpec(4, "Bombard Battery", "Splits into 8 shells covering two road bends.", 920),
                    UpgradeTierSpec(5, "IMPERIAL SIEGE HOWITZER", "Non-stop artillery rain across the meadow!", 1880)
                ),
                "Rocket-Assisted Cluster Pod", "Fires 12 sub-munitions across 3 parallel road bends.",
                "MIRV Thermobaric Silo", "Carpet-bombs the entire switchback sector every volley.",
                "OMEGA ORBITAL CARPET-BOMB", "Tier 8 Apex: Continuous 20-warhead MIRV saturation across the map!"
            ),
            extendPathTo8Tiers(
                2, "Path C: Shatter Bombard", "Combo Detonator",
                listOf(
                    UpgradeTierSpec(1, "Spiked Boulders", "Applies Cracked Armor and Bleeding in the blast.", 120),
                    UpgradeTierSpec(2, "Ice-Breaker Stone", "Shatter combo on Chilled/Frozen deals +75% more!", 240),
                    UpgradeTierSpec(3, "Formation Crusher", "Deals 3x damage to Slowed or Clumped targets.", 480),
                    UpgradeTierSpec(4, "Castle Buster", "+100% damage to Shielded and Boss enemies.", 950),
                    UpgradeTierSpec(5, "ROYAL MONOLITH CANNON", "4.5x Shatter across a massive blast zone!", 1950)
                ),
                "Atomic Bunker-Buster", "Deals 6.0x Shatter damage and vaporizes enemy shields.",
                "Antimatter Implosion Shell", "Pulls raiders inward before detonating for 3.5x splash.",
                "OMEGA SINGULARITY MORTAR", "Tier 8 Apex: 8.0x Shatter & Graviton Crush across a 220-unit radius!"
            ),
            buildFuturePathD("MORTAR", 155L, "Nuclear Payloads & Orbital Kinetic Pods")
        )
    ),

    HYDRO(
        id = "HYDRO",
        displayName = "Aqueduct Waterwheel",
        shortName = "Water Mill",
        category = TowerCategory.ELEMENTAL,
        baseCost = 130L,
        baseDamage = 24f,
        baseRange = 180f,
        baseFireRate = 1.8f,
        splashRadius = 78f,
        primaryColor = 0xFF4A829E,
        secondaryColor = 0xFFA5C9DC,
        roleSummary = "Hydro-kinetic flood tower. Evolves from Water Mill -> Aqueduct -> Steam Turbine -> Heavy-Water Reactor -> Superfluid Vortex -> Omega Abyssal Deluge.",
        primaryStatus = StatusEffect.WET,
        secondaryStatus = StatusEffect.CORRODED,
        doctrineSkillTitle = "Floodgate Surge",
        doctrineCooldownSec = 9.0f,
        paths = listOf(
            extendPathTo8Tiers(
                0, "Path A: Floodgate Sluice", "Knockback & Mass Soak",
                listOf(
                    UpgradeTierSpec(1, "Open Sluice", "Drenches targets for 6s and pushes back slightly.", 95),
                    UpgradeTierSpec(2, "River Surge", "Pushes hit enemies 16 units backward along the trail.", 195),
                    UpgradeTierSpec(3, "Aqueduct Spillway", "Sends a rushing wave across parallel road bends.", 390),
                    UpgradeTierSpec(4, "Great Deluge", "Doubles Storm Conduct & Flash-Freeze combo damage!", 780),
                    UpgradeTierSpec(5, "ROYAL DAM FLOODGATE", "Giant river surges sweep enemies back up the road!", 1600)
                ),
                "Hydro-Electric Spillway", "Surges push enemies back 32 units and generate +5G/surge.",
                "Tsunami Pressure Cannon", "Triples Storm Conduct & Flash-Freeze combo damage!",
                "OMEGA ABYSSAL TSUNAMI", "Tier 8 Apex: Sweeps entire waves backward & amplifies combos by 4x!"
            ),
            extendPathTo8Tiers(
                1, "Path B: Millpond Eddy", "Raider Clumping",
                listOf(
                    UpgradeTierSpec(1, "Muddy Trench", "Slows drenched enemies by 30%.", 90),
                    UpgradeTierSpec(2, "Whirlpool Basin", "Pulls nearby raiders together into a tight clump.", 185),
                    UpgradeTierSpec(3, "Scouring Current", "Reveals Stealth raiders and strips shields.", 375),
                    UpgradeTierSpec(4, "Drowning Pool", "Clumped enemies take +45% catapult splash damage.", 750),
                    UpgradeTierSpec(5, "MAELSTROM WEIR", "Giant whirlpool locks whole waves in place!", 1580)
                ),
                "Cavitation Vortex Pump", "Slows drenched foes by 60% and shreds 50% armor.",
                "Magneto-Fluidic Whirlpool", "Locks enemies in a 140-unit vortex taking +90% splash damage.",
                "OMEGA SUPERFLUID GYRE", "Tier 8 Apex: Permanent inescapable vortex across 3 road bends!"
            ),
            extendPathTo8Tiers(
                2, "Path C: Saltwater Brine", "Armor Rust & Combo Primer",
                listOf(
                    UpgradeTierSpec(1, "Sea Salt Spray", "Rushes rust enemy armor (-50% armor).", 100),
                    UpgradeTierSpec(2, "Mineral Conduct", "Storm Spire lightning jumps +3 extra times.", 200),
                    UpgradeTierSpec(3, "Scalding Steam", "+55% damage and triggers steam bursts on Burning foes.", 400),
                    UpgradeTierSpec(4, "Bitter Brine", "Adds Poison stacks to every water surge.", 800),
                    UpgradeTierSpec(5, "IMPERIAL AQUEDUCT", "Drenches, rusts armor, and slows whole columns!", 1650)
                ),
                "Deuterium Heavy-Water Jet", "Applies Wet + Corroded + Poisoned simultaneously on hit.",
                "Ionized Electrolytic Brine", "All lightning & cold attacks on target deal +150% damage.",
                "OMEGA PRIMORDIAL OCEAN", "Tier 8 Apex: Drenches the entire sector & strips 100% armor/shields!"
            ),
            buildFuturePathD("HYDRO", 130L, "Heavy-Water Deuterium & Cavitation")
        )
    ),

    CRYO(
        id = "CRYO",
        displayName = "Frost Stone Keep",
        shortName = "Ice Keep",
        category = TowerCategory.ELEMENTAL,
        baseCost = 125L,
        baseDamage = 22f,
        baseRange = 170f,
        baseFireRate = 1.6f,
        splashRadius = 68f,
        primaryColor = 0xFF6B9AB0,
        secondaryColor = 0xFFD4E9F2,
        roleSummary = "Cryogenic stasis fortress. Evolves from Frost Stone Keep -> Permafrost Bastion -> Liquid-Nitrogen Tower -> Zero-Kelvin Emitter -> Omega Absolute-Zero.",
        primaryStatus = StatusEffect.CHILLED,
        secondaryStatus = StatusEffect.FROZEN,
        doctrineSkillTitle = "Blizzard Frost Ring",
        doctrineCooldownSec = 8.5f,
        paths = listOf(
            extendPathTo8Tiers(
                0, "Path A: Deep Winter & Stasis", "Solid Freeze & Brittle Armor",
                listOf(
                    UpgradeTierSpec(1, "Bitter Rime", "Increases Chill slow to 45%.", 95),
                    UpgradeTierSpec(2, "Ice Encasement", "Every 3rd hit freezes enemies solid (Frozen).", 190),
                    UpgradeTierSpec(3, "Brittle Iron", "Frozen enemies take +40% Catapult/Ballista Shatter dmg.", 380),
                    UpgradeTierSpec(4, "Lord of Winter", "Can freeze Bosses and extends freeze by +75%.", 750),
                    UpgradeTierSpec(5, "CITADEL OF PERMAFROST", "Freezes whole bends & shatters foes below 20% HP!", 1600)
                ),
                "Liquid-Helium Cryo-Core", "Every 2nd hit freezes solid; Shatter deals +100% damage.",
                "Zero-Kelvin Molecular Lock", "Freezes bosses for 3.0s and shatters non-bosses below 28% HP.",
                "OMEGA ABSOLUTE-ZERO KEEP", "Tier 8 Apex: Instant solid freeze on hit & 6x Shatter vulnerability!"
            ),
            extendPathTo8Tiers(
                1, "Path B: Snowstorm Watch", "Wide Meadow Slow Aura",
                listOf(
                    UpgradeTierSpec(1, "High Parapet", "+30% range and +25% frost radius.", 90),
                    UpgradeTierSpec(2, "Winter Gale", "Passively chills all enemies within range.", 185),
                    UpgradeTierSpec(3, "Blinding Snow", "Reveals Stealth and slows fast/flying enemies by 60%.", 360),
                    UpgradeTierSpec(4, "Frost Nova Ring", "Emits a freezing ring every 4s across adjacent roads.", 720),
                    UpgradeTierSpec(5, "GREAT NORTHERN BLIZZARD", "Continuous snowstorm slows 3 road passes!", 1500)
                ),
                "Stratospheric Polar Vortex", "Aura covers +120 range and slows all enemies by 70%.",
                "Bose-Einstein Stasis Field", "Emits a solid-freeze pulse every 2.5 seconds across 4 bends.",
                "OMEGA ENTROPY ARRESTOR", "Tier 8 Apex: Halts thermal motion across the entire central spiral!"
            ),
            extendPathTo8Tiers(
                2, "Path C: Icicle Ballista", "Heavy Ice Lances",
                listOf(
                    UpgradeTierSpec(1, "Ice Spears", "+40% attack speed and +30% direct damage.", 95),
                    UpgradeTierSpec(2, "River Freeze", "Deals 2.5x damage to Drenched or Chilled targets.", 190),
                    UpgradeTierSpec(3, "Glacier Bolt", "Fires piercing ice bolts through 4 enemies.", 370),
                    UpgradeTierSpec(4, "Triple Ice Volley", "Launches 3 ice lances per shot with high crit.", 740),
                    UpgradeTierSpec(5, "WINTERGREAT BALLISTA", "Heavy ice lances trigger instant Shatter!", 1550)
                ),
                "Cryo-Rail Sabot Lances", "Fires 5 piercing cryo-lances that self-detonate Shatter.",
                "Orbital Comet Shards", "3.5x damage to Chilled/Frozen targets with 100% crit chance.",
                "OMEGA GLACIER RAIL-CITADEL", "Tier 8 Apex: Launches hyper-velocity ice comets that shatter whole waves!"
            ),
            buildFuturePathD("CRYO", 125L, "Zero-Kelvin Stasis & Cryo-Laser Cooling")
        )
    ),

    HUB(
        id = "HUB",
        displayName = "Royal Mint & Market",
        shortName = "Royal Mint",
        category = TowerCategory.SUPPORT,
        baseCost = 180L,
        baseDamage = 20f,
        baseRange = 220f,
        baseFireRate = 1.2f,
        splashRadius = 60f,
        primaryColor = 0xFFC59B27,
        secondaryColor = 0xFFF2DF9D,
        roleSummary = "Economic treasury & guild command. Evolves from Village Market -> Royal Mint -> Stock Exchange -> Crypto-Mint -> Matter Replicator -> Omega Cornucopia.",
        primaryStatus = StatusEffect.MARKED,
        secondaryStatus = StatusEffect.SHOCKED,
        doctrineSkillTitle = "Royal Caravan Dividend",
        doctrineCooldownSec = 10.0f,
        paths = listOf(
            extendPathTo8Tiers(
                0, "Path A: King's Coinage Mint", "Continuous Gold & Bank Yield",
                listOf(
                    UpgradeTierSpec(1, "Gold Foundry", "Generates +35 Gold every wave + 8 Gold/sec in combat.", 150),
                    UpgradeTierSpec(2, "Tax Collector", "Towers in range earn +30% Gold per enemy kill.", 300),
                    UpgradeTierSpec(3, "Royal Charter", "Generates +90 Gold/wave and +22 Gold/sec.", 600),
                    UpgradeTierSpec(4, "Crown Dividend", "Boosts damage of all towers by +1% per 1,000 Gold held.", 1200),
                    UpgradeTierSpec(5, "IMPERIAL EXCHEQUER", "+250 Gold/wave, +65 Gold/sec & +50% kill bounty!", 2400)
                ),
                "Industrial Reserve Bullion", "+800 Gold/wave, +180 Gold/sec & +3% Bank Wave APY.",
                "Quantum Matter Synthesizer", "+3,000 Gold/wave, +600 Gold/sec & +100% kill bounty.",
                "OMEGA INFINITE CORNUCOPIA", "Tier 8 Apex: +15,000 Gold/wave, +2,500 Gold/sec & +8% Bank APY!"
            ),
            extendPathTo8Tiers(
                1, "Path B: Watchfire Signal Tower", "Range, Crit & True Sight Aura",
                listOf(
                    UpgradeTierSpec(1, "Signal Brazier", "Grants True Sight vs Stealth and +18% range to nearby towers.", 140),
                    UpgradeTierSpec(2, "War Horns", "Grants +20% Critical Chance and +35% Crit Damage.", 280),
                    UpgradeTierSpec(3, "Scout Flare", "Fires flares that apply Marked to bosses.", 560),
                    UpgradeTierSpec(4, "Grand Garrison", "Aura range covers +100 extra units across road bends.", 1100),
                    UpgradeTierSpec(5, "SUPREME COMMAND KEEP", "Map-wide True Sight + 35% Range/Crit to all!", 2250)
                ),
                "AWACS Radar Command Hub", "Grants +50% Range and +60% Crit Damage to all nearby towers.",
                "Starfleet Fleet Tactical Net", "Grants +75% Damage and +40% Fire Rate to all towers in range.",
                "OMEGA OMNISCIENT CITADEL", "Tier 8 Apex: Map-wide +100% Damage, +50% Speed & +50% Crit!"
            ),
            extendPathTo8Tiers(
                2, "Path C: Alchemists' Guildhall", "Combo & Wonder Discount",
                listOf(
                    UpgradeTierSpec(1, "Guild Supply Carts", "Boosts all Combo reaction damage in range by +30%.", 145),
                    UpgradeTierSpec(2, "Refined Reagents", "All status effects applied in range last +50% longer.", 290),
                    UpgradeTierSpec(3, "Imperial Accord", "Unlocks Guild Accord synergy with Sundial (+35% spd/gold).", 580),
                    UpgradeTierSpec(4, "Master Masons", "Reduces cost of Wonder Fusions and Road Traps by 25%.", 1150),
                    UpgradeTierSpec(5, "GRAND GUILD CATHEDRAL", "Doubles ALL Combo Reaction damage on the map!", 2350)
                ),
                "Industrial Patent Office", "Reduces all upgrade & Ascension Star costs by 20%.",
                "Dyson Energy Subsidy", "Triples all Combo Reaction damage and doubles XP gain.",
                "OMEGA ARCHITECT NEXUS", "Tier 8 Apex: 4x Combo Reaction damage & -35% all upgrade costs!"
            ),
            buildFuturePathD("MINT", 180L, "High-Frequency Compound Yield & Matter Replication")
        )
    ),

    PYRO(
        id = "PYRO",
        displayName = "Boiling Pitch Keep",
        shortName = "Fire Pitch",
        category = TowerCategory.ELEMENTAL,
        baseCost = 135L,
        baseDamage = 32f,
        baseRange = 165f,
        baseFireRate = 1.9f,
        splashRadius = 72f,
        primaryColor = 0xFFC85A28,
        secondaryColor = 0xFFF0B265,
        roleSummary = "Thermal & plasma incinerator. Evolves from Boiling Pitch -> Greek Fire -> Blast Furnace -> Thermonuclear Core -> Solar Corona Torch -> Omega Star-Forge.",
        primaryStatus = StatusEffect.BURNING,
        secondaryStatus = StatusEffect.CORRODED,
        doctrineSkillTitle = "Greek Fire Eruption",
        doctrineCooldownSec = 8.5f,
        paths = listOf(
            extendPathTo8Tiers(
                0, "Path A: Greek Fire Siphon", "Persistent Road Burn",
                listOf(
                    UpgradeTierSpec(1, "Resin Pitch", "Burning lasts +4s and deals +40% DoT.", 100),
                    UpgradeTierSpec(2, "Blazing Trench", "Ignites road stones, burning all passing raiders.", 210),
                    UpgradeTierSpec(3, "Tar Ignitor", "Doubles Greek Fire Ignition damage on Tar-Slicked foes.", 410),
                    UpgradeTierSpec(4, "Alchemical Naphtha", "Fire melts armor (-50% armor) and stacks 5 times.", 820),
                    UpgradeTierSpec(5, "DRAGONFIRE BASTION", "Turns the entire road bend into a furnace!", 1650)
                ),
                "White-Phosphorus Napalm", "Burning stacks up to 12x and never extinguishes.",
                "Tokamak Plasma Inferno", "Deals 3.5x Greek Fire damage and melts 85% enemy armor.",
                "OMEGA PRIMORDIAL STAR-FORGE", "Tier 8 Apex: Bathes the road in stellar plasma dealing 5x DoT!"
            ),
            extendPathTo8Tiers(
                1, "Path B: Forge Blast Furnace", "Siege Boss Melter",
                listOf(
                    UpgradeTierSpec(1, "Bellows Draft", "+30% range and +35% direct fire damage.", 105),
                    UpgradeTierSpec(2, "Smelting Heat", "Deals +4% max HP bonus burn to Bosses.", 215),
                    UpgradeTierSpec(3, "Thermal Crack", "Triggers 3.2x Thermal Shock on Frozen without thawing!", 420),
                    UpgradeTierSpec(4, "Twin Siphons", "+65% attack speed and pierces shields.", 840),
                    UpgradeTierSpec(5, "IMPERIAL SUN FORGE", "Continuous flame stream melts bosses and shields!", 1700)
                ),
                "Acetylene Plasma Torch", "Deals +8% max HP burn per second to Bosses and Juggernauts.",
                "Stellar Corona Lance", "Triggers 5.0x Thermal Shock and ignores 100% shields.",
                "OMEGA QUARK-GLUON TORCH", "Tier 8 Apex: Melts bosses for 14% Max HP/sec + 4x direct damage!"
            ),
            extendPathTo8Tiers(
                2, "Path C: Gunpowder Firepots", "Explosive Detonation",
                listOf(
                    UpgradeTierSpec(1, "Clay Firepots", "+35% splash radius.", 95),
                    UpgradeTierSpec(2, "Blackpowder Casks", "Burning enemies explode when slain.", 200),
                    UpgradeTierSpec(3, "Fuse Detonator", "Every 4th shot detonates all Burn & Poison stacks.", 400),
                    UpgradeTierSpec(4, "Mortar Fireballs", "Launches 3 cluster firepots across road bends.", 800),
                    UpgradeTierSpec(5, "GRAND BOMBARD FURNACE", "Huge gunpowder barrels trigger chain explosions!", 1680)
                ),
                "Thermobaric Fuel-Air Bomb", "Every 2nd shot detonates all status stacks in a 150-unit blast.",
                "Antimatter Firestorm Casks", "Slain enemies detonate for 40% of their Max HP in splash.",
                "OMEGA SUPERNOVA INCINERATOR", "Tier 8 Apex: Chain-detonates entire waves in thermonuclear fire!"
            ),
            buildFuturePathD("PYRO", 135L, "Magnetic Plasma Containment & Fusion Ignition")
        )
    ),

    VENOM(
        id = "VENOM",
        displayName = "Apothecary Tower",
        shortName = "Apothecary",
        category = TowerCategory.ELEMENTAL,
        baseCost = 140L,
        baseDamage = 26f,
        baseRange = 175f,
        baseFireRate = 1.7f,
        splashRadius = 75f,
        primaryColor = 0xFF688E41,
        secondaryColor = 0xFFC2D69B,
        roleSummary = "Alchemical & nanite dissolver. Evolves from Apothecary -> Vitriol Lab -> Chemical Distillery -> Bio-Viral Vat -> Nanite Goo -> Omega Oblivion Crucible.",
        primaryStatus = StatusEffect.POISONED,
        secondaryStatus = StatusEffect.OILED,
        doctrineSkillTitle = "Plague Cauldron Cloud",
        doctrineCooldownSec = 9.0f,
        paths = listOf(
            extendPathTo8Tiers(
                0, "Path A: Vitriol Acid Flasks", "Armor Sunder & Vulnerability",
                listOf(
                    UpgradeTierSpec(1, "Acid Splash", "Cracks armor (-50% armor, +35% dmg taken).", 105),
                    UpgradeTierSpec(2, "Cauldron Brew", "+40% splash area and +35% acid damage.", 210),
                    UpgradeTierSpec(3, "Shield Eater", "Acid dissolves shields and stops enemy healing.", 420),
                    UpgradeTierSpec(4, "Alchemical Catalyst", "Increases all Combo reactions on target by +50%.", 840),
                    UpgradeTierSpec(5, "GRAND ALCHEMIST LAB", "Dissolves 100% armor & doubles all tower damage!", 1700)
                ),
                "Fluoroantimonic Super-Acid", "Targets take +125% damage from all towers on the map.",
                "Molecular Disassembler Vat", "Instantly strips 100% armor/shields and deals +180% acid damage.",
                "OMEGA OBLIVION CRUCIBLE", "Tier 8 Apex: Triples all incoming damage on hit enemies!"
            ),
            extendPathTo8Tiers(
                1, "Path B: Boiling Tar Pits", "Fire & Storm Primer",
                listOf(
                    UpgradeTierSpec(1, "Pitch Coating", "Coats targets in Tar Oil (-25% speed).", 100),
                    UpgradeTierSpec(2, "Deep Tar Bog", "Tar slow increases to 45% across the splash zone.", 200),
                    UpgradeTierSpec(3, "Lamp Oil Mix", "Greek Fire Ignition with Boiling Pitch deals +80% damage.", 400),
                    UpgradeTierSpec(4, "Mineral Pitch", "Tar also triggers Wet combos with Storm & Ice Keeps!", 800),
                    UpgradeTierSpec(5, "ROYAL TAR FOUNDRY", "Coats enemies in Tar + Wet + Cracked Armor at once!", 1650)
                ),
                "Petrochemical Napalm Gel", "Slows by 65% and triples Greek Fire & Storm Conduct damage.",
                "Conductive Ferro-Fluid Bog", "Primes enemies for ALL elemental combos simultaneously!",
                "OMEGA CATALYST MIASMA", "Tier 8 Apex: Every hit triggers automatic Alchemical Blast + Greek Fire!"
            ),
            extendPathTo8Tiers(
                2, "Path C: Plague Doctor & Nanites", "Contagious Herbal Toxin",
                listOf(
                    UpgradeTierSpec(1, "Nightshade Brew", "Poison stacks up to 10x and deals +45% DoT.", 110),
                    UpgradeTierSpec(2, "Contagion Cloud", "Poisoned enemies spread toxin to nearby foes on death.", 220),
                    UpgradeTierSpec(3, "Volatile Vapors", "Triggers Alchemical Blast when hit by Fire or Lightning.", 430),
                    UpgradeTierSpec(4, "Blight Mist", "Leaves lingering poison fog along road corners.", 860),
                    UpgradeTierSpec(5, "ARCH-APOTHECARY KEEP", "Spreading blight weakens and clears entire waves!", 1780)
                ),
                "Bio-Engineered Phage Swarm", "Poison stacks up to 25x and reduces enemy damage/speed by 50%.",
                "Self-Replicating Grey-Goo", "Nanite plague spreads across the entire road on every kill.",
                "OMEGA ENTROPIC PLAGUE", "Tier 8 Apex: Exponentially stacking nanite virus dissolves whole waves!"
            ),
            buildFuturePathD("VENOM", 140L, "Grey-Goo Nanite Disassemblers & Super-Acids")
        )
    ),

    DRONE(
        id = "DRONE",
        displayName = "Falconry & Glider Roost",
        shortName = "Falcon Roost",
        category = TowerCategory.ARCANE_TECH,
        baseCost = 175L,
        baseDamage = 28f,
        baseRange = 280f,
        baseFireRate = 2.8f,
        splashRadius = 45f,
        primaryColor = 0xFF5A7D6C,
        secondaryColor = 0xFFD9C7A3,
        roleSummary = "Aerial squadron command. Evolves from Falconry Roost -> Ornithopter -> Dirigible -> Jet Hangar -> UAV Swarm -> Starfleet Carrier -> Omega Armada.",
        primaryStatus = StatusEffect.MARKED,
        secondaryStatus = StatusEffect.SHOCKED,
        doctrineSkillTitle = "War-Glider Bombing Sortie",
        doctrineCooldownSec = 9.0f,
        paths = listOf(
            extendPathTo8Tiers(
                0, "Path A: Royal War Falcons & Carriers", "Multi-Flock Harriers",
                listOf(
                    UpgradeTierSpec(1, "Twin Perches", "Launches +1 extra circling war falcon.", 130),
                    UpgradeTierSpec(2, "Steel Talons", "+40% attack rate and marks targets for crits.", 260),
                    UpgradeTierSpec(3, "Master Falconer", "Deploys 4 war falcons with 350 patrol range.", 510),
                    UpgradeTierSpec(4, "Fire-Arrow Gliders", "Clockwork gliders drop incendiary bolts.", 1020),
                    UpgradeTierSpec(5, "IMPERIAL AIRSHIP HANGAR", "6 armed airships patrol the entire valley!", 2000)
                ),
                "Supersonic Strike Squadron", "Deploys 8 jet interceptors firing armor-piercing missiles.",
                "Starfleet Orbital Carrier", "12 autonomous star-fighters patrol the entire map.",
                "OMEGA SERAPHIM ARMADA", "Tier 8 Apex: 16 dimensional gunships obliterate targets map-wide!"
            ),
            extendPathTo8Tiers(
                1, "Path B: Scout Hawks & Recon Net", "Gold Spoils & Tower Buff",
                listOf(
                    UpgradeTierSpec(1, "Coin Scavengers", "Generates +35% bonus Gold on every kill in range.", 125),
                    UpgradeTierSpec(2, "Watchman Hawks", "Grants True Sight and +20% range to nearby towers.", 250),
                    UpgradeTierSpec(3, "Supply Drop", "Boosts nearby tower damage by +25%.", 490),
                    UpgradeTierSpec(4, "Trade Caravans", "Generates Gold every second while enemies are in range.", 980),
                    UpgradeTierSpec(5, "ROYAL AVIARY GUILD", "Continuous gold harvest + 45% buff to nearby towers!", 1950)
                ),
                "Airborne AWACS Telemetry", "Grants +65% damage and +35% range to all nearby towers.",
                "Orbital Logistics Drop-Pods", "Mints +120 Gold/sec and doubles kill bounties in range.",
                "OMEGA VALKYRIE COMMAND", "Tier 8 Apex: +110% Damage/Speed aura & +500 Gold/sec!"
            ),
            extendPathTo8Tiers(
                2, "Path C: Petard Demolition Bombers", "Boss-Hunting Bombers",
                listOf(
                    UpgradeTierSpec(1, "Powder Satchels", "+45% splash radius and +35% blast damage.", 130),
                    UpgradeTierSpec(2, "Siege Hunters", "Prioritizes Bosses and Armored raiders.", 265),
                    UpgradeTierSpec(3, "Concussion Casks", "Detonations apply Shocked, Cracked Armor, and Slow.", 520),
                    UpgradeTierSpec(4, "Rapid Launch", "Launches powder gliders twice as fast.", 1040),
                    UpgradeTierSpec(5, "GRAND BOMBER AERIE", "Non-stop stream of heavy powder gliders!", 2050)
                ),
                "Stealth Bunker-Buster UAVs", "Drops laser-guided bombs dealing 3x damage to Bosses.",
                "Antimatter Torpedo Wings", "Every bombing run triggers Siege Breach + Storm Conduct.",
                "OMEGA STAR-BOMBER FLEET", "Tier 8 Apex: Carpet-bombs bosses with antimatter warheads!"
            ),
            buildFuturePathD("DRONE", 175L, "Autonomous Von-Neumann Drone Swarms")
        )
    ),

    TESLA(
        id = "TESLA",
        displayName = "Lightning Rod Spire",
        shortName = "Storm Spire",
        category = TowerCategory.ELEMENTAL,
        baseCost = 150L,
        baseDamage = 36f,
        baseRange = 185f,
        baseFireRate = 1.5f,
        splashRadius = 55f,
        primaryColor = 0xFFB8963E,
        secondaryColor = 0xFFF2E2A2,
        roleSummary = "High-voltage arc spire. Evolves from Lightning Rod -> Storm Spire -> Wardenclyffe Tower -> Cyclotron Arc -> Magnetar Emitter -> Omega Fulmination.",
        primaryStatus = StatusEffect.SHOCKED,
        secondaryStatus = StatusEffect.MARKED,
        doctrineSkillTitle = "Chain Tempest Discharge",
        doctrineCooldownSec = 8.5f,
        paths = listOf(
            extendPathTo8Tiers(
                0, "Path A: Copper Chain Conductor", "Multi-Lane Arcing",
                listOf(
                    UpgradeTierSpec(1, "Copper Coils", "Chains to +2 additional enemies (5 total).", 115),
                    UpgradeTierSpec(2, "Leyden Jars", "Chain damage no longer decays per jump.", 230),
                    UpgradeTierSpec(3, "Storm Conductor", "Wet targets jump to +5 more foes at 2.5x damage.", 450),
                    UpgradeTierSpec(4, "Road Bridge Arc", "Lightning jumps across parallel road lanes.", 900),
                    UpgradeTierSpec(5, "THUNDERHEAD CATHEDRAL", "14-target chain lightning across the valley!", 1800)
                ),
                "Wardenclyffe Wireless Grid", "Chains to 20 targets and gains +10% damage per jump!",
                "Superconducting Magnetar Arc", "Chains to 28 targets and triggers 3.5x Storm Conductor.",
                "OMEGA FULMINATION THRONE", "Tier 8 Apex: Strikes every enemy on the map with 4x chain lightning!"
            ),
            extendPathTo8Tiers(
                1, "Path B: Lodestone Shock & EMP", "Stun & Shield Breaker",
                listOf(
                    UpgradeTierSpec(1, "Ground Discharge", "Deals 250% damage to enemy shields.", 110),
                    UpgradeTierSpec(2, "Thunder Stagger", "Shocked enemies are stunned for 0.4s.", 220),
                    UpgradeTierSpec(3, "Lodestone Pulse", "Reveals Stealth and disables enemy healing.", 440),
                    UpgradeTierSpec(4, "Paralyzing Spire", "Stuns whole clusters every 3rd discharge.", 880),
                    UpgradeTierSpec(5, "GRAND DYNAMO CITADEL", "Strips all shields & stuns bosses!", 1750)
                ),
                "High-Altitude EMP Burst", "Instantly vaporizes 100% shields and stuns for 1.2s.",
                "Cyclotron Ion Disruptor", "Stunned enemies take +120% critical damage from all towers.",
                "OMEGA SINGULARITY EMP", "Tier 8 Apex: Paralyzes whole columns and deals 5x shield/armor damage!"
            ),
            extendPathTo8Tiers(
                2, "Path C: Rolling Thunder Orbs", "Ball Lightning Orbs",
                listOf(
                    UpgradeTierSpec(1, "Dynamo Wheel", "+30% attack speed.", 110),
                    UpgradeTierSpec(2, "Spark Sphere", "Launches a rolling thunder orb down the path.", 225),
                    UpgradeTierSpec(3, "Charged Wake", "Orbs apply Shocked + Marked (+50% crit taken).", 445),
                    UpgradeTierSpec(4, "Twin Dynamos", "Fires 2 thunder orbs per cycle.", 890),
                    UpgradeTierSpec(5, "SOVEREIGN STORM SPIRE", "Giant lightning spheres roll down the highway!", 1820)
                ),
                "Plasma Ball Accelerators", "Launches 3 plasma orbs that explode at every road bend.",
                "Relativistic Ion Spheres", "Orbs travel backward up the entire road shocking every foe.",
                "OMEGA ASTRAL TEMPEST", "Tier 8 Apex: Continuous storm of giant plasma spheres!"
            ),
            buildFuturePathD("TESLA", 150L, "Superconducting Coils & Magnetar Pulses")
        )
    ),

    PRISM(
        id = "PRISM",
        displayName = "Sun-Mirror Obelisk",
        shortName = "Sun Obelisk",
        category = TowerCategory.ARCANE_TECH,
        baseCost = 185L,
        baseDamage = 48f,
        baseRange = 215f,
        baseFireRate = 2.4f,
        splashRadius = 40f,
        primaryColor = 0xFFC99E44,
        secondaryColor = 0xFFFFF0C2,
        roleSummary = "Solar & photon convergence spire. Evolves from Sun-Mirror Obelisk -> Solarium -> Ruby Maser -> X-Ray Laser -> Dyson Nicol-Beam -> Omega Lux-Eternal.",
        primaryStatus = StatusEffect.MARKED,
        secondaryStatus = StatusEffect.BURNING,
        doctrineSkillTitle = "Archimedes Solar Ray",
        doctrineCooldownSec = 9.0f,
        paths = listOf(
            extendPathTo8Tiers(
                0, "Path A: Mirror Array & Prism", "Multi-Beam Convergence",
                listOf(
                    UpgradeTierSpec(1, "Polished Bronze", "+30% sunbeam damage and +25% range.", 140),
                    UpgradeTierSpec(2, "Harmonic Mirrors", "+25% damage for each elemental tower nearby.", 280),
                    UpgradeTierSpec(3, "Triple Reflector", "Splits into 3 sunbeams hitting multiple bends.", 550),
                    UpgradeTierSpec(4, "Elemental Focus", "Copies status effects of all adjacent towers!", 1100),
                    UpgradeTierSpec(5, "SOLAR PANTHEON WONDER", "Golden sunbeam triggers 3 combos per hit!", 2100)
                ),
                "Phased-Array Laser Matrix", "Splits into 6 coherent beams absorbing all nearby elements.",
                "Dyson Swarm Relay Mirror", "Focuses orbital sunlight for 3.2x multi-beam damage.",
                "OMEGA LUX-ETERNAL MATRIX", "Tier 8 Apex: 10 prismatic beams trigger every elemental combo at once!"
            ),
            extendPathTo8Tiers(
                1, "Path B: Searing Focus & Gamma", "Armor-Piercing True Damage",
                listOf(
                    UpgradeTierSpec(1, "Burning Glass", "Beams deal 50% True Damage (ignores armor & shields).", 135),
                    UpgradeTierSpec(2, "Beacon of Truth", "Reveals Stealth enemies in range and applies Marked.", 270),
                    UpgradeTierSpec(3, "Purifying Ray", "Strips enemy shields and regeneration.", 530),
                    UpgradeTierSpec(4, "Noon Glare", "100% True Damage + bonus % current HP damage.", 1050),
                    UpgradeTierSpec(5, "ARCHIMEDES SUN CANNON", "Pure sunlight lance melts any armor or shield!", 2050)
                ),
                "Free-Electron X-Ray Beam", "100% True Damage + 6% current HP damage per hit.",
                "Gamma-Ray Coherent Lance", "Melts 10% Max HP per second on Bosses & Juggernauts.",
                "OMEGA QUASAR DISINTEGRATOR", "Tier 8 Apex: Vaporizes shields and deals 4.5x Pure True Damage!"
            ),
            extendPathTo8Tiers(
                2, "Path C: Inspiring Monument", "Adjacent Building Buff",
                listOf(
                    UpgradeTierSpec(1, "Warmth Aura", "Boosts damage of adjacent towers by +15%.", 130),
                    UpgradeTierSpec(2, "Call to Arms", "Boosts fire rate of adjacent towers by +18%.", 260),
                    UpgradeTierSpec(3, "Order Banner", "Adjacent towers deal +40% Combo Reaction damage.", 520),
                    UpgradeTierSpec(4, "Radiant Chime", "Releases a golden shockwave every 5 shots.", 1040),
                    UpgradeTierSpec(5, "IMPERIAL MONUMENT", "+40% Damage/Speed to all nearby towers!", 2000)
                ),
                "Solar Power Grid Relay", "Boosts adjacent tower Damage and Speed by +65%.",
                "Dyson Energy Uplinker", "Boosts adjacent tower Damage by +100% and Range by +35%.",
                "OMEGA HELIOS CITADEL", "Tier 8 Apex: Doubles (2.2x) the power of all surrounding towers!"
            ),
            buildFuturePathD("PRISM", 185L, "Dyson Nicol-Dyson Beams & Quasar Optics")
        )
    ),

    CHRONO(
        id = "CHRONO",
        displayName = "Royal Sundial Belltower",
        shortName = "Sundial",
        category = TowerCategory.ARCANE_TECH,
        baseCost = 195L,
        baseDamage = 30f,
        baseRange = 195f,
        baseFireRate = 1.3f,
        splashRadius = 82f,
        primaryColor = 0xFF7D6E8C,
        secondaryColor = 0xFFE2D7C5,
        roleSummary = "Temporal & graviton manipulator. Evolves from Sundial Belltower -> Horologium -> Atomic Clock -> Tachyon Anchor -> Event-Horizon Dome -> Omega Ouroboros.",
        primaryStatus = StatusEffect.TIME_ANCHORED,
        secondaryStatus = StatusEffect.CHILLED,
        doctrineSkillTitle = "Grand Carillon Rewind",
        doctrineCooldownSec = 10.0f,
        paths = listOf(
            extendPathTo8Tiers(
                0, "Path A: Repelling Toll & Rewind", "Push Raiders Backward",
                listOf(
                    UpgradeTierSpec(1, "Heavy Bell", "Slows raiders by 35% & increases splash damage taken by +45%.", 150),
                    UpgradeTierSpec(2, "Retreat Chime", "Every 3rd toll drives targets 45 units backward!", 300),
                    UpgradeTierSpec(3, "Grand Carillon", "Pushback distance doubled (90 units back along the road!).", 590),
                    UpgradeTierSpec(4, "King's Decree", "Can push back Bosses and stuns pushed enemies.", 1150),
                    UpgradeTierSpec(5, "GREAT CLOCKWORK CATHEDRAL", "Continuously drives waves back up the road!", 2200)
                ),
                "Cesium Temporal Rewinder", "Teleports hit enemies 140 units backward along the road.",
                "Tipler Causality Reversal", "Rewinds entire enemy clusters and Bosses every 2nd toll.",
                "OMEGA ETERNAL OUROBOROS", "Tier 8 Apex: Sends enemies all the way back toward the map entrance!"
            ),
            extendPathTo8Tiers(
                1, "Path B: Workman's Tempo & Haste", "Garrison Speed Aura",
                listOf(
                    UpgradeTierSpec(1, "Rhythm Drums", "Adjacent towers attack +20% faster.", 145),
                    UpgradeTierSpec(2, "Brass Gears", "+30% range and +30% own attack speed.", 290),
                    UpgradeTierSpec(3, "Guild Accord", "Completes Imperial Guild Accord with Royal Mint.", 570),
                    UpgradeTierSpec(4, "Double Time", "Adjacent towers attack +45% faster.", 1120),
                    UpgradeTierSpec(5, "GRAND ASTROLABE TOWER", "+75% attack speed to all towers in range!", 2150)
                ),
                "Relativistic Time Acceleration", "+110% attack speed to all towers within range.",
                "Tachyon Overclock Field", "+150% attack speed and -40% Doctrine cooldowns in range.",
                "OMEGA CHRONO-ACCELERATOR", "Tier 8 Apex: Triples (3.0x) the fire rate of all nearby towers!"
            ),
            extendPathTo8Tiers(
                2, "Path C: Siege Garrison Trap", "Hold & Crush Zone",
                listOf(
                    UpgradeTierSpec(1, "Drawbridge Gate", "Pulls raiders toward the center of the road bend.", 140),
                    UpgradeTierSpec(2, "Echoing Bell", "Slowed enemies take 35% of hit damage again after 1s.", 285),
                    UpgradeTierSpec(3, "Kill-Zone Bell", "Doubles Trebuchet Crush combo damage with Catapult/Water Mill.", 560),
                    UpgradeTierSpec(4, "Portcullis Drop", "Halts movement in a 110-radius zone every 5s.", 1100),
                    UpgradeTierSpec(5, "SOVEREIGN CITADEL BELL", "Halts waves and erupts for 4x stored damage!", 2180)
                ),
                "Lorentz Stasis Bubble", "Freezes time in a 150-unit dome and echoes 70% of all damage.",
                "Event-Horizon Time Lock", "Triples Graviton Crush damage and halts Bosses.",
                "OMEGA TIMELINE COLLAPSER", "Tier 8 Apex: Locks whole waves in stasis & erupts for 6x damage!"
            ),
            buildFuturePathD("CHRONO", 195L, "Tachyon Causality Loops & Time Dilation")
        )
    ),

    // =========================================================================
    // 4 NEW MULTI-ERA BASE TOWERS (16 TOTAL BUILDABLE BASE TOWERS!)
    // =========================================================================
    RAIL_CANNON(
        id = "RAIL_CANNON",
        displayName = "Bronze Culverin",
        shortName = "Culverin",
        category = TowerCategory.KINETIC,
        baseCost = 210L,
        baseDamage = 185f,
        baseRange = 330f,
        baseFireRate = 0.95f,
        splashRadius = 52f,
        primaryColor = 0xFF6E7885,
        secondaryColor = 0xFFD4AF37,
        roleSummary = "Heavy linear siege cannon. Evolves from Ribault/Culverin -> Krupp Siege Gun -> Dora Railway Cannon -> EM Railgun -> Orbital MAC -> Omega God-Slayer Railgun.",
        primaryStatus = StatusEffect.CORRODED,
        secondaryStatus = StatusEffect.BLEEDING,
        doctrineSkillTitle = "Hyper-Velocity Rail Salvo",
        doctrineCooldownSec = 8.5f,
        paths = listOf(
            extendPathTo8Tiers(
                0, "Path A: Cast-Bronze to Magnetic Rail", "Armor-Piercing Line Penetration",
                listOf(
                    UpgradeTierSpec(1, "Solid Iron Roundshot", "+45% damage and pierces 3 enemies.", 160),
                    UpgradeTierSpec(2, "Rifled Ordnance", "Ignores 60% armor and +60 range.", 320),
                    UpgradeTierSpec(3, "Bessemer Breechloader", "Pierces 7 enemies and cracks 50% armor.", 640),
                    UpgradeTierSpec(4, "Dora Railway Barrel", "+120% damage and triggers 3.5x Shatter on Frozen.", 1280),
                    UpgradeTierSpec(5, "ELECTROMAGNETIC RAILGUN", "Mach-7 tungsten slug pierces whole columns!", 2500)
                ),
                "Superconducting Sabot Accelerator", "Mach-15 slug deals 2.5x damage and ignores 100% armor.",
                "Orbital MAC Defense Battery", "Deals +300% damage to Bosses and Juggernauts.",
                "OMEGA GOD-SLAYER RAILGUN", "Tier 8 Apex: Relativistic slug obliterates every enemy in its path!"
            ),
            extendPathTo8Tiers(
                1, "Path B: Canister & Flak Shrapnel", "Cone Shockwave & Knockback",
                listOf(
                    UpgradeTierSpec(1, "Canister Shot", "Adds a 75-unit shrapnel blast on impact.", 150),
                    UpgradeTierSpec(2, "Double Powder Charge", "Knocks hit enemies backward 22 units.", 300),
                    UpgradeTierSpec(3, "Flak Airburst", "Strikes both ground and flying columns for +50% crit.", 600),
                    UpgradeTierSpec(4, "Recoilless Autoloader", "+55% fire rate and stuns armored targets.", 1200),
                    UpgradeTierSpec(5, "GRAND BATTERY CANNONADE", "Twin barrels fire concussive shrapnel shells!", 2400)
                ),
                "Electro-Thermal Flak Storm", "Shrapnel chains through 8 nearby targets with Bleeding.",
                "Plasma-Sheathed Kinetic Burst", "140-unit shockwave knocks whole waves backward.",
                "OMEGA STAR-BREAKER CANNON", "Tier 8 Apex: Massive kinetic shockwaves clear entire switchbacks!"
            ),
            extendPathTo8Tiers(
                2, "Path C: Siege Breaker Telemetry", "Shield Vaporizer & Bounty",
                listOf(
                    UpgradeTierSpec(1, "Rangefinder Quadrant", "+25% range and +15% crit chance.", 145),
                    UpgradeTierSpec(2, "Shell Fuse", "Deals 300% damage to enemy shields and pavises.", 290),
                    UpgradeTierSpec(3, "Spotter Balloon", "Grants True Sight and applies Marked on hit.", 580),
                    UpgradeTierSpec(4, "Treasury Prize Shot", "Slain targets yield +60% bonus Gold.", 1160),
                    UpgradeTierSpec(5, "SOVEREIGN DARDANELLES", "Map-wide siege gun vaporizes shields & bosses!", 2350)
                ),
                "Laser-Guided Smart Shells", "Never misses; 100% crit chance against Marked targets.",
                "Quantum Bore Ammunition", "Deals bonus damage equal to 8% of target's Max HP.",
                "OMEGA ENTROPY BORE-CANNON", "Tier 8 Apex: 5.0x Crit Damage + 15% Max HP hull breach on hit!"
            ),
            buildFuturePathD("RAILGUN", 210L, "Relativistic Mass Drivers & MAC Accelerators")
        )
    ),

    LASER_ARRAY(
        id = "LASER_ARRAY",
        displayName = "Alchemical Lens Spire",
        shortName = "Lens Spire",
        category = TowerCategory.ARCANE_TECH,
        baseCost = 225L,
        baseDamage = 64f,
        baseRange = 250f,
        baseFireRate = 3.2f,
        splashRadius = 48f,
        primaryColor = 0xFF48A8D8,
        secondaryColor = 0xFFE6F7FF,
        roleSummary = "Coherent beam emitter. Evolves from Alchemical Lens -> Searchlight -> CO2 Gas Laser -> FEL X-Ray Laser -> Phaser Battery -> Omega Genesis Beam.",
        primaryStatus = StatusEffect.BURNING,
        secondaryStatus = StatusEffect.MARKED,
        doctrineSkillTitle = "Ionosphere Orbital Sweep",
        doctrineCooldownSec = 8.0f,
        paths = listOf(
            extendPathTo8Tiers(
                0, "Path A: Coherent Beam Focusing", "Ramp-Up Thermal Lance",
                listOf(
                    UpgradeTierSpec(1, "Quartz Crystal Lens", "+35% beam damage and +20% fire rate.", 170),
                    UpgradeTierSpec(2, "Thermal Lock", "Continuous hits on the same target increase damage by +25%/hit.", 340),
                    UpgradeTierSpec(3, "Ruby Maser Core", "Ignores 65% armor and triggers Thermal Shock on Frozen.", 680),
                    UpgradeTierSpec(4, "Excimer UV Emitter", "+80% beam damage and melts enemy shields.", 1350),
                    UpgradeTierSpec(5, "MEGAWATT CHEMICAL LASER", "Searing coherent lance melts bosses in seconds!", 2650)
                ),
                "Free-Electron X-Ray Turret", "Ramps up to +300% damage on high-HP targets.",
                "Stellar Corona Maser Lance", "Pierces through all enemies in a line for 100% True Damage.",
                "OMEGA GENESIS BEAM", "Tier 8 Apex: Continuous orbital genesis beam disintegrates whole columns!"
            ),
            extendPathTo8Tiers(
                1, "Path B: Diffraction & Prism Split", "Multi-Target Laser Web",
                listOf(
                    UpgradeTierSpec(1, "Split Mirror", "Strikes 2 targets simultaneously.", 165),
                    UpgradeTierSpec(2, "Optical Fiber Relay", "+30% range and reveals Stealth raiders.", 330),
                    UpgradeTierSpec(3, "Quad-Beam Array", "Strikes 4 targets across multiple road bends.", 660),
                    UpgradeTierSpec(4, "Ionizing Lattice", "Beams apply Shocked + Burning + Marked at once.", 1320),
                    UpgradeTierSpec(5, "PHASED-ARRAY LASER DOME", "8 synchronized laser beams sweep the valley!", 2600)
                ),
                "Holographic Beam Splitter", "Strikes 12 targets simultaneously with zero damage loss.",
                "Starfleet Phaser Bank", "Chains coherent phaser arcs between all hit enemies.",
                "OMEGADYSON PHOTON WEB", "Tier 8 Apex: 18-beam stellar laser web covers the entire map!"
            ),
            extendPathTo8Tiers(
                2, "Path C: Target Telemetry & Gold Ionizer", "Crit & Economy Support",
                listOf(
                    UpgradeTierSpec(1, "Optical Rangefinder", "Nearby towers gain +15% Range and +12% Crit.", 160),
                    UpgradeTierSpec(2, "Gold Transmutation Beam", "Mints +3 Gold on every laser hit.", 320),
                    UpgradeTierSpec(3, "Vulnerability Tag", "Hit enemies take +40% damage from all towers.", 640),
                    UpgradeTierSpec(4, "Satellite Uplink", "Boosts adjacent tower attack speed by +35%.", 1280),
                    UpgradeTierSpec(5, "ORBITAL TELEMETRY CORE", "Mints +10G/hit & grants +50% Dmg/Range to neighbors!", 2550)
                ),
                "Quantum Entangled Optics", "Mints +25G/hit and grants +75% Crit Damage in range.",
                "Zero-Point Energy Siphon", "Mints +60G/hit and recharges nearby Doctrine skills.",
                "OMEGA PHOTON SOVEREIGN", "Tier 8 Apex: +150G/hit & doubles nearby tower attack speed!"
            ),
            buildFuturePathD("LASER", 225L, "Planck-Scale Grasers & Orbital Mirror Arrays")
        )
    ),

    GRAVITY_WELL(
        id = "GRAVITY_WELL",
        displayName = "Lodestone Gravity Well",
        shortName = "Gravity Well",
        category = TowerCategory.ARCANE_TECH,
        baseCost = 240L,
        baseDamage = 44f,
        baseRange = 205f,
        baseFireRate = 1.45f,
        splashRadius = 96f,
        primaryColor = 0xFF6C5B8E,
        secondaryColor = 0xFFD2C4F2,
        roleSummary = "Magnetic & graviton singularity tower. Evolves from Lodestone Pillar -> Orrery -> Solenoid Trap -> Graviton Emitter -> Black-Hole Horizon -> Omega Great Attractor.",
        primaryStatus = StatusEffect.TIME_ANCHORED,
        secondaryStatus = StatusEffect.CORRODED,
        doctrineSkillTitle = "Singularity Implosion",
        doctrineCooldownSec = 9.5f,
        paths = listOf(
            extendPathTo8Tiers(
                0, "Path A: Magnetic Pull & Singularity", "Clumping & Road Reversal",
                listOf(
                    UpgradeTierSpec(1, "Heavy Lodestone", "Slows armored and regular raiders by 40%.", 180),
                    UpgradeTierSpec(2, "Magnetic Well", "Pulls raiders backward 25 units into the blast center.", 360),
                    UpgradeTierSpec(3, "Crushing G-Force", "Crushes armor (-50% armor) and deals +60% splash.", 720),
                    UpgradeTierSpec(4, "Graviton Eddy", "Triggers 2.9x Graviton Crush on slowed formations.", 1440),
                    UpgradeTierSpec(5, "MICRO-SINGULARITY WELL", "Black-hole eddy drags whole waves backward!", 2800)
                ),
                "Neutron-Star Mass Anchor", "Drags enemies 65 units backward and stuns for 1.2s.",
                "Event-Horizon Implosion", "Deals +12% Max HP crushing damage to all trapped enemies.",
                "OMEGA GREAT ATTRACTOR", "Tier 8 Apex: Colossal black hole locks and crushes entire waves!"
            ),
            extendPathTo8Tiers(
                1, "Path B: Orbital Kinetic Slingshot", "Tower Range & Projectile Boost",
                listOf(
                    UpgradeTierSpec(1, "Gravity Assist", "Nearby towers gain +22% Range and +20% Damage.", 175),
                    UpgradeTierSpec(2, "Magnetic Deflection", "Strips enemy shields across the entire well radius.", 350),
                    UpgradeTierSpec(3, "Kepler Orrery", "Nearby towers gain +30% Attack Speed.", 700),
                    UpgradeTierSpec(4, "Dark-Matter Lens", "All Combo reactions in range deal +65% bonus damage.", 1400),
                    UpgradeTierSpec(5, "IMPERIAL ORRERY DOME", "+55% Damage, Range & Speed to all nearby towers!", 2750)
                ),
                "Relativistic Frame-Dragging", "Nearby towers gain +90% Damage and +50% Attack Speed.",
                "Wormhole Ammunition Feed", "Nearby towers fire +1 extra projectile per attack.",
                "OMEGA COSMIC ORRERY", "Tier 8 Apex: +150% Damage & +80% Speed to all towers in range!"
            ),
            extendPathTo8Tiers(
                2, "Path C: Repulsor Shockwave", "Area Knockback & Armor Sunder",
                listOf(
                    UpgradeTierSpec(1, "Iron Repulsor", "+35% splash radius and +30% fire rate.", 170),
                    UpgradeTierSpec(2, "Gravity Hammer", "Slams flying and ground foes, removing flight & stealth.", 340),
                    UpgradeTierSpec(3, "Tidal Shear", "Applies Bleeding + Corroded + Bell-Slowed on every pulse.", 680),
                    UpgradeTierSpec(4, "Twin Singularities", "Emits 2 overlapping graviton pulses per cycle.", 1360),
                    UpgradeTierSpec(5, "SOVEREIGN GRAVITON CITADEL", "Crushes armor, strips shields & halts bosses!", 2700)
                ),
                "Spatial Metric Rupture", "Each pulse deals 3.5x damage and shatters Frozen enemies.",
                "Zero-Point Vacuum Collapse", "Executes non-bosses below 25% HP inside the gravity well.",
                "OMEGA BIG-CRUNCH MONOLITH", "Tier 8 Apex: Collapses spacetime across 3 road bends for 5x damage!"
            ),
            buildFuturePathD("GRAVITY", 240L, "Schwarzschild Event Horizons & Dark-Matter Wells")
        )
    ),

    SHIELD_GARRISON(
        id = "SHIELD_GARRISON",
        displayName = "Bastion Shield Keep",
        shortName = "Bastion Keep",
        category = TowerCategory.SUPPORT,
        baseCost = 200L,
        baseDamage = 40f,
        baseRange = 230f,
        baseFireRate = 1.75f,
        splashRadius = 70f,
        primaryColor = 0xFF4F7A94,
        secondaryColor = 0xFFD4AF37,
        roleSummary = "Fortified citadel & Aegis projector. Repairs Castle Keep HP, buffs nearby towers, and intercepts raiders with artillery counter-fire.",
        primaryStatus = StatusEffect.MARKED,
        secondaryStatus = StatusEffect.SHOCKED,
        doctrineSkillTitle = "Aegis Keep Fortification",
        doctrineCooldownSec = 9.0f,
        paths = listOf(
            extendPathTo8Tiers(
                0, "Path A: Keep Masonry & Aegis Dome", "Castle HP Repair & Shielding",
                listOf(
                    UpgradeTierSpec(1, "Stone Buttresses", "Repairs +3 Castle Keep HP every wave and +25% damage.", 155),
                    UpgradeTierSpec(2, "Iron Portcullis", "Repairs +6 Keep HP/wave and slows nearby raiders.", 310),
                    UpgradeTierSpec(3, "Star-Fort Ravelin", "Increases Max Castle Keep HP by +50 and +30% range.", 620),
                    UpgradeTierSpec(4, "Royal Bastion Guard", "Repairs +12 Keep HP/wave and stuns leaking raiders.", 1240),
                    UpgradeTierSpec(5, "IMPERIAL AEGIS CITADEL", "+120 Max Keep HP, +20 HP/wave & powerful counter-fire!", 2450)
                ),
                "Hard-Light Deflector Grid", "Repairs +35 Keep HP/wave and +80% counter-battery damage.",
                "Planetary Magnetosphere Dome", "Repairs +60 Keep HP/wave and grants +60% Dmg to neighbors.",
                "OMEGA INVULNERABLE BASTION", "Tier 8 Apex: Restores +100 Keep HP/wave & +150% aura buff!"
            ),
            extendPathTo8Tiers(
                1, "Path B: Garrison Counter-Battery", "Flak & Armor-Piercing Volley",
                listOf(
                    UpgradeTierSpec(1, "Wall Swivel Guns", "+40% attack speed and +25% splash radius.", 150),
                    UpgradeTierSpec(2, "Case-Shot Volley", "Applies Bleeding and Marked to all hit enemies.", 300),
                    UpgradeTierSpec(3, "Casemate Cannons", "Ignores 50% armor and deals 2.5x shield damage.", 600),
                    UpgradeTierSpec(4, "Trophy Active Intercept", "Zaps incoming fast raiders and wyverns for 2x crit.", 1200),
                    UpgradeTierSpec(5, "FORTRESS ARTILLERY KEEP", "Rapid twin casemate cannons clear road bends!", 2400)
                ),
                "Automated Rail-Casemate", "Fires 3-round rail volleys that knock enemies backward.",
                "Starfleet Point-Defense Array", "Intercepts all enemies in a 300-unit dome with plasma fire.",
                "OMEGA CITADEL DREADNOUGHT", "Tier 8 Apex: Devastating omni-directional plasma casemate!"
            ),
            extendPathTo8Tiers(
                2, "Path C: War College & Logistics", "XP, Mastery & Gold Aura",
                listOf(
                    UpgradeTierSpec(1, "Drill Sergeant", "Nearby towers earn +50% Combat XP toward Veteran Ranks.", 150),
                    UpgradeTierSpec(2, "Armory Supply Line", "Boosts equipped Forge Gear potency on nearby towers by +30%.", 305),
                    UpgradeTierSpec(3, "War College Charter", "Nearby towers gain +25% Damage and +20% Crit.", 610),
                    UpgradeTierSpec(4, "Royal War Bonds", "Generates +45 Gold/sec and +2% Bank Wave Interest.", 1220),
                    UpgradeTierSpec(5, "SUPREME WAR ACADEMY", "Doubles XP gain, +50% Gear power & +120 Gold/sec!", 2420)
                ),
                "Cybernetic Combat Simulators", "Nearby towers earn 3x Combat XP and +65% Damage.",
                "Starfleet Command Academy", "Nearby towers gain +85% Damage, +45% Speed & +300 Gold/sec.",
                "OMEGA SOVEREIGN ACADEMY", "Tier 8 Apex: 5x Combat XP, +150% Neighbor Damage & +800 Gold/sec!"
            ),
            buildFuturePathD("BASTION", 200L, "Hard-Light Aegis Domes & Planetary Defense")
        )
    ),

    // =========================================================================
    // 16 MONUMENTAL WONDER & FUTURE MEGA-STRUCTURE FUSIONS
    // Created by combining two compatible Level 4+ nearby towers!
    // =========================================================================
    FROSTFIRE_PARADOX(
        id = "FROSTFIRE_PARADOX",
        displayName = "Wonder: Frostfire Citadel",
        shortName = "Frostfire",
        category = TowerCategory.APEX_HYBRID,
        baseCost = 1800L,
        baseDamage = 195f,
        baseRange = 260f,
        baseFireRate = 3.2f,
        splashRadius = 105f,
        primaryColor = 0xFF6B9AB0,
        secondaryColor = 0xFFC85A28,
        roleSummary = "GRAND WONDER (Ice Keep + Fire Pitch): Twin battlements of winter frost and Greek Fire, triggering 3.2x Thermal Shock on every shot!",
        primaryStatus = StatusEffect.FROZEN,
        secondaryStatus = StatusEffect.BURNING,
        doctrineSkillTitle = "Frostfire Cataclysm",
        doctrineCooldownSec = 7.0f,
        isFusedApex = true,
        fusionRecipe = "CRYO" to "PYRO",
        paths = emptyList()
    ),

    TEMPEST_MAELSTROM(
        id = "TEMPEST_MAELSTROM",
        displayName = "Wonder: Great Storm Pharos",
        shortName = "Storm Pharos",
        category = TowerCategory.APEX_HYBRID,
        baseCost = 1850L,
        baseDamage = 180f,
        baseRange = 275f,
        baseFireRate = 3.0f,
        splashRadius = 115f,
        primaryColor = 0xFF4A829E,
        secondaryColor = 0xFFB8963E,
        roleSummary = "GRAND WONDER (Water Mill + Storm Spire): Monumental lighthouse that floods the road and arcs 12-target storm lightning!",
        primaryStatus = StatusEffect.WET,
        secondaryStatus = StatusEffect.SHOCKED,
        doctrineSkillTitle = "Pharos Deluge Bolt",
        doctrineCooldownSec = 7.0f,
        isFusedApex = true,
        fusionRecipe = "HYDRO" to "TESLA",
        paths = emptyList()
    ),

    PLAGUEBRINGER_VULCAN(
        id = "PLAGUEBRINGER_VULCAN",
        displayName = "Wonder: Alchemical Bastion",
        shortName = "AlchemKeep",
        category = TowerCategory.APEX_HYBRID,
        baseCost = 1750L,
        baseDamage = 92f,
        baseRange = 245f,
        baseFireRate = 9.5f,
        splashRadius = 65f,
        primaryColor = 0xFF688E41,
        secondaryColor = 0xFF8C8275,
        roleSummary = "GRAND WONDER (Rock Tower + Apothecary): 10 shots/sec siege battery that strips 100% armor and triggers 2.7x Siege Breach!",
        primaryStatus = StatusEffect.POISONED,
        secondaryStatus = StatusEffect.CORRODED,
        doctrineSkillTitle = "Vitriol Cannonade",
        doctrineCooldownSec = 6.5f,
        isFusedApex = true,
        fusionRecipe = "GATLING" to "VENOM",
        paths = emptyList()
    ),

    TACHYON_RAILGUN(
        id = "TACHYON_RAILGUN",
        displayName = "Wonder: Grand Imperial Cannon",
        shortName = "GrandCannon",
        category = TowerCategory.APEX_HYBRID,
        baseCost = 2100L,
        baseDamage = 480f,
        baseRange = 460f,
        baseFireRate = 1.65f,
        splashRadius = 85f,
        primaryColor = 0xFF9C6B3B,
        secondaryColor = 0xFF7D6E8C,
        roleSummary = "GRAND WONDER (Ballista + Sundial): Map-wide great bombard that knocks bosses backward along the road and executes below 20% HP!",
        primaryStatus = StatusEffect.TIME_ANCHORED,
        secondaryStatus = StatusEffect.MARKED,
        doctrineSkillTitle = "Imperial Royal Salvo",
        doctrineCooldownSec = 7.5f,
        isFusedApex = true,
        fusionRecipe = "SNIPER" to "CHRONO",
        paths = emptyList()
    ),

    GLACIER_BOMBARD(
        id = "GLACIER_BOMBARD",
        displayName = "Wonder: Winter Trebuchet Keep",
        shortName = "WinterTreb",
        category = TowerCategory.APEX_HYBRID,
        baseCost = 1950L,
        baseDamage = 340f,
        baseRange = 320f,
        baseFireRate = 1.45f,
        splashRadius = 135f,
        primaryColor = 0xFF6B9AB0,
        secondaryColor = 0xFF7A6855,
        roleSummary = "GRAND WONDER (Catapult + Ice Keep): Hurls colossal frost boulders that freeze entire bends and immediately detonate 3.5x Shatter!",
        primaryStatus = StatusEffect.FROZEN,
        secondaryStatus = StatusEffect.BLEEDING,
        doctrineSkillTitle = "Avalanche Bombard",
        doctrineCooldownSec = 7.5f,
        isFusedApex = true,
        fusionRecipe = "MORTAR" to "CRYO",
        paths = emptyList()
    ),

    ASTRAL_NEXUS(
        id = "ASTRAL_NEXUS",
        displayName = "Wonder: Sovereign Cathedral",
        shortName = "Cathedral",
        category = TowerCategory.APEX_HYBRID,
        baseCost = 2300L,
        baseDamage = 165f,
        baseRange = 310f,
        baseFireRate = 3.6f,
        splashRadius = 90f,
        primaryColor = 0xFFC59B27,
        secondaryColor = 0xFFFFF0C2,
        roleSummary = "GRAND WONDER (Sun Obelisk + Royal Mint): Mints +55 Gold/sec while focusing golden sunbeams and buffing all nearby buildings by +45%!",
        primaryStatus = StatusEffect.MARKED,
        secondaryStatus = StatusEffect.SHOCKED,
        doctrineSkillTitle = "Sovereign Sun Blessing",
        doctrineCooldownSec = 8.0f,
        isFusedApex = true,
        fusionRecipe = "PRISM" to "HUB",
        paths = emptyList()
    ),

    COLOSSUS_SIEGEWORKS(
        id = "COLOSSUS_SIEGEWORKS",
        displayName = "Wonder: Colossus Siege Foundry",
        shortName = "Colossus",
        category = TowerCategory.APEX_HYBRID,
        baseCost = 1900L,
        baseDamage = 290f,
        baseRange = 300f,
        baseFireRate = 2.6f,
        splashRadius = 125f,
        primaryColor = 0xFF8C8275,
        secondaryColor = 0xFFD4AF37,
        roleSummary = "GRAND WONDER (Rock Tower + Catapult): Bronze colossus that fires continuous grapeshot boulders and cracks all enemy armor!",
        primaryStatus = StatusEffect.BLEEDING,
        secondaryStatus = StatusEffect.CORRODED,
        doctrineSkillTitle = "Colossus Earthshaker",
        doctrineCooldownSec = 7.0f,
        isFusedApex = true,
        fusionRecipe = "GATLING" to "MORTAR",
        paths = emptyList()
    ),

    ALCHEMICAL_CATHEDRAL(
        id = "ALCHEMICAL_CATHEDRAL",
        displayName = "Wonder: Greek Fire Basilica",
        shortName = "Basilica",
        category = TowerCategory.APEX_HYBRID,
        baseCost = 1950L,
        baseDamage = 175f,
        baseRange = 255f,
        baseFireRate = 3.4f,
        splashRadius = 110f,
        primaryColor = 0xFFC85A28,
        secondaryColor = 0xFF688E41,
        roleSummary = "GRAND WONDER (Fire Pitch + Apothecary): Drenches the road in boiling tar and ignites 2.8x Greek Fire explosions on every shot!",
        primaryStatus = StatusEffect.BURNING,
        secondaryStatus = StatusEffect.OILED,
        doctrineSkillTitle = "Greek Fire Conflagration",
        doctrineCooldownSec = 7.0f,
        isFusedApex = true,
        fusionRecipe = "PYRO" to "VENOM",
        paths = emptyList()
    ),

    DAVINCI_SKY_FORTRESS(
        id = "DAVINCI_SKY_FORTRESS",
        displayName = "Wonder: Da Vinci Sky Citadel",
        shortName = "SkyCitadel",
        category = TowerCategory.APEX_HYBRID,
        baseCost = 2050L,
        baseDamage = 260f,
        baseRange = 420f,
        baseFireRate = 3.2f,
        splashRadius = 80f,
        primaryColor = 0xFF5A7D6C,
        secondaryColor = 0xFF9C6B3B,
        roleSummary = "GRAND WONDER (Ballista + Falcon Roost): Launches armored war-airships and siege bolts across the entire valley!",
        primaryStatus = StatusEffect.MARKED,
        secondaryStatus = StatusEffect.BLEEDING,
        doctrineSkillTitle = "Imperial Air Armada",
        doctrineCooldownSec = 7.5f,
        isFusedApex = true,
        fusionRecipe = "SNIPER" to "DRONE",
        paths = emptyList()
    ),

    CHRONOS_OBSERVATORY(
        id = "CHRONOS_OBSERVATORY",
        displayName = "Wonder: Royal Astrolabe Dome",
        shortName = "Astrolabe",
        category = TowerCategory.APEX_HYBRID,
        baseCost = 2200L,
        baseDamage = 210f,
        baseRange = 300f,
        baseFireRate = 3.0f,
        splashRadius = 100f,
        primaryColor = 0xFF7D6E8C,
        secondaryColor = 0xFFC99E44,
        roleSummary = "GRAND WONDER (Sun Obelisk + Sundial): Bends sunlight and time, driving raiders backward while granting +60% speed to nearby towers!",
        primaryStatus = StatusEffect.TIME_ANCHORED,
        secondaryStatus = StatusEffect.BURNING,
        doctrineSkillTitle = "Equinox Time Reversal",
        doctrineCooldownSec = 8.0f,
        isFusedApex = true,
        fusionRecipe = "PRISM" to "CHRONO",
        paths = emptyList()
    ),

    // 6 NEW FUTURE & COSMIC MEGA-WONDERS
    DYSON_HELIOS_ARRAY(
        id = "DYSON_HELIOS_ARRAY",
        displayName = "Wonder: Dyson Helios Array",
        shortName = "DysonHelios",
        category = TowerCategory.APEX_HYBRID,
        baseCost = 3400L,
        baseDamage = 420f,
        baseRange = 440f,
        baseFireRate = 4.5f,
        splashRadius = 110f,
        primaryColor = 0xFF48A8D8,
        secondaryColor = 0xFFE6C65A,
        roleSummary = "MEGA-WONDER (Lens Spire + Sun Obelisk): Channels orbital Dyson-sphere beams that deal 100% True Damage and mint +80 Gold/sec!",
        primaryStatus = StatusEffect.BURNING,
        secondaryStatus = StatusEffect.MARKED,
        doctrineSkillTitle = "Nicol-Dyson Stellar Flare",
        doctrineCooldownSec = 6.5f,
        isFusedApex = true,
        fusionRecipe = "LASER_ARRAY" to "PRISM",
        paths = emptyList()
    ),

    LEVIATHAN_GAUSS_CITADEL(
        id = "LEVIATHAN_GAUSS_CITADEL",
        displayName = "Wonder: Leviathan Gauss Citadel",
        shortName = "GaussKeep",
        category = TowerCategory.APEX_HYBRID,
        baseCost = 3200L,
        baseDamage = 650f,
        baseRange = 500f,
        baseFireRate = 2.4f,
        splashRadius = 95f,
        primaryColor = 0xFF6E7885,
        secondaryColor = 0xFF9C6B3B,
        roleSummary = "MEGA-WONDER (Culverin/Railgun + Ballista): Twin relativistic mass drivers pierce the entire valley and execute bosses below 22% HP!",
        primaryStatus = StatusEffect.MARKED,
        secondaryStatus = StatusEffect.CORRODED,
        doctrineSkillTitle = "Orbital MAC Broadside",
        doctrineCooldownSec = 6.5f,
        isFusedApex = true,
        fusionRecipe = "RAIL_CANNON" to "SNIPER",
        paths = emptyList()
    ),

    OMEGA_SINGULARITY_CORE(
        id = "OMEGA_SINGULARITY_CORE",
        displayName = "Wonder: Omega Singularity Core",
        shortName = "Singularity",
        category = TowerCategory.APEX_HYBRID,
        baseCost = 3600L,
        baseDamage = 520f,
        baseRange = 350f,
        baseFireRate = 2.8f,
        splashRadius = 160f,
        primaryColor = 0xFF6C5B8E,
        secondaryColor = 0xFFF5D76E,
        roleSummary = "MEGA-WONDER (Gravity Well + Sundial): Collapses spacetime to drag entire waves backward while doubling nearby tower attack speed!",
        primaryStatus = StatusEffect.TIME_ANCHORED,
        secondaryStatus = StatusEffect.CORRODED,
        doctrineSkillTitle = "Event-Horizon Collapse",
        doctrineCooldownSec = 7.0f,
        isFusedApex = true,
        fusionRecipe = "GRAVITY_WELL" to "CHRONO",
        paths = emptyList()
    ),

    AEGIS_ORBITAL_BASTION(
        id = "AEGIS_ORBITAL_BASTION",
        displayName = "Wonder: Aegis Orbital Bastion",
        shortName = "AegisKeep",
        category = TowerCategory.APEX_HYBRID,
        baseCost = 3100L,
        baseDamage = 310f,
        baseRange = 360f,
        baseFireRate = 4.0f,
        splashRadius = 115f,
        primaryColor = 0xFF4F7A94,
        secondaryColor = 0xFFC59B27,
        roleSummary = "MEGA-WONDER (Bastion Keep + Royal Mint): Repairs +40 Keep HP/wave, mints +150 Gold/sec, and buffs all nearby towers by +80%!",
        primaryStatus = StatusEffect.MARKED,
        secondaryStatus = StatusEffect.SHOCKED,
        doctrineSkillTitle = "Planetary Aegis Decree",
        doctrineCooldownSec = 7.5f,
        isFusedApex = true,
        fusionRecipe = "SHIELD_GARRISON" to "HUB",
        paths = emptyList()
    ),

    QUANTUM_CHRONO_MATRIX(
        id = "QUANTUM_CHRONO_MATRIX",
        displayName = "Wonder: Quantum Storm Matrix",
        shortName = "QuantumArc",
        category = TowerCategory.APEX_HYBRID,
        baseCost = 3300L,
        baseDamage = 380f,
        baseRange = 370f,
        baseFireRate = 4.2f,
        splashRadius = 120f,
        primaryColor = 0xFF48A8D8,
        secondaryColor = 0xFFB8963E,
        roleSummary = "MEGA-WONDER (Lens Spire + Storm Spire): Fires 16-target quantum laser-lightning arcs that strip all shields and stun bosses!",
        primaryStatus = StatusEffect.SHOCKED,
        secondaryStatus = StatusEffect.BURNING,
        doctrineSkillTitle = "Quantum Magnetar Storm",
        doctrineCooldownSec = 6.5f,
        isFusedApex = true,
        fusionRecipe = "LASER_ARRAY" to "TESLA",
        paths = emptyList()
    ),

    NANITE_SWARM_HIVE(
        id = "NANITE_SWARM_HIVE",
        displayName = "Wonder: Nanite Carrier Hive",
        shortName = "NaniteHive",
        category = TowerCategory.APEX_HYBRID,
        baseCost = 3150L,
        baseDamage = 295f,
        baseRange = 440f,
        baseFireRate = 5.0f,
        splashRadius = 105f,
        primaryColor = 0xFF688E41,
        secondaryColor = 0xFF5A7D6C,
        roleSummary = "MEGA-WONDER (Falcon Roost + Apothecary): Launches autonomous nanite drone swarms that dissolve 100% armor across the whole map!",
        primaryStatus = StatusEffect.POISONED,
        secondaryStatus = StatusEffect.MARKED,
        doctrineSkillTitle = "Grey-Goo Swarm Eclipse",
        doctrineCooldownSec = 6.5f,
        isFusedApex = true,
        fusionRecipe = "DRONE" to "VENOM",
        paths = emptyList()
    );

    /**
     * Returns an Age-evolving building title across all 18 Historical-to-Cosmic Ages!
     */
    fun ageDisplayName(ageNum: Int): String =
        EraEvolutionCatalog.profileFor(this, ageNum).evolvedDisplayName

    fun ageShortName(ageNum: Int): String =
        EraEvolutionCatalog.profileFor(this, ageNum).evolvedShortName

    companion object {
        const val MAX_PATH_TIER = 8
        const val PATH_COUNT = 4

        val buildableBaseTowers: List<TowerType> = entries.filter { !it.isFusedApex }
        val fusedApexTowers: List<TowerType> = entries.filter { it.isFusedApex }

        fun findFusionResult(a: TowerType, b: TowerType): TowerType? {
            if (a.isFusedApex || b.isFusedApex) return null
            for (apex in fusedApexTowers) {
                val rec = apex.fusionRecipe ?: continue
                if ((rec.first == a.id && rec.second == b.id) || (rec.first == b.id && rec.second == a.id)) {
                    return apex
                }
            }
            return null
        }
    }
}

/**
 * Deployable Road Fortifications
 */
enum class TrackGadgetType(
    val id: String,
    val displayName: String,
    val shortLabel: String,
    val cost: Long,
    val colorArgb: Long,
    val description: String
) {
    REVERSE_CONVEYOR(
        id = "REVERSE_CONVEYOR",
        displayName = "Spiked Timber Barricade",
        shortLabel = "BARRICADE",
        cost = 180L,
        colorArgb = 0xFFA67B5B,
        description = "Placed on the road: forces raiders backward along the trail and slows their march!"
    ),
    ELEMENTAL_RUNE(
        id = "ELEMENTAL_RUNE",
        displayName = "Alchemist Pitch & Tar Trench",
        shortLabel = "TAR PIT",
        cost = 210L,
        colorArgb = 0xFF7A5C43,
        description = "Coats all passing raiders in Tar Oil + Water + Cracked Armor for instant combo reactions!"
    ),
    STASIS_SNARE(
        id = "STASIS_SNARE",
        displayName = "Iron Caltrops & Bear Snares",
        shortLabel = "CALTROPS",
        cost = 195L,
        colorArgb = 0xFF8C9298,
        description = "Reveals stealth, breaks shields, and slows raider movement by 68%!"
    ),
    ALCHEMY_TRANSMUTER(
        id = "ALCHEMY_TRANSMUTER",
        displayName = "Royal Toll & Tax Gate",
        shortLabel = "3x TOLL",
        cost = 240L,
        colorArgb = 0xFFD4AF37,
        description = "Raiders defeated near this toll gate yield 3x Gold bounty!"
    )
}

/**
 * Dynamic Seasonal & Cosmic Weather Fronts
 */
enum class WeatherFront(
    val title: String,
    val badge: String,
    val summary: String,
    val accentColor: Long
) {
    CLEAR_OVERWATCH(
        title = "Sunlit Meadow Skies",
        badge = "☀️ SUNNY",
        summary = "+15% All Building Range & +12% Critical Hit Chance across the valley.",
        accentColor = 0xFFD4AF37
    ),
    MONSOON_SQUALL(
        title = "Summer Rainstorm",
        badge = "🌧️ RAIN",
        summary = "Rain periodically drenches raiders! +35% Water Mill, Storm Spire & Ice Keep power.",
        accentColor = 0xFF6B9AB0
    ),
    SOLAR_FLARE(
        title = "Midsummer Heatwave",
        badge = "🔥 DROUGHT",
        summary = "+40% Fire Pitch, Sun Obelisk, Lens Spire & Catapult damage; Burning deals +50% damage.",
        accentColor = 0xFFC85A28
    ),
    GEOMAGNETIC_TEMPEST(
        title = "Geomagnetic Gale",
        badge = "💨 GALE",
        summary = "+30% Attack Speed to Ballistas, Railguns, Falcon Roosts & Storm Spires!",
        accentColor = 0xFFB8963E
    ),
    AETHER_METEOR_SHOWER(
        title = "Royal Harvest & Starfall",
        badge = "🌾 HARVEST",
        summary = "+75% Enemy Kill Gold Bounties & +45% Combo Reaction damage!",
        accentColor = 0xFFD4AF37
    )
}

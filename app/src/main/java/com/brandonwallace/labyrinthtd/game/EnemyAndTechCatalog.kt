package com.brandonwallace.labyrinthtd.game

enum class EnemyShape {
    TRIANGLE_SWARM,
    DIAMOND_STRIDER,
    HEX_JUGGERNAUT,
    ARROW_SPRINTER,
    OCTAGON_SHIELDER,
    HYDRA_CELL,
    CROSS_HEALER,
    STEALTH_WRAITH,
    CRYSTAL_GOLEM,
    WINGED_WYVERN,
    DREADNOUGHT_BOSS
}

/**
 * LAYER: ENEMY ELITE AFFIXES (14 Tactical Modifiers across Historical & Future Waves)
 */
enum class EnemyEliteAffix(
    val id: String,
    val title: String,
    val badge: String,
    val hpMult: Float,
    val speedMult: Float,
    val bonusArmor: Float,
    val bonusShieldRatio: Float,
    val knockbackResist: Float,
    val regenPerSecRatio: Float,
    val goldAndXpMult: Float,
    val ringColorArgb: Long
) {
    NONE("NONE", "Regular", "", 1.0f, 1.0f, 0f, 0f, 0f, 0f, 1.0f, 0x00000000),
    IRON_PLATED(
        id = "IRON_PLATED",
        title = "Iron-Plated",
        badge = "IRON",
        hpMult = 1.35f,
        speedMult = 0.92f,
        bonusArmor = 0.22f,
        bonusShieldRatio = 0f,
        knockbackResist = 0.35f,
        regenPerSecRatio = 0f,
        goldAndXpMult = 1.45f,
        ringColorArgb = 0xFF8C969E
    ),
    VANGUARD_HASTE(
        id = "VANGUARD_HASTE",
        title = "Vanguard Charger",
        badge = "FAST",
        hpMult = 1.15f,
        speedMult = 1.32f,
        bonusArmor = 0f,
        bonusShieldRatio = 0.15f,
        knockbackResist = 0.20f,
        regenPerSecRatio = 0f,
        goldAndXpMult = 1.40f,
        ringColorArgb = 0xFFD9A73E
    ),
    PAVISE_WARD(
        id = "PAVISE_WARD",
        title = "Pavise Bastion",
        badge = "WARD",
        hpMult = 1.25f,
        speedMult = 0.95f,
        bonusArmor = 0.10f,
        bonusShieldRatio = 0.75f,
        knockbackResist = 0.25f,
        regenPerSecRatio = 0f,
        goldAndXpMult = 1.50f,
        ringColorArgb = 0xFF5A8BB0
    ),
    WARLORD_BANNER(
        id = "WARLORD_BANNER",
        title = "Warlord Standard",
        badge = "LORD",
        hpMult = 1.60f,
        speedMult = 1.08f,
        bonusArmor = 0.15f,
        bonusShieldRatio = 0.30f,
        knockbackResist = 0.50f,
        regenPerSecRatio = 0.015f,
        goldAndXpMult = 1.85f,
        ringColorArgb = 0xFFC75B39
    ),
    REGENERATING_SUPPLY(
        id = "REGENERATING_SUPPLY",
        title = "Provisioned Guard",
        badge = "HEAL",
        hpMult = 1.40f,
        speedMult = 1.0f,
        bonusArmor = 0.08f,
        bonusShieldRatio = 0.20f,
        knockbackResist = 0.15f,
        regenPerSecRatio = 0.035f,
        goldAndXpMult = 1.55f,
        ringColorArgb = 0xFF5B9E48
    ),
    SIEGE_RESISTANT(
        id = "SIEGE_RESISTANT",
        title = "Heavy Ballast",
        badge = "HVY",
        hpMult = 1.75f,
        speedMult = 0.85f,
        bonusArmor = 0.18f,
        bonusShieldRatio = 0.25f,
        knockbackResist = 0.80f,
        regenPerSecRatio = 0f,
        goldAndXpMult = 1.70f,
        ringColorArgb = 0xFF7D6E8C
    ),
    GOLDEN_CARAVAN(
        id = "GOLDEN_CARAVAN",
        title = "Royal Treasure Wagon",
        badge = "3xG",
        hpMult = 1.80f,
        speedMult = 1.10f,
        bonusArmor = 0.15f,
        bonusShieldRatio = 0.40f,
        knockbackResist = 0.40f,
        regenPerSecRatio = 0f,
        goldAndXpMult = 3.00f,
        ringColorArgb = 0xFFD4AF37
    ),
    BESSEMER_ARMORED(
        id = "BESSEMER_ARMORED",
        title = "Bessemer Steelclad",
        badge = "STL",
        hpMult = 2.10f,
        speedMult = 0.96f,
        bonusArmor = 0.28f,
        bonusShieldRatio = 0.35f,
        knockbackResist = 0.60f,
        regenPerSecRatio = 0f,
        goldAndXpMult = 2.20f,
        ringColorArgb = 0xFF6E7F8D
    ),
    ATOMIC_OVERCHARGED(
        id = "ATOMIC_OVERCHARGED",
        title = "Isotope Overcharged",
        badge = "RAD",
        hpMult = 2.35f,
        speedMult = 1.18f,
        bonusArmor = 0.15f,
        bonusShieldRatio = 0.50f,
        knockbackResist = 0.50f,
        regenPerSecRatio = 0.025f,
        goldAndXpMult = 2.50f,
        ringColorArgb = 0xFF68B078
    ),
    CYBER_NANITE_REPAIR(
        id = "CYBER_NANITE_REPAIR",
        title = "Nanite Self-Healing",
        badge = "NANO",
        hpMult = 2.60f,
        speedMult = 1.12f,
        bonusArmor = 0.20f,
        bonusShieldRatio = 0.65f,
        knockbackResist = 0.55f,
        regenPerSecRatio = 0.045f,
        goldAndXpMult = 2.80f,
        ringColorArgb = 0xFF48A8D8
    ),
    PLASMA_DEFLECTOR(
        id = "PLASMA_DEFLECTOR",
        title = "Plasma Ray-Shielded",
        badge = "PLSM",
        hpMult = 2.85f,
        speedMult = 1.15f,
        bonusArmor = 0.22f,
        bonusShieldRatio = 1.20f,
        knockbackResist = 0.65f,
        regenPerSecRatio = 0.02f,
        goldAndXpMult = 3.20f,
        ringColorArgb = 0xFF64C8F0
    ),
    QUANTUM_PHASE_SHIFTER(
        id = "QUANTUM_PHASE_SHIFTER",
        title = "Quantum Phase-Walker",
        badge = "QTM",
        hpMult = 3.25f,
        speedMult = 1.28f,
        bonusArmor = 0.25f,
        bonusShieldRatio = 0.90f,
        knockbackResist = 0.75f,
        regenPerSecRatio = 0.03f,
        goldAndXpMult = 3.80f,
        ringColorArgb = 0xFFA290BD
    ),
    OMEGA_BULLION_CONVOY(
        id = "OMEGA_BULLION_CONVOY",
        title = "Omega Star-Bullion Convoy",
        badge = "5xG",
        hpMult = 3.60f,
        speedMult = 1.15f,
        bonusArmor = 0.25f,
        bonusShieldRatio = 1.00f,
        knockbackResist = 0.80f,
        regenPerSecRatio = 0.025f,
        goldAndXpMult = 5.00f,
        ringColorArgb = 0xFFF5D76E
    )
}

/**
 * 32 DISTINCT ENEMY TYPES SPANNING DARK AGE -> INDUSTRIAL -> CYBERNETIC -> OMEGA FUTURE
 */
enum class EnemyType(
    val id: String,
    val displayName: String,
    val badge: String,
    val baseHp: Float,
    val baseSpeed: Float,
    val armor: Float,
    val shieldRatio: Float,
    val goldReward: Long,
    val radius: Float,
    val colorArgb: Long,
    val shape: EnemyShape,
    val isCamo: Boolean = false,
    val isFlying: Boolean = false,
    val isBoss: Boolean = false,
    val splitsOnDeath: Boolean = false,
    val healsNearby: Boolean = false,
    val weakTo: StatusEffect? = null,
    val summary: String,
    val counterTip: String
) {
    SWARMLING(
        id = "SWARMLING",
        displayName = "Militia Skirmisher",
        badge = "SKM",
        baseHp = 65f,
        baseSpeed = 84f,
        armor = 0.0f,
        shieldRatio = 0.0f,
        goldReward = 7L,
        radius = 10f,
        colorArgb = 0xFFC89D54,
        shape = EnemyShape.TRIANGLE_SWARM,
        weakTo = StatusEffect.BURNING,
        summary = "Lightly armed foot skirmishers that rush the meadow road in packs.",
        counterTip = "Use Stone Catapults, Rock Watchtowers, or Boiling Pitch."
    ),
    STRIDER(
        id = "STRIDER",
        displayName = "Feudal Man-at-Arms",
        badge = "INF",
        baseHp = 135f,
        baseSpeed = 66f,
        armor = 0.10f,
        shieldRatio = 0.0f,
        goldReward = 11L,
        radius = 13f,
        colorArgb = 0xFF8C583A,
        shape = EnemyShape.DIAMOND_STRIDER,
        weakTo = StatusEffect.BLEEDING,
        summary = "Standard infantry column forming the backbone of early raids.",
        counterTip = "Rock Watchtowers and Wooden Ballistas clear them reliably."
    ),
    ARBALESTER(
        id = "ARBALESTER",
        displayName = "Crossbow Vanguard",
        badge = "XBW",
        baseHp = 175f,
        baseSpeed = 72f,
        armor = 0.15f,
        shieldRatio = 0.15f,
        goldReward = 15L,
        radius = 12.5f,
        colorArgb = 0xFF9C7A4C,
        shape = EnemyShape.DIAMOND_STRIDER,
        weakTo = StatusEffect.BLEEDING,
        summary = "Disciplined crossbow infantry wearing light mail and bucklers.",
        counterTip = "Catapult splash and Ballista bolts break their formation."
    ),
    IRONCLAD(
        id = "IRONCLAD",
        displayName = "Teutonic Siege Ram",
        badge = "RAM",
        baseHp = 420f,
        baseSpeed = 44f,
        armor = 0.55f,
        shieldRatio = 0.0f,
        goldReward = 24L,
        radius = 17f,
        colorArgb = 0xFF707880,
        shape = EnemyShape.HEX_JUGGERNAUT,
        weakTo = StatusEffect.CORRODED,
        summary = "Iron-plated siege ram that shrugs off light stones.",
        counterTip = "Crack armor with Catapults, Apothecary Acid, or Freeze + Shatter!"
    ),
    SPRINTER(
        id = "SPRINTER",
        displayName = "Steppe Light Outrider",
        badge = "CAV",
        baseHp = 110f,
        baseSpeed = 122f,
        armor = 0.05f,
        shieldRatio = 0.15f,
        goldReward = 14L,
        radius = 11f,
        colorArgb = 0xFFB87333,
        shape = EnemyShape.ARROW_SPRINTER,
        weakTo = StatusEffect.FROZEN,
        summary = "Fast cavalry outrider that gallops around road switchbacks.",
        counterTip = "Slow them down with Water Mills, Ice Keeps, or Frost Runestones."
    ),
    SHIELDER(
        id = "SHIELDER",
        displayName = "Pavise Shield Guard",
        badge = "SHD",
        baseHp = 260f,
        baseSpeed = 54f,
        armor = 0.20f,
        shieldRatio = 0.85f,
        goldReward = 22L,
        radius = 15f,
        colorArgb = 0xFF5A7D9A,
        shape = EnemyShape.OCTAGON_SHIELDER,
        weakTo = StatusEffect.SHOCKED,
        summary = "Carries a heavy pavise barrier that absorbs frontal fire.",
        counterTip = "Break shields with Storm Spires, Sun Obelisks, or Apothecary Acid."
    ),
    SAPPER_PETARD(
        id = "SAPPER_PETARD",
        displayName = "Siege Sapper Petardier",
        badge = "SAP",
        baseHp = 210f,
        baseSpeed = 96f,
        armor = 0.12f,
        shieldRatio = 0.10f,
        goldReward = 19L,
        radius = 12f,
        colorArgb = 0xFFB85233,
        shape = EnemyShape.TRIANGLE_SWARM,
        weakTo = StatusEffect.WET,
        summary = "Fast powder sapper rushing toward the Castle Keep gates.",
        counterTip = "Drench with Water Mills or snipe early with Ballistas."
    ),
    HYDRA_SPLITTER(
        id = "HYDRA_SPLITTER",
        displayName = "Siege Trojan Wagon",
        badge = "WGN",
        baseHp = 310f,
        baseSpeed = 52f,
        armor = 0.15f,
        shieldRatio = 0.0f,
        goldReward = 20L,
        radius = 16f,
        colorArgb = 0xFF8A6240,
        shape = EnemyShape.HYDRA_CELL,
        splitsOnDeath = true,
        weakTo = StatusEffect.BURNING,
        summary = "Armored war wagon that deploys 3 Foot Raiders when destroyed.",
        counterTip = "Catapult boulders and Boiling Pitch clear the wagon and its crew."
    ),
    HYDRA_SPAWN(
        id = "HYDRA_SPAWN",
        displayName = "Wagon Foot Raider",
        badge = "RDR",
        baseHp = 75f,
        baseSpeed = 94f,
        armor = 0.0f,
        shieldRatio = 0.0f,
        goldReward = 5L,
        radius = 9f,
        colorArgb = 0xFFB5895A,
        shape = EnemyShape.TRIANGLE_SWARM,
        weakTo = StatusEffect.BURNING,
        summary = "Disembarked foot raider from a destroyed Siege Wagon.",
        counterTip = "Catapult splash and road fire clear them quickly."
    ),
    NANITE_HEALER(
        id = "NANITE_HEALER",
        displayName = "Battlefield Monk",
        badge = "MNK",
        baseHp = 240f,
        baseSpeed = 58f,
        armor = 0.15f,
        shieldRatio = 0.25f,
        goldReward = 22L,
        radius = 14f,
        colorArgb = 0xFFD4AF37,
        shape = EnemyShape.CROSS_HEALER,
        healsNearby = true,
        weakTo = StatusEffect.POISONED,
        summary = "Heals nearby raiders along the road every 2.2 seconds.",
        counterTip = "Snipe with Ballistas or stop healing with Apothecary Acid."
    ),
    SHADOW_CAMO(
        id = "SHADOW_CAMO",
        displayName = "Forest Ranger Scout",
        badge = "STL",
        baseHp = 195f,
        baseSpeed = 80f,
        armor = 0.10f,
        shieldRatio = 0.20f,
        goldReward = 20L,
        radius = 13f,
        colorArgb = 0xFF4E6E4E,
        shape = EnemyShape.STEALTH_WRAITH,
        isCamo = true,
        weakTo = StatusEffect.MARKED,
        summary = "Cloaked woodland scout unseen by towers without Lookout Sight.",
        counterTip = "Place towers near trees (Woodland Overlook), equip a Spyglass, or build a Royal Mint."
    ),
    CATAPHRACT(
        id = "CATAPHRACT",
        displayName = "Imperial Cataphract Knight",
        badge = "KNT",
        baseHp = 490f,
        baseSpeed = 76f,
        armor = 0.42f,
        shieldRatio = 0.35f,
        goldReward = 30L,
        radius = 16f,
        colorArgb = 0xFF8C7248,
        shape = EnemyShape.HEX_JUGGERNAUT,
        weakTo = StatusEffect.SHOCKED,
        summary = "Barded heavy cavalry knight combining speed and plate armor.",
        counterTip = "Slow with Sundials or Ice Keeps and pierce armor with Bodkin Tips."
    ),
    FROST_GOLEM(
        id = "FROST_GOLEM",
        displayName = "Imperial War Elephant",
        badge = "ELP",
        baseHp = 580f,
        baseSpeed = 40f,
        armor = 0.45f,
        shieldRatio = 0.30f,
        goldReward = 32L,
        radius = 18f,
        colorArgb = 0xFF7D746D,
        shape = EnemyShape.CRYSTAL_GOLEM,
        weakTo = StatusEffect.BURNING,
        summary = "Massive armored war beast with thick hide and tower howdah.",
        counterTip = "Trigger Thermal Shock (Ice + Fire Pitch) or focus Ballistas & Catapults."
    ),
    SKY_WYVERN(
        id = "SKY_WYVERN",
        displayName = "War Zeppelin Glider",
        badge = "AIR",
        baseHp = 275f,
        baseSpeed = 84f,
        armor = 0.15f,
        shieldRatio = 0.20f,
        goldReward = 25L,
        radius = 15f,
        colorArgb = 0xFFA67B5B,
        shape = EnemyShape.WINGED_WYVERN,
        isFlying = true,
        weakTo = StatusEffect.SHOCKED,
        summary = "Airborne siege glider advancing along the valley road.",
        counterTip = "Intercept with Ballistas, Falconry Roosts, and Storm Spires."
    ),
    BOMBARD_TRAIN(
        id = "BOMBARD_TRAIN",
        displayName = "Royal Siege Bombard Train",
        badge = "BMB",
        baseHp = 920f,
        baseSpeed = 38f,
        armor = 0.50f,
        shieldRatio = 0.40f,
        goldReward = 48L,
        radius = 20f,
        colorArgb = 0xFF5E564E,
        shape = EnemyShape.HEX_JUGGERNAUT,
        splitsOnDeath = true,
        weakTo = StatusEffect.CORRODED,
        summary = "Heavily armored iron cannon carriage escorted by siege crew.",
        counterTip = "Sunder armor with Vitriol Acid and shatter with Trebuchets."
    ),
    LEVIATHAN_BOSS(
        id = "LEVIATHAN_BOSS",
        displayName = "Emperor's Great Siege Tower",
        badge = "BOSS",
        baseHp = 2600f,
        baseSpeed = 34f,
        armor = 0.45f,
        shieldRatio = 0.50f,
        goldReward = 180L,
        radius = 24f,
        colorArgb = 0xFFB83B2B,
        shape = EnemyShape.DREADNOUGHT_BOSS,
        isBoss = true,
        healsNearby = true,
        weakTo = StatusEffect.MARKED,
        summary = "Towering iron-clad siege belfry reinforced with shields and war drums.",
        counterTip = "Mark with Ballistas, crack armor with Catapults, or build Grand Wonders!"
    ),

    // =========================================================================
    // 16 NEW GUNPOWDER, INDUSTRIAL, ATOMIC, CYBERNETIC & OMEGA FUTURE ENEMIES
    // =========================================================================
    MUSKET_DRAGOON(
        id = "MUSKET_DRAGOON",
        displayName = "Enlightenment Carbine Dragoon",
        badge = "DRG",
        baseHp = 620f,
        baseSpeed = 98f,
        armor = 0.25f,
        shieldRatio = 0.25f,
        goldReward = 42L,
        radius = 14f,
        colorArgb = 0xFF8C5230,
        shape = EnemyShape.ARROW_SPRINTER,
        weakTo = StatusEffect.FROZEN,
        summary = "Gunpowder Age mounted dragoon charging down the highway.",
        counterTip = "Freeze with Cryo Keep and shatter with Culverin/Railgun."
    ),
    STEAM_LANDSHIP(
        id = "STEAM_LANDSHIP",
        displayName = "Bessemer Steam Landship",
        badge = "TNK",
        baseHp = 1350f,
        baseSpeed = 48f,
        armor = 0.58f,
        shieldRatio = 0.45f,
        goldReward = 68L,
        radius = 19f,
        colorArgb = 0xFF545B61,
        shape = EnemyShape.HEX_JUGGERNAUT,
        weakTo = StatusEffect.CORRODED,
        summary = "Industrial Age riveted steel tank powered by twin coal boilers.",
        counterTip = "Pierce with Rail Cannons or melt with Laser Arrays & Acid."
    ),
    IRONCLAD_ZEPPELIN(
        id = "IRONCLAD_ZEPPELIN",
        displayName = "Dreadnought Sky-Dirigible",
        badge = "ZEP",
        baseHp = 1100f,
        baseSpeed = 68f,
        armor = 0.35f,
        shieldRatio = 0.55f,
        goldReward = 64L,
        radius = 18f,
        colorArgb = 0xFF6E7885,
        shape = EnemyShape.WINGED_WYVERN,
        isFlying = true,
        weakTo = StatusEffect.SHOCKED,
        summary = "Armored Rigid Airship floating above ground obstacles.",
        counterTip = "Use Storm Spires, Falcon/Jet Squadrons, and Laser Arrays."
    ),
    DIESEL_MECH_WALKER(
        id = "DIESEL_MECH_WALKER",
        displayName = "Dynamo Automaton Strider",
        badge = "MCH",
        baseHp = 1680f,
        baseSpeed = 62f,
        armor = 0.48f,
        shieldRatio = 0.60f,
        goldReward = 82L,
        radius = 17f,
        colorArgb = 0xFF75684B,
        shape = EnemyShape.CRYSTAL_GOLEM,
        splitsOnDeath = true,
        weakTo = StatusEffect.SHOCKED,
        summary = "Machine Age bipedal walker that ejects combat drones on destruction.",
        counterTip = "Overload its dynamo with Tesla EMP and Gravity Wells."
    ),
    ATOMIC_GOLIATH(
        id = "ATOMIC_GOLIATH",
        displayName = "Isotope Fission Juggernaut",
        badge = "NUK",
        baseHp = 2400f,
        baseSpeed = 44f,
        armor = 0.62f,
        shieldRatio = 0.75f,
        goldReward = 115L,
        radius = 21f,
        colorArgb = 0xFF588A62,
        shape = EnemyShape.HEX_JUGGERNAUT,
        healsNearby = true,
        weakTo = StatusEffect.TIME_ANCHORED,
        summary = "Atomic Age reactor tank that energizes and repairs nearby vehicles.",
        counterTip = "Lock in Gravity Wells and focus True Damage from Prism/Laser."
    ),
    STEALTH_NIGHTHAWK(
        id = "STEALTH_NIGHTHAWK",
        displayName = "Silicon Stealth Drone",
        badge = "UAV",
        baseHp = 980f,
        baseSpeed = 112f,
        armor = 0.28f,
        shieldRatio = 0.65f,
        goldReward = 78L,
        radius = 14f,
        colorArgb = 0xFF37434F,
        shape = EnemyShape.STEALTH_WRAITH,
        isCamo = true,
        isFlying = true,
        weakTo = StatusEffect.MARKED,
        summary = "Radar-absorbent supersonic drone that requires True Sight to target.",
        counterTip = "Equip Radar/Spyglass gear or place a Royal Mint / Laser Array."
    ),
    CYBER_CENTURION(
        id = "CYBER_CENTURION",
        displayName = "Cybernetic Exo-Centurion",
        badge = "CYB",
        baseHp = 2150f,
        baseSpeed = 78f,
        armor = 0.52f,
        shieldRatio = 0.85f,
        goldReward = 105L,
        radius = 16.5f,
        colorArgb = 0xFF48A8D8,
        shape = EnemyShape.OCTAGON_SHIELDER,
        weakTo = StatusEffect.POISONED,
        summary = "Cybernetic Age shock trooper with regenerating hard-light shields.",
        counterTip = "Dissolve shields with Nanite Venom and stun with Tesla."
    ),
    NANITE_CARRIER(
        id = "NANITE_CARRIER",
        displayName = "Self-Replicating Nanite Hive",
        badge = "HVE",
        baseHp = 2900f,
        baseSpeed = 52f,
        armor = 0.45f,
        shieldRatio = 0.80f,
        goldReward = 135L,
        radius = 19.5f,
        colorArgb = 0xFF4C8C64,
        shape = EnemyShape.HYDRA_CELL,
        splitsOnDeath = true,
        healsNearby = true,
        weakTo = StatusEffect.BURNING,
        summary = "Repairs nearby units and splits into high-speed nanite clusters on death.",
        counterTip = "Incinerate with Plasma Pyro and crush with Singularity Wells."
    ),
    PLASMA_HOVER_TANK(
        id = "PLASMA_HOVER_TANK",
        displayName = "Tokamak Mag-Lev Grav-Tank",
        badge = "MAG",
        baseHp = 3500f,
        baseSpeed = 86f,
        armor = 0.56f,
        shieldRatio = 1.00f,
        goldReward = 160L,
        radius = 18.5f,
        colorArgb = 0xFF5298C4,
        shape = EnemyShape.ARROW_SPRINTER,
        weakTo = StatusEffect.FROZEN,
        summary = "Fusion Age hover-tank that skims rapidly over road traps.",
        counterTip = "Arrest with Absolute-Zero Cryo and pierce with MAC Railguns."
    ),
    STARFLEET_CORVETTE(
        id = "STARFLEET_CORVETTE",
        displayName = "Orbital Strike Corvette",
        badge = "CRV",
        baseHp = 4200f,
        baseSpeed = 74f,
        armor = 0.55f,
        shieldRatio = 1.15f,
        goldReward = 195L,
        radius = 20f,
        colorArgb = 0xFF5A7896,
        shape = EnemyShape.WINGED_WYVERN,
        isFlying = true,
        weakTo = StatusEffect.MARKED,
        summary = "Starfleet Age low-orbit gunship with heavy ray-deflector shields.",
        counterTip = "Combine Leviathan Gauss Citadel and Dyson Helios Array."
    ),
    DYSON_HARVESTER_TITAN(
        id = "DYSON_HARVESTER_TITAN",
        displayName = "Dyson Stellar Automaton",
        badge = "DYS",
        baseHp = 5800f,
        baseSpeed = 46f,
        armor = 0.65f,
        shieldRatio = 1.25f,
        goldReward = 260L,
        radius = 22f,
        colorArgb = 0xFFD4AF37,
        shape = EnemyShape.CRYSTAL_GOLEM,
        weakTo = StatusEffect.CORRODED,
        summary = "Dyson Age star-forged colossus plated in solar-reflective alloy.",
        counterTip = "Rewind with Chrono Ouroboros and shatter with Tier-8 Railguns."
    ),
    QUANTUM_PHASE_WRAITH(
        id = "QUANTUM_PHASE_WRAITH",
        displayName = "Quantum Entangled Spectre",
        badge = "QSP",
        baseHp = 3800f,
        baseSpeed = 105f,
        armor = 0.45f,
        shieldRatio = 1.30f,
        goldReward = 220L,
        radius = 16f,
        colorArgb = 0xFF8C7AA8,
        shape = EnemyShape.STEALTH_WRAITH,
        isCamo = true,
        weakTo = StatusEffect.TIME_ANCHORED,
        summary = "Quantum Age phase-walker that flickers in and out of spacetime.",
        counterTip = "Anchor in spacetime with Gravity Wells and True-Sight Lasers."
    ),
    CHRONO_PARADOX_DRIFTER(
        id = "CHRONO_PARADOX_DRIFTER",
        displayName = "Dimensional Rift Strider",
        badge = "RFT",
        baseHp = 6800f,
        baseSpeed = 68f,
        armor = 0.60f,
        shieldRatio = 1.40f,
        goldReward = 310L,
        radius = 20f,
        colorArgb = 0xFF7D5BA6,
        shape = EnemyShape.OCTAGON_SHIELDER,
        splitsOnDeath = true,
        healsNearby = true,
        weakTo = StatusEffect.MARKED,
        summary = "Dimensional Age anomaly that warps timelines and mends nearby rifts.",
        counterTip = "Use Omega Singularity Core and Tier-8 Sniper Execution."
    ),
    INDUSTRIAL_DREADNOUGHT_BOSS(
        id = "INDUSTRIAL_DREADNOUGHT_BOSS",
        displayName = "Bessemer Railway Dreadnought",
        badge = "BOSS",
        baseHp = 6500f,
        baseSpeed = 36f,
        armor = 0.58f,
        shieldRatio = 0.85f,
        goldReward = 450L,
        radius = 25f,
        colorArgb = 0xFF545B61,
        shape = EnemyShape.DREADNOUGHT_BOSS,
        isBoss = true,
        splitsOnDeath = true,
        weakTo = StatusEffect.CORRODED,
        summary = "Industrial Age super-heavy armored train bristling with steam turrets.",
        counterTip = "Sunder armor with Vitriol/Nanite Acid and pummel with Railguns."
    ),
    STARFLEET_MOTHERSHIP_BOSS(
        id = "STARFLEET_MOTHERSHIP_BOSS",
        displayName = "Starfleet Orbital Dreadnought",
        badge = "BOSS",
        baseHp = 14000f,
        baseSpeed = 38f,
        armor = 0.65f,
        shieldRatio = 1.35f,
        goldReward = 950L,
        radius = 27f,
        colorArgb = 0xFF3E6B89,
        shape = EnemyShape.DREADNOUGHT_BOSS,
        isBoss = true,
        isFlying = true,
        healsNearby = true,
        weakTo = StatusEffect.SHOCKED,
        summary = "Starfleet Age capital ship projecting fleet-wide deflector barriers.",
        counterTip = "Ascend ★15+ Mega-Wonders and unleash Commander Abilities!"
    ),
    OMEGA_ARCHITECT_BOSS(
        id = "OMEGA_ARCHITECT_BOSS",
        displayName = "Omega Omniversal Leviathan",
        badge = "OMEGA",
        baseHp = 32000f,
        baseSpeed = 40f,
        armor = 0.70f,
        shieldRatio = 1.75f,
        goldReward = 2500L,
        radius = 29f,
        colorArgb = 0xFFD4AF37,
        shape = EnemyShape.DREADNOUGHT_BOSS,
        isBoss = true,
        splitsOnDeath = true,
        healsNearby = true,
        weakTo = StatusEffect.MARKED,
        summary = "Age XVIII Cosmic Boss: Reality-bending leviathan from beyond the Omega Horizon.",
        counterTip = "Advance to Age XVIII (Omega Age), forge Mega-Wonders, and Ascend ★25!"
    )
}

data class TechNodeSpec(
    val id: String,
    val title: String,
    val branch: String,
    val summary: String,
    val shardCost: Int,
    val maxRank: Int = 5
)

/**
 * LAYER: 40 HISTORICAL TO FUTURE TECHNOLOGIES ACROSS 6 BRANCHES
 */
object TechTreeCatalog {
    val nodes: List<TechNodeSpec> = listOf(
        // 1. Blacksmith & Ballistics Branch (7 Techs)
        TechNodeSpec("KIN_DMG", "Forging & Blast Furnace", "Blacksmith", "+12% Stone, Bolt, Cannon & Railgun Damage per rank.", 2, 15),
        TechNodeSpec("KIN_CRIT", "Fletching & Sabot Finning", "Blacksmith", "+5% Crit Chance & +25% Crit Damage per rank.", 3, 12),
        TechNodeSpec("KIN_PIERCE", "Tempered Tungsten Quarrels", "Blacksmith", "Attacks ignore +10% enemy armor per rank.", 4, 8),
        TechNodeSpec("KIN_RANGE", "Bracer & Telemetry Parapets", "Blacksmith", "+8% Firing Range for all fortifications per rank.", 3, 10),
        TechNodeSpec("KIN_GEAR", "Master Armorer's Forge", "Blacksmith", "+18% potency of all equipped Tower Forge Gear per rank.", 4, 10),
        TechNodeSpec("KIN_VET", "Royal Military Academy", "Blacksmith", "+35% Tower Combat XP earned toward Veteran Ranks per rank.", 3, 10),
        TechNodeSpec("KIN_PATH_D", "Path-D Overdrive Blueprints", "Blacksmith", "+20% bonus damage for every Path-D tier upgraded on a tower per rank.", 5, 8),

        // 2. Siege Workshop Branch (6 Techs)
        TechNodeSpec("SIG_SPLASH", "Torsion & MIRV Warheads", "Siegecraft", "+12% Splash Radius for Catapults, Gravity Wells & Pitch per rank.", 3, 10),
        TechNodeSpec("SIG_BOSS", "Siege & Dreadnought Breakers", "Siegecraft", "+20% bonus damage against Bosses & Juggernauts per rank.", 4, 10),
        TechNodeSpec("SIG_KNOCK", "Graviton & Granite Boulders", "Siegecraft", "+25% Knockback & Chrono-Pushback distance per rank.", 3, 8),
        TechNodeSpec("SIG_SHIELD", "Culverin & EMP Shield Breakers", "Siegecraft", "+30% bonus damage against enemy Shields & Deflectors per rank.", 3, 10),
        TechNodeSpec("SIG_DOCTRINE", "Garrison Drill Sergeants", "Siegecraft", "Tier-5+ Doctrine Active Skills recharge 12% faster per rank.", 4, 7),
        TechNodeSpec("SIG_TIER8", "Tier-8 Apex Ordinance", "Siegecraft", "+25% damage to all towers that have reached Tier 6, 7, or 8 per rank.", 5, 10),

        // 3. Alchemists' Guild Branch (7 Techs)
        TechNodeSpec("ELM_DMG", "Royal Alchemists & Plasma", "Alchemy", "+14% Elemental, Laser & Alchemical Damage per rank.", 2, 15),
        TechNodeSpec("ELM_COMBO", "Greek Fire & Fusion Formula", "Alchemy", "+20% Combo Reaction damage per rank.", 3, 12),
        TechNodeSpec("ELM_DUR", "Refined Pitch & Cryo-Salts", "Alchemy", "+15% Status Effect duration per rank.", 3, 10),
        TechNodeSpec("ELM_DOT", "Distilled Naphtha & Nanites", "Alchemy", "+22% Burning, Poison & Bleeding DoT damage per rank.", 3, 10),
        TechNodeSpec("ELM_SLOW", "Zero-Kelvin Permafrost", "Alchemy", "+8% stronger Slow from Chilled, Wet, Tar & Chrono chimes per rank.", 4, 8),
        TechNodeSpec("ELM_CHAIN", "Superconducting Coils", "Alchemy", "+1 extra chain bounce for Storm Spires, Lasers & Ricochets per rank.", 4, 8),
        TechNodeSpec("ELM_EXECUTE", "Entropic Molecular Decay", "Alchemy", "Increases execute threshold on Marked/Frozen foes by +1.5% HP per rank.", 5, 8),

        // 4. Imperial University & Engineering Branch (7 Techs)
        TechNodeSpec("ARC_SPD", "Crane, Dynamo & Tachyon Winches", "University", "+8% Attack Speed for ALL buildings per rank.", 3, 15),
        TechNodeSpec("ARC_FUSE", "Monumental & Dyson Architecture", "University", "+18% Grand Wonder power & -8% Wonder cost per rank.", 5, 10),
        TechNodeSpec("ARC_ASCEND", "Star-Metal Ascension Mastery", "University", "+15% Ascension Star damage bonus & -8% Ascension cost per rank.", 5, 10),
        TechNodeSpec("ARC_CROSSFIRE", "Ballistics & Crossfire", "University", "+5% bonus damage per road pass covered by a tower per rank.", 4, 8),
        TechNodeSpec("ARC_KEEP", "Fortified Aegis Keep Walls", "University", "+50 Max Castle Keep HP & +4 HP regenerated per wave per rank.", 3, 10),
        TechNodeSpec("ARC_ERA", "Historical-to-Future Evolution", "University", "+20% stronger Age-Up Tower Evolution multipliers per rank.", 5, 10),
        TechNodeSpec("ARC_EDICT", "Imperial Senate Decrees", "University", "+15% effectiveness of all active Civilization Edicts per rank.", 5, 8),

        // 5. Royal Market & Sovereign Treasury Branch (7 Techs)
        TechNodeSpec("ECO_BOUNTY", "Spoils of War", "Treasury", "+12% Enemy Kill Gold Bounty per rank.", 2, 15),
        TechNodeSpec("ECO_WAVE", "Feudal & Starfleet Tithe", "Treasury", "+15% Wave Completion Gold & Rush bonus per rank.", 3, 12),
        TechNodeSpec("ECO_BUILD", "Stone & Titanium Guild", "Treasury", "-5% Building & Upgrade Gold cost per rank.", 4, 8),
        TechNodeSpec("ECO_BANK", "Sovereign Unlimited Banking", "Treasury", "+3.0% Bank Wave Compound Interest Rate per rank (No Vault Cap!).", 3, 12),
        TechNodeSpec("ECO_MINT", "Silk Road & Orbital Caravans", "Treasury", "+25% passive Gold/sec from Royal Mints, Lasers & Bastions per rank.", 4, 10),
        TechNodeSpec("ECO_SELL", "Salvage & Matter Reclamation", "Treasury", "Increases tower demolish refund from 75% up to 99% (+4%/rank).", 3, 6),
        TechNodeSpec("ECO_BOND", "High-Yield War Bonds", "Treasury", "Increases 3-Wave Treasury Bond maturity payout from 2.0x up to 3.5x.", 5, 6),

        // 6. Future & Cosmic Sciences Branch (6 Techs)
        TechNodeSpec("FUT_RAIL", "Relativistic Mass Accelerators", "FutureTech", "+25% Damage & Pierce for Culverins, Ballistas & Gatlings per rank.", 5, 10),
        TechNodeSpec("FUT_PHOTON", "Coherent Graser & Dyson Optics", "FutureTech", "+25% Damage & Range for Lens Spires, Sun Obelisks & Wonders per rank.", 5, 10),
        TechNodeSpec("FUT_GRAV", "Zero-Point Singularity Wells", "FutureTech", "+25% Pull/Slow power & Splash for Gravity Wells & Sundials per rank.", 5, 10),
        TechNodeSpec("FUT_NANO", "Autonomous Grey-Goo Nanites", "FutureTech", "+25% DoT & Armor Sunder for Apothecaries, Pitch & Drones per rank.", 5, 10),
        TechNodeSpec("FUT_OMNI", "Quantum Crosspath Harmonizer", "FutureTech", "+10% All Tower Stats for every active upgrade path on a tower per rank.", 6, 10),
        TechNodeSpec("FUT_OMEGA", "Omega Singularity Ascension", "FutureTech", "+30% Global Tower Damage and +15% Global Attack Speed per rank.", 8, 15)
    )
}

enum class CommanderAbility(
    val id: String,
    val title: String,
    val shortName: String,
    val cooldownSec: Float,
    val colorArgb: Long,
    val description: String
) {
    ORBITAL_ION_STRIKE(
        id = "ION_STRIKE",
        title = "Siege & Orbital Bombardment",
        shortName = "☄️ SIEGE SALVO",
        cooldownSec = 18f,
        colorArgb = 0xFFC75B39,
        description = "Strikes the strongest enemies on the map with heavy siege/orbital fire, inflicting Burn, Stagger, and Cracked Armor!"
    ),
    ABSOLUTE_ZERO_NOVA(
        id = "CRYO_NOVA",
        title = "Absolute-Zero Winter Gale",
        shortName = "❄️ WINTER GALE",
        cooldownSec = 22f,
        colorArgb = 0xFF6B9AB0,
        description = "Freezes ALL enemies on the winding road for 4.5s and primes them for 3.5x Shatter!"
    ),
    HYPER_OVERCLOCK(
        id = "OVERCLOCK",
        title = "Sovereign Overclock Horn",
        shortName = "📯 RALLY HORN",
        cooldownSec = 24f,
        colorArgb = 0xFFD4AF37,
        description = "Doubles (2.0x) the attack speed and combo damage of ALL towers for 9.5 seconds!"
    ),
    GOLDEN_TRANSMUTATION(
        id = "ALCHEMIZE",
        title = "Royal & Quantum Dividend",
        shortName = "🪙 ROYAL LEVY",
        cooldownSec = 26f,
        colorArgb = 0xFFC59B27,
        description = "Grants an immediate Gold & Relic Shard windfall (scaling with Age & Wave!) and marks all active foes for 2x bounty!"
    )
}

/**
 * 18 HISTORICAL TO FAR-FUTURE CIVILIZATION AGES (Ages I through XVIII):
 * Advancing your Age directly evolves and upgrades ALL placed and future towers!
 */
enum class ClearancePhase(
    val phaseNum: Int,
    val badge: String,
    val title: String,
    val waveRangeLabel: String,
    val advanceCostGold: Long,
    val damageBonusMult: Float,
    val fireRateBonusMult: Float,
    val bonusMaxCastleHp: Int,
    val bonusBankInterestRate: Double,
    val unlockedTowerIds: Set<String>,
    val coachBanner: String,
    val accentColor: Long
) {
    PHASE_1_RECON(
        phaseNum = 1,
        badge = "AGE I",
        title = "Dark Age",
        waveRangeLabel = "Stone & Timber",
        advanceCostGold = 0L,
        damageBonusMult = 1.00f,
        fireRateBonusMult = 1.00f,
        bonusMaxCastleHp = 0,
        bonusBankInterestRate = 0.0,
        unlockedTowerIds = setOf("GATLING", "SNIPER", "MORTAR"),
        coachBanner = "Dark Age: Tap anywhere on the grass to place Rock Towers, Ballistas & Catapults!",
        accentColor = 0xFF8C8275
    ),
    PHASE_2_SYNERGY_BANK(
        phaseNum = 2,
        badge = "AGE II",
        title = "Feudal Age",
        waveRangeLabel = "Masonry & Trade",
        advanceCostGold = 250L,
        damageBonusMult = 1.22f,
        fireRateBonusMult = 1.10f,
        bonusMaxCastleHp = 30,
        bonusBankInterestRate = 0.015,
        unlockedTowerIds = setOf("GATLING", "SNIPER", "MORTAR", "HYDRO", "CRYO", "HUB"),
        coachBanner = "Feudal Age (+22% Tower Dmg): All towers evolve! Water Mill, Ice Keep & Royal Mint unlocked.",
        accentColor = 0xFFC59B27
    ),
    PHASE_3_SPECIALIST_STRIKES(
        phaseNum = 3,
        badge = "AGE III",
        title = "Castle Age",
        waveRangeLabel = "Alchemy & Siege",
        advanceCostGold = 600L,
        damageBonusMult = 1.48f,
        fireRateBonusMult = 1.20f,
        bonusMaxCastleHp = 65,
        bonusBankInterestRate = 0.03,
        unlockedTowerIds = setOf("GATLING", "SNIPER", "MORTAR", "HYDRO", "CRYO", "HUB", "PYRO", "VENOM", "DRONE", "SHIELD_GARRISON"),
        coachBanner = "Castle Age (+48% Tower Dmg): Towers evolve to Castle masonry! Pitch, Apothecary, Falcon & Bastion unlocked.",
        accentColor = 0xFFC85A28
    ),
    PHASE_4_TRACK_ENGINEERING(
        phaseNum = 4,
        badge = "AGE IV",
        title = "Imperial Age",
        waveRangeLabel = "Gunpowder & Wonders",
        advanceCostGold = 1_400L,
        damageBonusMult = 1.82f,
        fireRateBonusMult = 1.30f,
        bonusMaxCastleHp = 110,
        bonusBankInterestRate = 0.05,
        unlockedTowerIds = TowerType.buildableBaseTowers.map { it.id }.toSet(),
        coachBanner = "Imperial Age (+82% Tower Dmg): All 16 Base Towers unlocked including Culverin, Lens Spire & Gravity Well!",
        accentColor = 0xFFD4AF37
    ),
    PHASE_5_APEX_ENDLESS(
        phaseNum = 5,
        badge = "AGE V",
        title = "Wonder Age",
        waveRangeLabel = "Grand Empire",
        advanceCostGold = 3_000L,
        damageBonusMult = 2.28f,
        fireRateBonusMult = 1.40f,
        bonusMaxCastleHp = 160,
        bonusBankInterestRate = 0.075,
        unlockedTowerIds = TowerType.buildableBaseTowers.map { it.id }.toSet(),
        coachBanner = "Wonder Age (2.28x Tower Dmg): Combine Level 4+ nearby towers into 16 Monumental Wonders!",
        accentColor = 0xFFC75B39
    ),
    PHASE_6_GOLDEN_SOVEREIGN(
        phaseNum = 6,
        badge = "AGE VI",
        title = "Golden Age",
        waveRangeLabel = "Immortal Dynasty",
        advanceCostGold = 6_500L,
        damageBonusMult = 2.90f,
        fireRateBonusMult = 1.50f,
        bonusMaxCastleHp = 220,
        bonusBankInterestRate = 0.10,
        unlockedTowerIds = TowerType.buildableBaseTowers.map { it.id }.toSet(),
        coachBanner = "Golden Age (2.90x Tower Dmg): Sovereign gilded fortifications and high-yield Bullion Reserves!",
        accentColor = 0xFFE6C65A
    ),
    PHASE_7_GUNPOWDER_STARFORT(
        phaseNum = 7,
        badge = "AGE VII",
        title = "Gunpowder Age",
        waveRangeLabel = "Star Forts & Rifling",
        advanceCostGold = 14_000L,
        damageBonusMult = 3.85f,
        fireRateBonusMult = 1.64f,
        bonusMaxCastleHp = 300,
        bonusBankInterestRate = 0.13,
        unlockedTowerIds = TowerType.buildableBaseTowers.map { it.id }.toSet(),
        coachBanner = "Gunpowder Age (3.85x Tower Dmg): Towers evolve into Rifled Star-Forts, Flintlock Batteries & Rocket Forts!",
        accentColor = 0xFFB87333
    ),
    PHASE_8_INDUSTRIAL_STEAM(
        phaseNum = 8,
        badge = "AGE VIII",
        title = "Industrial Age",
        waveRangeLabel = "Bessemer Steel & Steam",
        advanceCostGold = 30_000L,
        damageBonusMult = 5.25f,
        fireRateBonusMult = 1.78f,
        bonusMaxCastleHp = 400,
        bonusBankInterestRate = 0.16,
        unlockedTowerIds = TowerType.buildableBaseTowers.map { it.id }.toSet(),
        coachBanner = "Industrial Age (5.25x Tower Dmg): Towers upgrade to Bessemer Steel, Steam Gatlings & Railway Berthas!",
        accentColor = 0xFFC87D3B
    ),
    PHASE_9_MACHINE_DYNAMO(
        phaseNum = 9,
        badge = "AGE IX",
        title = "Machine Age",
        waveRangeLabel = "Dynamos & Autocannons",
        advanceCostGold = 65_000L,
        damageBonusMult = 7.40f,
        fireRateBonusMult = 1.92f,
        bonusMaxCastleHp = 520,
        bonusBankInterestRate = 0.20,
        unlockedTowerIds = TowerType.buildableBaseTowers.map { it.id }.toSet(),
        coachBanner = "Machine Age (7.40x Tower Dmg): Towers evolve into Maxim Autocannons, 88mm Flak, Wardenclyffe Coils & Biplanes!",
        accentColor = 0xFF7FA8B8
    ),
    PHASE_10_ATOMIC_FISSION(
        phaseNum = 10,
        badge = "AGE X",
        title = "Atomic Age",
        waveRangeLabel = "Fission & Supersonics",
        advanceCostGold = 140_000L,
        damageBonusMult = 11.0f,
        fireRateBonusMult = 2.06f,
        bonusMaxCastleHp = 660,
        bonusBankInterestRate = 0.25,
        unlockedTowerIds = TowerType.buildableBaseTowers.map { it.id }.toSet(),
        coachBanner = "Atomic Age (11.0x Tower Dmg): Towers evolve into Vulcan Rotary Guns, Atomic Howitzers & Jet Interceptors!",
        accentColor = 0xFF68B078
    ),
    PHASE_11_INFORMATION_SILICON(
        phaseNum = 11,
        badge = "AGE XI",
        title = "Information Age",
        waveRangeLabel = "Silicon & Guided",
        advanceCostGold = 300_000L,
        damageBonusMult = 16.5f,
        fireRateBonusMult = 2.20f,
        bonusMaxCastleHp = 820,
        bonusBankInterestRate = 0.30,
        unlockedTowerIds = TowerType.buildableBaseTowers.map { it.id }.toSet(),
        coachBanner = "Information Age (16.5x Tower Dmg): Towers upgrade to Phalanx CIWS, Tomahawk Cruise Silos & Predator UAVs!",
        accentColor = 0xFF48A8D8
    ),
    PHASE_12_CYBERNETIC_NANO(
        phaseNum = 12,
        badge = "AGE XII",
        title = "Cybernetic Age",
        waveRangeLabel = "Neural & Nanotech",
        advanceCostGold = 650_000L,
        damageBonusMult = 25.0f,
        fireRateBonusMult = 2.34f,
        bonusMaxCastleHp = 1000,
        bonusBankInterestRate = 0.36,
        unlockedTowerIds = TowerType.buildableBaseTowers.map { it.id }.toSet(),
        coachBanner = "Cybernetic Age (25.0x Tower Dmg): Towers evolve into Gauss Chainguns, Nanite Vats & X-Ray FEL Lasers!",
        accentColor = 0xFF4FD1C5
    ),
    PHASE_13_FUSION_PLASMA(
        phaseNum = 13,
        badge = "AGE XIII",
        title = "Fusion Age",
        waveRangeLabel = "Tokamak Plasma",
        advanceCostGold = 1_500_000L,
        damageBonusMult = 38.0f,
        fireRateBonusMult = 2.54f,
        bonusMaxCastleHp = 1250,
        bonusBankInterestRate = 0.43,
        unlockedTowerIds = TowerType.buildableBaseTowers.map { it.id }.toSet(),
        coachBanner = "Fusion Age (38.0x Tower Dmg): Towers upgrade to Plasma Repeaters, Magnetic Coilguns & Graviton Wells!",
        accentColor = 0xFF64C8F0
    ),
    PHASE_14_STARFLEET_ORBITAL(
        phaseNum = 14,
        badge = "AGE XIV",
        title = "Starfleet Age",
        waveRangeLabel = "Orbital Overwatch",
        advanceCostGold = 3_500_000L,
        damageBonusMult = 60.0f,
        fireRateBonusMult = 2.74f,
        bonusMaxCastleHp = 1550,
        bonusBankInterestRate = 0.52,
        unlockedTowerIds = TowerType.buildableBaseTowers.map { it.id }.toSet(),
        coachBanner = "Starfleet Age (60.0x Tower Dmg): Unlocks 4-Path Omni-Crosspathing & evolves towers to Orbital MACs and Star-Carriers!",
        accentColor = 0xFF7CA8E8
    ),
    PHASE_15_DYSON_STELLAR(
        phaseNum = 15,
        badge = "AGE XV",
        title = "Dyson Age",
        waveRangeLabel = "Stellar Megastructures",
        advanceCostGold = 8_500_000L,
        damageBonusMult = 95.0f,
        fireRateBonusMult = 2.94f,
        bonusMaxCastleHp = 1900,
        bonusBankInterestRate = 0.62,
        unlockedTowerIds = TowerType.buildableBaseTowers.map { it.id }.toSet(),
        coachBanner = "Dyson Age (95.0x Tower Dmg): Towers channel Dyson Stellar power with Antimatter Bombardment & Nicol-Beams!",
        accentColor = 0xFFE6C65A
    ),
    PHASE_16_QUANTUM_ZEROPOINT(
        phaseNum = 16,
        badge = "AGE XVI",
        title = "Quantum Age",
        waveRangeLabel = "Zero-Point Matrix",
        advanceCostGold = 22_000_000L,
        damageBonusMult = 150.0f,
        fireRateBonusMult = 3.14f,
        bonusMaxCastleHp = 2350,
        bonusBankInterestRate = 0.75,
        unlockedTowerIds = TowerType.buildableBaseTowers.map { it.id }.toSet(),
        coachBanner = "Quantum Age (150x Tower Dmg): Towers evolve into Tachyon Hyper-Gatlings, Phase Disruptors & Black-Hole Wells!",
        accentColor = 0xFFA290BD
    ),
    PHASE_17_DIMENSIONAL_CHRONO(
        phaseNum = 17,
        badge = "AGE XVII",
        title = "Dimensional Age",
        waveRangeLabel = "Chrono-Rift Continuum",
        advanceCostGold = 60_000_000L,
        damageBonusMult = 240.0f,
        fireRateBonusMult = 3.34f,
        bonusMaxCastleHp = 2900,
        bonusBankInterestRate = 0.90,
        unlockedTowerIds = TowerType.buildableBaseTowers.map { it.id }.toSet(),
        coachBanner = "Dimensional Age (240x Tower Dmg): Towers strike across timelines with Void-Rift Silos & Quasar Spires!",
        accentColor = 0xFFC084FC
    ),
    PHASE_18_OMEGA_TRANSCENDENT(
        phaseNum = 18,
        badge = "AGE XVIII",
        title = "Omega Age",
        waveRangeLabel = "Omniversal Horizon",
        advanceCostGold = 180_000_000L,
        damageBonusMult = 400.0f,
        fireRateBonusMult = 3.60f,
        bonusMaxCastleHp = 3600,
        bonusBankInterestRate = 1.15,
        unlockedTowerIds = TowerType.buildableBaseTowers.map { it.id }.toSet(),
        coachBanner = "Omega Age (400x Tower Dmg, +260% Speed): Ultimate Omniversal Citadels unlocked across the Eternal Meadow!",
        accentColor = 0xFFF5D76E
    );

    companion object {
        fun forAgeLevel(level: Int): ClearancePhase =
            entries.getOrElse((level - 1).coerceIn(0, entries.lastIndex)) { PHASE_1_RECON }

        fun naturalAgeForWave(wave: Int): Int = when {
            wave <= 3 -> 1
            wave <= 7 -> 2
            wave <= 12 -> 3
            wave <= 18 -> 4
            wave <= 26 -> 5
            wave <= 35 -> 6
            wave <= 45 -> 7
            wave <= 56 -> 8
            wave <= 68 -> 9
            wave <= 82 -> 10
            wave <= 96 -> 11
            wave <= 112 -> 12
            wave <= 130 -> 13
            wave <= 150 -> 14
            wave <= 175 -> 15
            wave <= 205 -> 16
            wave <= 240 -> 17
            else -> 18
        }

        fun forWave(wave: Int): ClearancePhase = forAgeLevel(naturalAgeForWave(wave))
    }
}

enum class WaveMutator(
    val badge: String,
    val title: String,
    val summary: String,
    val hpMult: Float,
    val speedMult: Float,
    val countMult: Float,
    val bountyMult: Float,
    val colorArgb: Long
) {
    STANDARD(
        badge = "STD",
        title = "Standard March",
        summary = "Balanced raider column.",
        hpMult = 1.0f,
        speedMult = 1.0f,
        countMult = 1.0f,
        bountyMult = 1.0f,
        colorArgb = 0xFFD4AF37
    ),
    BLITZ_SURGE(
        badge = "🐎 CAVALRY",
        title = "Outrider & Hover Raid",
        summary = "+25% Raider Speed, +30% Gold Bounty! Use Ice Keeps, Gravity Wells & Water Mills.",
        hpMult = 0.92f,
        speedMult = 1.25f,
        countMult = 1.05f,
        bountyMult = 1.30f,
        colorArgb = 0xFFD9B44A
    ),
    REACTIVE_PLATING(
        badge = "🛡️ IRONCLAD",
        title = "Heavy Siege Column",
        summary = "+25% Armor & Shields, +35% Bounty! Use Railguns, Catapults & Apothecary Acid.",
        hpMult = 1.25f,
        speedMult = 0.92f,
        countMult = 1.0f,
        bountyMult = 1.35f,
        colorArgb = 0xFFA67B5B
    ),
    SWARM_OVERDRIVE(
        badge = "⚔️ HORDE",
        title = "Great Raider Horde",
        summary = "+35% Pack Size! Catapults, Lasers, and Boiling Pitch excel.",
        hpMult = 0.85f,
        speedMult = 1.08f,
        countMult = 1.35f,
        bountyMult = 1.25f,
        colorArgb = 0xFF7FA852
    ),
    TITAN_ESCORT(
        badge = "👑 ROYAL GUARD",
        title = "Imperial & Starfleet Escort",
        summary = "Elite Siege Guard (+40% HP, +50% Gold Bounty)!",
        hpMult = 1.40f,
        speedMult = 0.90f,
        countMult = 0.90f,
        bountyMult = 1.50f,
        colorArgb = 0xFFC75B39
    ),
    QUANTUM_ANOMALY(
        badge = "⚛️ QUANTUM",
        title = "Chrono-Phase Surge",
        summary = "+50% HP, +15% Speed, +75% Gold Bounty!",
        hpMult = 1.50f,
        speedMult = 1.15f,
        countMult = 1.10f,
        bountyMult = 1.75f,
        colorArgb = 0xFFA290BD
    );

    companion object {
        fun forWave(wave: Int): WaveMutator {
            if (wave < 9) return STANDARD
            return when (wave % 7) {
                0 -> TITAN_ESCORT
                2 -> BLITZ_SURGE
                4 -> SWARM_OVERDRIVE
                5 -> REACTIVE_PLATING
                6 -> if (wave >= 35) QUANTUM_ANOMALY else STANDARD
                else -> STANDARD
            }
        }
    }
}

data class BankVaultTierSpec(
    val tier: Int,
    val name: String,
    val waveInterestRate: Double, // e.g. 0.12 = 12% per wave
    val liveTickRate: Double,     // e.g. 0.015 = 1.5% every 6 seconds of active combat
    val maxVaultCap: Long,        // UNLIMITED (CurrencyMath.MAX_TREASURY_GOLD) so money never hits a wall!
    val upgradeCostGold: Long
)

/**
 * 15 SOVEREIGN BANK VAULT TIERS WITH UNLIMITED VAULT CAPACITY!
 * Fixes all money max limits — your Bank Balance and Wave Interest grow without artificial ceilings!
 */
object BankCatalog {
    private const val UNLIMITED_CAP = CurrencyMath.MAX_TREASURY_GOLD

    val tiers: List<BankVaultTierSpec> = listOf(
        BankVaultTierSpec(1, "Tier I: Village Strongbox", 0.14, 0.018, UNLIMITED_CAP, 0L),
        BankVaultTierSpec(2, "Tier II: Feudal Guild Vault", 0.18, 0.024, UNLIMITED_CAP, 600L),
        BankVaultTierSpec(3, "Tier III: Castle Keep Treasury", 0.23, 0.030, UNLIMITED_CAP, 2_000L),
        BankVaultTierSpec(4, "Tier IV: Royal Mint Reserve", 0.28, 0.038, UNLIMITED_CAP, 7_500L),
        BankVaultTierSpec(5, "Tier V: Sovereign Imperial Bank", 0.34, 0.046, UNLIMITED_CAP, 25_000L),
        BankVaultTierSpec(6, "Tier VI: Grand World Exchange", 0.40, 0.055, UNLIMITED_CAP, 90_000L),
        BankVaultTierSpec(7, "Tier VII: Immortal Crown Treasury", 0.48, 0.065, UNLIMITED_CAP, 350_000L),
        BankVaultTierSpec(8, "Tier VIII: Industrial Bullion Clearinghouse", 0.56, 0.075, UNLIMITED_CAP, 1_200_000L),
        BankVaultTierSpec(9, "Tier IX: Dynamo Central Reserve", 0.65, 0.088, UNLIMITED_CAP, 4_500_000L),
        BankVaultTierSpec(10, "Tier X: Atomic Isotope Depository", 0.75, 0.100, UNLIMITED_CAP, 16_000_000L),
        BankVaultTierSpec(11, "Tier XI: Silicon Fiber Bourse", 0.86, 0.115, UNLIMITED_CAP, 60_000_000L),
        BankVaultTierSpec(12, "Tier XII: Cybernetic Quantum Ledger", 0.98, 0.130, UNLIMITED_CAP, 220_000_000L),
        BankVaultTierSpec(13, "Tier XIII: Starfleet Orbital Mint", 1.12, 0.150, UNLIMITED_CAP, 850_000_000L),
        BankVaultTierSpec(14, "Tier XIV: Dyson Stellar Treasury", 1.30, 0.175, UNLIMITED_CAP, 3_500_000_000L),
        BankVaultTierSpec(15, "Tier XV: Omega Infinite Singularity Bank", 1.50, 0.200, UNLIMITED_CAP, 15_000_000_000L)
    )

    fun specForTier(tier: Int): BankVaultTierSpec =
        tiers.getOrElse((tier - 1).coerceIn(0, tiers.lastIndex)) { tiers.first() }
}

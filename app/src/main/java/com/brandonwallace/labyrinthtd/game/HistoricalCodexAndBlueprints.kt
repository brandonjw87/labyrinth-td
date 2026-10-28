package com.brandonwallace.labyrinthtd.game

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * =================================================================================================
 * IMPERIAL ARCHIVES, ARCHITECTURAL SCHEMATICS & PROCEDURAL 4K TEXTURE / TELEMETRY CODEX
 * =================================================================================================
 * Provides:
 * 1. Complete 18-Age Civilization Chronicle (650 AD Dark Age -> 42,000+ AD Omega Age)
 * 2. Architectural Blueprints & Ballistic Engineering Specs for all 16 Base Towers & 16 Wonders
 * 3. Threat Intelligence Dossiers for all 32 Historical, Industrial, Cybernetic & Cosmic Raiders
 * 4. Procedural High-Resolution Tileable Texture & Harmonic Wavetable Synthesis Engine
 * =================================================================================================
 */

data class EraChronicleEntry(
    val ageNumber: Int,
    val romanBadge: String,
    val eraTitle: String,
    val historicalYearSpan: String,
    val masonryAndMaterials: String,
    val primaryPowerSource: String,
    val militaryDoctrineSummary: String,
    val architecturalMilestone: String,
    val chronicleLore: String
)

data class TowerBlueprintDossier(
    val towerType: TowerType,
    val codeDesignation: String,
    val structuralClass: String,
    val darkAgeFoundationSpec: String,
    val industrialEvolutionSpec: String,
    val cosmicOmegaEvolutionSpec: String,
    val primarySynergyPartner: String,
    val grandWonderResult: String,
    val tacticalPlacementAdvice: String
)

data class RaiderIntelligenceFile(
    val enemyType: EnemyType,
    val originAgeNum: Int,
    val armorClassification: String,
    val propulsionMechanism: String,
    val primaryWeakness: String,
    val counterTowerRecommendation: String
)

object HistoricalCodexAndBlueprints {

    val eraChronicles: List<EraChronicleEntry> = listOf(
        EraChronicleEntry(
            ageNumber = 1,
            romanBadge = "Age I",
            eraTitle = "Dark Age",
            historicalYearSpan = "500 AD – 850 AD",
            masonryAndMaterials = "Rough-hewn fieldstone, oak palisade timber, hemp cordage, wrought iron",
            primaryPowerSource = "Torsion skeins, counter-weights, human draw-strength",
            militaryDoctrineSummary = "Establish overlapping crossfire at the first three hairpin bends of the meadow road.",
            architecturalMilestone = "Motte-and-Bailey Wooden Keep & Watchtower Palisade",
            chronicleLore = "From humble green pastures and stacked river-stone foundations, the first frontier garrison rises to guard the valley road against marauding footmen."
        ),
        EraChronicleEntry(
            ageNumber = 2,
            romanBadge = "Age II",
            eraTitle = "Feudal Age",
            historicalYearSpan = "850 AD – 1150 AD",
            masonryAndMaterials = "Squared ashlar limestone, seasoned yew staves, lime mortar, quenched steel",
            primaryPowerSource = "Compound windlass cranks, hydraulic sluice gates, bellows-fed braziers",
            militaryDoctrineSummary = "Pair Frost Moat sluices with Mangonel artillery to shatter slowed infantry columns.",
            architecturalMilestone = "Feudal Stone Manor, Sluice Gate & Falconry Roost",
            chronicleLore = "Feudal lords organize stone quarries and guild craftsmen, replacing timber palisades with mortared limestone curtain walls and moat-fed frost traps."
        ),
        EraChronicleEntry(
            ageNumber = 3,
            romanBadge = "Age III",
            eraTitle = "Castle Age",
            historicalYearSpan = "1150 AD – 1400 AD",
            masonryAndMaterials = "Concentric granite bastions, cast bronze bells, Greek fire naphtha vats",
            primaryPowerSource = "Counterweight trebuchets, pressurized pitch siphons, chartered guild mints",
            militaryDoctrineSummary = "Ignite frozen raiders with Pitch Braziers for Thermal Shock and fund Royal Mints.",
            architecturalMilestone = "Concentric Keep, Alchemist Lab & Chartered Royal Mint",
            chronicleLore = "Massive counterweight trebuchets and alchemical pitch cauldrons turn every bend of the labyrinth into a fortified kill-zone while Royal Mints compound the realm's treasury."
        ),
        EraChronicleEntry(
            ageNumber = 4,
            romanBadge = "Age IV",
            eraTitle = "Imperial Age",
            historicalYearSpan = "1400 AD – 1550 AD",
            masonryAndMaterials = "Star-fort glacis masonry, bell-metal bronze cannon barrels, corned black powder",
            primaryPowerSource = "Saltpeter-sulfur-charcoal deflagration, clockwork escapement gears",
            militaryDoctrineSummary = "Deploy Culverin Batteries down long road straightaways to pierce heavy barding.",
            architecturalMilestone = "Trace Italienne Star Citadel & Royal Foundry",
            chronicleLore = "Master founders cast long-barreled bronze culverins and spring-driven clockwork towers, ushering in the era of high-velocity ballistic warfare."
        ),
        EraChronicleEntry(
            ageNumber = 5,
            romanBadge = "Age V",
            eraTitle = "Enlightenment Age",
            historicalYearSpan = "1550 AD – 1750 AD",
            masonryAndMaterials = "Polished marble colonnades, crown-flint achromatic lenses, brass Leyden jars",
            primaryPowerSource = "Electrostatic induction generators, focused solar heliostats, maritime finance",
            militaryDoctrineSummary = "Combine Water Cisterns with Lightning Spires for chain-lightning Electro-Conduction.",
            architecturalMilestone = "Royal Academy of Sciences & Electrostatic Observatory",
            chronicleLore = "Natural philosophers harness optical refraction and atmospheric electricity, erecting prism arrays and lightning spires alongside sovereign central banks."
        ),
        EraChronicleEntry(
            ageNumber = 6,
            romanBadge = "Age VI",
            eraTitle = "Golden Age",
            historicalYearSpan = "1750 AD – 1815 AD",
            masonryAndMaterials = "Gilded imperial granite, precision chronometer brass, sovereign bullion vaults",
            primaryPowerSource = "High-pressure early steam engines, voltaic piles, grand wonder architecture",
            militaryDoctrineSummary = "Fuse neighboring Level-4 towers into Grand Wonders to anchor the labyrinth.",
            architecturalMilestone = "Grand Imperial Pantheon & Sovereign Bullion Reserve",
            chronicleLore = "At the zenith of pre-industrial civilization, master architects fuse paired guild towers into monumental Grand Wonders that dominate the battlefield."
        ),
        EraChronicleEntry(
            ageNumber = 7,
            romanBadge = "Age VII",
            eraTitle = "Gunpowder Age",
            historicalYearSpan = "1815 AD – 1865 AD",
            masonryAndMaterials = "Rifled wrought-iron barrels, percussion fulminate caps, brick casemates",
            primaryPowerSource = "Smokeless nitrocellulose propellant, rotary hand-cranked cams",
            militaryDoctrineSummary = "Unlock Tier 6 tower upgrades and suppress massed musketeer lines with rotary fire.",
            architecturalMilestone = "Polygonal Rifled Casemate Fort & Arsenal Works",
            chronicleLore = "Rifled ordnance and percussion shells multiply effective range and armor penetration, transforming medieval watchtowers into rapid-fire casemate batteries."
        ),
        EraChronicleEntry(
            ageNumber = 8,
            romanBadge = "Age VIII",
            eraTitle = "Industrial Age",
            historicalYearSpan = "1865 AD – 1905 AD",
            masonryAndMaterials = "Bessemer open-hearth steel, riveted boiler plate, reinforced ferro-concrete",
            primaryPowerSource = "Triple-expansion coal steam turbines, hydraulic recoil recuperators",
            militaryDoctrineSummary = "Equip Bessemer Steam Turbines in the Armory to boost fire rate across all batteries.",
            architecturalMilestone = "Bessemer Steel Foundry & Steam Dreadnought Redoubt",
            chronicleLore = "Coal-fired steam turbines and Bessemer steel armor plate replace stone and timber across the entire valley, driving mechanized rotary turrets at blistering speeds."
        ),
        EraChronicleEntry(
            ageNumber = 9,
            romanBadge = "Age IX",
            eraTitle = "Machine Age",
            historicalYearSpan = "1905 AD – 1945 AD",
            masonryAndMaterials = "Krupp cemented chrome-nickel armor, tungsten-steel barrels, high-voltage copper",
            primaryPowerSource = "Polyphase AC turbo-alternators, internal combustion diesel-electric drives",
            militaryDoctrineSummary = "Counter armored Landship Tanks with high-caliber railway siege mortars and Tesla arcs.",
            architecturalMilestone = "High-Voltage Grid Substation & Flak Command Bunker",
            chronicleLore = "Electrified power grids link every turret on the meadow, feeding high-voltage Wardenclyffe coils and radar-directed anti-air batteries."
        ),
        EraChronicleEntry(
            ageNumber = 10,
            romanBadge = "Age X",
            eraTitle = "Atomic Age",
            historicalYearSpan = "1945 AD – 1975 AD",
            masonryAndMaterials = "Lead-lined prestressed concrete, depleted uranium penetrators, zirconium cladding",
            primaryPowerSource = "Pressurized heavy-water fission reactors, magnetron microwave klystrons",
            militaryDoctrineSummary = "Advance primary tower paths to Tier 7 and equip Depleted Uranium Sabots.",
            architecturalMilestone = "Pressurized Fission Core & Strategic Radar Dome",
            chronicleLore = "Compact nuclear fission piles provide virtually limitless megawatt power to every tower, enabling hyper-velocity rail prototypes and solid-state laser arrays."
        ),
        EraChronicleEntry(
            ageNumber = 11,
            romanBadge = "Age XI",
            eraTitle = "Information Age",
            historicalYearSpan = "1975 AD – 2025 AD",
            masonryAndMaterials = "Carbon-fiber composites, reactive ceramic armor tiles, gallium-arsenide optics",
            primaryPowerSource = "Phased-array digital fire-control networks, fiber-optic laser pump diodes",
            militaryDoctrineSummary = "Use Silicon Radar Uplinks to reveal Stealth Jets and snipe bosses from across the map.",
            architecturalMilestone = "Aegis Phased-Array Command Center & Algorithmic Exchange",
            chronicleLore = "Satellite telemetry and sub-millisecond fire-control computers network every turret into a single unified defense grid."
        ),
        EraChronicleEntry(
            ageNumber = 12,
            romanBadge = "Age XII",
            eraTitle = "Cybernetic Age",
            historicalYearSpan = "2025 AD – 2120 AD",
            masonryAndMaterials = "Self-healing metamaterial alloy, graphene nanotube weave, neural-link optics",
            primaryPowerSource = "Superconducting SMES rings, autonomous grey-goo nanite fabricators",
            militaryDoctrineSummary = "Deploy Nanite Swarm Hives and Cyber Nanite Injectors to dissolve mech armor.",
            architecturalMilestone = "Neural-Grid Arcology Spire & Nanite Forging Vat",
            chronicleLore = "Programmable nanite swarms and neural-linked AI gunners allow towers to self-repair, adapt ammunition mid-flight, and disassemble enemy mechs at the molecular level."
        ),
        EraChronicleEntry(
            ageNumber = 13,
            romanBadge = "Age XIII",
            eraTitle = "Fusion Age",
            historicalYearSpan = "2120 AD – 2280 AD",
            masonryAndMaterials = "Tokamak diverter tungsten-rhenium, superconducting yttrium-barium tape",
            primaryPowerSource = "Deuterium-Helium-3 aneutronic magnetic-confinement fusion cores",
            militaryDoctrineSummary = "Unlock Tier 8 Apex Doctrines on all base tower paths for colossal damage multipliers.",
            architecturalMilestone = "Helium-3 Tokamak Citadel & Magnetic Rail Spire",
            chronicleLore = "Miniature star-hot fusion reactors sit beneath every emplacement, unlocking Tier 8 Apex Doctrines and continuous relativistic particle beams."
        ),
        EraChronicleEntry(
            ageNumber = 14,
            romanBadge = "Age XIV",
            eraTitle = "Starfleet Age",
            historicalYearSpan = "2280 AD – 2600 AD",
            masonryAndMaterials = "Tritanium hull plating, transparent aluminum domes, graviton field coils",
            primaryPowerSource = "Orbital antimatter power relays, artificial gravity well projectors",
            militaryDoctrineSummary = "Starfleet Omni-Crosspathing unlocked! Upgrade all 4 paths (8-8-8-8) on every tower!",
            architecturalMilestone = "Orbital Fleet Drydock & Graviton Bastion Keep",
            chronicleLore = "With orbital shipyard uplinks online, standard crosspath restrictions vanish—every tower on the field can now be upgraded to full 8-8-8-8 Omni-Mastery!"
        ),
        EraChronicleEntry(
            ageNumber = 15,
            romanBadge = "Age XV",
            eraTitle = "Dyson Age",
            historicalYearSpan = "2600 AD – 4500 AD",
            masonryAndMaterials = "Solar-forged neutronium lattice, statite mirror sails, stellar plasma conduits",
            primaryPowerSource = "Kardashev Type-II Dyson Swarm stellar collector beams (3.8 × 10²⁶ Watts)",
            militaryDoctrineSummary = "Fuse Dyson Helios Arrays and Leviathan Gauss Citadels to vaporize cosmic fleets.",
            architecturalMilestone = "Matrioshka Dyson Relay & Stellar Crown Vault",
            chronicleLore = "An entire star's output is channeled directly into your citadel's defense grid, turning every laser array and prism tower into a miniature solar flare."
        ),
        EraChronicleEntry(
            ageNumber = 16,
            romanBadge = "Age XVI",
            eraTitle = "Quantum Age",
            historicalYearSpan = "4500 AD – 12,000 AD",
            masonryAndMaterials = "Phase-locked Bose-Einstein condensate crystal, entangled chronon waveguides",
            primaryPowerSource = "Zero-point quantum vacuum energy extraction",
            militaryDoctrineSummary = "Use Quantum Chrono Matrices to lock Chrono Wraiths and Quantum Phantoms in time.",
            architecturalMilestone = "Zero-Point Entanglement Nexus & Planck Vault",
            chronicleLore = "Projectiles tunnel directly through enemy shields via quantum superposition, striking targets across multiple probability states simultaneously."
        ),
        EraChronicleEntry(
            ageNumber = 17,
            romanBadge = "Age XVII",
            eraTitle = "Dimensional Age",
            historicalYearSpan = "12,000 AD – 28,000 AD",
            masonryAndMaterials = "Folded 11-dimensional Calabi-Yau manifold geometry, hyper-spatial tesseracts",
            primaryPowerSource = "Inter-brane metric shear & multiverse siphon conduits",
            militaryDoctrineSummary = "Combine Gravity Wells and Chrono Towers into Omega Singularity Cores.",
            architecturalMilestone = "Tesseract Horizon Spire & Multiverse Treasury",
            chronicleLore = "The labyrinth itself is anchored across eleven spatial dimensions; raiders marching along the road face crossfire from parallel timelines at once."
        ),
        EraChronicleEntry(
            ageNumber = 18,
            romanBadge = "Age XVIII",
            eraTitle = "Omega Age",
            historicalYearSpan = "42,000+ AD (End of Time)",
            masonryAndMaterials = "Immutable acausal astronium, transcendent golden chronon weave",
            primaryPowerSource = "The Omega Point Singularity — infinite convergent cosmic consciousness & energy",
            militaryDoctrineSummary = "Ascend towers to ★25 Omega Stars and command all 16 Grand Wonders in unison.",
            architecturalMilestone = "The Eternal Omega Citadel",
            chronicleLore = "Civilization transcends physical entropy. Every watchtower placed on the ancient meadow now stands as an immortal Omega spire commanding 400x base firepower."
        )
    )

    val towerBlueprints: List<TowerBlueprintDossier> = listOf(
        TowerBlueprintDossier(
            towerType = TowerType.GATLING,
            codeDesignation = "BAL-01",
            structuralClass = "Rapid Kinetic Suppressor",
            darkAgeFoundationSpec = "Timber palisade turret with dual repeating crossbows and iron-tipped bolts.",
            industrialEvolutionSpec = "Water-cooled Maxim & Bessemer rotary casemate firing 1,200 rounds/min.",
            cosmicOmegaEvolutionSpec = "Omega Chronon Repeater firing relativistic tachyon flechettes at 3.6x cycle rate.",
            primarySynergyPartner = "Alchemist Lab (VENOM) or Pitch Brazier (PYRO)",
            grandWonderResult = "Plaguebringer Keep (PLAGUEBRINGER_VULCAN)",
            tacticalPlacementAdvice = "Place on inner hairpin corners where high fire rate shreds swarms and strips boss shields."
        ),
        TowerBlueprintDossier(
            towerType = TowerType.SNIPER,
            codeDesignation = "BAL-02",
            structuralClass = "Long-Range Precision Marksman",
            darkAgeFoundationSpec = "Elevated stone belfry manned by Welsh yew longbowmen with bodkin arrows.",
            industrialEvolutionSpec = "Optical rangefinder steel cupola with high-velocity smokeless-powder rifles.",
            cosmicOmegaEvolutionSpec = "Omega Tachyon Lance Platform striking anywhere on the map with 92% armor pierce.",
            primarySynergyPartner = "Lightning Spire (TESLA) or Gauss Railgun (RAIL_CANNON)",
            grandWonderResult = "Archimedes Iron Cannon (TACHYON_RAILGUN)",
            tacticalPlacementAdvice = "Position on Elevated Stone bonuses near the map center for full-meadow boss execution."
        ),
        TowerBlueprintDossier(
            towerType = TowerType.MORTAR,
            codeDesignation = "ART-03",
            structuralClass = "Heavy Area-of-Effect Siege Artillery",
            darkAgeFoundationSpec = "Counterweight mangonel and trebuchet hurling bound stone payloads.",
            industrialEvolutionSpec = "Krupp 420mm rifled railway siege howitzer with high-explosive shells.",
            cosmicOmegaEvolutionSpec = "Omega Supernova Bombardier lobbing miniature antimatter star-cores.",
            primarySynergyPartner = "Frost Moat (CRYO)",
            grandWonderResult = "Avalanche Trebuchet (CRYO_QUAKE_COLOSSUS)",
            tacticalPlacementAdvice = "Aim at road bottlenecks where Frost Moats or Gravity Wells cluster enemies together."
        ),
        TowerBlueprintDossier(
            towerType = TowerType.CRYO,
            codeDesignation = "ELM-04",
            structuralClass = "Cryogenic Crowd-Control Sluice",
            darkAgeFoundationSpec = "Glacial mountain sluice gate flooding the road with freezing slush.",
            industrialEvolutionSpec = "Liquid-nitrogen ammonia compressor spraying sub-zero frost clouds.",
            cosmicOmegaEvolutionSpec = "Omega Entropy Stasis Spire locking raiders at absolute zero (0 Kelvin).",
            primarySynergyPartner = "Trebuchet (MORTAR) or Pitch Brazier (PYRO)",
            grandWonderResult = "Avalanche Trebuchet (CRYO_QUAKE_COLOSSUS)",
            tacticalPlacementAdvice = "Place slightly upstream of artillery and railguns so targets are brittle-frozen on impact."
        ),
        TowerBlueprintDossier(
            towerType = TowerType.PYRO,
            codeDesignation = "ELM-05",
            structuralClass = "Thermal Incendiary Projector",
            darkAgeFoundationSpec = "Bronze siphon spraying Greek fire pitch across the cobblestones.",
            industrialEvolutionSpec = "Pressurized thermite-napalm projector with white-phosphorus igniters.",
            cosmicOmegaEvolutionSpec = "Omega Big-Bang Plasma Jet unleashing 100-million-degree stellar fire.",
            primarySynergyPartner = "Alchemist Lab (VENOM) or Frost Moat (CRYO)",
            grandWonderResult = "Dragonfire Citadel (BIO_NAPALM_FORTRESS)",
            tacticalPlacementAdvice = "Pair with Frost Moat for Thermal Shock bursts or Alchemist Lab for Alchemical Detonation."
        ),
        TowerBlueprintDossier(
            towerType = TowerType.TESLA,
            codeDesignation = "ARC-06",
            structuralClass = "Chain-Lightning Electrostatic Spire",
            darkAgeFoundationSpec = "Copper-clad lodestone obelisk drawing storm lightning from the clouds.",
            industrialEvolutionSpec = "High-voltage Wardenclyffe resonant transformer arcing across 6+ targets.",
            cosmicOmegaEvolutionSpec = "Omega Vacuum Arc Singularity discharging multi-gigawatt cosmic bolts.",
            primarySynergyPartner = "Water Cistern (HYDRO) or Sun Mirror (PRISM)",
            grandWonderResult = "Poseidon Storm Keep (TEMPEST_MAELSTROM)",
            tacticalPlacementAdvice = "Pair with Water Cisterns so WET raiders conduct +125% chain-lightning damage."
        ),
        TowerBlueprintDossier(
            towerType = TowerType.VENOM,
            codeDesignation = "ALC-07",
            structuralClass = "Corrosive Armor-Stripping Alchemist",
            darkAgeFoundationSpec = "Monastic apothecary distilling vitriol acid flasks and hemlock.",
            industrialEvolutionSpec = "Chemical munitions plant dispersing fluoroantimonic super-acid mist.",
            cosmicOmegaEvolutionSpec = "Omega Ontological Dissolver unraveling atomic bonds and armor plating.",
            primarySynergyPartner = "Watchtower (GATLING) or Falconry Roost (DRONE)",
            grandWonderResult = "Plaguebringer Keep / Nanite Swarm Hive",
            tacticalPlacementAdvice = "Place near the entrance of the labyrinth so heavy armor is corroded before bosses reach your main DPS."
        ),
        TowerBlueprintDossier(
            towerType = TowerType.HYDRO,
            codeDesignation = "ELM-08",
            structuralClass = "Hydraulic Knockback & Conduction Cistern",
            darkAgeFoundationSpec = "Roman-style stone aqueduct cistern unleashing surging water waves.",
            industrialEvolutionSpec = "High-pressure steam-driven hydraulic water cannon stripping camo.",
            cosmicOmegaEvolutionSpec = "Omega Dimensional Tidal Gate sweeping raiders backward along the road.",
            primarySynergyPartner = "Lightning Spire (TESLA)",
            grandWonderResult = "Poseidon Storm Keep (TEMPEST_MAELSTROM)",
            tacticalPlacementAdvice = "Essential for soaking enemies in the WET status to amplify Lightning Spires and Frost Moats."
        ),
        TowerBlueprintDossier(
            towerType = TowerType.PRISM,
            codeDesignation = "ARC-09",
            structuralClass = "Continuous Coherent Beam Refractor",
            darkAgeFoundationSpec = "Polished bronze heliostat mirror focusing sunlight onto siege engines.",
            industrialEvolutionSpec = "Carbon-arc searchlight and ruby crystal optical resonator.",
            cosmicOmegaEvolutionSpec = "Omega Quasar Beam Spire channeling relativistic gamma-ray jets.",
            primarySynergyPartner = "Royal Mint (HUB) or Helios Laser Array (LASER_ARRAY)",
            grandWonderResult = "Cathedral of Light / Dyson Helios Array",
            tacticalPlacementAdvice = "Excels against high-HP bosses where continuous beam ramp-up reaches maximum intensity."
        ),
        TowerBlueprintDossier(
            towerType = TowerType.CHRONO,
            codeDesignation = "ARC-10",
            structuralClass = "Temporal Deceleration & Recall Astrolabe",
            darkAgeFoundationSpec = "Water-clock sundial belfry and brass astrolabe coordinating volley fire.",
            industrialEvolutionSpec = "Precision marine chronometer tower and Lorentz time-dilation coil.",
            cosmicOmegaEvolutionSpec = "Omega Acausal Time Spire freezing enemies outside the flow of time.",
            primarySynergyPartner = "Falconry Roost (DRONE) or Helios Laser Array (LASER_ARRAY)",
            grandWonderResult = "Da Vinci Clocktower / Quantum Chrono Matrix",
            tacticalPlacementAdvice = "Place near your highest-DPS Wonders to buff their attack speed and teleport bosses backward."
        ),
        TowerBlueprintDossier(
            towerType = TowerType.DRONE,
            codeDesignation = "SUM-11",
            structuralClass = "Autonomous Aerial Hunter Squadron",
            darkAgeFoundationSpec = "Royal falconry mews launching trained peregrine hunting hawks.",
            industrialEvolutionSpec = "Biplane & gyrocopter aerodrome scrambling armed scout aircraft.",
            cosmicOmegaEvolutionSpec = "Omega Seraphim Swarm Carrier deploying autonomous combat interceptors.",
            primarySynergyPartner = "Sundial Belfry (CHRONO) or Alchemist Lab (VENOM)",
            grandWonderResult = "Da Vinci Clocktower / Nanite Swarm Hive",
            tacticalPlacementAdvice = "Provides global harassment and gold-per-second bounty collection during active waves."
        ),
        TowerBlueprintDossier(
            towerType = TowerType.HUB,
            codeDesignation = "ECO-12",
            structuralClass = "Sovereign Treasury Mint & Guild Aura Hub",
            darkAgeFoundationSpec = "Chartered medieval market guildhall minting silver pennies every second.",
            industrialEvolutionSpec = "Steam-powered sovereign bullion press and central telegraph exchange.",
            cosmicOmegaEvolutionSpec = "Omega Multiverse Treasury synthesizing pure astronium wealth.",
            primarySynergyPartner = "Sun Mirror (PRISM) or Bastion Keep (SHIELD_GARRISON)",
            grandWonderResult = "Cathedral of Light (ASTRAL_NEXUS)",
            tacticalPlacementAdvice = "Build 2–4 Royal Mints early on Trade Guild pads to supercharge Bank interest and buff adjacent towers."
        ),
        TowerBlueprintDossier(
            towerType = TowerType.RAIL_CANNON,
            codeDesignation = "FUT-13",
            structuralClass = "Hyper-Velocity Electromagnetic Penetrator",
            darkAgeFoundationSpec = "Heavy bronze culverin battery firing cast-iron roundshot.",
            industrialEvolutionSpec = "Armored dreadnought turret with twin rifled naval guns.",
            cosmicOmegaEvolutionSpec = "Omega Cosmic Mass Driver accelerating tungsten slugs to 0.4c.",
            primarySynergyPartner = "Trebuchet (MORTAR) or Longbow Tower (SNIPER)",
            grandWonderResult = "Leviathan Gauss Citadel (LEVIATHAN_GAUSS_CITADEL)",
            tacticalPlacementAdvice = "Unlocks in Imperial Age (IV). Devastates heavily armored Landships, Mechs, and Cosmic Bosses."
        ),
        TowerBlueprintDossier(
            towerType = TowerType.LASER_ARRAY,
            codeDesignation = "FUT-14",
            structuralClass = "Coherent Directed-Energy Phased Array",
            darkAgeFoundationSpec = "Archimedes parabolic burning-glass array.",
            industrialEvolutionSpec = "High-intensity carbon-arc searchlight and early maser emitter.",
            cosmicOmegaEvolutionSpec = "Omega Gamma-Ray Burst Emitter melting shields and armor instantaneously.",
            primarySynergyPartner = "Sun Mirror (PRISM) or Sundial Belfry (CHRONO)",
            grandWonderResult = "Dyson Helios Array / Quantum Chrono Matrix",
            tacticalPlacementAdvice = "Unlocks in Enlightenment Age (V). Instantly locks onto fast aerial units and melts energy shields."
        ),
        TowerBlueprintDossier(
            towerType = TowerType.GRAVITY_WELL,
            codeDesignation = "FUT-15",
            structuralClass = "Gravitational Singularity & Crushing Field",
            darkAgeFoundationSpec = "Lodestone vortex pit and weighted chain-net trap.",
            industrialEvolutionSpec = "Electromagnetic eddy-current brake and heavy flywheel tractor.",
            cosmicOmegaEvolutionSpec = "Omega Event-Horizon Projector crushing raiders inside a Kerr black hole.",
            primarySynergyPartner = "Sundial Belfry (CHRONO)",
            grandWonderResult = "Omega Singularity Core (OMEGA_SINGULARITY_CORE)",
            tacticalPlacementAdvice = "Unlocks in Machine Age (IX). Crushes high-HP bosses with percentage-HP gravitational shear."
        ),
        TowerBlueprintDossier(
            towerType = TowerType.SHIELD_GARRISON,
            codeDesignation = "FUT-16",
            structuralClass = "Command Bastion, Keep Fortifier & Aura Relay",
            darkAgeFoundationSpec = "Crenellated stone command bastion with bannermen and horn-blowers.",
            industrialEvolutionSpec = "Ferro-concrete flak command bunker with telegraph fire-control.",
            cosmicOmegaEvolutionSpec = "Omega Immortal Citadel Core projecting invulnerable forcefields and +50% aura buffs.",
            primarySynergyPartner = "Gauss Railgun (RAIL_CANNON)",
            grandWonderResult = "Aegis Orbital Bastion (AEGIS_ORBITAL_BASTION)",
            tacticalPlacementAdvice = "Unlocks in Feudal Age (II). Directly increases max Castle Keep HP and buffs all nearby towers."
        )
    )

    val raiderIntelligenceFiles: List<RaiderIntelligenceFile> = EnemyType.entries.map { e ->
        val originAge = when (e) {
            EnemyType.SWARMLING, EnemyType.STRIDER -> 1
            EnemyType.SPRINTER, EnemyType.ARBALESTER -> 2
            EnemyType.IRONCLAD, EnemyType.SAPPER_PETARD, EnemyType.SHIELDER -> 3
            EnemyType.SHADOW_CAMO, EnemyType.NANITE_HEALER, EnemyType.SKY_WYVERN -> 4
            EnemyType.CATAPHRACT, EnemyType.HYDRA_SPLITTER, EnemyType.FROST_GOLEM -> 5
            EnemyType.BOMBARD_TRAIN, EnemyType.LEVIATHAN_BOSS -> 6
            EnemyType.MUSKET_DRAGOON -> 7
            EnemyType.STEAM_LANDSHIP, EnemyType.IRONCLAD_ZEPPELIN -> 8
            EnemyType.DIESEL_MECH_WALKER, EnemyType.INDUSTRIAL_DREADNOUGHT_BOSS -> 9
            EnemyType.ATOMIC_GOLIATH -> 10
            EnemyType.STEALTH_NIGHTHAWK -> 11
            EnemyType.CYBER_CENTURION, EnemyType.NANITE_CARRIER -> 12
            EnemyType.PLASMA_HOVER_TANK -> 13
            EnemyType.STARFLEET_CORVETTE, EnemyType.STARFLEET_MOTHERSHIP_BOSS -> 14
            EnemyType.DYSON_HARVESTER_TITAN -> 15
            EnemyType.QUANTUM_PHASE_WRAITH -> 16
            EnemyType.CHRONO_PARADOX_DRIFTER -> 17
            EnemyType.OMEGA_ARCHITECT_BOSS, EnemyType.HYDRA_SPAWN -> 18
        }
        RaiderIntelligenceFile(
            enemyType = e,
            originAgeNum = originAge,
            armorClassification = "${(e.armor * 100).toInt()}% Mitigation (${if (e.shieldRatio > 0f) "+${(e.shieldRatio * 100).toInt()}% Shield" else "Unshielded"})",
            propulsionMechanism = when {
                e.isFlying -> "Low-Altitude / Orbital Flight Vector"
                e.baseSpeed >= 100f -> "High-Velocity Recon / Phase Dash"
                e.isBoss -> "Super-Heavy Siege Dreadnought Chassis"
                else -> "Armored Ground Column"
            },
            primaryWeakness = when {
                e.isCamo -> "Radar Uplinks, Watchtower Flare (Path B), or Hydro Wash"
                e.armor >= 0.45f -> "Gauss Railguns, Helios Lasers, and Alchemist Corrosion"
                e.shieldRatio >= 0.40f -> "Tesla Chain-Lightning & Rapid Gatling Fire"
                else -> "Frost Moat + Trebuchet Shatter Combos"
            },
            counterTowerRecommendation = when {
                e.isBoss -> "Leviathan Gauss Citadel + Omega Singularity Core"
                e.isFlying -> "Longbow/Rail Sniper + Falconry Drone Squadron"
                e.armor >= 0.40f -> "Gauss Railgun + Alchemist Lab"
                else -> "Trebuchet + Frost Moat + Pitch Brazier"
            }
        )
    }

    val chronicles get() = eraChronicles
    val blueprints get() = towerBlueprints
    val raiderDossiers get() = raiderIntelligenceFiles

    /**
     * Generates a deterministic multi-harmonic acoustic wavetable for procedural sound synthesis
     * or visual oscilloscope telemetry across the 18 Ages.
     */
    fun synthesizeEraHarmonicSignature(ageLevel: Int, sampleCount: Int = 256): FloatArray {
        val out = FloatArray(sampleCount.coerceIn(16, 4096))
        val fundamental = 1.0 + (ageLevel.coerceIn(1, 18) * 0.35)
        for (i in out.indices) {
            val phase = (i.toDouble() / out.size.toDouble()) * 2.0 * PI
            val h1 = sin(phase * fundamental)
            val h2 = 0.5 * sin(phase * fundamental * 2.0 + 0.4)
            val h3 = 0.25 * cos(phase * fundamental * 3.0 - 0.7)
            val env = sin((i.toDouble() / out.size.toDouble()) * PI)
            out[i] = ((h1 + h2 + h3) * env * 0.57).toFloat()
        }
        return out
    }

    /**
     * Generates a procedural heightmap/albedo matrix for era terrain previews & high-DPI emblems.
     */
    fun generateEraProceduralEmblemField(ageLevel: Int, gridSize: Int = 32): IntArray {
        val side = gridSize.coerceIn(8, 128)
        val pixels = IntArray(side * side)
        val theme = EraEvolutionCatalog.themeForAge(ageLevel)
        val accentInt = theme.turretAccentArgb.toInt()
        val baseR = (accentInt ushr 16) and 0xFF
        val baseG = (accentInt ushr 8) and 0xFF
        val baseB = accentInt and 0xFF
        val half = side * 0.5f

        for (y in 0 until side) {
            for (x in 0 until side) {
                val dx = (x - half) / half
                val dy = (y - half) / half
                val dist = sqrt(dx * dx + dy * dy)
                val wave = (sin((x * 0.45f) + ageLevel) * cos((y * 0.45f) - ageLevel) * 0.5f + 0.5f)
                val radial = (1.0f - dist.coerceIn(0f, 1f))
                val intensity = (0.35f + 0.65f * radial * (0.6f + 0.4f * wave)).coerceIn(0f, 1f)
                val r = (baseR * intensity).toInt().coerceIn(0, 255)
                val g = (baseG * intensity).toInt().coerceIn(0, 255)
                val b = (baseB * intensity).toInt().coerceIn(0, 255)
                pixels[y * side + x] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            }
        }
        return pixels
    }
}

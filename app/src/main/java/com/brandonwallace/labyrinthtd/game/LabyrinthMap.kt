package com.brandonwallace.labyrinthtd.game

import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/**
 * Natural terrain affinity discovered dynamically wherever the player free-places a tower
 * on the sunlit meadow grass. No pre-drawn pad circles clutter the map!
 */
enum class PadBonusType(
    val title: String,
    val badge: String,
    val description: String,
    val rangeMult: Float,
    val fireRateMult: Float,
    val damageMult: Float,
    val reactionMult: Float,
    val goldMult: Float,
    val grantsTrueSight: Boolean,
    val colorArgb: Long
) {
    STANDARD(
        title = "Open Meadow Lawn",
        badge = "LAWN",
        description = "+10% Range & +10% Damage balanced meadow footing.",
        rangeMult = 1.10f,
        fireRateMult = 1.0f,
        damageMult = 1.10f,
        reactionMult = 1.0f,
        goldMult = 1.0f,
        grantsTrueSight = false,
        colorArgb = 0xFF8C857B
    ),
    OVERLOOK(
        title = "Woodland Overlook",
        badge = "WOODS",
        description = "+28% Range & automatic Scout Sight (reveals stealth) near trees & high ground.",
        rangeMult = 1.28f,
        fireRateMult = 1.0f,
        damageMult = 1.05f,
        reactionMult = 1.0f,
        goldMult = 1.0f,
        grantsTrueSight = true,
        colorArgb = 0xFF5A8F5A
    ),
    CONDUIT(
        title = "Roadside Garrison",
        badge = "ROAD",
        description = "+28% Attack Speed & +15% faster reload gearing right beside the trail.",
        rangeMult = 1.05f,
        fireRateMult = 1.28f,
        damageMult = 1.05f,
        reactionMult = 1.10f,
        goldMult = 1.0f,
        grantsTrueSight = false,
        colorArgb = 0xFF588BAE
    ),
    AMPLIFIER(
        title = "Stone Quarry Outcrop",
        badge = "QUARRY",
        description = "+32% Base Damage & +45% Combo Reaction impact near natural rock outcrops.",
        rangeMult = 1.08f,
        fireRateMult = 1.05f,
        damageMult = 1.32f,
        reactionMult = 1.45f,
        goldMult = 1.0f,
        grantsTrueSight = false,
        colorArgb = 0xFFC27238
    ),
    TREASURY(
        title = "Golden Meadow Clearing",
        badge = "GOLD",
        description = "+40% Enemy Kill Gold Bounty & +25% Royal Mint income.",
        rangeMult = 1.08f,
        fireRateMult = 1.05f,
        damageMult = 1.05f,
        reactionMult = 1.0f,
        goldMult = 1.40f,
        grantsTrueSight = false,
        colorArgb = 0xFFD4AF37
    )
}

data class Vec2(val x: Float, val y: Float) {
    fun distTo(other: Vec2): Float = hypot(other.x - x, other.y - y)
}

data class BuildPad(
    val id: Int,
    val x: Float,
    val y: Float,
    val bonus: PadBonusType,
    val multiPassRating: Int
)

data class FreePlacementSite(
    val x: Float,
    val y: Float,
    val terrainBonus: PadBonusType,
    val multiPassRating: Int,
    val crossfireDamageBonus: Float
)

data class TrackGadgetSlot(
    val id: Int,
    val x: Float,
    val y: Float,
    val pathDistance: Float,
    val progressRatio: Float
)

enum class MeadowPropType {
    OAK_TREE,
    PINE_TREE,
    BOULDER,
    GRASS_TUFT,
    WILDFLOWER
}

data class MeadowProp(
    val x: Float,
    val y: Float,
    val scale: Float,
    val type: MeadowPropType,
    val seed: Int
)

/**
 * THE KING'S VALLEY ROAD (Ouroboros Switchback)
 * One long, winding, player-focused 2D top-down meadow & fortress road map.
 * World coordinate space: 1000 x 1360.
 * Supports 100% Free-Placement of towers anywhere on the open lawn grass!
 */
object LabyrinthMap {
    const val WORLD_WIDTH = 1000f
    const val WORLD_HEIGHT = 1360f
    // Slightly zoomed-out scenic 3D viewport bounds so all edge towers & 3D superstructures are 100% visible
    const val VIEW_MIN_X = -56f
    const val VIEW_MAX_X = 1056f
    const val VIEW_SPAN_X = 1112f
    const val VIEW_MIN_Y = -52f
    const val VIEW_MAX_Y = 1412f
    const val VIEW_SPAN_Y = 1464f

    const val ROAD_WIDTH = 44f
    const val PAD_RADIUS = 22f
    const val TOWER_FOOTPRINT_RADIUS = 20f
    const val MIN_TOWER_SPACING = 42f
    const val MIN_ROAD_CENTER_CLEARANCE = (ROAD_WIDTH * 0.5f) + TOWER_FOOTPRINT_RADIUS - 2f // 40f

    val waypoints: List<Vec2> = listOf(
        // Entry Gate (Top-Left Raider Camp) -> Leg 1 East
        Vec2(75f, 90f),
        Vec2(915f, 90f),
        // Hairpin 1 (Top-Right) -> Leg 2 West
        Vec2(915f, 220f),
        Vec2(105f, 220f),
        // Hairpin 2 (Upper-Left) -> Leg 3 East
        Vec2(105f, 350f),
        Vec2(895f, 350f),
        // Outer East Drop into Central Spiral -> Leg 4 West
        Vec2(895f, 670f),
        Vec2(245f, 670f),
        // Central Spiral Inner Climb -> Leg 5 East (The Valley Heart)
        Vec2(245f, 485f),
        Vec2(755f, 485f),
        // Central Spiral Exit Drop -> Leg 6 West
        Vec2(755f, 805f),
        Vec2(95f, 805f),
        // Hairpin 6 (Mid-South Left) -> Leg 7 East
        Vec2(95f, 940f),
        Vec2(905f, 940f),
        // Hairpin 7 (South-Right) -> Leg 8 West
        Vec2(905f, 1075f),
        Vec2(110f, 1075f),
        // Final Serpentine Bastion Approach (Bottom) -> Royal Stone Castle Keep
        Vec2(110f, 1215f),
        Vec2(480f, 1215f),
        Vec2(480f, 1150f),
        Vec2(710f, 1150f),
        Vec2(710f, 1255f),
        Vec2(900f, 1255f)
    )

    data class Segment(
        val start: Vec2,
        val end: Vec2,
        val length: Float,
        val cumulativeStart: Float,
        val dirX: Float,
        val dirY: Float
    )

    val segments: List<Segment>
    val totalLength: Float

    init {
        val segs = mutableListOf<Segment>()
        var acc = 0f
        for (i in 0 until waypoints.size - 1) {
            val a = waypoints[i]
            val b = waypoints[i + 1]
            val len = a.distTo(b).coerceAtLeast(0.001f)
            segs.add(
                Segment(
                    start = a,
                    end = b,
                    length = len,
                    cumulativeStart = acc,
                    dirX = (b.x - a.x) / len,
                    dirY = (b.y - a.y) / len
                )
            )
            acc += len
        }
        segments = segs
        totalLength = acc
    }

    private fun findSegmentIndex(dist: Float): Int {
        val clamped = dist.coerceIn(0f, totalLength)
        var low = 0
        var high = segments.lastIndex
        while (low <= high) {
            val mid = (low + high) ushr 1
            val seg = segments[mid]
            val segEnd = seg.cumulativeStart + seg.length
            if (clamped < seg.cumulativeStart) {
                high = mid - 1
            } else if (clamped > segEnd) {
                low = mid + 1
            } else {
                return mid
            }
        }
        return low.coerceIn(0, segments.lastIndex)
    }

    fun positionAtDistance(dist: Float): Vec2 {
        val clamped = dist.coerceIn(0f, totalLength)
        val seg = segments[findSegmentIndex(clamped)]
        val local = (clamped - seg.cumulativeStart).coerceIn(0f, seg.length)
        return Vec2(
            seg.start.x + seg.dirX * local,
            seg.start.y + seg.dirY * local
        )
    }

    fun directionAtDistance(dist: Float): Vec2 {
        val clamped = dist.coerceIn(0f, totalLength)
        val seg = segments[findSegmentIndex(clamped)]
        return Vec2(seg.dirX, seg.dirY)
    }

    /**
     * Returns the closest point on the road centerline to (px, py) and its distance.
     */
    fun closestRoadPoint(px: Float, py: Float): Pair<Vec2, Float> {
        var bestDist = Float.MAX_VALUE
        var bestPt = Vec2(px, py)
        for (seg in segments) {
            val vx = seg.end.x - seg.start.x
            val vy = seg.end.y - seg.start.y
            val lenSq = max(1f, vx * vx + vy * vy)
            val t = (((px - seg.start.x) * vx + (py - seg.start.y) * vy) / lenSq).coerceIn(0f, 1f)
            val projX = seg.start.x + t * vx
            val projY = seg.start.y + t * vy
            val d = hypot(px - projX, py - projY)
            if (d < bestDist) {
                bestDist = d
                bestPt = Vec2(projX, projY)
            }
        }
        return bestPt to bestDist
    }

    fun minDistanceToRoad(px: Float, py: Float): Float = closestRoadPoint(px, py).second

    /**
     * Counts how many distinct road segments pass within [range] of (px, py).
     * Rewarding players for placing towers inside switchback bends and spiral loops!
     */
    fun countRoadPassesInRange(px: Float, py: Float, range: Float): Int {
        var count = 0
        for (seg in segments) {
            val vx = seg.end.x - seg.start.x
            val vy = seg.end.y - seg.start.y
            val lenSq = max(1f, vx * vx + vy * vy)
            val t = (((px - seg.start.x) * vx + (py - seg.start.y) * vy) / lenSq).coerceIn(0f, 1f)
            val projX = seg.start.x + t * vx
            val projY = seg.start.y + t * vy
            if (hypot(px - projX, py - projY) <= range) {
                count++
            }
        }
        return count.coerceIn(1, 6)
    }

    /**
     * Checks if a world coordinate (wx, wy) is valid open meadow grass for free tower placement.
     */
    fun isPointValidForFreeTower(
        wx: Float,
        wy: Float,
        existingTowerCoords: Iterable<Vec2>
    ): Boolean {
        if (wx < 18f || wx > WORLD_WIDTH + 48f || wy < 18f || wy > WORLD_HEIGHT - 18f) return false
        val entry = waypoints.first()
        val keep = waypoints.last()
        if (hypot(wx - entry.x, wy - entry.y) < 52f) return false
        if (hypot(wx - keep.x, wy - keep.y) < 62f) return false
        if (minDistanceToRoad(wx, wy) < MIN_ROAD_CENTER_CLEARANCE) return false
        for (pos in existingTowerCoords) {
            if (hypot(wx - pos.x, wy - pos.y) < MIN_TOWER_SPACING) return false
        }
        return true
    }

    /**
     * Forgiving Free-Placement Resolver:
     * If the player taps directly on valid open grass, returns that exact coordinate.
     * If the player taps slightly too close to the road edge or another tower, smoothly nudges
     * the placement point to the nearest valid grass spot within 44 units so placement always feels responsive!
     */
    fun resolveFreePlacementPoint(
        rawX: Float,
        rawY: Float,
        existingTowerCoords: Iterable<Vec2>
    ): Vec2? {
        val clampedX = rawX.coerceIn(22f, WORLD_WIDTH + 42f)
        val clampedY = rawY.coerceIn(22f, WORLD_HEIGHT - 22f)
        if (isPointValidForFreeTower(clampedX, clampedY, existingTowerCoords)) {
            return Vec2(clampedX, clampedY)
        }

        // Try nudging perpendicular away from the closest road centerline first
        val (roadPt, roadDist) = closestRoadPoint(clampedX, clampedY)
        if (roadDist < MIN_ROAD_CENTER_CLEARANCE && roadDist > 1f) {
            val nx = (clampedX - roadPt.x) / roadDist
            val ny = (clampedY - roadPt.y) / roadDist
            val nudgedX = (roadPt.x + nx * (MIN_ROAD_CENTER_CLEARANCE + 2f)).coerceIn(22f, WORLD_WIDTH + 42f)
            val nudgedY = (roadPt.y + ny * (MIN_ROAD_CENTER_CLEARANCE + 2f)).coerceIn(22f, WORLD_HEIGHT - 22f)
            if (isPointValidForFreeTower(nudgedX, nudgedY, existingTowerCoords)) {
                return Vec2(nudgedX, nudgedY)
            }
        }

        // Radial spiral search within 46 world units for the closest valid blade of grass
        val radii = floatArrayOf(12f, 22f, 32f, 44f)
        for (r in radii) {
            for (angleIdx in 0 until 16) {
                val rad = (angleIdx * 22.5) * Math.PI / 180.0
                val cx = (clampedX + Math.cos(rad).toFloat() * r).coerceIn(36f, WORLD_WIDTH - 36f)
                val cy = (clampedY + Math.sin(rad).toFloat() * r).coerceIn(36f, WORLD_HEIGHT - 36f)
                if (isPointValidForFreeTower(cx, cy, existingTowerCoords)) {
                    return Vec2(cx, cy)
                }
            }
        }
        return null
    }

    /**
     * Evaluates the natural meadow terrain at (wx, wy) for a free-placed tower.
     * Nearby boulders grant Stone Quarry (+32% Dmg), nearby trees grant Woodland Overlook (+28% Range & True Sight),
     * roadside placement grants Roadside Garrison (+28% Fire Rate), and wildflowers grant Golden Clearing (+40% Gold).
     */
    fun evaluateFreePlacementSite(wx: Float, wy: Float, baseRange: Float = 200f): FreePlacementSite {
        var nearBoulder = false
        var nearTree = false
        var nearGoldFlower = false
        for (prop in meadowProps) {
            val d = hypot(prop.x - wx, prop.y - wy)
            when (prop.type) {
                MeadowPropType.BOULDER -> if (d <= 68f) nearBoulder = true
                MeadowPropType.OAK_TREE, MeadowPropType.PINE_TREE -> if (d <= 78f) nearTree = true
                MeadowPropType.WILDFLOWER -> if (d <= 48f && prop.seed % 2 == 0) nearGoldFlower = true
                else -> {}
            }
        }
        val roadDist = minDistanceToRoad(wx, wy)
        val bonus = when {
            nearBoulder -> PadBonusType.AMPLIFIER
            nearTree -> PadBonusType.OVERLOOK
            roadDist <= MIN_ROAD_CENTER_CLEARANCE + 18f -> PadBonusType.CONDUIT
            nearGoldFlower -> PadBonusType.TREASURY
            else -> PadBonusType.STANDARD
        }
        val passes = countRoadPassesInRange(wx, wy, baseRange)
        val crossfireBonus = (passes - 1).coerceAtLeast(0) * 0.08f
        return FreePlacementSite(
            x = wx,
            y = wy,
            terrainBonus = bonus,
            multiPassRating = passes,
            crossfireDamageBonus = crossfireBonus
        )
    }

    /**
     * Strategic meadow reference coordinates inside the switchback peninsulas and spiral heart.
     * Used by the AI Auto-Governor and test suite as ideal free-placement coordinates (never drawn as circles!).
     */
    val buildPads: List<BuildPad> = buildList {
        var nextId = 0
        fun addRow(y: Float, xs: FloatArray, types: List<PadBonusType>, multiPass: Int) {
            for (i in xs.indices) {
                add(BuildPad(nextId++, xs[i], y, types[i % types.size], multiPass))
            }
        }

        // Row 1: Peninsula between Leg 1 (y=90) and Leg 2 (y=220) -> y = 155
        addRow(
            y = 155f,
            xs = floatArrayOf(160f, 265f, 370f, 475f, 580f, 685f, 790f, 850f),
            types = listOf(
                PadBonusType.TREASURY,
                PadBonusType.STANDARD,
                PadBonusType.CONDUIT,
                PadBonusType.OVERLOOK,
                PadBonusType.STANDARD,
                PadBonusType.AMPLIFIER,
                PadBonusType.CONDUIT,
                PadBonusType.OVERLOOK
            ),
            multiPass = 2
        )

        // Row 2: Peninsula between Leg 2 (y=220) and Leg 3 (y=350) -> y = 285
        addRow(
            y = 285f,
            xs = floatArrayOf(175f, 280f, 385f, 490f, 595f, 700f, 805f),
            types = listOf(
                PadBonusType.OVERLOOK,
                PadBonusType.AMPLIFIER,
                PadBonusType.STANDARD,
                PadBonusType.CONDUIT,
                PadBonusType.TREASURY,
                PadBonusType.STANDARD,
                PadBonusType.OVERLOOK
            ),
            multiPass = 3
        )

        // Row 3: Upper Central Spiral Peninsula between Leg 3 (y=350) and Leg 5 (y=485) -> y = 418
        addRow(
            y = 418f,
            xs = floatArrayOf(165f, 285f, 390f, 495f, 600f, 705f, 825f),
            types = listOf(
                PadBonusType.CONDUIT,
                PadBonusType.OVERLOOK,
                PadBonusType.AMPLIFIER,
                PadBonusType.CONDUIT,
                PadBonusType.AMPLIFIER,
                PadBonusType.OVERLOOK,
                PadBonusType.TREASURY
            ),
            multiPass = 4
        )

        // Row 4: Inner Heart Kill-Box between Leg 5 (y=485) and Leg 4 (y=670) -> y = 578
        addRow(
            y = 578f,
            xs = floatArrayOf(155f, 325f, 425f, 525f, 625f, 690f, 825f),
            types = listOf(
                PadBonusType.OVERLOOK,
                PadBonusType.AMPLIFIER,
                PadBonusType.CONDUIT,
                PadBonusType.AMPLIFIER,
                PadBonusType.TREASURY,
                PadBonusType.OVERLOOK,
                PadBonusType.CONDUIT
            ),
            multiPass = 4
        )

        // Row 5: Lower Spiral Peninsula between Leg 4 (y=670) and Leg 6 (y=805) -> y = 738
        addRow(
            y = 738f,
            xs = floatArrayOf(165f, 275f, 385f, 495f, 605f, 690f, 835f),
            types = listOf(
                PadBonusType.STANDARD,
                PadBonusType.CONDUIT,
                PadBonusType.AMPLIFIER,
                PadBonusType.OVERLOOK,
                PadBonusType.CONDUIT,
                PadBonusType.AMPLIFIER,
                PadBonusType.OVERLOOK
            ),
            multiPass = 3
        )

        // Row 6: Peninsula between Leg 6 (y=805) and Leg 7 (y=940) -> y = 872
        addRow(
            y = 872f,
            xs = floatArrayOf(170f, 275f, 380f, 485f, 590f, 695f, 800f),
            types = listOf(
                PadBonusType.OVERLOOK,
                PadBonusType.STANDARD,
                PadBonusType.TREASURY,
                PadBonusType.CONDUIT,
                PadBonusType.AMPLIFIER,
                PadBonusType.STANDARD,
                PadBonusType.OVERLOOK
            ),
            multiPass = 3
        )

        // Row 7: Peninsula between Leg 7 (y=940) and Leg 8 (y=1075) -> y = 1008
        addRow(
            y = 1008f,
            xs = floatArrayOf(160f, 265f, 370f, 475f, 580f, 685f, 790f, 845f),
            types = listOf(
                PadBonusType.AMPLIFIER,
                PadBonusType.CONDUIT,
                PadBonusType.STANDARD,
                PadBonusType.OVERLOOK,
                PadBonusType.STANDARD,
                PadBonusType.CONDUIT,
                PadBonusType.AMPLIFIER,
                PadBonusType.OVERLOOK
            ),
            multiPass = 3
        )

        // Row 8: Final Bastion pockets
        val finalPads = listOf(
            Triple(185f, 1145f, PadBonusType.CONDUIT),
            Triple(290f, 1145f, PadBonusType.AMPLIFIER),
            Triple(395f, 1145f, PadBonusType.OVERLOOK),
            Triple(565f, 1090f, PadBonusType.AMPLIFIER),
            Triple(625f, 1215f, PadBonusType.AMPLIFIER),
            Triple(785f, 1185f, PadBonusType.CONDUIT),
            Triple(855f, 1185f, PadBonusType.OVERLOOK),
            Triple(220f, 1285f, PadBonusType.STANDARD),
            Triple(350f, 1285f, PadBonusType.TREASURY),
            Triple(490f, 1285f, PadBonusType.CONDUIT),
            Triple(630f, 1310f, PadBonusType.AMPLIFIER),
            Triple(780f, 1315f, PadBonusType.OVERLOOK),
            Triple(595f, 1085f, PadBonusType.TREASURY)
        )
        for ((px, py, pType) in finalPads) {
            add(BuildPad(nextId++, px, py, pType, 3))
        }
    }

    /**
     * 20 On-Road Sapper Trap Coordinates along the winding road.
     */
    val gadgetSlots: List<TrackGadgetSlot> = buildList {
        val count = 20
        for (i in 0 until count) {
            val ratio = (i + 1).toFloat() / (count + 1).toFloat()
            val dist = totalLength * ratio
            val pos = positionAtDistance(dist)
            add(TrackGadgetSlot(id = i, x = pos.x, y = pos.y, pathDistance = dist, progressRatio = ratio))
        }
    }

    /**
     * Deterministic medieval woodland trees, river rocks, and wildflower tufts around the lawn meadow.
     */
    val meadowProps: List<MeadowProp> = buildList {
        var seed = 104729
        fun nextRand(): Float {
            seed = (seed * 1664525 + 1013904223) and 0x7fffffff
            return (seed % 10000) / 10000f
        }

        for (i in 0 until 210) {
            val rx = 18f + nextRand() * (WORLD_WIDTH - 36f)
            val ry = 18f + nextRand() * (WORLD_HEIGHT - 36f)
            val roadDist = minDistanceToRoad(rx, ry)
            val nearEntry = hypot(rx - 75f, ry - 90f) < 72f
            val nearKeep = hypot(rx - 900f, ry - 1255f) < 88f
            if (nearEntry || nearKeep) continue

            val isBorder = rx < 56f || rx > WORLD_WIDTH - 56f || ry < 46f || ry > WORLD_HEIGHT - 46f
            if (roadDist > ROAD_WIDTH * 0.5f + 22f) {
                val type = when {
                    isBorder && i % 3 == 0 -> MeadowPropType.PINE_TREE
                    isBorder -> MeadowPropType.OAK_TREE
                    i % 8 == 0 -> MeadowPropType.OAK_TREE
                    i % 7 == 0 -> MeadowPropType.BOULDER
                    i % 2 == 0 -> MeadowPropType.WILDFLOWER
                    else -> MeadowPropType.GRASS_TUFT
                }
                add(MeadowProp(rx, ry, 0.78f + nextRand() * 0.44f, type, i))
            } else if (roadDist > ROAD_WIDTH * 0.5f + 11f && i % 2 == 0) {
                val type = if (i % 4 == 0) MeadowPropType.WILDFLOWER else MeadowPropType.GRASS_TUFT
                add(MeadowProp(rx, ry, 0.65f + nextRand() * 0.35f, type, i))
            }
        }
    }
}

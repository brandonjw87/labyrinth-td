package com.brandonwallace.labyrinthtd.render

import android.graphics.Bitmap
import com.brandonwallace.labyrinthtd.game.EraEvolutionCatalog
import com.brandonwallace.labyrinthtd.game.LabyrinthMap
import com.brandonwallace.labyrinthtd.game.MeadowPropType
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * ARGON TURBO MULTI-THREADED PER-PIXEL 3D RAYMARCHED HEIGHTFIELD, NORMAL-MAP & PBR TERRAIN BAKER (v1.7)
 *
 * Computes a physically-based 3D heightfield H(x,y), exact central-difference 3D surface normals N(x,y),
 * 18-step raymarched directional sun shadows, cavity ambient occlusion, Beer-Lambert 3D water refraction
 * with wave-normal sun glint & shoreline foam, 3D Voronoi/Ashlar cobblestone & granite curbstone relief,
 * and 12 warm 3D brazier point-lights across the entire fixed battlefield map.
 */
object Labyrinth3dTerrainBaker {

    // Grid resolution covering VIEW_MIN_X..VIEW_MAX_X (-56..1056 = 1112) x VIEW_MIN_Y..VIEW_MAX_Y (-52..1412 = 1464)
    private const val STEP = 2.0f
    const val GRID_W = 556 // 1112 / 2.0
    const val GRID_H = 732 // 1464 / 2.0

    // Material IDs
    private const val MAT_MEADOW = 0
    private const val MAT_TERRACE_CLIFF = 1
    private const val MAT_TERRACE_TOP = 2
    private const val MAT_OUTER_CLIFF = 3
    private const val MAT_RIVER_CANYON_WALL = 4
    private const val MAT_RIVER_WATER = 5
    private const val MAT_ROAD_EMBANKMENT = 6
    private const val MAT_ROAD_CURB = 7
    private const val MAT_ROAD_COBBLE = 8
    private const val MAT_BRIDGE_DECK = 9
    private const val MAT_BRIDGE_PARAPET = 10

    // Directional NW Sun light vector (normalized)
    private const val SUN_X = -0.55f
    private const val SUN_Y = -0.60f
    private const val SUN_Z = 0.58f

    // Half-vector between Sun and Viewer (0, 0, 1) for Blinn-Phong specular highlights
    private const val HALF_X = -0.31f
    private const val HALF_Y = -0.34f
    private const val HALF_Z = 0.89f

    private val workerPool = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors().coerceIn(2, 8)
    )

    // Precomputed static geometry arrays on the 556 x 732 3D grid (computed once, reused across Ages)
    @Volatile
    private var geometryBuilt = false
    private val geomLock = Any()

    private val heightField = FloatArray(GRID_W * GRID_H)
    private val shadowCasterHeight = FloatArray(GRID_W * GRID_H)
    private val waterBedHeight = FloatArray(GRID_W * GRID_H)
    private val materialMap = ByteArray(GRID_W * GRID_H)
    private val stoneToneMap = FloatArray(GRID_W * GRID_H)
    private val shadowMap = FloatArray(GRID_W * GRID_H)
    private val aoMap = FloatArray(GRID_W * GRID_H)
    private val pixelBuffer = IntArray(GRID_W * GRID_H)

    // Precomputed 10 raised 3D limestone & turf plateau terraces in the switchback bays
    private val terracePlateaus = arrayOf(
        floatArrayOf(285f, 155f, 150f, 26f, 13.5f),
        floatArrayOf(715f, 155f, 150f, 26f, 13.5f),
        floatArrayOf(285f, 285f, 150f, 26f, 14.0f),
        floatArrayOf(715f, 285f, 150f, 26f, 14.0f),
        floatArrayOf(155f, 520f, 42f, 115f, 15.0f),
        floatArrayOf(825f, 575f, 36f, 165f, 15.0f),
        floatArrayOf(285f, 738f, 150f, 26f, 13.5f),
        floatArrayOf(715f, 738f, 150f, 26f, 13.5f),
        floatArrayOf(285f, 872f, 150f, 26f, 14.0f),
        floatArrayOf(715f, 872f, 150f, 26f, 14.0f),
        floatArrayOf(285f, 1008f, 150f, 26f, 13.5f),
        floatArrayOf(715f, 1008f, 150f, 26f, 13.5f),
        floatArrayOf(295f, 1142f, 135f, 24f, 12.5f)
    )

    // 12 Roadside Brazier positions casting warm 3D point light
    val brazierPositions = arrayOf(
        floatArrayOf(165f, 155f),
        floatArrayOf(835f, 155f),
        floatArrayOf(165f, 285f),
        floatArrayOf(835f, 285f),
        floatArrayOf(310f, 578f),
        floatArrayOf(690f, 578f),
        floatArrayOf(165f, 738f),
        floatArrayOf(835f, 738f),
        floatArrayOf(165f, 872f),
        floatArrayOf(835f, 872f),
        floatArrayOf(165f, 1008f),
        floatArrayOf(835f, 1008f)
    )

    fun riverCenterX(wy: Float): Float =
        496f + 22f * sin(wy * 0.0058f) - 10f * cos(wy * 0.0125f)

    private fun hash2D(ix: Int, iy: Int): Float {
        var n = ix * 374761393 + iy * 668265263
        n = (n xor (n ushr 13)) * 1274126177
        return ((n xor (n ushr 16)) and 0x7FFFFFFF).toFloat() / 2147483647.0f
    }

    private fun smoothNoise(x: Float, y: Float): Float {
        val ix = floor(x).toInt()
        val iy = floor(y).toInt()
        val fx = x - ix
        val fy = y - iy
        val ux = fx * fx * (3f - 2f * fx)
        val uy = fy * fy * (3f - 2f * fy)

        val n00 = hash2D(ix, iy)
        val n10 = hash2D(ix + 1, iy)
        val n01 = hash2D(ix, iy + 1)
        val n11 = hash2D(ix + 1, iy + 1)

        val nx0 = n00 + (n10 - n00) * ux
        val nx1 = n01 + (n11 - n01) * ux
        return nx0 + (nx1 - nx0) * uy
    }

    private fun fbm3(x: Float, y: Float): Float {
        var v = 0f
        v += 0.55f * smoothNoise(x, y)
        v += 0.30f * smoothNoise(x * 2.13f + 17.3f, y * 2.13f + 31.7f)
        v += 0.15f * smoothNoise(x * 4.41f + 53.1f, y * 4.41f + 11.9f)
        return v
    }

    private fun ensureStaticGeometryBuilt() {
        if (geometryBuilt) return
        synchronized(geomLock) {
            if (geometryBuilt) return

            val segs = LabyrinthMap.segments
            val nSegs = segs.size
            val segAx = FloatArray(nSegs)
            val segAy = FloatArray(nSegs)
            val segBx = FloatArray(nSegs)
            val segBy = FloatArray(nSegs)
            val segDx = FloatArray(nSegs)
            val segDy = FloatArray(nSegs)
            val segLen = FloatArray(nSegs)
            val segCum = FloatArray(nSegs)
            for (i in 0 until nSegs) {
                val s = segs[i]
                segAx[i] = s.start.x
                segAy[i] = s.start.y
                segBx[i] = s.end.x
                segBy[i] = s.end.y
                segDx[i] = s.dirX
                segDy[i] = s.dirY
                segLen[i] = s.length
                segCum[i] = s.cumulativeStart
            }

            val props = LabyrinthMap.meadowProps
            val nProps = props.size
            val propX = FloatArray(nProps)
            val propY = FloatArray(nProps)
            val propR = FloatArray(nProps)
            val propH = FloatArray(nProps)
            for (i in 0 until nProps) {
                val p = props[i]
                propX[i] = p.x
                propY[i] = p.y
                when (p.type) {
                    MeadowPropType.PINE_TREE -> {
                        propR[i] = 20f * p.scale
                        propH[i] = 36f * p.scale
                    }
                    MeadowPropType.OAK_TREE -> {
                        propR[i] = 22f * p.scale
                        propH[i] = 32f * p.scale
                    }
                    MeadowPropType.BOULDER -> {
                        propR[i] = 14f * p.scale
                        propH[i] = 14f * p.scale
                    }
                    else -> {
                        propR[i] = 0f
                        propH[i] = 0f
                    }
                }
            }

            // STEP 1: Multi-threaded 3D Heightfield & Material Construction
            val numThreads = Runtime.getRuntime().availableProcessors().coerceIn(2, 8)
            val tasks1 = ArrayList<Callable<Unit>>(numThreads)
            val rowsPerThread = (GRID_H + numThreads - 1) / numThreads

            for (tIdx in 0 until numThreads) {
                val yStart = tIdx * rowsPerThread
                val yEnd = min(GRID_H, yStart + rowsPerThread)
                if (yStart >= yEnd) continue
                tasks1.add(Callable {
                    for (gy in yStart until yEnd) {
                        val wy = LabyrinthMap.VIEW_MIN_Y + (gy + 0.5f) * STEP
                        val rCenterX = riverCenterX(wy)
                        val rowOffset = gy * GRID_W

                        for (gx in 0 until GRID_W) {
                            val wx = LabyrinthMap.VIEW_MIN_X + (gx + 0.5f) * STEP
                            val idx = rowOffset + gx

                            val nLow = fbm3(wx * 0.014f + 5f, wy * 0.014f + 9f)
                            val nMid = fbm3(wx * 0.055f + 21f, wy * 0.055f + 37f)
                            val nHigh = smoothNoise(wx * 0.22f, wy * 0.22f)

                            // 1. Base Rolling 3D Meadow Elevation
                            var h = (nLow - 0.45f) * 8.5f + (nMid - 0.5f) * 2.2f + (nHigh - 0.5f) * 0.65f
                            var mat: Byte = MAT_MEADOW
                            var stoneTone = 0f

                            // 2. Outer Valley Perimeter 3D Limestone Cliffs (framing the zoomed-out map)
                            val distLeft = 18f - wx
                            val distRight = wx - (LabyrinthMap.WORLD_WIDTH - 18f)
                            val distTop = 18f - wy
                            val distBot = wy - (LabyrinthMap.WORLD_HEIGHT - 18f)
                            val outerPen = max(max(distLeft, distRight), max(distTop, distBot))
                            if (outerPen > 0f) {
                                val tCliff = (outerPen / 34f).coerceIn(0f, 1.35f)
                                val smoothStep = tCliff * tCliff * (3f - 2f * min(1f, tCliff))
                                val strata = sin(smoothStep * 16f + nMid * 3.5f) * 1.4f
                                val cliffH = smoothStep * 34f + strata + (nMid - 0.5f) * 6.5f
                                if (cliffH > h) {
                                    h = cliffH
                                    mat = if (tCliff > 0.72f) MAT_TERRACE_TOP else MAT_OUTER_CLIFF
                                    stoneTone = nMid
                                }
                            }

                            // 3. Raised 3D Limestone & Turf Plateau Terraces in Switchback Bays
                            for (tr in terracePlateaus) {
                                val dx = abs(wx - tr[0]) - (tr[2] - 18f)
                                val dy = abs(wy - tr[1]) - (tr[3] - 14f)
                                val dBox = hypot(max(dx, 0f), max(dy, 0f)) + min(max(dx, dy), 0f) - 14f
                                if (dBox < 9f) {
                                    val ramp = ((9f - dBox) / 9f).coerceIn(0f, 1f)
                                    val sRamp = ramp * ramp * (3f - 2f * ramp)
                                    val strata = sin(sRamp * 12f + nHigh * 2f) * 0.85f
                                    val th = sRamp * tr[4] + strata + (nMid - 0.5f) * 1.8f
                                    if (th > h) {
                                        h = th
                                        mat = if (ramp > 0.84f) MAT_TERRACE_TOP else MAT_TERRACE_CLIFF
                                        stoneTone = nMid
                                    }
                                }
                            }

                            // 4. Nearest Distance & Parametric (u, v) to the 21-Segment Labyrinth Road
                            var bestRoadDist = 9999f
                            var bestU = 0f
                            var bestV = 0f
                            for (sIdx in 0 until nSegs) {
                                val ax = segAx[sIdx]
                                val ay = segAy[sIdx]
                                val vx = segBx[sIdx] - ax
                                val vy = segBy[sIdx] - ay
                                val l = segLen[sIdx]
                                val proj = (((wx - ax) * segDx[sIdx] + (wy - ay) * segDy[sIdx])).coerceIn(0f, l)
                                val px = ax + segDx[sIdx] * proj
                                val py = ay + segDy[sIdx] * proj
                                val d = hypot(wx - px, wy - py)
                                if (d < bestRoadDist) {
                                    bestRoadDist = d
                                    bestU = segCum[sIdx] + proj
                                    // Signed perpendicular distance
                                    bestV = (wx - px) * (-segDy[sIdx]) + (wy - py) * segDx[sIdx]
                                }
                            }

                            // 5. Deep 3D Sunken River Canyon vs 3D Vaulted Stone Bridges
                            val riverDist = abs(wx - rCenterX) + (nMid - 0.5f) * 5.5f
                            val isBridgeSpan = bestRoadDist <= 29.5f && riverDist <= 46f

                            var bedH = h
                            if (riverDist < 45f) {
                                val canyonT = ((45f - riverDist) / 24f).coerceIn(0f, 1f)
                                val canyonSmooth = canyonT * canyonT * (3f - 2f * canyonT)
                                val rockStrata = sin(canyonSmooth * 18f + nMid * 4f) * 1.6f + (nHigh - 0.5f) * 2.2f
                                bedH = h * (1f - canyonSmooth) - canyonSmooth * 30.0f + rockStrata
                                if (!isBridgeSpan) {
                                    if (riverDist < 21.5f) {
                                        // 3D Water Surface with multi-directional wave ripple heightfield
                                        val wave1 = sin(wy * 0.19f + wx * 0.11f + nMid * 4f) * 0.55f
                                        val wave2 = cos(wy * 0.31f - wx * 0.24f) * 0.35f
                                        h = -11.5f + wave1 + wave2
                                        mat = MAT_RIVER_WATER
                                    } else {
                                        h = bedH
                                        mat = MAT_RIVER_CANYON_WALL
                                        stoneTone = nMid
                                    }
                                }
                            }
                            waterBedHeight[idx] = bedH

                            // 6. Raised 3D Labyrinth Causeway, Embankments, Curbstones & 3D Domed Cobblestones
                            val halfRoad = LabyrinthMap.ROAD_WIDTH * 0.5f // 22f
                            if (bestRoadDist < halfRoad + 13f) {
                                if (isBridgeSpan) {
                                    // 3D Stone Bridge Deck & High Crenellated Parapets spanning the River Canyon
                                    if (bestRoadDist > halfRoad - 1.5f && bestRoadDist <= halfRoad + 6.5f) {
                                        val merlon = if (((bestU / 14f).toInt() and 1) == 0) 3.2f else 0f
                                        h = 21.5f + merlon + (nHigh - 0.5f) * 0.6f
                                        mat = MAT_BRIDGE_PARAPET
                                        stoneTone = ((bestU / 14f).toInt() * 0.37f) % 1f
                                    } else if (bestRoadDist <= halfRoad - 1.5f) {
                                        // Bridge timber/ashlar deck
                                        val plankU = (bestU * 0.14f)
                                        val plankFrac = plankU - floor(plankU)
                                        val groove = if (plankFrac < 0.12f || plankFrac > 0.88f) -1.2f else 0.7f
                                        h = 14.2f + groove + (nHigh - 0.5f) * 0.4f
                                        mat = MAT_BRIDGE_DECK
                                        stoneTone = hash2D(floor(plankU).toInt(), 7)
                                    }
                                } else if (bestRoadDist > halfRoad + 2.2f) {
                                    // 3D Sloped Stone Retaining Embankment (24.2 .. 35.0)
                                    val embT = ((halfRoad + 13f - bestRoadDist) / 10.8f).coerceIn(0f, 1f)
                                    val embSmooth = embT * embT * (3f - 2f * embT)
                                    val blockNoise = (nHigh - 0.5f) * 1.6f
                                    val embH = h * (1f - embSmooth) + 10.2f * embSmooth + blockNoise
                                    if (embH >= h - 1f) {
                                        h = embH
                                        mat = MAT_ROAD_EMBANKMENT
                                        stoneTone = nMid
                                    }
                                } else if (bestRoadDist >= halfRoad - 3.2f) {
                                    // 3D Chiseled Granite Curbstones along both edges of the road (18.8 .. 24.2)
                                    val curbU = bestU / 19.0f
                                    val curbIdx = floor(curbU).toInt()
                                    val curbFracU = curbU - curbIdx
                                    val curbFracV = (bestRoadDist - (halfRoad - 3.2f)) / 5.4f
                                    val sideSign = if (bestV >= 0f) 1 else 2
                                    val blockRand = hash2D(curbIdx, sideSign * 31)

                                    val edgeU = min(curbFracU, 1f - curbFracU)
                                    val edgeV = min(curbFracV, 1f - curbFracV)
                                    val bevel = min(1f, min(edgeU * 7.5f, edgeV * 5.0f))
                                    val dome = bevel * bevel * (3f - 2f * bevel)
                                    h = 10.5f + dome * (3.8f + blockRand * 0.9f) + (nHigh - 0.5f) * 0.45f
                                    mat = MAT_ROAD_CURB
                                    stoneTone = blockRand
                                } else {
                                    // 3D Cambered Cobblestone / Ashlar Roadway Surface (0 .. 18.8)
                                    val camber = (1f - (bestRoadDist / (halfRoad - 3.2f)).pow(2)) * 1.6f
                                    val laneV = (bestV + (halfRoad - 3.2f)) / 7.6f
                                    val laneIdx = floor(laneV).toInt()
                                    val fracV = laneV - laneIdx
                                    val stagger = (laneIdx and 1) * 0.5f
                                    val cobbleU = (bestU / 14.5f) + stagger
                                    val cobbleIdx = floor(cobbleU).toInt()
                                    val fracU = cobbleU - cobbleIdx

                                    val cellRand = hash2D(cobbleIdx, laneIdx * 19 + 3)
                                    val edgeU = min(fracU, 1f - fracU)
                                    val edgeV = min(fracV, 1f - fracV)
                                    val mortarFactor = min(1f, min(edgeU * 6.8f, edgeV * 6.2f))
                                    val pillow = mortarFactor * mortarFactor * (3f - 2f * mortarFactor)

                                    // Mortar joints dip -1.6m; stone pillow domes rise +1.9m
                                    val stoneRelief = -1.5f + pillow * (3.1f + (cellRand - 0.5f) * 0.9f)
                                    h = 10.2f + camber + stoneRelief + (nHigh - 0.5f) * 0.5f
                                    mat = MAT_ROAD_COBBLE
                                    stoneTone = cellRand
                                }
                            }

                            heightField[idx] = h
                            materialMap[idx] = mat
                            stoneToneMap[idx] = stoneTone

                            // Compute 3D Shadow-Caster Height (Terrain + Trees + Boulders + Gate + Keep + Monument)
                            var castH = h
                            // Bridge shadow over river water
                            if (isBridgeSpan) {
                                castH = max(castH, 22f)
                            }
                            // Trees & Boulders cast real raymarched 3D ground shadows
                            for (pIdx in 0 until nProps) {
                                val pr = propR[pIdx]
                                if (pr <= 0f) continue
                                val dx = wx - propX[pIdx]
                                val dy = wy - propY[pIdx]
                                val dSq = dx * dx + dy * dy
                                if (dSq < pr * pr) {
                                    val d = sqrt(dSq)
                                    val dome = sqrt(max(0f, 1f - (d / pr) * (d / pr)))
                                    castH = max(castH, h + propH[pIdx] * dome)
                                }
                            }
                            // Raider Gate (75, 90), Castle Keep (900, 1255), Creator Monument (500, 582)
                            if (hypot(wx - 75f, wy - 90f) < 36f) {
                                castH = max(castH, h + 46f)
                            }
                            if (hypot(wx - 900f, wy - 1255f) < 46f) {
                                castH = max(castH, h + 64f)
                            }
                            if (abs(wx - 500f) < 168f && abs(wy - 582f) < 22f) {
                                castH = max(castH, h + 26f)
                            }
                            shadowCasterHeight[idx] = castH
                        }
                    }
                })
            }
            workerPool.invokeAll(tasks1)

            // STEP 2: Multi-threaded 18-Step 3D Raymarched Directional Sun Shadows & Cavity Ambient Occlusion
            val tasks2 = ArrayList<Callable<Unit>>(numThreads)
            // Ray step in grid units toward NW Sun (-0.55, -0.60, +0.58)
            val rayDirX = -0.74f
            val rayDirY = -0.81f
            val raySlopeZ = 1.15f // Height gain per step in world meters

            for (tIdx in 0 until numThreads) {
                val yStart = tIdx * rowsPerThread
                val yEnd = min(GRID_H, yStart + rowsPerThread)
                if (yStart >= yEnd) continue
                tasks2.add(Callable {
                    for (gy in yStart until yEnd) {
                        val rowOffset = gy * GRID_W
                        for (gx in 0 until GRID_W) {
                            val idx = rowOffset + gx
                            val h0 = heightField[idx]

                            // 1) 18-Step Soft Penumbra Shadow Raymarch toward the NW Sun
                            var maxBlockingRatio = 0f
                            var rx = gx.toFloat()
                            var ry = gy.toFloat()
                            var rz = h0 + 0.65f
                            for (step in 1..18) {
                                rx += rayDirX * 1.15f
                                ry += rayDirY * 1.15f
                                rz += raySlopeZ * 1.15f
                                val ix = rx.toInt()
                                val iy = ry.toInt()
                                if (ix <= 0 || ix >= GRID_W - 1 || iy <= 0 || iy >= GRID_H - 1) break
                                val occluderH = shadowCasterHeight[iy * GRID_W + ix]
                                val diff = occluderH - rz
                                if (diff > 0f) {
                                    val penumbra = (diff / (1.5f + step * 0.48f)).coerceIn(0f, 1f)
                                    if (penumbra > maxBlockingRatio) {
                                        maxBlockingRatio = penumbra
                                    }
                                }
                            }
                            shadowMap[idx] = (1f - maxBlockingRatio * 0.66f).coerceIn(0.32f, 1.0f)

                            // 2) Cavity & Horizon Ambient Occlusion from local 3D Laplacian curvature
                            val gx0 = max(0, gx - 1)
                            val gx1 = min(GRID_W - 1, gx + 1)
                            val gy0 = max(0, gy - 1)
                            val gy1 = min(GRID_H - 1, gy + 1)
                            val hL = heightField[rowOffset + gx0]
                            val hR = heightField[rowOffset + gx1]
                            val hU = heightField[gy0 * GRID_W + gx]
                            val hD = heightField[gy1 * GRID_W + gx]
                            val avgNeighbors = (hL + hR + hU + hD) * 0.25f
                            val cavity = max(0f, avgNeighbors - h0)
                            aoMap[idx] = (1f - cavity * 0.16f).coerceIn(0.42f, 1.0f)
                        }
                    }
                })
            }
            workerPool.invokeAll(tasks2)

            geometryBuilt = true
        }
    }

    /**
     * Shades the 3D heightfield, normal map, water refraction, shadows, and torchlights for [ageNum]
     * and returns a Bitmap ready to blit onto the static battlefield cache.
     */
    fun render3dRaymarchedTerrainBitmap(ageNum: Int): Bitmap {
        ensureStaticGeometryBuilt()
        val theme = EraEvolutionCatalog.themeForAge(ageNum)

        val roadSurfArgb = theme.roadSurfaceArgb.toInt()
        val roadCurbArgb = theme.roadBorderArgb.toInt()
        val roadSurfR = (roadSurfArgb ushr 16) and 0xFF
        val roadSurfG = (roadSurfArgb ushr 8) and 0xFF
        val roadSurfB = roadSurfArgb and 0xFF

        val roadCurbR = (roadCurbArgb ushr 16) and 0xFF
        val roadCurbG = (roadCurbArgb ushr 8) and 0xFF
        val roadCurbB = roadCurbArgb and 0xFF

        val isFutureEra = ageNum >= 11

        val numThreads = Runtime.getRuntime().availableProcessors().coerceIn(2, 8)
        val tasks = ArrayList<Callable<Unit>>(numThreads)
        val rowsPerThread = (GRID_H + numThreads - 1) / numThreads

        for (tIdx in 0 until numThreads) {
            val yStart = tIdx * rowsPerThread
            val yEnd = min(GRID_H, yStart + rowsPerThread)
            if (yStart >= yEnd) continue

            tasks.add(Callable {
                for (gy in yStart until yEnd) {
                    val wy = LabyrinthMap.VIEW_MIN_Y + (gy + 0.5f) * STEP
                    val gy0 = max(0, gy - 1)
                    val gy1 = min(GRID_H - 1, gy + 1)
                    val rowOffset = gy * GRID_W
                    val rCenterX = riverCenterX(wy)

                    // Mow-band subtle turf stripe factor
                    val turfBand = if (((wy / 68f).toInt() and 1) == 0) 1.04f else 0.96f

                    for (gx in 0 until GRID_W) {
                        val wx = LabyrinthMap.VIEW_MIN_X + (gx + 0.5f) * STEP
                        val gx0 = max(0, gx - 1)
                        val gx1 = min(GRID_W - 1, gx + 1)
                        val idx = rowOffset + gx

                        val hC = heightField[idx]
                        val hL = heightField[rowOffset + gx0]
                        val hR = heightField[rowOffset + gx1]
                        val hU = heightField[gy0 * GRID_W + gx]
                        val hD = heightField[gy1 * GRID_W + gx]

                        // Central-difference 3D surface normal N = normalize(-dh/dx, -dh/dy, 1.0)
                        val dhdx = (hR - hL) * 0.42f
                        val dhdy = (hD - hU) * 0.42f
                        val invLen = 1.0f / sqrt(dhdx * dhdx + dhdy * dhdy + 1.0f)
                        val nx = -dhdx * invLen
                        val ny = -dhdy * invLen
                        val nz = invLen

                        val ndotl = max(0f, nx * SUN_X + ny * SUN_Y + nz * SUN_Z)
                        val ndoth = max(0f, nx * HALF_X + ny * HALF_Y + nz * HALF_Z)

                        val shadow = shadowMap[idx]
                        val ao = aoMap[idx]
                        val tone = stoneToneMap[idx]
                        val mat = materialMap[idx].toInt()

                        var baseR: Float
                        var baseG: Float
                        var baseB: Float
                        var shininess = 20f
                        var specStrength = 0.18f

                        when (mat) {
                            MAT_MEADOW -> {
                                val grassVar = (tone - 0.5f) * 18f + (hC * 1.4f)
                                baseR = (66f + grassVar) * turfBand
                                baseG = (118f + grassVar * 1.3f) * turfBand
                                baseB = (44f + grassVar * 0.6f) * turfBand
                                shininess = 12f
                                specStrength = 0.10f
                            }
                            MAT_TERRACE_TOP -> {
                                baseR = 82f + tone * 16f
                                baseG = 136f + tone * 20f
                                baseB = 54f + tone * 10f
                                shininess = 14f
                                specStrength = 0.14f
                            }
                            MAT_TERRACE_CLIFF, MAT_OUTER_CLIFF -> {
                                // Stratified 3D Limestone & Granite Cliff Face with moss on flatter ledges
                                val mossBlend = (nz - 0.55f).coerceIn(0f, 0.35f) * 2.2f
                                val rockR = 112f + (tone - 0.5f) * 32f
                                val rockG = 106f + (tone - 0.5f) * 28f
                                val rockB = 96f + (tone - 0.5f) * 24f
                                baseR = rockR * (1f - mossBlend) + 68f * mossBlend
                                baseG = rockG * (1f - mossBlend) + 114f * mossBlend
                                baseB = rockB * (1f - mossBlend) + 48f * mossBlend
                                shininess = 28f
                                specStrength = 0.32f
                            }
                            MAT_RIVER_CANYON_WALL -> {
                                // Wet canyon rock darkening near waterline (-11.5m)
                                val wetness = (1f - ((hC - (-11.5f)) / 10f).coerceIn(0f, 1f)) * 0.45f
                                baseR = (104f + (tone - 0.5f) * 28f) * (1f - wetness)
                                baseG = (96f + (tone - 0.5f) * 25f) * (1f - wetness)
                                baseB = (86f + (tone - 0.5f) * 22f) * (1f - wetness)
                                shininess = 36f
                                specStrength = 0.28f + wetness * 0.45f
                            }
                            MAT_RIVER_WATER -> {
                                // Beer-Lambert 3D Water Refraction + Depth Absorption + Specular Sun Glint + Shoreline Foam!
                                val bedZ = waterBedHeight[idx]
                                val waterDepth = (hC - bedZ).coerceIn(0.4f, 24f)
                                val absorb = exp(-waterDepth * 0.16f)

                                // Refracted golden pebble riverbed vs deep sapphire-emerald water
                                val shallowR = 118f; val shallowG = 138f; val shallowB = 96f
                                val deepR = if (isFutureEra) 16f else 22f
                                val deepG = if (isFutureEra) 96f else 108f
                                val deepB = if (isFutureEra) 156f else 148f

                                baseR = deepR + (shallowR - deepR) * absorb
                                baseG = deepG + (shallowG - deepG) * absorb
                                baseB = deepB + (shallowB - deepB) * absorb

                                // Shoreline & mid-stream whitewater foam
                                val rDist = abs(wx - rCenterX)
                                val shoreFoam = ((rDist - 15.5f) / 5.5f).coerceIn(0f, 1f).pow(2)
                                val rapidFoam = max(0f, sin(wy * 0.09f + wx * 0.15f) - 0.72f) * 1.8f
                                val foam = (shoreFoam * 0.65f + rapidFoam).coerceIn(0f, 0.85f)
                                baseR = baseR * (1f - foam) + 225f * foam
                                baseG = baseG * (1f - foam) + 242f * foam
                                baseB = baseB * (1f - foam) + 252f * foam

                                shininess = 64f
                                specStrength = 0.88f
                            }
                            MAT_ROAD_EMBANKMENT -> {
                                baseR = roadCurbR * 0.72f + (tone - 0.5f) * 22f
                                baseG = roadCurbG * 0.72f + (tone - 0.5f) * 20f
                                baseB = roadCurbB * 0.72f + (tone - 0.5f) * 18f
                                shininess = 22f
                                specStrength = 0.24f
                            }
                            MAT_ROAD_CURB, MAT_BRIDGE_PARAPET -> {
                                val vShift = (tone - 0.5f) * 28f
                                baseR = roadCurbR + 14f + vShift
                                baseG = roadCurbG + 12f + vShift
                                baseB = roadCurbB + 10f + vShift
                                shininess = 34f
                                specStrength = 0.42f
                            }
                            MAT_BRIDGE_DECK -> {
                                val vShift = (tone - 0.5f) * 24f
                                baseR = 132f + vShift
                                baseG = 98f + vShift * 0.8f
                                baseB = 64f + vShift * 0.6f
                                shininess = 26f
                                specStrength = 0.30f
                            }
                            else -> {
                                // MAT_ROAD_COBBLE: Individual 3D Domed Cobblestone Pavers with Mortar Crevices
                                val vShift = (tone - 0.5f) * 32f
                                val warmShift = ((tone * 7f) % 1f - 0.5f) * 14f
                                baseR = roadSurfR + vShift + warmShift
                                baseG = roadSurfG + vShift - warmShift * 0.2f
                                baseB = roadSurfB + vShift - warmShift * 0.5f
                                shininess = 30f
                                specStrength = 0.36f
                            }
                        }

                        // Direct Sun Diffuse + Ambient Sky Dome + SE Warm Bounce Light + Blinn-Phong Specular
                        val skyAmbient = 0.36f + 0.10f * nz
                        val bounceLight = max(0f, -nx * 0.45f - ny * 0.45f) * 0.14f
                        val diffuse = ndotl * 0.82f * shadow
                        val totalLight = (skyAmbient + bounceLight + diffuse) * ao

                        val spec = if (shadow > 0.45f) ndoth.pow(shininess) * specStrength * shadow else 0f
                        val specAdd = spec * 245f

                        // 12 Roadside 3D Braziers Warm Golden Point-Light Illumination!
                        var torchLight = 0f
                        for (b in brazierPositions) {
                            val dx = b[0] - wx
                            val dy = b[1] - wy
                            val dSq = dx * dx + dy * dy
                            if (dSq < 4200f) {
                                val d = sqrt(dSq).coerceAtLeast(1f)
                                val tDirX = dx / d
                                val tDirY = dy / d
                                val tDirZ = 16f / d
                                val nDotTorch = max(0.15f, nx * tDirX + ny * tDirY + nz * tDirZ)
                                val atten = (1f - dSq / 4200f).pow(2)
                                torchLight += atten * nDotTorch * 0.62f
                            }
                        }

                        val rOut = (baseR * totalLight + specAdd + torchLight * 135f).toInt().coerceIn(0, 255)
                        val gOut = (baseG * totalLight + specAdd * 0.96f + torchLight * 82f).toInt().coerceIn(0, 255)
                        val bOut = (baseB * totalLight + specAdd * 0.85f + torchLight * 20f).toInt().coerceIn(0, 255)

                        pixelBuffer[idx] = (0xFF shl 24) or (rOut shl 16) or (gOut shl 8) or bOut
                    }
                }
            })
        }
        workerPool.invokeAll(tasks)

        return Bitmap.createBitmap(pixelBuffer, GRID_W, GRID_H, Bitmap.Config.ARGB_8888)
    }
}

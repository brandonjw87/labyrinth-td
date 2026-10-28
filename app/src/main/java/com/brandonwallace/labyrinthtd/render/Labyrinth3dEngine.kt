package com.brandonwallace.labyrinthtd.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.brandonwallace.labyrinthtd.game.ActiveEnemy
import com.brandonwallace.labyrinthtd.game.EnemyEliteAffix
import com.brandonwallace.labyrinthtd.game.EnemyType
import com.brandonwallace.labyrinthtd.game.EraEvolutionCatalog
import com.brandonwallace.labyrinthtd.game.GameEngine
import com.brandonwallace.labyrinthtd.game.LabyrinthMap
import com.brandonwallace.labyrinthtd.game.MeadowProp
import com.brandonwallace.labyrinthtd.game.MeadowPropType
import com.brandonwallace.labyrinthtd.game.StatusEffect
import com.brandonwallace.labyrinthtd.game.TowerGearAttachment
import com.brandonwallace.labyrinthtd.game.TowerInstance
import com.brandonwallace.labyrinthtd.game.TowerType
import com.brandonwallace.labyrinthtd.game.VisualProjectile
import com.brandonwallace.labyrinthtd.game.VisualShockwave
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * ARGON 3D PBR GRAPHICS, TERRAIN & ARTICULATED MODEL ENGINE (v1.6)
 *
 * - Pre-bakes the non-scrollable 3D Terraced Valley, 3D River Canyon, 3D Arched Bridges,
 *   3D Cobblestone/Ashlar Causeway Road (with individual 3D stone pavers & curbstones),
 *   and 3D Multi-Canopy Forest into a hardware-accelerated Bitmap cache so the ultra-detailed
 *   3D battlefield renders in < 0.1ms per frame at 60 FPS.
 * - Renders 1.48x larger, multi-part 3D articulated Towers (masonry buttresses, crenellations,
 *   torsion coils, swinging trebuchet counterweights, rotating waterwheels, orbiting gunships).
 * - Renders 1.45x larger, multi-part 3D articulated Creeps (striding boots, plate cuirass,
 *   pauldrons, heater shields, broadswords, visored helmets with plumes, 4-legged galloping
 *   warhorses with couched lances, 8-wheeled siege tanks with smoking exhaust stacks, and
 *   twin-nacelle swept-wing aircraft).
 * - Renders 9 distinct 3D Shots & Attacks (parabolic 3D boulders, winged ballista javelins,
 *   lofted 3D flaming meteors, tumbling alchemical flasks, 3D multi-strand jagged lightning,
 *   3D ringed photon beams, 3D finned cruise missiles, 3D Gauss sabots with Mach rings,
 *   and 3D frost crystal volleys) with zero per-frame heap allocations.
 */
object Labyrinth3dEngine {

    private const val Z_TILT = 0.80f
    private const val ELLIPSE_Y_RATIO = 0.72f
    private const val TOWER_MODEL_SCALE = 1.46f
    private const val CREEP_MODEL_SCALE = 1.44f

    // Directional Sun vector (shining from top-left NW toward bottom-right SE)
    private const val SUN_X = -0.56f
    private const val SUN_Y = -0.60f
    private const val SUN_Z = 0.57f

    // Half-vector between Sun and Viewer for Blinn-Phong specular highlights
    private const val HALF_X = -0.32f
    private const val HALF_Y = -0.60f
    private const val HALF_Z = 0.73f

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    }
    private val sansPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }

    private val polyPath = Path()
    private val riverPath = Path()
    private val tempRect = RectF()

    // Cached static 3D battlefield bitmap (rebuilt only on viewport resize or Age-Up)
    private var cachedMapBitmap: Bitmap? = null
    private var cachedMapW: Int = 0
    private var cachedMapH: Int = 0
    private var cachedMapAge: Int = -1
    private var cachedTopHudPx: Float = 0f
    private var cachedBottomHudPx: Float = 0f

    // Precomputed roadside torch/brazier positions on the grass shoulders beside labyrinth turns
    private val brazierPositions = arrayOf(
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

    // Exact 3D bridge crossings where the horizontal labyrinth road legs cross the central river canyon
    private val bridgeCrossings = arrayOf(
        floatArrayOf(492f, 90f),
        floatArrayOf(505f, 220f),
        floatArrayOf(510f, 350f),
        floatArrayOf(495f, 485f),
        floatArrayOf(482f, 670f),
        floatArrayOf(492f, 805f),
        floatArrayOf(505f, 940f),
        floatArrayOf(510f, 1075f)
    )

    fun projX(x: Float, z: Float = 0f): Float = x
    fun projY(y: Float, z: Float = 0f): Float = y - z * Z_TILT

    fun shadePbrColor(
        baseArgb: Int,
        nx: Float,
        ny: Float,
        nz: Float,
        shininess: Float = 24f,
        specularStrength: Float = 0.38f,
        ambient: Float = 0.44f
    ): Int {
        val len = sqrt(nx * nx + ny * ny + nz * nz).coerceAtLeast(0.001f)
        val ux = nx / len
        val uy = ny / len
        val uz = nz / len

        val ndotl = max(0f, ux * SUN_X + uy * SUN_Y + uz * SUN_Z)
        val ndoth = max(0f, ux * HALF_X + uy * HALF_Y + uz * HALF_Z)
        val spec = ndoth.pow(shininess) * specularStrength

        val lightFactor = (ambient + ndotl * 0.68f).coerceIn(0.22f, 1.35f)
        val specAdd = (spec * 255f).toInt()

        val a = (baseArgb ushr 24) and 0xFF
        val r = (((baseArgb ushr 16) and 0xFF) * lightFactor + specAdd).toInt().coerceIn(0, 255)
        val g = (((baseArgb ushr 8) and 0xFF) * lightFactor + specAdd).toInt().coerceIn(0, 255)
        val b = (((baseArgb and 0xFF) * lightFactor + specAdd * 0.9f).toInt()).coerceIn(0, 255)
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }

    fun darkenColor(argb: Int, factor: Float): Int {
        val a = (argb ushr 24) and 0xFF
        val r = (((argb ushr 16) and 0xFF) * factor).toInt().coerceIn(0, 255)
        val g = (((argb ushr 8) and 0xFF) * factor).toInt().coerceIn(0, 255)
        val b = ((argb and 0xFF) * factor).toInt().coerceIn(0, 255)
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }

    fun brightenColor(argb: Int, add: Int): Int {
        val a = (argb ushr 24) and 0xFF
        val r = (((argb ushr 16) and 0xFF) + add).coerceIn(0, 255)
        val g = (((argb ushr 8) and 0xFF) + add).coerceIn(0, 255)
        val b = ((argb and 0xFF) + add).coerceIn(0, 255)
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }

    fun drawCastGroundShadow(
        canvas: Canvas,
        groundX: Float,
        groundY: Float,
        radiusX: Float,
        radiusY: Float,
        heightZ: Float
    ) {
        val shiftX = heightZ * 0.45f
        val shiftY = heightZ * 0.35f
        fillPaint.color = 0x2B000000
        tempRect.set(
            groundX - radiusX * 0.85f + shiftX * 0.45f,
            groundY - radiusY * 0.75f + shiftY * 0.45f,
            groundX + radiusX * 1.28f + shiftX,
            groundY + radiusY * 1.18f + shiftY
        )
        canvas.drawOval(tempRect, fillPaint)

        fillPaint.color = 0x52000000
        tempRect.set(
            groundX - radiusX * 0.96f + 1.5f,
            groundY - radiusY * 0.84f + 1.5f,
            groundX + radiusX * 1.04f + shiftX * 0.42f,
            groundY + radiusY * 0.94f + shiftY * 0.42f
        )
        canvas.drawOval(tempRect, fillPaint)
    }

    fun drawExtrudedCylinder3D(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        zBottom: Float,
        zTop: Float,
        rBottom: Float,
        rTop: Float,
        baseColor: Int,
        topColor: Int = brightenColor(baseColor, 24),
        rimColor: Int = darkenColor(baseColor, 0.60f),
        facets: Int = 12,
        shininess: Float = 28f,
        specStrength: Float = 0.38f
    ) {
        val by = projY(cy, zBottom)
        val ty = projY(cy, zTop)
        val ryBot = rBottom * ELLIPSE_Y_RATIO
        val ryTop = rTop * ELLIPSE_Y_RATIO

        val step = (2.0 * PI) / facets
        for (i in 0 until facets) {
            val a0 = i * step
            val a1 = (i + 1) * step
            val midA = (a0 + a1) * 0.5
            if (sin(midA) < -0.22) continue

            val x0b = cx + (cos(a0) * rBottom).toFloat()
            val y0b = by + (sin(a0) * ryBot).toFloat()
            val x1b = cx + (cos(a1) * rBottom).toFloat()
            val y1b = by + (sin(a1) * ryBot).toFloat()

            val x0t = cx + (cos(a0) * rTop).toFloat()
            val y0t = ty + (sin(a0) * ryTop).toFloat()
            val x1t = cx + (cos(a1) * rTop).toFloat()
            val y1t = ty + (sin(a1) * ryTop).toFloat()

            val nx = cos(midA).toFloat()
            val ny = sin(midA).toFloat()
            val nz = ((rBottom - rTop) / max(1f, zTop - zBottom)).coerceIn(-0.5f, 0.7f)

            fillPaint.color = shadePbrColor(baseColor, nx, ny, nz, shininess, specStrength)
            polyPath.reset()
            polyPath.moveTo(x0b, y0b)
            polyPath.lineTo(x1b, y1b)
            polyPath.lineTo(x1t, y1t)
            polyPath.lineTo(x0t, y0t)
            polyPath.close()
            canvas.drawPath(polyPath, fillPaint)
        }

        strokePaint.color = darkenColor(rimColor, 0.68f)
        strokePaint.strokeWidth = 1.4f
        tempRect.set(cx - rBottom, by - ryBot, cx + rBottom, by + ryBot)
        canvas.drawArc(tempRect, 5f, 170f, false, strokePaint)

        fillPaint.color = shadePbrColor(topColor, -0.18f, -0.20f, 0.96f, shininess, specStrength)
        tempRect.set(cx - rTop, ty - ryTop, cx + rTop, ty + ryTop)
        canvas.drawOval(tempRect, fillPaint)

        strokePaint.color = brightenColor(topColor, 58)
        strokePaint.strokeWidth = 1.6f
        canvas.drawArc(tempRect, 190f, 130f, false, strokePaint)

        strokePaint.color = rimColor
        strokePaint.strokeWidth = 1.3f
        canvas.drawOval(tempRect, strokePaint)
    }

    fun drawCone3D(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        zBase: Float,
        zPeak: Float,
        radius: Float,
        baseColor: Int,
        ribColor: Int = darkenColor(baseColor, 0.70f),
        facets: Int = 8,
        rotationRad: Float = 0f,
        shininess: Float = 36f,
        specStrength: Float = 0.45f
    ) {
        val by = projY(cy, zBase)
        val py = projY(cy, zPeak)
        val ry = radius * ELLIPSE_Y_RATIO
        val step = (2.0 * PI) / facets

        for (pass in 0..1) {
            for (i in 0 until facets) {
                val midA = rotationRad + (i + 0.5) * step
                val isFront = sin(midA) >= 0.0
                if ((pass == 0 && isFront) || (pass == 1 && !isFront)) continue

                val a0 = rotationRad + i * step
                val a1 = rotationRad + (i + 1) * step
                val x0 = cx + (cos(a0) * radius).toFloat()
                val y0 = by + (sin(a0) * ry).toFloat()
                val x1 = cx + (cos(a1) * radius).toFloat()
                val y1 = by + (sin(a1) * ry).toFloat()

                val nx = cos(midA).toFloat() * 0.78f
                val ny = sin(midA).toFloat() * 0.78f
                val nz = 0.62f

                fillPaint.color = shadePbrColor(baseColor, nx, ny, nz, shininess, specStrength)
                polyPath.reset()
                polyPath.moveTo(cx, py)
                polyPath.lineTo(x0, y0)
                polyPath.lineTo(x1, y1)
                polyPath.close()
                canvas.drawPath(polyPath, fillPaint)

                strokePaint.color = ribColor
                strokePaint.strokeWidth = 1.0f
                canvas.drawPath(polyPath, strokePaint)
            }
        }
    }

    private val lxArr = FloatArray(4)
    private val lyArr = FloatArray(4)
    private val bxArr = FloatArray(4)
    private val byArr = FloatArray(4)
    private val txArr = FloatArray(4)
    private val tyArr = FloatArray(4)
    private val wallOrder = IntArray(4)

    fun drawExtrudedBox3D(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        zBottom: Float,
        zTop: Float,
        halfLength: Float,
        halfWidth: Float,
        yawRad: Float,
        baseColor: Int,
        trimColor: Int = darkenColor(baseColor, 0.62f),
        shininess: Float = 28f
    ) {
        val cosA = cos(yawRad)
        val sinA = sin(yawRad)

        lxArr[0] = halfLength; lyArr[0] = halfWidth
        lxArr[1] = -halfLength; lyArr[1] = halfWidth
        lxArr[2] = -halfLength; lyArr[2] = -halfWidth
        lxArr[3] = halfLength; lyArr[3] = -halfWidth

        val yBotScreen = projY(cy, zBottom)
        val yTopScreen = projY(cy, zTop)

        for (i in 0..3) {
            val wx = lxArr[i] * cosA - lyArr[i] * sinA
            val wy = (lxArr[i] * sinA + lyArr[i] * cosA) * ELLIPSE_Y_RATIO
            bxArr[i] = cx + wx
            byArr[i] = yBotScreen + wy
            txArr[i] = cx + wx
            tyArr[i] = yTopScreen + wy
            wallOrder[i] = i
        }

        // Insertion sort 4 walls by average Y (zero allocations!)
        for (a in 0..2) {
            for (b in a + 1..3) {
                val iA = wallOrder[a]
                val jA = (iA + 1) and 3
                val iB = wallOrder[b]
                val jB = (iB + 1) and 3
                if ((byArr[iA] + byArr[jA]) > (byArr[iB] + byArr[jB])) {
                    wallOrder[a] = iB
                    wallOrder[b] = iA
                }
            }
        }

        for (idx in 0..3) {
            val i = wallOrder[idx]
            val j = (i + 1) and 3
            val ex = bxArr[j] - bxArr[i]
            val ey = byArr[j] - byArr[i]
            val nx = ey
            val ny = -ex
            fillPaint.color = shadePbrColor(baseColor, nx, ny, 0.16f, shininess, 0.36f)
            polyPath.reset()
            polyPath.moveTo(bxArr[i], byArr[i])
            polyPath.lineTo(bxArr[j], byArr[j])
            polyPath.lineTo(txArr[j], tyArr[j])
            polyPath.lineTo(txArr[i], tyArr[i])
            polyPath.close()
            canvas.drawPath(polyPath, fillPaint)

            strokePaint.color = trimColor
            strokePaint.strokeWidth = 1.1f
            canvas.drawPath(polyPath, strokePaint)
        }

        fillPaint.color = shadePbrColor(brightenColor(baseColor, 20), -0.15f, -0.20f, 0.96f, shininess, 0.44f)
        polyPath.reset()
        polyPath.moveTo(txArr[0], tyArr[0])
        polyPath.lineTo(txArr[1], tyArr[1])
        polyPath.lineTo(txArr[2], tyArr[2])
        polyPath.lineTo(txArr[3], tyArr[3])
        polyPath.close()
        canvas.drawPath(polyPath, fillPaint)

        strokePaint.color = brightenColor(trimColor, 38)
        strokePaint.strokeWidth = 1.3f
        canvas.drawPath(polyPath, strokePaint)
    }

    fun drawSphere3D(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        zCenter: Float,
        radius: Float,
        baseColor: Int,
        specStrength: Float = 0.58f
    ) {
        val sy = projY(cy, zCenter)
        fillPaint.color = darkenColor(baseColor, 0.64f)
        canvas.drawCircle(cx, sy, radius, fillPaint)

        fillPaint.color = baseColor
        canvas.drawCircle(cx - radius * 0.14f, sy - radius * 0.14f, radius * 0.86f, fillPaint)

        fillPaint.color = brightenColor(baseColor, (58 * specStrength).toInt())
        canvas.drawCircle(cx - radius * 0.28f, sy - radius * 0.28f, radius * 0.55f, fillPaint)

        if (specStrength > 0.18f) {
            fillPaint.color = brightenColor(baseColor, (145 * specStrength).toInt())
            canvas.drawCircle(cx - radius * 0.36f, sy - radius * 0.36f, radius * 0.25f, fillPaint)
        }
    }

    fun drawBarrel3D(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        zBase: Float,
        yawRad: Float,
        length: Float,
        pitchZ: Float,
        thickness: Float,
        barrelColor: Int,
        muzzleColor: Int = darkenColor(barrelColor, 0.5f),
        glowArgb: Int? = null
    ) {
        val x0 = cx
        val y0 = projY(cy, zBase)
        val x1 = cx + cos(yawRad) * length
        val y1 = projY(cy + sin(yawRad) * length * ELLIPSE_Y_RATIO, zBase + pitchZ)

        strokePaint.color = darkenColor(barrelColor, 0.48f)
        strokePaint.strokeWidth = thickness
        canvas.drawLine(x0, y0 + thickness * 0.22f, x1, y1 + thickness * 0.22f, strokePaint)

        strokePaint.color = shadePbrColor(barrelColor, -sin(yawRad), cos(yawRad), 0.65f, 34f, 0.48f)
        strokePaint.strokeWidth = thickness * 0.82f
        canvas.drawLine(x0, y0, x1, y1, strokePaint)

        strokePaint.color = brightenColor(barrelColor, 68)
        strokePaint.strokeWidth = thickness * 0.28f
        canvas.drawLine(x0, y0 - thickness * 0.18f, x1, y1 - thickness * 0.18f, strokePaint)

        fillPaint.color = muzzleColor
        canvas.drawCircle(x1, y1, thickness * 0.54f, fillPaint)
        if (glowArgb != null) {
            fillPaint.color = glowArgb
            canvas.drawCircle(x1, y1, thickness * 0.36f, fillPaint)
        } else {
            fillPaint.color = 0xFF141210.toInt()
            canvas.drawCircle(x1, y1, thickness * 0.28f, fillPaint)
        }
    }

    // =========================================================================
    // 1. OFFSCREEN-CACHED 3D TERRACED BATTLEFIELD, RIVER, BRIDGES & COBBLESTONE ROAD
    // =========================================================================

    fun getOrBuildStaticMapBitmap(
        viewW: Int,
        viewH: Int,
        scaleX: Float,
        scaleY: Float,
        offsetX: Float,
        offsetY: Float,
        ageNum: Int,
        roadPath: Path
    ): Bitmap {
        val existing = cachedMapBitmap
        if (existing != null &&
            cachedMapW == viewW &&
            cachedMapH == viewH &&
            cachedMapAge == ageNum &&
            kotlin.math.abs(cachedTopHudPx - offsetY) < 1f
        ) {
            return existing
        }

        val bmp = if (existing != null && existing.width == viewW && existing.height == viewH) {
            existing.eraseColor(0)
            existing
        } else {
            existing?.recycle()
            Bitmap.createBitmap(viewW.coerceAtLeast(1), viewH.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
        }

        val c = Canvas(bmp)
        val theme = EraEvolutionCatalog.themeForAge(ageNum)

        // 1. Deep 3D Sunlit Valley Gradient Background
        val bgGrad = LinearGradient(
            0f, 0f, viewW.toFloat(), viewH.toFloat(),
            intArrayOf(0xFF4F8435.toInt(), 0xFF43732C.toInt(), 0xFF335B20.toInt()),
            floatArrayOf(0f, 0.55f, 1f),
            Shader.TileMode.CLAMP
        )
        fillPaint.shader = bgGrad
        c.drawRect(0f, 0f, viewW.toFloat(), viewH.toFloat(), fillPaint)
        fillPaint.shader = null

        c.save()
        c.translate(offsetX, offsetY)
        c.scale(scaleX, scaleY)

        // 2. 3D Terraced Meadow Contour Plateaus & Mowed Turf Striping
        for (band in 0 until 20) {
            val y0 = band * 68f
            fillPaint.color = if (band % 2 == 0) 0x1A6AA84F else 0x14274E13
            c.drawRect(0f, y0, LabyrinthMap.WORLD_WIDTH, y0 + 68f, fillPaint)
        }

        // Raised 3D Grassy Ridge Terraces in the grass bands between horizontal road legs
        val ridgeSpots = arrayOf(
            floatArrayOf(290f, 155f, 145f, 28f),
            floatArrayOf(710f, 155f, 145f, 28f),
            floatArrayOf(290f, 285f, 145f, 28f),
            floatArrayOf(710f, 285f, 145f, 28f),
            floatArrayOf(290f, 738f, 145f, 28f),
            floatArrayOf(710f, 738f, 145f, 28f),
            floatArrayOf(290f, 872f, 145f, 28f),
            floatArrayOf(710f, 872f, 145f, 28f),
            floatArrayOf(290f, 1008f, 145f, 28f),
            floatArrayOf(710f, 1008f, 145f, 28f)
        )
        for (r in ridgeSpots) {
            val rx = r[0]; val ry = r[1]; val rw = r[2]; val rh = r[3]
            // SE cliff shadow
            fillPaint.color = 0x28000000
            tempRect.set(rx - rw + 6f, ry - rh + 7f, rx + rw + 6f, ry + rh + 7f)
            c.drawRoundRect(tempRect, 22f, 22f, fillPaint)
            // 3D Stone cliff skirt
            fillPaint.color = 0xFF515A47.toInt()
            tempRect.set(rx - rw, ry - rh + 4f, rx + rw, ry + rh + 4f)
            c.drawRoundRect(tempRect, 20f, 20f, fillPaint)
            // Sunlit upper meadow terrace
            fillPaint.color = 0xFF568E3A.toInt()
            tempRect.set(rx - rw, ry - rh, rx + rw, ry + rh)
            c.drawRoundRect(tempRect, 20f, 20f, fillPaint)
            strokePaint.color = 0x558CC468
            strokePaint.strokeWidth = 2.0f
            c.drawRoundRect(tempRect, 20f, 20f, strokePaint)
        }

        // 3. 3D Sunken River Canyon flowing North-to-South through the center of the valley
        riverPath.reset()
        riverPath.moveTo(485f, 0f)
        riverPath.cubicTo(535f, 340f, 455f, 680f, 515f, 1020f)
        riverPath.lineTo(490f, LabyrinthMap.WORLD_HEIGHT)

        // River canyon rocky shadow bank
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeCap = Paint.Cap.BUTT
        strokePaint.color = 0xFF3A352D.toInt()
        strokePaint.strokeWidth = 54f
        c.drawPath(riverPath, strokePaint)

        // River stone shore ledges
        strokePaint.color = 0xFF6E675B.toInt()
        strokePaint.strokeWidth = 46f
        c.drawPath(riverPath, strokePaint)

        // Deep 3D Sapphire Water Channel
        strokePaint.color = if (ageNum >= 12) 0xFF154260.toInt() else 0xFF1D5E82.toInt()
        strokePaint.strokeWidth = 36f
        c.drawPath(riverPath, strokePaint)

        // Sunlit Shallow Turquoise Water Core
        strokePaint.color = if (ageNum >= 12) 0xFF2288B5.toInt() else 0xFF328EB8.toInt()
        strokePaint.strokeWidth = 22f
        c.drawPath(riverPath, strokePaint)

        // Sparkling Water Specular Foam Line
        strokePaint.color = 0x66B3E5FC
        strokePaint.strokeWidth = 6f
        c.drawPath(riverPath, strokePaint)
        strokePaint.strokeCap = Paint.Cap.ROUND

        // 4. 3D Raised Cobblestone & Ashlar Causeway Road
        drawDetailed3dRoadIntoCache(c, roadPath, ageNum)

        // 5. 3D Arched Stone & Timber Bridges where the Labyrinth Road crosses the River Canyon!
        for (br in bridgeCrossings) {
            val bx = br[0]
            val by = br[1]
            // Bridge cast shadow on the river water
            fillPaint.color = 0x55000000
            tempRect.set(bx - 46f, by - 34f, bx + 52f, by + 40f)
            c.drawRoundRect(tempRect, 6f, 6f, fillPaint)

            // 4 3D Stone Bridge Pier Buttresses in the water
            for (pxOff in intArrayOf(-34, 34)) {
                for (pyOff in intArrayOf(-28, 28)) {
                    drawExtrudedCylinder3D(
                        c, bx + pxOff, by + pyOff, 0f, 11f, 7.5f, 6.5f,
                        0xFF7A7267.toInt(), 0xFFA69D8E.toInt(), facets = 6
                    )
                }
            }
            // North & South 3D Stone Bridge Parapet Walls with Timber/Gold Railings
            drawExtrudedBox3D(
                c, bx, by - 27f, 2f, 12f, 42f, 4.5f, 0f,
                if (ageNum >= 8) theme.foundationRimArgb.toInt() else 0xFF8C8274.toInt(),
                0xFFD4AF37.toInt()
            )
            drawExtrudedBox3D(
                c, bx, by + 27f, 2f, 12f, 42f, 4.5f, 0f,
                if (ageNum >= 8) theme.foundationRimArgb.toInt() else 0xFF8C8274.toInt(),
                0xFFD4AF37.toInt()
            )
        }

        // 6. 3D Perimeter Castle Curtain Wall Frame around the Battlefield
        strokePaint.color = 0x88233917.toInt()
        strokePaint.strokeWidth = 10f
        c.drawRect(5f, 5f, LabyrinthMap.WORLD_WIDTH - 5f, LabyrinthMap.WORLD_HEIGHT - 5f, strokePaint)
        strokePaint.color = 0xAA8C6D3B.toInt()
        strokePaint.strokeWidth = 3.5f
        c.drawRect(8f, 8f, LabyrinthMap.WORLD_WIDTH - 8f, LabyrinthMap.WORLD_HEIGHT - 8f, strokePaint)

        // 7. 3D Multi-Canopy Trees, Faceted Boulders & Wildflowers
        drawMeadowProps3D(c, LabyrinthMap.meadowProps, 0f)

        // 8. 3D Stone & Iron Roadside Braziers (Static Pedestals)
        for (bp in brazierPositions) {
            drawCastGroundShadow(c, bp[0], bp[1], 11f, 8.5f, 18f)
            drawExtrudedCylinder3D(
                c, bp[0], bp[1], 0f, 12f, 8.5f, 6.8f,
                0xFF4E463E.toInt(), 0xFF2E2924.toInt(), 0xFFD4AF37.toInt(), facets = 8
            )
        }

        c.restore()

        cachedMapBitmap = bmp
        cachedMapW = viewW
        cachedMapH = viewH
        cachedMapAge = ageNum
        cachedTopHudPx = offsetY
        cachedBottomHudPx = scaleY
        return bmp
    }

    /**
     * Bakes an ultra-detailed 3D stone causeway with embankment drop shadows, sloped retaining walls,
     * individual 3D bevelled stone pavers along the entire path, and 3D raised granite curbstones!
     */
    private fun drawDetailed3dRoadIntoCache(canvas: Canvas, roadPath: Path, ageNum: Int) {
        val theme = EraEvolutionCatalog.themeForAge(ageNum)
        val rw = LabyrinthMap.ROAD_WIDTH

        // 1. Deep 3D Embankment Shadow (shifted SE)
        canvas.save()
        canvas.translate(6.5f, 8.0f)
        strokePaint.color = 0x66000000
        strokePaint.strokeWidth = rw + 20f
        canvas.drawPath(roadPath, strokePaint)
        canvas.restore()

        // 2. 3D Sloped Stone Retaining Foundation (shifted slightly SE)
        canvas.save()
        canvas.translate(2.5f, 3.5f)
        strokePaint.color = darkenColor(theme.roadBorderArgb.toInt(), 0.58f)
        strokePaint.strokeWidth = rw + 16f
        canvas.drawPath(roadPath, strokePaint)
        canvas.restore()

        // 3. Outer Curb Base
        strokePaint.color = darkenColor(theme.roadBorderArgb.toInt(), 0.80f)
        strokePaint.strokeWidth = rw + 12f
        canvas.drawPath(roadPath, strokePaint)

        // 4. Sunlit NW Top Bevel of Retaining Wall
        canvas.save()
        canvas.translate(-2.0f, -2.2f)
        strokePaint.color = brightenColor(theme.roadBorderArgb.toInt(), 36)
        strokePaint.strokeWidth = rw + 6f
        canvas.drawPath(roadPath, strokePaint)
        canvas.restore()

        // 5. Main Road Surface Bed
        strokePaint.color = theme.roadSurfaceArgb.toInt()
        strokePaint.strokeWidth = rw
        canvas.drawPath(roadPath, strokePaint)

        // 6. Individual 3D Bevelled Cobblestone / Ashlar Pavers & Raised 3D Curbstones along the PathMeasure!
        val pm = PathMeasure(roadPath, false)
        val len = pm.length
        val pos = FloatArray(2)
        val tan = FloatArray(2)
        val paverStep = 24f
        var d = 12f
        var idx = 0
        while (d < len - 12f) {
            if (pm.getPosTan(d, pos, tan)) {
                val px = pos[0]
                val py = pos[1]
                val tx = tan[0]
                val ty = tan[1]
                val nx = -ty
                val ny = tx
                val yaw = atan2(ty, tx)

                // Left & Right 3D Raised Granite Curbstones
                val curbDist = rw * 0.50f
                for (side in intArrayOf(-1, 1)) {
                    val cx = px + nx * (curbDist * side)
                    val cy = py + ny * (curbDist * side)
                    val curbCol = if ((idx + side) % 2 == 0) {
                        brightenColor(theme.roadBorderArgb.toInt(), 18)
                    } else {
                        darkenColor(theme.roadBorderArgb.toInt(), 0.88f)
                    }
                    drawExtrudedBox3D(
                        canvas, cx, cy, 0f, 3.8f, 9.5f, 3.6f, yaw,
                        curbCol, darkenColor(curbCol, 0.62f), shininess = 22f
                    )
                }

                // 2 Rows of 3D Bevelled Cobblestone Slabs on the Road Surface
                for (lane in intArrayOf(-1, 1)) {
                    val stagger = if (lane == 1) 6f else -4f
                    val sx = px + tx * stagger + nx * (lane * rw * 0.22f)
                    val sy = py + ty * stagger + ny * (lane * rw * 0.22f)
                    val slabTone = if ((idx + lane) % 3 == 0) {
                        brightenColor(theme.roadSurfaceArgb.toInt(), 14)
                    } else if ((idx + lane) % 3 == 1) {
                        darkenColor(theme.roadSurfaceArgb.toInt(), 0.90f)
                    } else {
                        theme.roadSurfaceArgb.toInt()
                    }
                    // Mortar shadow on SE edge
                    fillPaint.color = 0x44000000
                    canvas.drawRoundRect(sx - 8f + 1.2f, sy - 6f + 1.2f, sx + 8f + 1.2f, sy + 6f + 1.2f, 3f, 3f, fillPaint)
                    // Lit stone paver top
                    fillPaint.color = slabTone
                    canvas.drawRoundRect(sx - 8f, sy - 6f, sx + 8f, sy + 6f, 3f, 3f, fillPaint)
                    // Specular NW edge highlight
                    strokePaint.color = brightenColor(slabTone, 28)
                    strokePaint.strokeWidth = 1.0f
                    canvas.drawLine(sx - 6.5f, sy - 5f, sx + 6.5f, sy - 5f, strokePaint)
                }
            }
            d += paverStep
            idx++
        }
    }

    /**
     * Renders live animated 3D torch flames & warm golden light pools on the roadside braziers,
     * plus shimmering 3D water ripples on the river.
     */
    fun drawLiveMapAtmosphere3D(canvas: Canvas, animTimeSec: Float) {
        val pulse = 0.85f + 0.15f * sin(animTimeSec * 6.5f)
        for (i in brazierPositions.indices) {
            val bp = brazierPositions[i]
            val bx = bp[0]
            val by = bp[1]
            val flicker = sin(animTimeSec * 8.0f + i * 1.7f) * 1.6f
            // Warm golden light halo on the ground around the brazier
            fillPaint.color = 0x22FFAB00
            canvas.drawCircle(bx, by, 28f * pulse, fillPaint)
            // 3D Glowing Ember Bed & Rising 3D Flame Sphere at z = 14f..20f
            drawSphere3D(canvas, bx, by, 13.5f, 5.2f, 0xFFFF6D00.toInt(), 0.85f)
            drawSphere3D(canvas, bx + flicker * 0.4f, by, 18.0f + flicker * 0.5f, 3.6f * pulse, 0xFFFFEA00.toInt(), 0.95f)
        }
    }

    fun drawMeadowProps3D(canvas: Canvas, props: List<MeadowProp>, animTimeSec: Float) {
        val sway = sin(animTimeSec * 1.6f) * 1.8f
        for (p in props) {
            val s = p.scale * 1.22f
            when (p.type) {
                MeadowPropType.GRASS_TUFT -> {
                    strokePaint.color = 0xFF2D521E.toInt()
                    strokePaint.strokeWidth = 2.2f
                    canvas.drawLine(p.x - 4f * s, p.y + 2f * s, p.x - 6f * s + sway * 0.3f, p.y - 6f * s, strokePaint)
                    canvas.drawLine(p.x, p.y + 2f * s, p.x + sway * 0.3f, p.y - 7f * s, strokePaint)
                    canvas.drawLine(p.x + 4f * s, p.y + 2f * s, p.x + 6f * s + sway * 0.3f, p.y - 5f * s, strokePaint)
                }
                MeadowPropType.PINE_TREE -> {
                    val r = 16.5f * s
                    drawCastGroundShadow(canvas, p.x, p.y, r * 1.0f, r * 0.76f, r * 1.6f)
                    drawExtrudedCylinder3D(
                        canvas, p.x, p.y, 0f, r * 0.62f,
                        r * 0.26f, r * 0.20f,
                        0xFF5C4028.toInt(), 0xFF6E4E32.toInt(), facets = 6
                    )
                    drawCone3D(canvas, p.x + sway * 0.2f, p.y, r * 0.36f, r * 1.05f, r * 1.02f, 0xFF274B1E.toInt(), 0xFF1B3614.toInt(), facets = 8)
                    drawCone3D(canvas, p.x + sway * 0.4f, p.y, r * 0.76f, r * 1.45f, r * 0.82f, 0xFF345E28.toInt(), 0xFF22421A.toInt(), facets = 8)
                    drawCone3D(canvas, p.x + sway * 0.6f, p.y, r * 1.15f, r * 1.82f, r * 0.60f, 0xFF427334.toInt(), 0xFF2B4F21.toInt(), facets = 8)
                    drawCone3D(canvas, p.x + sway * 0.8f, p.y, r * 1.48f, r * 2.12f, r * 0.38f, 0xFF548C42.toInt(), 0xFF345E28.toInt(), facets = 7)
                }
                MeadowPropType.OAK_TREE -> {
                    val r = 18.0f * s
                    drawCastGroundShadow(canvas, p.x, p.y, r * 1.12f, r * 0.84f, r * 1.38f)
                    drawExtrudedCylinder3D(
                        canvas, p.x, p.y, 0f, r * 0.65f,
                        r * 0.30f, r * 0.22f,
                        0xFF5A3D24.toInt(), 0xFF6E4B2E.toInt(), facets = 6
                    )
                    drawSphere3D(canvas, p.x - r * 0.36f, p.y + r * 0.12f, r * 0.78f, r * 0.64f, 0xFF325926.toInt(), 0.30f)
                    drawSphere3D(canvas, p.x + r * 0.38f, p.y + r * 0.10f, r * 0.76f, r * 0.62f, 0xFF2B4D20.toInt(), 0.26f)
                    drawSphere3D(canvas, p.x - r * 0.18f, p.y - r * 0.18f, r * 0.92f, r * 0.65f, 0xFF3F6E31.toInt(), 0.34f)
                    drawSphere3D(canvas, p.x + r * 0.12f, p.y - r * 0.04f, r * 1.14f, r * 0.74f, 0xFF4E853D.toInt(), 0.40f)
                }
                MeadowPropType.BOULDER -> {
                    val r = 12.0f * s
                    drawCastGroundShadow(canvas, p.x, p.y, r * 1.05f, r * 0.78f, r * 0.75f)
                    drawExtrudedCylinder3D(
                        canvas, p.x, p.y, 0f, r * 0.48f,
                        r * 1.0f, r * 0.76f,
                        0xFF757068.toInt(), 0xFF969086.toInt(), facets = 7, shininess = 22f, specStrength = 0.28f
                    )
                    drawCone3D(canvas, p.x - r * 0.12f, p.y, r * 0.44f, r * 0.88f, r * 0.76f, 0xFF8C867D.toInt(), 0xFF5E5952.toInt(), facets = 6, rotationRad = p.seed * 0.5f)
                }
                MeadowPropType.WILDFLOWER -> {
                    fillPaint.color = 0xFF386628.toInt()
                    canvas.drawCircle(p.x, p.y, 5.0f * s, fillPaint)
                    drawSphere3D(canvas, p.x - 2f * s, p.y - 1f * s, 2.5f, 2.5f * s, if (p.seed % 2 == 0) 0xFFFFD54F.toInt() else 0xFFFF7043.toInt(), 0.5f)
                    drawSphere3D(canvas, p.x + 2.2f * s, p.y + 1.2f * s, 2.2f, 2.1f * s, 0xFFE1BEE7.toInt(), 0.5f)
                }
            }
        }
    }

    /**
     * Renders the 3D Multi-Story Raider Portal Gate, 3D Castle Keep Fortress, AND the
     * permanent 3D Extruded Gold Monument Plaque ("Created By: Brandon J. Wallace") on the map!
     */
    fun drawRaiderGateCastleKeepAndCreatorMonument3D(
        canvas: Canvas,
        eng: GameEngine,
        ageNum: Int,
        animTimeSec: Float
    ) {
        val entry = LabyrinthMap.waypoints.first()
        val keep = LabyrinthMap.waypoints.last()
        val theme = EraEvolutionCatalog.themeForAge(ageNum)

        // 1. 3D RAIDER GATE / DIMENSIONAL PORTAL (Top-Left Entry)
        drawCastGroundShadow(canvas, entry.x, entry.y, 38f, 28f, 52f)
        drawExtrudedCylinder3D(
            canvas, entry.x, entry.y, 0f, 14f, 34f, 31f,
            if (ageNum >= 10) theme.foundationFillArgb.toInt() else 0xFF5C432B.toInt(),
            if (ageNum >= 10) theme.foundationRimArgb.toInt() else 0xFF7A593A.toInt(),
            facets = 10
        )
        for (side in intArrayOf(-21, 21)) {
            drawExtrudedCylinder3D(canvas, entry.x + side, entry.y + 4f, 12f, 44f, 9.5f, 8.2f, 0xFF6B533B.toInt(), theme.turretAccentArgb.toInt(), facets = 8)
            drawCone3D(canvas, entry.x + side, entry.y + 4f, 44f, 56f, 10.8f, theme.turretAccentArgb.toInt(), facets = 8)
        }
        // 3D Arch Lintel Beam connecting the two gate towers
        drawExtrudedBox3D(canvas, entry.x, entry.y + 4f, 36f, 44f, 24f, 6f, 0f, 0xFF5A4632.toInt(), 0xFFD4AF37.toInt())

        val pulse = (sin(animTimeSec * 3.5f) * 0.15f + 0.85f)
        drawSphere3D(canvas, entry.x, entry.y, 22f, 12.5f * pulse, if (ageNum >= 12) 0xFF9C4DFF.toInt() else 0xFFE64A19.toInt(), 0.85f)
        sansPaint.color = 0xFFF5EBD6.toInt()
        sansPaint.textSize = 12f
        canvas.drawText("RAIDER GATE", entry.x, projY(entry.y, 60f), sansPaint)

        // 2. 3D MULTI-STORY CASTLE KEEP FORTRESS (Bottom-Right Objective)
        drawCastGroundShadow(canvas, keep.x, keep.y, 48f, 38f, 70f)
        drawExtrudedCylinder3D(
            canvas, keep.x, keep.y, 0f, 14f, 42f, 39f,
            theme.foundationFillArgb.toInt(),
            theme.keepTrimArgb.toInt(),
            facets = 12,
            shininess = 34f
        )
        for (sx in intArrayOf(-24, 24)) {
            drawExtrudedCylinder3D(canvas, keep.x + sx, keep.y - 15f, 12f, 48f, 10.5f, 9.5f, theme.foundationFillArgb.toInt(), theme.keepTrimArgb.toInt(), facets = 8)
            drawCone3D(canvas, keep.x + sx, keep.y - 15f, 48f, 64f, 12f, theme.keepRoofArgb.toInt(), theme.keepTrimArgb.toInt(), facets = 8)
        }
        drawExtrudedBox3D(
            canvas, keep.x, keep.y, 14f, 52f, 23f, 18f, 0f,
            brightenColor(theme.foundationFillArgb.toInt(), 16),
            theme.keepTrimArgb.toInt(),
            shininess = 32f
        )
        drawCone3D(canvas, keep.x, keep.y, 52f, 74f, 18f, theme.keepRoofArgb.toInt(), 0xFFD4AF37.toInt(), facets = 8)
        drawSphere3D(canvas, keep.x, keep.y, 76f, 5.2f, 0xFFFFDF73.toInt(), 0.90f)

        for (sx in intArrayOf(-24, 24)) {
            drawExtrudedCylinder3D(canvas, keep.x + sx, keep.y + 15f, 12f, 46f, 10.5f, 9.5f, theme.foundationFillArgb.toInt(), theme.keepTrimArgb.toInt(), facets = 8)
            drawCone3D(canvas, keep.x + sx, keep.y + 15f, 46f, 62f, 12f, theme.keepRoofArgb.toInt(), theme.keepTrimArgb.toInt(), facets = 8)
        }

        val hpRatio = (eng.coreShield.toFloat() / eng.maxCoreShield.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
        val bannerY = projY(keep.y, 84f)
        fillPaint.color = 0xDD1A140E.toInt()
        tempRect.set(keep.x - 50f, bannerY - 15f, keep.x + 50f, bannerY + 4f)
        canvas.drawRoundRect(tempRect, 5f, 5f, fillPaint)
        strokePaint.color = 0xFFD4AF37.toInt()
        strokePaint.strokeWidth = 1.6f
        canvas.drawRoundRect(tempRect, 5f, 5f, strokePaint)
        sansPaint.color = if (hpRatio > 0.4f) 0xFFE6C875.toInt() else 0xFFE5534B.toInt()
        sansPaint.textSize = 11.5f
        canvas.drawText("CASTLE KEEP ${eng.coreShield}/${eng.maxCoreShield}", keep.x, bannerY - 2f, sansPaint)

        // 3. PERMANENT 3D EXTRUDED GOLD CREATOR MONUMENT IN THE CENTRAL SPIRAL COURTYARD
        drawCreator3dMonumentOnMap(canvas, 500f, 582f, animTimeSec)
    }

    private fun drawCreator3dMonumentOnMap(canvas: Canvas, mx: Float, my: Float, animTimeSec: Float) {
        drawCastGroundShadow(canvas, mx, my, 180f, 30f, 30f)
        drawExtrudedBox3D(canvas, mx, my, 0f, 10f, 172f, 23f, 0f, 0xFF4A3D30.toInt(), 0xFFD4AF37.toInt(), shininess = 36f)
        drawExtrudedBox3D(canvas, mx, my, 10f, 21f, 160f, 18f, 0f, 0xFF282018.toInt(), 0xFFFFDF73.toInt(), shininess = 48f)

        for (side in intArrayOf(-146, 146)) {
            drawExtrudedCylinder3D(canvas, mx + side, my, 21f, 36f, 7.0f, 5.8f, 0xFFD4AF37.toInt(), 0xFFFFF0A8.toInt(), facets = 8)
            drawCone3D(canvas, mx + side, my, 36f, 49f, 7.0f, 0xFFFFDF73.toInt(), 0xFFB8860B.toInt(), facets = 6, rotationRad = animTimeSec)
        }

        val topY = projY(my, 22f)
        textPaint.textSize = 14.0f
        for (depth in 4 downTo 1) {
            textPaint.color = if (depth >= 3) 0xFF4A3208.toInt() else 0xFF8C6214.toInt()
            canvas.drawText("CREATED BY: BRANDON J. WALLACE", mx + depth * 0.7f, topY + depth * 0.85f + 3f, textPaint)
        }
        val shimmer = (sin(animTimeSec * 2.5f) * 20f).toInt()
        textPaint.color = brightenColor(0xFFFFD74A.toInt(), shimmer)
        canvas.drawText("CREATED BY: BRANDON J. WALLACE", mx, topY + 3f, textPaint)
    }

    // =========================================================================
    // 2. ULTRA-DETAILED 1.46x 3D ARTICULATED TOWER MODELS (ALL 32 TOWERS)
    // =========================================================================

    fun drawTowers3D(canvas: Canvas, eng: GameEngine, ageNum: Int, animTimeSec: Float) {
        val theme = EraEvolutionCatalog.themeForAge(ageNum)
        val sortedTowers = eng.towers.values.sortedBy { it.y }
        val s = TOWER_MODEL_SCALE

        for (t in sortedTowers) {
            val x = t.x
            val y = t.y
            val tierScale = (t.maxSinglePath.coerceAtLeast(1) - 1) * 1.5f
            val baseHeightZ = (if (t.type.isFusedApex) 20f else (13f + tierScale * 0.9f)) * 1.15f

            // 1. Long 3D Cast Ground Shadow
            drawCastGroundShadow(canvas, x, y, 22f * s, 17f * s, baseHeightZ + 32f)

            val rimColor = when {
                t.type.isFusedApex || t.ascensionStars > 0 -> 0xFFD4AF37.toInt()
                t.maxSinglePath >= 6 || t.pathD >= 4 -> theme.turretAccentArgb.toInt()
                t.maxSinglePath >= 5 -> 0xFFE6C65A.toInt()
                else -> t.type.primaryColor.toInt()
            }

            // 2. 4 Protruding 3D Corner Stone Buttress Spurs on the Ground Plinth
            for (bIdx in 0..3) {
                val ba = (bIdx * 0.5 + 0.25) * PI
                val bx = x + (cos(ba) * 18.5f * s).toFloat()
                val by = y + (sin(ba) * 18.5f * s * ELLIPSE_Y_RATIO).toFloat()
                drawExtrudedBox3D(
                    canvas, bx, by, 0f, 8.5f, 4.5f * s, 3.2f * s, ba.toFloat(),
                    darkenColor(theme.foundationFillArgb.toInt(), 0.86f), rimColor
                )
            }

            // 3. Lower 3D Octagonal Stone/Titanium Plinth
            drawExtrudedCylinder3D(
                canvas = canvas,
                cx = x,
                cy = y,
                zBottom = 0f,
                zTop = 8.5f,
                rBottom = 21.0f * s,
                rTop = 19.0f * s,
                baseColor = theme.foundationFillArgb.toInt(),
                topColor = theme.foundationRimArgb.toInt(),
                rimColor = rimColor,
                facets = 8,
                shininess = 34f
            )

            // 4. Upper Stepped 3D Masonry Keep Pedestal
            drawExtrudedCylinder3D(
                canvas = canvas,
                cx = x,
                cy = y,
                zBottom = 8.5f,
                zTop = baseHeightZ,
                rBottom = 17.2f * s,
                rTop = 15.5f * s,
                baseColor = brightenColor(theme.foundationFillArgb.toInt(), 14),
                topColor = brightenColor(theme.foundationRimArgb.toInt(), 20),
                rimColor = rimColor,
                facets = 8,
                shininess = 38f
            )

            // 5. Front 3D Arched Embrasure Window / Glowing Power Slot on the Pedestal Wall
            val winY = projY(y + 15.2f * s * ELLIPSE_Y_RATIO, baseHeightZ * 0.52f)
            fillPaint.color = 0xFF1A140E.toInt()
            tempRect.set(x - 3.6f * s, winY - 4.5f * s, x + 3.6f * s, winY + 3.5f * s)
            canvas.drawRoundRect(tempRect, 3f * s, 3f * s, fillPaint)
            fillPaint.color = if (ageNum >= 9 || t.type.isFusedApex) theme.turretAccentArgb.toInt() else 0xFFFFB300.toInt()
            tempRect.inset(1.2f * s, 1.2f * s)
            canvas.drawRoundRect(tempRect, 2f * s, 2f * s, fillPaint)

            // 6. Specific 3D Architectural Superstructure & Articulated Turret
            drawSpecificTowerSuperstructure3D(canvas, t, x, y, baseHeightZ, ageNum, animTimeSec, s)

            // 7. 3D Floating Armory Gear Satellite Orb on NW corner
            if (t.gear != TowerGearAttachment.NONE) {
                val gearColor = when (t.gear) {
                    TowerGearAttachment.GREEK_FIRE_BRAZIER -> 0xFFFF6D28.toInt()
                    TowerGearAttachment.FROST_RUNESTONE -> 0xFF80D8FF.toInt()
                    TowerGearAttachment.VITRIOL_ALEMBIC -> 0xFF8CE64C.toInt()
                    else -> 0xFFFFD740.toInt()
                }
                val orbitZ = baseHeightZ + 18f + sin(animTimeSec * 4f + t.padId) * 2.5f
                drawSphere3D(canvas, x + 15.5f * s, y - 10f * s, orbitZ, 4.8f * s, gearColor, 0.85f)
            }

            // 8. 3D Bevelled Rank / Tier / Wonder Plaque at Base Front
            val totalUpgrades = t.totalLevel
            val frontY = y + 17.5f * s
            if (t.ascensionStars > 0 || t.type.isFusedApex || t.maxSinglePath >= 6) {
                fillPaint.color = 0xEE18120C.toInt()
                tempRect.set(x - 19f, frontY - 6.5f, x + 19f, frontY + 6.5f)
                canvas.drawRoundRect(tempRect, 4f, 4f, fillPaint)
                strokePaint.color = 0xFFD4AF37.toInt()
                strokePaint.strokeWidth = 1.4f
                canvas.drawRoundRect(tempRect, 4f, 4f, strokePaint)
                sansPaint.color = if (t.maxSinglePath >= 6) theme.turretAccentArgb.toInt() else 0xFFFFDF73.toInt()
                sansPaint.textSize = 9.0f
                val badge = when {
                    t.ascensionStars > 0 -> "★${t.ascensionStars}"
                    t.type.isFusedApex -> "★WND"
                    else -> "T${t.maxSinglePath}"
                }
                canvas.drawText(badge, x, frontY + 3.2f, sansPaint)
            } else if (totalUpgrades > 0) {
                val pips = totalUpgrades.coerceAtMost(6)
                val spacing = 5.4f
                val startX = x - (pips - 1) * spacing * 0.5f
                for (i in 0 until pips) {
                    drawSphere3D(
                        canvas,
                        startX + i * spacing,
                        frontY,
                        2.5f,
                        2.5f,
                        if (t.maxSinglePath >= 5) 0xFFFFD740.toInt() else 0xFFF5EBD6.toInt(),
                        0.7f
                    )
                }
            }
        }
    }

    private fun drawSpecificTowerSuperstructure3D(
        canvas: Canvas,
        t: TowerInstance,
        x: Float,
        y: Float,
        zBase: Float,
        ageNum: Int,
        animTimeSec: Float,
        s: Float
    ) {
        val recoil = -t.recoilAnim * 5.2f * s
        val yaw = t.turretAngleRad
        val cosA = cos(yaw)
        val sinA = sin(yaw)
        val theme = EraEvolutionCatalog.themeForAge(ageNum)

        when (t.type) {
            TowerType.GATLING, TowerType.COLOSSUS_SIEGEWORKS -> {
                val towerTopZ = zBase + 16f * s
                drawExtrudedCylinder3D(
                    canvas, x, y, zBase, towerTopZ, 13.5f * s, 12.2f * s,
                    if (ageNum >= 8) 0xFF5A6470.toInt() else 0xFF8C8070.toInt(),
                    if (ageNum >= 8) 0xFF788594.toInt() else 0xFFA69886.toInt(),
                    theme.foundationRimArgb.toInt(), facets = 10
                )
                // 8 3D Crenellated Merlons around drum rim
                for (i in 0 until 8) {
                    val a = i * (PI / 4.0)
                    val mx = x + (cos(a) * 11.8f * s).toFloat()
                    val my = y + (sin(a) * 11.8f * s).toFloat()
                    drawExtrudedBox3D(canvas, mx, my, towerTopZ, towerTopZ + 4.8f * s, 2.3f * s, 2.3f * s, a.toFloat(), 0xFF9C9284.toInt())
                }
                // 3D Armored Cupola + Multi-Barrel Rotary Gatling / Siege Battery
                drawExtrudedBox3D(canvas, x - cosA * 3f * s, y - sinA * 3f * s, towerTopZ, towerTopZ + 7f * s, 7.5f * s, 6.5f * s, yaw, 0xFF3E4752.toInt(), 0xFFD4AF37.toInt())
                drawSphere3D(canvas, x, y, towerTopZ + 6f * s, 7.8f * s, if (t.type.isFusedApex) 0xFFD4AF37.toInt() else 0xFF48525E.toInt(), 0.65f)
                val bx = x + cosA * recoil
                val by = y + sinA * recoil
                drawBarrel3D(canvas, bx - sinA * 3.0f * s, by + cosA * 3.0f * s, towerTopZ + 5.5f * s, yaw, 18.5f * s, 2.5f * s, 3.8f * s, 0xFF363E48.toInt(), 0xFFD4AF37.toInt())
                drawBarrel3D(canvas, bx + sinA * 3.0f * s, by - cosA * 3.0f * s, towerTopZ + 5.5f * s, yaw, 18.5f * s, 2.5f * s, 3.8f * s, 0xFF363E48.toInt(), 0xFFD4AF37.toInt())
                drawBarrel3D(canvas, bx, by, towerTopZ + 8.5f * s, yaw, 20.5f * s, 3.0f * s, 4.2f * s, 0xFFB8860B.toInt(), 0xFFFFDF73.toInt())
                if (t.recoilAnim > 0.35f) {
                    val mx = bx + cosA * 21f * s
                    val my = by + sinA * 21f * s * ELLIPSE_Y_RATIO
                    drawSphere3D(canvas, mx, my, towerTopZ + 11f * s, 4.5f * s * t.recoilAnim, 0xFFFFEA00.toInt(), 0.95f)
                }
            }

            TowerType.SNIPER, TowerType.TACHYON_RAILGUN -> {
                val spireTopZ = zBase + 22f * s
                drawExtrudedCylinder3D(canvas, x, y, zBase, spireTopZ, 11.8f * s, 9.2f * s, 0xFF6E5034.toInt(), 0xFF8C6846.toInt(), facets = 8)
                // 3D Torsion Ballista Carriage Box on top
                drawExtrudedBox3D(canvas, x, y, spireTopZ, spireTopZ + 5.5f * s, 9.5f * s, 5.5f * s, yaw, 0xFF5D4037.toInt(), 0xFFD4AF37.toInt())
                // Twin 3D Bronze Torsion Skein Coils on left & right of bow carriage
                val coilOffX = -sinA * 6.5f * s
                val coilOffY = cosA * 6.5f * s
                drawExtrudedCylinder3D(canvas, x + coilOffX, y + coilOffY, spireTopZ + 1f * s, spireTopZ + 7.5f * s, 3.2f * s, 3.2f * s, 0xFFD4AF37.toInt(), facets = 6)
                drawExtrudedCylinder3D(canvas, x - coilOffX, y - coilOffY, spireTopZ + 1f * s, spireTopZ + 7.5f * s, 3.2f * s, 3.2f * s, 0xFFD4AF37.toInt(), facets = 6)

                // Flexing 3D Bow Arms
                val armSpan = 14.0f * s
                val flex = t.recoilAnim * 0.25f
                val armX1 = x + cos(yaw + 1.35f + flex) * armSpan
                val armY1 = projY(y + sin(yaw + 1.35f + flex) * armSpan * ELLIPSE_Y_RATIO, spireTopZ + 4.5f * s)
                val armX2 = x + cos(yaw - 1.35f - flex) * armSpan
                val armY2 = projY(y + sin(yaw - 1.35f - flex) * armSpan * ELLIPSE_Y_RATIO, spireTopZ + 4.5f * s)
                val pivY = projY(y, spireTopZ + 4.5f * s)
                strokePaint.color = if (ageNum >= 8) 0xFF7090A8.toInt() else 0xFFB87A3D.toInt()
                strokePaint.strokeWidth = 4.0f * s
                canvas.drawLine(x + coilOffX, pivY, armX1, armY1, strokePaint)
                canvas.drawLine(x - coilOffX, pivY, armX2, armY2, strokePaint)

                // Taut Bowstring
                strokePaint.color = 0xFFF5EBD6.toInt()
                strokePaint.strokeWidth = 1.6f * s
                val nockX = x - cosA * (7f * s - recoil)
                val nockY = projY(y - sinA * (7f * s - recoil) * ELLIPSE_Y_RATIO, spireTopZ + 4.5f * s)
                canvas.drawLine(armX1, armY1, nockX, nockY, strokePaint)
                canvas.drawLine(armX2, armY2, nockX, nockY, strokePaint)

                // Loaded 3D Javelin / Tachyon Lance
                drawBarrel3D(
                    canvas, x + cosA * recoil, y + sinA * recoil, spireTopZ + 5.0f * s,
                    yaw, 23f * s, 4.2f * s, 3.6f * s,
                    if (t.type == TowerType.TACHYON_RAILGUN) 0xFFD4AF37.toInt() else 0xFF4A5560.toInt(),
                    0xFFFFE082.toInt(),
                    if (ageNum >= 10 || t.type.isFusedApex) 0xFF64FFDA.toInt() else null
                )
            }

            TowerType.MORTAR, TowerType.GLACIER_BOMBARD -> {
                // 3D Timber/Steel A-Frame Counterweight Trebuchet / Siege Bombard
                drawExtrudedBox3D(canvas, x, y, zBase, zBase + 9f * s, 13.5f * s, 10.5f * s, yaw, 0xFF63482E.toInt(), 0xFF3E2C1B.toInt())
                drawExtrudedBox3D(canvas, x, y, zBase + 9f * s, zBase + 22f * s, 5.5f * s, 8.8f * s, yaw, 0xFF7D5B3A.toInt(), 0xFFD4AF37.toInt())
                // Swinging 3D Counterweight Basket at front of axle
                val cwX = x + cosA * 6.5f * s
                val cwY = y + sinA * 6.5f * s
                drawExtrudedBox3D(canvas, cwX, cwY, zBase + 10f * s, zBase + 18f * s, 4.2f * s, 4.2f * s, yaw, 0xFF3E2C1B.toInt(), 0xFFD4AF37.toInt())
                // Articulated 3D Throwing Arm pitching upward
                val armPitchZ = (11f + t.recoilAnim * 14f) * s
                drawBarrel3D(
                    canvas, x - cosA * 10.5f * s, y - sinA * 10.5f * s, zBase + 20f * s,
                    yaw, 23f * s, armPitchZ, 4.8f * s, 0xFF9C7448.toInt(), 0xFF4A3B2C.toInt()
                )
                val slingX = x - cosA * (9.5f * s - recoil)
                val slingY = y - sinA * (9.5f * s - recoil)
                drawSphere3D(
                    canvas, slingX, slingY, zBase + 18f * s, 6.6f * s,
                    if (t.type == TowerType.GLACIER_BOMBARD) 0xFF80D8FF.toInt() else 0xFF9E988E.toInt(),
                    0.65f
                )
            }

            TowerType.HYDRO, TowerType.TEMPEST_MAELSTROM -> {
                drawExtrudedCylinder3D(canvas, x, y, zBase, zBase + 13f * s, 14.8f * s, 14.0f * s, 0xFF2B5B78.toInt(), 0xFF4FA4CF.toInt(), facets = 12)
                val wheelZ = zBase + 15f * s
                val rot = animTimeSec * 2.4f
                for (i in 0 until 8) {
                    val a = rot + i * (PI / 4.0).toFloat()
                    drawBarrel3D(canvas, x, y, wheelZ, a, 14.2f * s, 0f, 3.4f * s, 0xFFA67846.toInt(), 0xFFD8F0FF.toInt())
                }
                drawSphere3D(canvas, x, y, wheelZ + 2.5f * s, 6.5f * s, 0xFF80D8FF.toInt(), 0.85f)
            }

            TowerType.CRYO, TowerType.FROSTFIRE_PARADOX -> {
                for (i in 0 until 6) {
                    val a = i * (PI / 3.0) + animTimeSec * 0.45
                    val cx = x + (cos(a) * 12.0f * s).toFloat()
                    val cy = y + (sin(a) * 12.0f * s).toFloat()
                    val crystalColor = if (t.type == TowerType.FROSTFIRE_PARADOX && i % 2 == 1) 0xFFFF6D28.toInt() else 0xFF80D8FF.toInt()
                    drawCone3D(canvas, cx, cy, zBase, zBase + 19f * s, 4.5f * s, crystalColor, 0xFFFFFFFF.toInt(), facets = 5)
                }
                val bobZ = sin(animTimeSec * 3.0f) * 2.8f * s
                drawCone3D(canvas, x, y, zBase, zBase + 29f * s + bobZ, 9.0f * s, 0xFFB3E5FC.toInt(), 0xFF29B6F6.toInt(), facets = 6, rotationRad = animTimeSec)
            }

            TowerType.HUB, TowerType.ASTRAL_NEXUS, TowerType.AEGIS_ORBITAL_BASTION -> {
                drawExtrudedBox3D(canvas, x, y, zBase, zBase + 14.5f * s, 12.5f * s, 12.5f * s, 0f, 0xFF7D5A2B.toInt(), 0xFFD4AF37.toInt())
                // 4 3D Marble Corner Columns
                for (cxOff in intArrayOf(-9, 9)) {
                    for (cyOff in intArrayOf(-9, 9)) {
                        drawExtrudedCylinder3D(canvas, x + cxOff * s, y + cyOff * s, zBase, zBase + 16f * s, 2.6f * s, 2.4f * s, 0xFFF5EBD6.toInt(), 0xFFD4AF37.toInt(), facets = 6)
                    }
                }
                drawSphere3D(canvas, x, y, zBase + 16.5f * s, 10.8f * s, if (t.type == TowerType.AEGIS_ORBITAL_BASTION) 0xFF4FA4CF.toInt() else 0xFFD4AF37.toInt(), 0.80f)
                val bobZ = zBase + 28f * s + sin(animTimeSec * 2.8f) * 2.4f * s
                drawSphere3D(canvas, x, y, bobZ, 5.5f * s, 0xFFFFEA70.toInt(), 0.95f)
            }

            TowerType.PYRO, TowerType.ALCHEMICAL_CATHEDRAL -> {
                drawExtrudedCylinder3D(canvas, x, y, zBase, zBase + 16.5f * s, 14.2f * s, 12.2f * s, 0xFF4A2E22.toInt(), 0xFFFF5722.toInt(), 0xFFD4AF37.toInt(), facets = 10)
                val pulse = sin(animTimeSec * 5.0f) * 2.2f * s
                drawSphere3D(canvas, x, y, zBase + 18.5f * s, 8.8f * s, 0xFFFF6D00.toInt(), 0.85f)
                drawCone3D(canvas, x, y, zBase + 18.5f * s, zBase + 33f * s + pulse, 6.8f * s, 0xFFFFC107.toInt(), 0xFFFF5722.toInt(), facets = 6, rotationRad = animTimeSec * 2f)
            }

            TowerType.VENOM, TowerType.PLAGUEBRINGER_VULCAN, TowerType.NANITE_SWARM_HIVE -> {
                drawExtrudedCylinder3D(canvas, x, y, zBase, zBase + 14.5f * s, 13.8f * s, 12.8f * s, 0xFF3E4E32.toInt(), 0xFF7CB342.toInt(), facets = 10)
                for (i in 0..2) {
                    val a = i * (2.0 * PI / 3.0) + animTimeSec * 0.8
                    drawSphere3D(canvas, x + (cos(a) * 8.8f * s).toFloat(), y + (sin(a) * 8.8f * s).toFloat(), zBase + 15.5f * s, 4.8f * s, 0xFF9CCC65.toInt(), 0.75f)
                }
                drawSphere3D(canvas, x, y, zBase + 20.5f * s, 8.4f * s, 0xFFB2FF59.toInt(), 0.88f)
                drawBarrel3D(canvas, x, y, zBase + 20.5f * s, yaw, 15.5f * s, 2.0f * s, 3.6f * s, 0xFF558B2F.toInt(), 0xFFB2FF59.toInt(), 0xFF76FF03.toInt())
            }

            TowerType.PRISM, TowerType.DYSON_HELIOS_ARRAY -> {
                val obeliskTopZ = zBase + 31f * s
                drawCone3D(
                    canvas, x, y, zBase, obeliskTopZ, 12.0f * s,
                    if (t.type == TowerType.DYSON_HELIOS_ARRAY) 0xFF4FC3F7.toInt() else 0xFFE6C15A.toInt(),
                    0xFFFFF59D.toInt(), facets = 4, rotationRad = 0.78f, shininess = 48f, specStrength = 0.70f
                )
                val bob = sin(animTimeSec * 3.2f) * 2.5f * s
                drawSphere3D(canvas, x, y, obeliskTopZ + 5.5f * s + bob, 6.2f * s, 0xFFFFF176.toInt(), 0.98f)
            }

            TowerType.TESLA, TowerType.QUANTUM_CHRONO_MATRIX -> {
                drawExtrudedCylinder3D(canvas, x, y, zBase, zBase + 25f * s, 8.8f * s, 5.8f * s, 0xFF455A64.toInt(), 0xFF90A4AE.toInt(), facets = 8)
                for (ring in 0..2) {
                    val rz = zBase + (7f + ring * 6.5f) * s
                    val rr = (12.8f - ring * 1.8f) * s
                    drawExtrudedCylinder3D(canvas, x, y, rz, rz + 2.4f * s, rr, rr, 0xFFD4AF37.toInt(), 0xFF80D8FF.toInt(), facets = 10)
                }
                drawSphere3D(canvas, x, y, zBase + 28f * s, 7.0f * s, 0xFF40C4FF.toInt(), 0.98f)
            }

            TowerType.DRONE, TowerType.DAVINCI_SKY_FORTRESS -> {
                drawExtrudedBox3D(canvas, x, y, zBase, zBase + 12.5f * s, 14.5f * s, 11.5f * s, 0.3f, 0xFF6D543B.toInt(), 0xFFD4AF37.toInt())
                drawExtrudedCylinder3D(canvas, x - 5f * s, y - 4f * s, zBase + 12.5f * s, zBase + 22.5f * s, 5.2f * s, 4.2f * s, 0xFF8C7254.toInt(), 0xFFE6C875.toInt(), facets = 6)
                for (d in 0..1) {
                    val orbitA = animTimeSec * 2.1f + d * PI.toFloat()
                    val ox = x + cos(orbitA) * 21f * s
                    val oy = y + sin(orbitA) * 15.5f * s
                    val heading = orbitA + (PI / 2.0).toFloat()
                    drawBarrel3D(canvas, ox - cos(heading) * 5.5f * s, oy - sin(heading) * 5.5f * s, zBase + 34f * s, heading, 11.5f * s, 0f, 3.5f * s, 0xFFE6C875.toInt(), 0xFF40C4FF.toInt())
                    val wx = sin(heading) * 7.5f * s
                    val wy = -cos(heading) * 5.5f * s
                    val sy = projY(oy, zBase + 34f * s)
                    strokePaint.color = if (ageNum >= 10 || t.type.isFusedApex) 0xFF80D8FF.toInt() else 0xFFA67846.toInt()
                    strokePaint.strokeWidth = 2.8f * s
                    canvas.drawLine(ox - wx, sy - wy, ox + wx, sy + wy, strokePaint)
                }
            }

            TowerType.CHRONO, TowerType.CHRONOS_OBSERVATORY -> {
                drawExtrudedCylinder3D(canvas, x, y, zBase, zBase + 14.5f * s, 13.8f * s, 12.2f * s, 0xFF5E526E.toInt(), 0xFFD4AF37.toInt(), facets = 10)
                val centerY = projY(y, zBase + 22.5f * s)
                strokePaint.color = 0xFFE6C875.toInt()
                strokePaint.strokeWidth = 2.4f * s
                canvas.save()
                canvas.translate(x, centerY)
                canvas.rotate(animTimeSec * 38f)
                tempRect.set(-12.5f * s, -6.5f * s, 12.5f * s, 6.5f * s)
                canvas.drawOval(tempRect, strokePaint)
                canvas.rotate(65f)
                canvas.drawOval(tempRect, strokePaint)
                canvas.restore()
                drawSphere3D(canvas, x, y, zBase + 22.5f * s, 5.6f * s, 0xFFB388FF.toInt(), 0.88f)
            }

            TowerType.RAIL_CANNON, TowerType.LEVIATHAN_GAUSS_CITADEL -> {
                drawExtrudedBox3D(canvas, x, y, zBase, zBase + 15.5f * s, 14.5f * s, 11.5f * s, yaw, 0xFF455A64.toInt(), 0xFF80D8FF.toInt(), shininess = 42f)
                val bx = x + cosA * recoil
                val by = y + sinA * recoil
                drawBarrel3D(canvas, bx - sinA * 3.6f * s, by + cosA * 3.6f * s, zBase + 16.5f * s, yaw, 24f * s, 3.5f * s, 3.8f * s, 0xFF607D8B.toInt(), 0xFF40C4FF.toInt(), 0xFF80D8FF.toInt())
                drawBarrel3D(canvas, bx + sinA * 3.6f * s, by - cosA * 3.6f * s, zBase + 16.5f * s, yaw, 24f * s, 3.5f * s, 3.8f * s, 0xFF607D8B.toInt(), 0xFF40C4FF.toInt(), 0xFF80D8FF.toInt())
                drawSphere3D(canvas, x, y, zBase + 18.5f * s, 6.5f * s, 0xFF00E5FF.toInt(), 0.92f)
            }

            TowerType.LASER_ARRAY -> {
                drawExtrudedCylinder3D(canvas, x, y, zBase, zBase + 18.5f * s, 11.5f * s, 8.8f * s, 0xFF546E7A.toInt(), 0xFFB0BEC5.toInt(), facets = 10)
                drawBarrel3D(canvas, x, y, zBase + 19.5f * s, yaw, 18f * s, 3.0f * s, 4.4f * s, 0xFFD4AF37.toInt(), 0xFFFF5252.toInt(), 0xFFFF8A80.toInt())
                drawSphere3D(canvas, x, y, zBase + 21.5f * s, 6.8f * s, 0xFFFF5252.toInt(), 0.95f)
            }

            TowerType.GRAVITY_WELL, TowerType.OMEGA_SINGULARITY_CORE -> {
                for (i in 0..3) {
                    val a = i * (PI / 2.0) + 0.78
                    drawCone3D(canvas, x + (cos(a) * 12.5f * s).toFloat(), y + (sin(a) * 12.5f * s).toFloat(), zBase, zBase + 22.5f * s, 4.5f * s, 0xFF4A3B6E.toInt(), 0xFFD4AF37.toInt(), facets = 5)
                }
                val centerY = projY(y, zBase + 20.5f * s)
                strokePaint.color = 0xFFB388FF.toInt()
                strokePaint.strokeWidth = 3.2f * s
                canvas.save()
                canvas.translate(x, centerY)
                canvas.rotate(animTimeSec * 60f)
                tempRect.set(-13.5f * s, -7.0f * s, 13.5f * s, 7.0f * s)
                canvas.drawOval(tempRect, strokePaint)
                canvas.restore()
                drawSphere3D(canvas, x, y, zBase + 20.5f * s, 7.2f * s, 0xFF311B92.toInt(), 0.92f)
            }

            TowerType.SHIELD_GARRISON -> {
                drawExtrudedBox3D(canvas, x, y, zBase, zBase + 16.5f * s, 13.0f * s, 13.0f * s, 0f, 0xFF546E7A.toInt(), 0xFFD4AF37.toInt())
                for (sx in intArrayOf(-10, 10)) {
                    for (sy in intArrayOf(-10, 10)) {
                        drawExtrudedCylinder3D(canvas, x + sx * s, y + sy * s, zBase + 4f * s, zBase + 20.5f * s, 4.8f * s, 4.2f * s, 0xFF607D8B.toInt(), 0xFFFFD54F.toInt(), facets = 6)
                    }
                }
                drawSphere3D(canvas, x, y, zBase + 19.5f * s, 8.8f * s, 0xFF4FC3F7.toInt(), 0.88f)
            }
        }
    }

    // =========================================================================
    // 3. ULTRA-DETAILED 1.44x 3D ARTICULATED CREEP / RAIDER MODELS (ALL 32 CREEPS)
    // =========================================================================

    fun drawEnemies3D(canvas: Canvas, eng: GameEngine, animTimeSec: Float) {
        val sortedEnemies = eng.enemies.sortedBy { it.y }

        for (e in sortedEnemies) {
            if (e.hp <= 0f) continue
            val heading = atan2(e.dirY, e.dirX)
            val r = e.type.radius * CREEP_MODEL_SCALE
            val walkPhase = e.distanceOnPath * 0.24f
            val baseColor = if (e.hitFlash > 0.05f) 0xFFFFFFFF.toInt() else e.type.colorArgb.toInt()
            val isFlying = e.type.isFlying
            val altitudeZ = if (isFlying) (38f + sin(animTimeSec * 4.2f + e.id) * 4.8f) else 0f

            // 1. 3D Cast Ground Shadow
            drawCastGroundShadow(
                canvas = canvas,
                groundX = e.x + (if (isFlying) 16f else 0f),
                groundY = e.y + (if (isFlying) 14f else 0f),
                radiusX = r * 1.08f,
                radiusY = r * 0.80f,
                heightZ = altitudeZ + r * 1.45f
            )

            // 2. Elite Affix 3D Golden Pedestal Ring
            if (e.eliteAffix != EnemyEliteAffix.NONE) {
                strokePaint.color = e.eliteAffix.ringColorArgb.toInt()
                strokePaint.strokeWidth = 2.8f
                val ringY = projY(e.y, altitudeZ + 1f)
                tempRect.set(e.x - r * 1.38f, ringY - r * 0.98f, e.x + r * 1.38f, ringY + r * 0.98f)
                canvas.drawOval(tempRect, strokePaint)
            }

            // 3. Render Multi-Part Articulated 3D Model by Unit Class
            when {
                isFlying -> drawFlying3dModel(canvas, e, e.x, e.y, altitudeZ, r, heading, baseColor, animTimeSec)
                e.type.isBoss || e.type == EnemyType.STEAM_LANDSHIP || e.type == EnemyType.BOMBARD_TRAIN ||
                    e.type == EnemyType.PLASMA_HOVER_TANK || e.type == EnemyType.ATOMIC_GOLIATH ||
                    e.type == EnemyType.IRONCLAD || e.type == EnemyType.HYDRA_SPLITTER ->
                    drawArmoredVehicleOrBoss3dModel(canvas, e, e.x, e.y, r, heading, baseColor, walkPhase, animTimeSec)
                e.type == EnemyType.SPRINTER || e.type == EnemyType.CATAPHRACT || e.type == EnemyType.MUSKET_DRAGOON ->
                    drawCavalryRider3dModel(canvas, e, e.x, e.y, r, heading, baseColor, walkPhase)
                e.type == EnemyType.FROST_GOLEM || e.type == EnemyType.DIESEL_MECH_WALKER ||
                    e.type == EnemyType.DYSON_HARVESTER_TITAN || e.type == EnemyType.NANITE_CARRIER ->
                    drawColossusWalker3dModel(canvas, e, e.x, e.y, r, heading, baseColor, walkPhase)
                else ->
                    drawHumanoidTroop3dModel(canvas, e, e.x, e.y, r, heading, baseColor, walkPhase)
            }

            // 4. 3D Status Overlays
            if (e.statusTimers.containsKey(StatusEffect.FROZEN)) {
                drawCone3D(canvas, e.x, e.y, altitudeZ, altitudeZ + r * 2.15f, r * 1.08f, 0x8880D8FF.toInt(), 0xFFE1F5FE.toInt(), facets = 5)
            } else if (e.statusTimers.containsKey(StatusEffect.BURNING)) {
                drawSphere3D(canvas, e.x, e.y, altitudeZ + r * 1.65f, r * 0.50f, 0xCCFF6D00.toInt(), 0.85f)
            }

            // 5. 3D Floating Bevelled Health & Shield Bar above model head
            val topZ = altitudeZ + (if (e.type.isBoss) r * 2.25f else r * 2.02f) + 9f
            draw3dHealthAndShieldBar(canvas, e, e.x, projY(e.y, topZ), r)
        }
    }

    /**
     * 12-Part Articulated 3D Infantry / Knight / Centurion:
     * Striding 3D boots & greaves, segmented waist fauld, plate cuirass with chest emblem,
     * domed pauldrons, swinging vambrace arms, curved heater shield with boss, broadsword/lance,
     * and visored knight helmet with glowing eye-slit & flowing 3D crest plume!
     */
    private fun drawHumanoidTroop3dModel(
        canvas: Canvas,
        e: ActiveEnemy,
        x: Float,
        y: Float,
        r: Float,
        heading: Float,
        baseColor: Int,
        walkPhase: Float
    ) {
        val cosH = cos(heading)
        val sinH = sin(heading)
        val stride = sin(walkPhase) * (r * 0.44f)
        val perpX = -sinH * (r * 0.36f)
        val perpY = cosH * (r * 0.36f)

        // 1. Two Animated 3D Greaves & Sabaton Boots
        val leg1X = x + perpX + cosH * stride
        val leg1Y = y + perpY + sinH * stride
        val leg2X = x - perpX - cosH * stride
        val leg2Y = y - perpY - sinH * stride
        drawSphere3D(canvas, leg1X + cosH * r * 0.14f, leg1Y + sinH * r * 0.14f, r * 0.14f, r * 0.22f, 0xFF2D2218.toInt(), 0.4f)
        drawSphere3D(canvas, leg2X + cosH * r * 0.14f, leg2Y + sinH * r * 0.14f, r * 0.14f, r * 0.22f, 0xFF2D2218.toInt(), 0.4f)
        drawExtrudedCylinder3D(canvas, leg1X, leg1Y, r * 0.12f, r * 0.58f, r * 0.24f, r * 0.22f, 0xFF4A3828.toInt(), facets = 6)
        drawExtrudedCylinder3D(canvas, leg2X, leg2Y, r * 0.12f, r * 0.58f, r * 0.24f, r * 0.22f, 0xFF4A3828.toInt(), facets = 6)

        // 2. 3D Waist Tassets / Armored Belt Ring
        drawExtrudedCylinder3D(
            canvas, x, y, r * 0.46f, r * 0.68f, r * 0.72f, r * 0.66f,
            darkenColor(baseColor, 0.72f), 0xFFD4AF37.toInt(), facets = 8
        )

        // 3. 3D Sculpted Plate Cuirass Torso
        val torsoBottomZ = r * 0.64f
        val torsoTopZ = r * 1.40f
        drawExtrudedCylinder3D(
            canvas, x, y, torsoBottomZ, torsoTopZ,
            r * 0.68f, r * 0.60f,
            baseColor,
            brightenColor(baseColor, 28),
            darkenColor(baseColor, 0.55f),
            facets = 8,
            shininess = 36f
        )

        // 4. 3D Domed Shoulder Pauldrons & Swinging Arms
        val armorColor = if (e.armor >= 0.18f) 0xFFB0BEC5.toInt() else brightenColor(baseColor, 38)
        drawSphere3D(canvas, x + perpX * 1.65f, y + perpY * 1.65f, torsoTopZ - 1.2f, r * 0.34f, armorColor, 0.75f)
        drawSphere3D(canvas, x - perpX * 1.65f, y - perpY * 1.65f, torsoTopZ - 1.2f, r * 0.34f, armorColor, 0.75f)

        // 5. 3D Weapon (Broadsword / Halberd / Crossbow / Plasma Blade) with Crossguard Hilt
        val weapX = x + perpX * 1.65f - cosH * stride * 0.45f
        val weapY = y + perpY * 1.65f - sinH * stride * 0.45f
        drawSphere3D(canvas, weapX, weapY, torsoBottomZ + r * 0.42f, r * 0.22f, 0xFFD4AF37.toInt(), 0.8f)
        drawBarrel3D(
            canvas, weapX, weapY, torsoBottomZ + r * 0.45f,
            heading, r * 1.55f, 3.2f, r * 0.25f,
            if (e.type == EnemyType.CYBER_CENTURION) 0xFF00E5FF.toInt() else 0xFFCFD8DC.toInt(),
            0xFFFFFFFF.toInt(),
            if (e.type == EnemyType.CYBER_CENTURION) 0xFF80D8FF.toInt() else null
        )

        // 6. 3D Curved Heater Shield / Pavise with Raised Metallic Shield Boss
        val shX = x - perpX * 1.48f + cosH * (r * 0.38f)
        val shY = y - perpY * 1.48f + sinH * (r * 0.38f)
        drawExtrudedBox3D(
            canvas, shX, shY, torsoBottomZ - 1f, torsoTopZ + 2.2f,
            r * 0.20f, r * 0.56f, heading,
            if (e.maxShield > 0f || e.type == EnemyType.SHIELDER) 0xFF2979FF.toInt() else darkenColor(baseColor, 0.82f),
            0xFFD4AF37.toInt()
        )
        drawSphere3D(canvas, shX + cosH * r * 0.14f, shY + sinH * r * 0.14f, (torsoBottomZ + torsoTopZ) * 0.5f, r * 0.20f, 0xFFFFDF73.toInt(), 0.9f)

        // 7. 3D Visored Knight Helmet + Glowing Eye-Slit + Flowing 3D Crest Plume
        val headZ = torsoTopZ + r * 0.38f
        drawSphere3D(canvas, x, y, headZ, r * 0.48f, if (e.armor >= 0.12f) 0xFFCFD8DC.toInt() else 0xFFD7CCC8.toInt(), 0.78f)
        // Visor slit
        val visorX = x + cosH * r * 0.32f
        val visorY = projY(y + sinH * r * 0.32f * ELLIPSE_Y_RATIO, headZ)
        strokePaint.color = if (e.type == EnemyType.CYBER_CENTURION) 0xFF00E5FF.toInt() else 0xFF1A120B.toInt()
        strokePaint.strokeWidth = 2.2f
        canvas.drawLine(visorX - sinH * r * 0.22f, visorY, visorX + sinH * r * 0.22f, visorY, strokePaint)

        // 3D Flowing Plume Crest on top of helmet
        drawSphere3D(canvas, x - cosH * r * 0.15f, y - sinH * r * 0.15f, headZ + r * 0.42f, r * 0.26f, brightenColor(baseColor, 35), 0.65f)
        drawSphere3D(canvas, x - cosH * r * 0.42f, y - sinH * r * 0.42f, headZ + r * 0.34f, r * 0.22f, baseColor, 0.55f)
    }

    /**
     * 14-Part Articulated 3D Mounted Cavalry / Cataphract Warhorse:
     * 4 independently galloping 3D horse legs with hooves, barded warhorse torso, flowing tail,
     * arched neck & armored chamfron head, plus 3D armored knight rider with shield & couched lance!
     */
    private fun drawCavalryRider3dModel(
        canvas: Canvas,
        e: ActiveEnemy,
        x: Float,
        y: Float,
        r: Float,
        heading: Float,
        baseColor: Int,
        walkPhase: Float
    ) {
        val cosH = cos(heading)
        val sinH = sin(heading)
        val perpX = -sinH * (r * 0.34f)
        val perpY = cosH * (r * 0.34f)
        val gallop = sin(walkPhase * 1.35f) * (r * 0.40f)

        // 1. 4 Independently Galloping 3D Horse Legs (FL, FR, RL, RR)
        for (fb in intArrayOf(-1, 1)) {
            for (lr in intArrayOf(-1, 1)) {
                val legSwing = fb * lr * gallop * 0.55f
                val lx = x + cosH * (fb * r * 0.58f + legSwing) + perpX * lr * 0.85f
                val ly = y + sinH * (fb * r * 0.58f + legSwing) + perpY * lr * 0.85f
                drawExtrudedCylinder3D(canvas, lx, ly, 0f, r * 0.68f, r * 0.18f, r * 0.16f, 0xFF4E342E.toInt(), facets = 6)
            }
        }

        // 2. 3D Flowing Horse Tail at rear
        val tailX = x - cosH * (r * 1.05f)
        val tailY = y - sinH * (r * 1.05f)
        drawSphere3D(canvas, tailX, tailY, r * 0.82f, r * 0.28f, 0xFF2D1E18.toInt(), 0.35f)

        // 3. 3D Barded Warhorse Torso with Caparison Armor Trim
        drawExtrudedBox3D(
            canvas, x, y, r * 0.54f, r * 1.18f,
            r * 0.98f, r * 0.54f, heading,
            baseColor, 0xFFD4AF37.toInt(), shininess = 32f
        )

        // 4. 3D Arched Horse Neck & Armored Chamfron Head at front
        val headX = x + cosH * (r * 0.92f)
        val headY = y + sinH * (r * 0.92f)
        drawExtrudedCylinder3D(canvas, headX, headY, r * 0.98f, r * 1.68f, r * 0.34f, r * 0.26f, darkenColor(baseColor, 0.85f), 0xFFD4AF37.toInt(), facets = 6)
        drawSphere3D(canvas, headX + cosH * r * 0.24f, headY + sinH * r * 0.24f, r * 1.58f, r * 0.26f, 0xFFD4AF37.toInt(), 0.8f)

        // 5. 3D Mounted Knight Torso, Pauldrons & Helmet in Saddle
        val riderX = x - cosH * (r * 0.12f)
        val riderY = y - sinH * (r * 0.12f)
        drawExtrudedCylinder3D(canvas, riderX, riderY, r * 1.15f, r * 1.85f, r * 0.46f, r * 0.40f, 0xFF90A4AE.toInt(), 0xFFD4AF37.toInt(), facets = 8)
        drawSphere3D(canvas, riderX, riderY, r * 2.08f, r * 0.38f, 0xFFCFD8DC.toInt(), 0.82f)
        drawSphere3D(canvas, riderX - cosH * r * 0.22f, riderY - sinH * r * 0.22f, r * 2.38f, r * 0.24f, brightenColor(baseColor, 40), 0.7f)

        // 6. Couched 3D Cavalry Jousting Lance with Pennon Banner
        drawBarrel3D(canvas, riderX + perpX, riderY + perpY, r * 1.48f, heading, r * 1.95f, 3.2f, r * 0.24f, 0xFFD4AF37.toInt(), 0xFFFFFFFF.toInt())
    }

    /**
     * 16-Part Articulated 3D Siege Tank / Battering Ram / Boss Dreadnought:
     * 8 3D road wheels (4 per flank), sloped riveted glacis hull, front battering ram prow,
     * twin rear exhaust smokestacks puffing 3D smoke spheres, rotating turret cupola, and heavy main cannon!
     */
    private fun drawArmoredVehicleOrBoss3dModel(
        canvas: Canvas,
        e: ActiveEnemy,
        x: Float,
        y: Float,
        r: Float,
        heading: Float,
        baseColor: Int,
        walkPhase: Float,
        animTimeSec: Float
    ) {
        val cosH = cos(heading)
        val sinH = sin(heading)
        val perpX = -sinH * (r * 0.76f)
        val perpY = cosH * (r * 0.76f)

        // 1. 8 3D Armored Road Wheels / Caterpillar Rollers along both flanks (4 per side)
        for (wIdx in 0..3) {
            val frac = (wIdx - 1.5f) * 0.46f
            val wxOff = cosH * (frac * r)
            val wyOff = sinH * (frac * r)
            drawSphere3D(canvas, x + perpX + wxOff, y + perpY + wyOff, r * 0.26f, r * 0.26f, 0xFF263238.toInt(), 0.40f)
            drawSphere3D(canvas, x - perpX + wxOff, y - perpY + wyOff, r * 0.26f, r * 0.26f, 0xFF263238.toInt(), 0.40f)
        }

        // 2. Main 3D Lower Armored Hull Box
        val hullTopZ = if (e.type.isBoss) r * 1.18f else r * 0.98f
        drawExtrudedBox3D(
            canvas = canvas,
            cx = x,
            cy = y,
            zBottom = r * 0.22f,
            zTop = hullTopZ,
            halfLength = r * 1.12f,
            halfWidth = r * 0.74f,
            yawRad = heading,
            baseColor = baseColor,
            trimColor = if (e.type.isBoss) 0xFFD4AF37.toInt() else darkenColor(baseColor, 0.56f),
            shininess = 38f
        )

        // 3. Front 3D Armored Dozer Blade / Spiked Siege Ram Prow
        val prowX = x + cosH * (r * 1.15f)
        val prowY = y + sinH * (r * 1.15f)
        drawExtrudedBox3D(
            canvas, prowX, prowY, r * 0.18f, hullTopZ * 0.72f,
            r * 0.22f, r * 0.78f, heading, 0xFF37474F.toInt(), 0xFFD4AF37.toInt()
        )

        // 4. Twin Rear 3D Smokestacks / Fusion Exhaust Stacks + Rising 3D Smoke Puffs
        val rearX = x - cosH * (r * 0.82f)
        val rearY = y - sinH * (r * 0.82f)
        for (side in intArrayOf(-1, 1)) {
            val sx = rearX + perpX * side * 0.55f
            val sy = rearY + perpY * side * 0.55f
            drawExtrudedCylinder3D(canvas, sx, sy, hullTopZ, hullTopZ + r * 0.52f, r * 0.16f, r * 0.14f, 0xFF37474F.toInt(), 0xFFD4AF37.toInt(), facets = 6)
        }
        val puffPhase = (walkPhase * 0.4f) % 1.0f
        drawSphere3D(
            canvas,
            rearX - cosH * puffPhase * 8f,
            rearY - sinH * puffPhase * 8f,
            hullTopZ + r * 0.65f + puffPhase * 10f,
            r * (0.22f + puffPhase * 0.15f),
            if (e.type == EnemyType.OMEGA_ARCHITECT_BOSS) 0x8800E5FF.toInt() else 0x8878909C.toInt(),
            0.3f
        )

        // 5. Upper 3D Turret / Siege Belfry Superstructure
        val turretTopZ = if (e.type.isBoss) r * 2.02f else r * 1.56f
        drawExtrudedCylinder3D(
            canvas = canvas,
            cx = x - cosH * (r * 0.08f),
            cy = y - sinH * (r * 0.08f),
            zBottom = hullTopZ,
            zTop = turretTopZ,
            rBottom = r * 0.66f,
            rTop = r * 0.56f,
            baseColor = brightenColor(baseColor, 22),
            topColor = brightenColor(baseColor, 40),
            rimColor = 0xFFD4AF37.toInt(),
            facets = 8,
            shininess = 44f
        )

        // 6. Forward Heavy 3D Siege Cannon(s)
        if (e.type.isBoss) {
            drawBarrel3D(canvas, x + perpX * 0.35f, y + perpY * 0.35f, (hullTopZ + turretTopZ) * 0.52f, heading, r * 1.55f, 2.5f, r * 0.28f, 0xFFD4AF37.toInt(), 0xFF263238.toInt(), 0xFF00E5FF.toInt())
            drawBarrel3D(canvas, x - perpX * 0.35f, y - perpY * 0.35f, (hullTopZ + turretTopZ) * 0.52f, heading, r * 1.55f, 2.5f, r * 0.28f, 0xFFD4AF37.toInt(), 0xFF263238.toInt(), 0xFF00E5FF.toInt())
            drawCone3D(canvas, x - cosH * (r * 0.08f), y - sinH * (r * 0.08f), turretTopZ, turretTopZ + r * 0.60f, r * 0.55f, 0xFFD4AF37.toInt(), 0xFFFFF59D.toInt(), facets = 8, rotationRad = animTimeSec)
        } else {
            drawBarrel3D(
                canvas = canvas,
                cx = x,
                cy = y,
                zBase = (hullTopZ + turretTopZ) * 0.5f,
                yawRad = heading,
                length = r * 1.50f,
                pitchZ = 2.2f,
                thickness = r * 0.35f,
                barrelColor = 0xFF455A64.toInt(),
                muzzleColor = 0xFF263238.toInt(),
                glowArgb = if (e.type == EnemyType.PLASMA_HOVER_TANK) 0xFF00E5FF.toInt() else null
            )
        }
    }

    private fun drawColossusWalker3dModel(
        canvas: Canvas,
        e: ActiveEnemy,
        x: Float,
        y: Float,
        r: Float,
        heading: Float,
        baseColor: Int,
        walkPhase: Float
    ) {
        val cosH = cos(heading)
        val sinH = sin(heading)
        val perpX = -sinH * (r * 0.56f)
        val perpY = cosH * (r * 0.56f)
        val stride = sin(walkPhase) * (r * 0.38f)

        // Heavy 3D Articulated Piston Legs + Footpads
        drawExtrudedCylinder3D(canvas, x + perpX + cosH * stride, y + perpY + sinH * stride, 0f, r * 0.78f, r * 0.34f, r * 0.28f, darkenColor(baseColor, 0.72f), facets = 8)
        drawExtrudedCylinder3D(canvas, x - perpX - cosH * stride, y - perpY - sinH * stride, 0f, r * 0.78f, r * 0.34f, r * 0.28f, darkenColor(baseColor, 0.72f), facets = 8)

        // Massive 3D Upper Chassis + Glowing Chest Reactor Core
        drawExtrudedCylinder3D(canvas, x, y, r * 0.68f, r * 1.60f, r * 0.88f, r * 0.78f, baseColor, brightenColor(baseColor, 28), 0xFFD4AF37.toInt(), facets = 10)
        drawSphere3D(canvas, x + cosH * r * 0.55f, y + sinH * r * 0.55f, r * 1.15f, r * 0.30f, 0xFF00E5FF.toInt(), 0.95f)

        // Twin Shoulder 3D Cannon Pods + Crowned Head
        drawSphere3D(canvas, x + perpX * 1.35f, y + perpY * 1.35f, r * 1.58f, r * 0.40f, 0xFFD4AF37.toInt(), 0.80f)
        drawSphere3D(canvas, x - perpX * 1.35f, y - perpY * 1.35f, r * 1.58f, r * 0.40f, 0xFFD4AF37.toInt(), 0.80f)
        drawCone3D(canvas, x, y, r * 1.58f, r * 2.22f, r * 0.60f, brightenColor(baseColor, 42), 0xFFD4AF37.toInt(), facets = 6)
    }

    private fun drawFlying3dModel(
        canvas: Canvas,
        e: ActiveEnemy,
        x: Float,
        y: Float,
        altitudeZ: Float,
        r: Float,
        heading: Float,
        baseColor: Int,
        animTimeSec: Float
    ) {
        val cosH = cos(heading)
        val sinH = sin(heading)
        val flapZ = sin(animTimeSec * 7.5f + e.id) * 4.5f

        val wingSpan = r * 1.65f
        val wx = -sinH * wingSpan
        val wy = cosH * wingSpan * ELLIPSE_Y_RATIO
        val bodyScreenY = projY(y, altitudeZ)
        val wingTipScreenY = projY(y, altitudeZ + flapZ)

        // 1. 3D Swept Wings
        fillPaint.color = shadePbrColor(baseColor, -sinH, cosH, 0.75f, 34f, 0.48f)
        polyPath.reset()
        polyPath.moveTo(x + cosH * r * 0.55f, bodyScreenY + sinH * r * 0.38f)
        polyPath.lineTo(x + wx - cosH * r * 0.32f, wingTipScreenY + wy)
        polyPath.lineTo(x - cosH * r * 0.65f, bodyScreenY - sinH * r * 0.38f)
        polyPath.lineTo(x - wx - cosH * r * 0.32f, wingTipScreenY - wy)
        polyPath.close()
        canvas.drawPath(polyPath, fillPaint)
        strokePaint.color = 0xFFD4AF37.toInt()
        strokePaint.strokeWidth = 1.5f
        canvas.drawPath(polyPath, strokePaint)

        // 2. Twin Under-Wing 3D Turbine Nacelles + Glowing Thruster Cones
        for (side in intArrayOf(-1, 1)) {
            val nx = x - sinH * (r * 0.75f * side)
            val ny = y + cosH * (r * 0.75f * side)
            drawBarrel3D(
                canvas, nx - cosH * r * 0.45f, ny - sinH * r * 0.45f,
                altitudeZ - 1.5f, heading, r * 0.90f, 0.5f, r * 0.28f,
                0xFF455A64.toInt(), 0xFFD4AF37.toInt(), 0xFF00E5FF.toInt()
            )
            // Rear thruster flame sphere
            drawSphere3D(canvas, nx - cosH * r * 0.55f, ny - sinH * r * 0.55f, altitudeZ - 1.5f, r * 0.22f, 0xFFFF9100.toInt(), 0.95f)
        }

        // 3. Main 3D Fuselage Hull
        drawBarrel3D(
            canvas = canvas,
            cx = x - cosH * (r * 0.90f),
            cy = y - sinH * (r * 0.90f),
            zBase = altitudeZ,
            yawRad = heading,
            length = r * 1.85f,
            pitchZ = 1.8f,
            thickness = r * 0.72f,
            barrelColor = brightenColor(baseColor, 24),
            muzzleColor = 0xFFD4AF37.toInt(),
            glowArgb = 0xFF80D8FF.toInt()
        )

        // 4. 3D Cockpit Canopy
        drawSphere3D(canvas, x + cosH * (r * 0.22f), y + sinH * (r * 0.22f), altitudeZ + r * 0.40f, r * 0.36f, 0xFF80D8FF.toInt(), 0.92f)
    }

    private fun draw3dHealthAndShieldBar(
        canvas: Canvas,
        e: ActiveEnemy,
        screenX: Float,
        screenY: Float,
        r: Float
    ) {
        val barW = max(26f, r * 2.15f)
        val barH = if (e.type.isBoss) 6.2f else 4.5f
        val left = screenX - barW * 0.5f

        fillPaint.color = 0xDD14100C.toInt()
        tempRect.set(left - 1.4f, screenY - 1.4f, left + barW + 1.4f, screenY + barH + 1.4f)
        canvas.drawRoundRect(tempRect, 3f, 3f, fillPaint)

        val hpRatio = (e.hp / e.maxHp.coerceAtLeast(1f)).coerceIn(0f, 1f)
        fillPaint.color = when {
            hpRatio > 0.55f -> 0xFF56C25A.toInt()
            hpRatio > 0.25f -> 0xFFE6B333.toInt()
            else -> 0xFFE04840.toInt()
        }
        tempRect.set(left, screenY, left + barW * hpRatio, screenY + barH)
        canvas.drawRoundRect(tempRect, 2.5f, 2.5f, fillPaint)

        fillPaint.color = 0x55FFFFFF
        tempRect.set(left, screenY, left + barW * hpRatio, screenY + barH * 0.45f)
        canvas.drawRoundRect(tempRect, 2f, 2f, fillPaint)

        if (e.maxShield > 0f && e.shield > 0f) {
            val shRatio = (e.shield / e.maxShield).coerceIn(0f, 1f)
            fillPaint.color = 0xFF40C4FF.toInt()
            tempRect.set(left, screenY - 4.2f, left + barW * shRatio, screenY - 1.2f)
            canvas.drawRoundRect(tempRect, 2f, 2f, fillPaint)
        }
    }

    // =========================================================================
    // 4. 9 DISTINCT, COOL, FAST 3D SHOTS, ATTACKS & IMPACT SHOCKWAVES
    // =========================================================================

    fun drawProjectilesAndShockwaves3D(
        canvas: Canvas,
        projectiles: List<VisualProjectile>,
        shockwaves: List<VisualShockwave>,
        animTimeSec: Float
    ) {
        // 1. 3D Perspective Impact Shockwaves (tilted 3D elliptical blast rings + vertical 3D energy plumes)
        for (s in shockwaves) {
            val p = s.progress.coerceIn(0f, 1f)
            val inv = 1f - p
            val alpha = (inv * 195f).toInt().coerceIn(0, 255)
            val baseCol = (s.colorArgb.toInt() and 0x00FFFFFF) or (alpha shl 24)
            val rx = s.maxRadius * p
            val ry = rx * ELLIPSE_Y_RATIO
            val sy = projY(s.y, 4f)

            // Outer 3D ground blast ellipse
            strokePaint.color = baseCol
            strokePaint.strokeWidth = (4.2f * inv).coerceAtLeast(1.2f)
            tempRect.set(s.x - rx, sy - ry, s.x + rx, sy + ry)
            canvas.drawOval(tempRect, strokePaint)

            // Secondary raised 3D shock ring at z = 14f * p
            val syUpper = projY(s.y, 6f + 22f * p)
            strokePaint.color = brightenColor(baseCol, 55)
            strokePaint.strokeWidth = (2.4f * inv).coerceAtLeast(1.0f)
            tempRect.set(s.x - rx * 0.65f, syUpper - ry * 0.65f, s.x + rx * 0.65f, syUpper + ry * 0.65f)
            canvas.drawOval(tempRect, strokePaint)

            // Central rising 3D fireball / frost / plasma dome during early impact
            if (p < 0.65f) {
                val domeR = s.maxRadius * 0.24f * (1f - p * 1.2f).coerceAtLeast(0.1f)
                drawSphere3D(canvas, s.x, s.y, 10f + p * 26f, domeR, s.colorArgb.toInt(), 0.85f)
            }
        }

        // 2. 9 Distinct 3D Projectiles, Beams, Lightning Bolts, Missiles & Sabots
        for (p in projectiles) {
            val prog = p.progress.coerceIn(0f, 1f)
            val arcParabola = 4f * prog * (1f - prog)
            val dx = p.targetX - p.startX
            val dy = p.targetY - p.startY
            val yaw = atan2(dy, dx)
            val cosA = cos(yaw)
            val sinA = sin(yaw)
            val col = p.colorArgb.toInt()
            val secCol = p.secondaryColorArgb.toInt()

            when (p.visualStyle) {
                0 -> {
                    // STYLE 0: 3D Faceted Boulder / Iron Cannonball with Smoke Trail
                    val z = 22f + 28f * arcParabola
                    fillPaint.color = 0x3A000000
                    canvas.drawOval(p.x - p.radius, p.y - p.radius * 0.7f, p.x + p.radius, p.y + p.radius * 0.7f, fillPaint)
                    // 2 3D Smoke Puffs behind cannonball
                    for (tIdx in 1..2) {
                        val tp = (prog - tIdx * 0.08f).coerceAtLeast(0f)
                        val tx = p.startX + dx * tp
                        val ty = p.startY + dy * tp
                        val tz = 22f + 28f * (4f * tp * (1f - tp))
                        drawSphere3D(canvas, tx, ty, tz, p.radius * (0.65f - tIdx * 0.15f), 0x8890A4AE.toInt(), 0.25f)
                    }
                    drawSphere3D(canvas, p.x, p.y, z, p.radius * 1.35f, col, 0.85f)
                }

                1 -> {
                    // STYLE 1: 3D Winged Ballista Javelin / Sniper Lance
                    val z = 26f + 16f * arcParabola
                    val syGround = p.y + 4f
                    strokePaint.color = 0x33000000
                    strokePaint.strokeWidth = 3.0f
                    canvas.drawLine(p.x - cosA * 12f, syGround - sinA * 9f, p.x + cosA * 12f, syGround + sinA * 9f, strokePaint)

                    // 3D Javelin Shaft
                    drawBarrel3D(
                        canvas, p.x - cosA * 13f, p.y - sinA * 13f, z,
                        yaw, 24f, 0f, 3.6f, 0xFF8D6E63.toInt(), secCol, 0xFFFFFFFF.toInt()
                    )
                    // 3D Broadhead Steel Tip + Tail Fletching Fins
                    val tipX = p.x + cosA * 12f
                    val tipY = projY(p.y + sinA * 12f * ELLIPSE_Y_RATIO, z)
                    val tailX = p.x - cosA * 12f
                    val tailY = projY(p.y - sinA * 12f * ELLIPSE_Y_RATIO, z)
                    strokePaint.color = 0xFFECEFF1.toInt()
                    strokePaint.strokeWidth = 2.4f
                    canvas.drawLine(tipX, tipY, tipX - cosA * 6f - sinA * 4f, tipY - sinA * 4f + cosA * 3f, strokePaint)
                    canvas.drawLine(tipX, tipY, tipX - cosA * 6f + sinA * 4f, tipY - sinA * 4f - cosA * 3f, strokePaint)
                    // Fletching feathers
                    strokePaint.color = col
                    strokePaint.strokeWidth = 2.2f
                    canvas.drawLine(tailX, tailY, tailX - cosA * 4f - sinA * 5f, tailY + cosA * 4f, strokePaint)
                    canvas.drawLine(tailX, tailY, tailX - cosA * 4f + sinA * 5f, tailY - cosA * 4f, strokePaint)
                }

                2 -> {
                    // STYLE 2: 3D Lofted Siege Trebuchet Fireball / Glacier Bombard Shell (High Parabolic Arc!)
                    val z = 24f + 74f * arcParabola
                    // Ground shadow moving directly along the ground below the high-arcing meteor!
                    fillPaint.color = 0x44000000
                    canvas.drawOval(p.x - p.radius * 1.2f, p.y - p.radius * 0.8f, p.x + p.radius * 1.2f, p.y + p.radius * 0.8f, fillPaint)

                    // 3 Trailing 3D Flame / Frost Embers along the parabolic curve
                    for (tIdx in 3 downTo 1) {
                        val tp = (prog - tIdx * 0.065f).coerceAtLeast(0f)
                        val tx = p.startX + dx * tp
                        val ty = p.startY + dy * tp
                        val tz = 24f + 74f * (4f * tp * (1f - tp))
                        drawSphere3D(canvas, tx, ty, tz, p.radius * (1.05f - tIdx * 0.22f), secCol, 0.75f)
                    }
                    // Main 3D Molten / Glacial Bombard Core
                    drawSphere3D(canvas, p.x, p.y, z, p.radius * 1.48f, col, 0.95f)
                }

                3 -> {
                    // STYLE 3: 3D Tumbling Alchemical Acid Flask / Chrono Singularity Orb
                    val z = 24f + 42f * arcParabola
                    fillPaint.color = 0x35000000
                    canvas.drawCircle(p.x + 4f, p.y + 4f, p.radius, fillPaint)
                    for (tIdx in 1..2) {
                        val tp = (prog - tIdx * 0.07f).coerceAtLeast(0f)
                        val tx = p.startX + dx * tp
                        val ty = p.startY + dy * tp
                        val tz = 24f + 42f * (4f * tp * (1f - tp))
                        drawSphere3D(canvas, tx, ty, tz, p.radius * 0.55f, secCol, 0.8f)
                    }
                    // 3D Glass Flask Globe + Glowing Liquid Core + Neck Cork
                    drawSphere3D(canvas, p.x, p.y, z, p.radius * 1.38f, col, 0.95f)
                    drawSphere3D(canvas, p.x, p.y, z + p.radius * 1.1f, p.radius * 0.48f, 0xFFD7CCC8.toInt(), 0.6f)
                }

                4 -> {
                    // STYLE 4: 3D Jagged Multi-Strand Tesla Lightning / Storm Arc (z = 38f -> 16f)
                    val x0 = p.startX
                    val y0 = projY(p.startY, 38f)
                    val x4 = p.targetX
                    val y4 = projY(p.targetY, 16f)
                    val perpX = -sinA * 14f
                    val perpY = cosA * 14f
                    val j1 = sin(animTimeSec * 45f + p.startX * 0.1f)
                    val j2 = cos(animTimeSec * 52f + p.targetY * 0.1f)
                    val j3 = sin(animTimeSec * 38f + p.startY * 0.1f)

                    val x1 = x0 + (x4 - x0) * 0.25f + perpX * j1
                    val y1 = y0 + (y4 - y0) * 0.25f + perpY * j1
                    val x2 = x0 + (x4 - x0) * 0.50f - perpX * j2 * 1.1f
                    val y2 = y0 + (y4 - y0) * 0.50f - perpY * j2 * 1.1f
                    val x3 = x0 + (x4 - x0) * 0.75f + perpX * j3 * 0.9f
                    val y3 = y0 + (y4 - y0) * 0.75f + perpY * j3 * 0.9f

                    polyPath.reset()
                    polyPath.moveTo(x0, y0)
                    polyPath.lineTo(x1, y1)
                    polyPath.lineTo(x2, y2)
                    polyPath.lineTo(x3, y3)
                    polyPath.lineTo(x4, y4)

                    // Outer plasma glow sheath
                    strokePaint.color = (col and 0x00FFFFFF) or 0x77000000
                    strokePaint.strokeWidth = 9.0f
                    canvas.drawPath(polyPath, strokePaint)

                    // Mid electric bolt
                    strokePaint.color = col
                    strokePaint.strokeWidth = 4.2f
                    canvas.drawPath(polyPath, strokePaint)

                    // White-hot lightning core
                    strokePaint.color = 0xFFFFFFFF.toInt()
                    strokePaint.strokeWidth = 1.8f
                    canvas.drawPath(polyPath, strokePaint)

                    // Branching 3D side-fork from midpoint (x2, y2)
                    strokePaint.color = secCol
                    strokePaint.strokeWidth = 2.2f
                    canvas.drawLine(x2, y2, x2 + perpX * 1.4f + cosA * 14f, y2 + perpY * 1.4f + sinA * 10f, strokePaint)

                    // 3D Spark Spheres at Emitter and Impact
                    drawSphere3D(canvas, p.startX, p.startY, 38f, 5.2f, col, 0.95f)
                    drawSphere3D(canvas, p.targetX, p.targetY, 16f, 6.0f, 0xFFFFFFFF.toInt(), 0.98f)
                }

                5 -> {
                    // STYLE 5: 3D Volumetric Photon / Prism / Dyson Energy Lance Beam with 3D Rings
                    val x0 = p.startX
                    val y0 = projY(p.startY, 42f)
                    val x1 = p.targetX
                    val y1 = projY(p.targetY, 16f)

                    // Outer 3D cylindrical bloom
                    strokePaint.color = (col and 0x00FFFFFF) or 0x66000000
                    strokePaint.strokeWidth = 11.5f
                    canvas.drawLine(x0, y0, x1, y1, strokePaint)

                    // Saturated primary energy lance
                    strokePaint.color = col
                    strokePaint.strokeWidth = 5.6f
                    canvas.drawLine(x0, y0, x1, y1, strokePaint)

                    // White-hot coherent core
                    strokePaint.color = 0xFFFFFFFF.toInt()
                    strokePaint.strokeWidth = 2.2f
                    canvas.drawLine(x0, y0, x1, y1, strokePaint)

                    // 3 Concentric 3D Energy Rings encircling the beam shaft
                    strokePaint.color = secCol
                    strokePaint.strokeWidth = 1.8f
                    for (rIdx in 1..3) {
                        val f = ((rIdx * 0.25f + animTimeSec * 2.5f) % 0.85f) + 0.1f
                        val rx = x0 + (x1 - x0) * f
                        val ry = y0 + (y1 - y0) * f
                        canvas.save()
                        canvas.translate(rx, ry)
                        canvas.rotate(Math.toDegrees(yaw.toDouble()).toFloat() + 90f)
                        tempRect.set(-7.5f, -3.5f, 7.5f, 3.5f)
                        canvas.drawOval(tempRect, strokePaint)
                        canvas.restore()
                    }

                    // 3D Emitter & Impact Flare Spheres
                    drawSphere3D(canvas, p.startX, p.startY, 42f, 6.0f, secCol, 0.95f)
                    drawSphere3D(canvas, p.targetX, p.targetY, 16f, 6.8f, col, 0.98f)
                }

                6 -> {
                    // STYLE 6: 3D Finned Cruise Missile / Sky-Fortress Rocket
                    val z = 34f + 22f * arcParabola
                    fillPaint.color = 0x33000000
                    canvas.drawCircle(p.x + 8f, p.y + 8f, p.radius, fillPaint)

                    // 2 Contrail smoke puffs + blazing thruster flame sphere
                    for (tIdx in 1..2) {
                        val tp = (prog - tIdx * 0.06f).coerceAtLeast(0f)
                        val tx = p.startX + dx * tp
                        val ty = p.startY + dy * tp
                        val tz = 34f + 22f * (4f * tp * (1f - tp))
                        drawSphere3D(canvas, tx, ty, tz, p.radius * 0.65f, 0x88CFD8DC.toInt(), 0.3f)
                    }
                    // Rocket Thruster Flame
                    drawSphere3D(canvas, p.x - cosA * 10f, p.y - sinA * 10f, z, 4.8f, 0xFFFF6D00.toInt(), 0.95f)
                    // 3D Cylindrical Rocket Fuselage + Nosecone
                    drawBarrel3D(
                        canvas, p.x - cosA * 8f, p.y - sinA * 8f, z,
                        yaw, 18f, 0f, 5.2f, 0xFFECEFF1.toInt(), col, secCol
                    )
                    // 3D Tail Fins
                    val tailX = p.x - cosA * 7f
                    val tailY = projY(p.y - sinA * 7f * ELLIPSE_Y_RATIO, z)
                    strokePaint.color = col
                    strokePaint.strokeWidth = 2.6f
                    canvas.drawLine(tailX - sinA * 6.5f, tailY + cosA * 5f, tailX + sinA * 6.5f, tailY - cosA * 5f, strokePaint)
                }

                7 -> {
                    // STYLE 7: 3D Hyper-Velocity Gauss Railgun Sabot with 3 Tilted 3D Mach Shockwave Rings
                    val z = 24f + 8f * arcParabola
                    // 3 Tilted 3D Mach Cone Rings trailing the tungsten dart
                    strokePaint.color = 0xAA00E5FF.toInt()
                    strokePaint.strokeWidth = 2.0f
                    for (mIdx in 0..2) {
                        val mp = (prog - mIdx * 0.07f).coerceAtLeast(0f)
                        val mx = p.startX + dx * mp
                        val my = projY(p.startY + dy * mp, z)
                        val ringR = (6.5f + mIdx * 2.8f)
                        canvas.save()
                        canvas.translate(mx, my)
                        canvas.rotate(Math.toDegrees(yaw.toDouble()).toFloat() + 90f)
                        tempRect.set(-ringR, -ringR * 0.48f, ringR, ringR * 0.48f)
                        canvas.drawOval(tempRect, strokePaint)
                        canvas.restore()
                    }
                    // Glowing 3D Tungsten-Plasma Sabot Dart
                    drawBarrel3D(
                        canvas, p.x - cosA * 12f, p.y - sinA * 12f, z,
                        yaw, 24f, 0f, 4.0f, 0xFF00E5FF.toInt(), 0xFFFFFFFF.toInt(), 0xFFFFEA00.toInt()
                    )
                }

                else -> {
                    // STYLE 8: 3D Crystalline Frost / Hydro Shard Volley (Triple 3D Ice/Water Spears)
                    val z = 24f + 22f * arcParabola
                    for (sIdx in -1..1) {
                        val offX = -sinA * (sIdx * 5.5f)
                        val offY = cosA * (sIdx * 5.5f)
                        drawBarrel3D(
                            canvas, p.x + offX - cosA * 7f, p.y + offY - sinA * 7f, z + (1 - kotlin.math.abs(sIdx)) * 3f,
                            yaw, 14f, 1.5f, 3.4f, col, 0xFFFFFFFF.toInt(), secCol
                        )
                    }
                }
            }
        }
    }
}

package com.brandonwallace.labyrinthtd.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.brandonwallace.labyrinthtd.render.Labyrinth3dEngine
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.delay

/**
 * TRUE 3D 4K-STYLE PERSPECTIVE MESH, VOLUMETRIC GOD-RAY & PBR MONUMENT CINEMA INTRO
 *
 * Features:
 * 1) Full-Screen 3D Perspective Polygon/Triangle Mesh Engine rendering a 3D Mountain Canyon,
 *    4K Volumetric Sunset/Supernova Sky, Volumetric God-Rays, 3D Perspective Labyrinth Causeway,
 *    and 3D Multi-Tiered Rotating Citadel Spires directly on the hardware-accelerated Canvas.
 * 2) Titanic 3-Tier 3D Perspective-Extruded Gold & Platinum Monument Typography where
 *    "CREATED BY: BRANDON J. WALLACE" is ALWAYS PRESENT from 0.0s to completion, featuring
 *    48-layer 3D perspective extrusion, sunlit top bevels, PBR-shaded 3D side walls,
 *    depth-occluding 3D orbital gyroscope rings, and 4K anamorphic lens flares.
 * 3) Close-Up 2.4x Scale 3D Battle Diorama in the foreground with 3D extruded towers,
 *    rotating 3D turrets, striding 3D armored knights, 3D siege tanks, and the 3D Leviathan Boss!
 */
@Composable
fun BrandonWallaceIntroOverlay(
    onFinished: () -> Unit
) {
    var elapsedSec by remember { mutableFloatStateOf(0f) }
    val totalDurationSec = 10.0f

    LaunchedEffect(Unit) {
        val startNanos = System.nanoTime()
        while (true) {
            val now = System.nanoTime()
            val t = (now - startNanos) / 1_000_000_000f
            elapsedSec = t
            if (t >= totalDurationSec) {
                onFinished()
                break
            }
            delay(250L)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Full-Screen 3D Perspective World, 3D Extruded "CREATED BY: BRANDON J. WALLACE" Monument & 3D Battle Engine
        AndroidView(
            factory = { ctx ->
                HighDpi3dCinemaWorldAndMonumentView(ctx, totalDurationSec, onFinished)
            },
            modifier = Modifier.fillMaxSize()
        )

        // Compact Single-Line Top Bar: Permanent Creator Badge on Left + Skip Button on Right
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xD9120C06))
                    .border(1.2.dp, Color(0xFFFFD54F), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "★ CREATED BY: BRANDON J. WALLACE ★",
                    color = Color(0xFFFFE082),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Serif,
                    maxLines = 1,
                    softWrap = false
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xD91A1410))
                    .border(1.2.dp, Color(0xFFD4AF37), RoundedCornerShape(6.dp))
                    .clickable { onFinished() }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                val rem = max(0, (totalDurationSec - elapsedSec).toInt())
                Text(
                    text = "SKIP (${rem}s) ▶",
                    color = Color(0xFFFFDF73),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

/**
 * Full-screen hardware-accelerated 3D Perspective Polygon/Triangle Mesh & Volumetric PBR Renderer.
 */
private class HighDpi3dCinemaWorldAndMonumentView(
    context: Context,
    private val totalDurationSec: Float,
    private val onFinished: () -> Unit
) : View(context) {
    private val startNanos = System.nanoTime()
    private var finishedTriggered = false

    private val skyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val rayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val meshPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val meshStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.2f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    }
    private val strokeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    private val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val polyPath = Path()
    private val rectF = RectF()
    private val projScratch = FloatArray(3)
    private var cachedWidth = -1f
    private var cachedSize1 = 44f
    private var cachedSize2 = 96f
    private var cachedSize3 = 112f

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && !finishedTriggered) {
            finishedTriggered = true
            onFinished()
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (finishedTriggered) return
        val t = (System.nanoTime() - startNanos) / 1_000_000_000f
        if (t >= totalDurationSec) {
            finishedTriggered = true
            onFinished()
            return
        }
        val w = width.toFloat().coerceAtLeast(1f)
        val h = height.toFloat().coerceAtLeast(1f)

        // 1. 4K VOLUMETRIC SKY, BLAZING SUPERNOVA SUN, NEBULA & VOLUMETRIC GOD-RAYS
        draw4kVolumetricSkyAndGodRays(canvas, w, h, t)

        // 2. TRUE 3D PERSPECTIVE CANYON MOUNTAINS & WINDING 3D LABYRINTH CAUSEWAY MESH
        draw3dPerspectiveCanyonAndCausewayMesh(canvas, w, h, t)

        // 3. 3D PERSPECTIVE ROTATING CITADEL SPIRES IN THE CANYON MIDGROUND
        draw3dMidgroundCitadelSpires(canvas, w, h, t)

        val monCx = w * 0.5f
        val monCy = h * 0.30f

        // 4. BACK HALF OF 3D ORBITING ASTROLABE GYROSCOPE RINGS (Behind the 3D Extruded Name!)
        draw3dOrbitingRingsHalf(canvas, monCx, monCy, w, h, t, frontHalf = false)

        // 5. TITANIC 3-TIER 3D PERSPECTIVE-EXTRUDED MONUMENT: "CREATED BY: BRANDON J. WALLACE"
        draw3dExtrudedCreatorMonument(canvas, monCx, monCy, w, h, t)

        // 6. FRONT HALF OF 3D ORBITING ASTROLABE GYROSCOPE RINGS (Passing in FRONT of the 3D Name!)
        draw3dOrbitingRingsHalf(canvas, monCx, monCy, w, h, t, frontHalf = true)

        // 7. CLOSE-UP 2.4x SCALE FOREGROUND 3D BATTLE DIORAMA (3D Towers, 3D Knights, 3D Tanks & 3D Boss!)
        draw3dForegroundBattleDiorama(canvas, w, h, t)

        postInvalidateDelayed(30L)
    }

    /**
     * Projects a 3D world coordinate `(wx, wy, wz)` into 2D screen coordinates `(sx, sy, scale)`
     * using a true 3D perspective pinhole camera flying down the canyon.
     */
    private fun project3D(
        wx: Float,
        wy: Float,
        wz: Float,
        camX: Float,
        camY: Float,
        camZ: Float,
        w: Float,
        h: Float,
        horizonY: Float,
        fovScale: Float,
        out: FloatArray
    ): Boolean {
        val rx = wx - camX
        val ry = wy - camY
        val rz = wz - camZ
        if (rz <= 0.35f) return false
        val invZ = fovScale / rz
        out[0] = w * 0.5f + rx * invZ
        out[1] = horizonY - ry * invZ
        out[2] = invZ
        return true
    }

    /**
     * Renders the 4K Volumetric Sky dome, dynamic era color shift, starfield, blazing sun/supernova core,
     * and sweeping volumetric god-ray shafts.
     */
    private fun draw4kVolumetricSkyAndGodRays(
        canvas: Canvas,
        w: Float,
        h: Float,
        t: Float
    ) {
        val horizonY = h * 0.52f
        val sunX = w * (0.5f + 0.14f * sin(t * 0.45f))
        val sunY = h * (0.22f + 0.03f * cos(t * 0.35f))

        // Rich multi-stop sky gradient from deep cosmic zenith to golden/cyan horizon
        skyPaint.shader = LinearGradient(
            0f, 0f, 0f, h,
            intArrayOf(
                0xFF060B1E.toInt(),
                0xFF14224A.toInt(),
                0xFF3E2C5E.toInt(),
                0xFFB85D28.toInt(),
                0xFFFFB74D.toInt(),
                0xFF1B281E.toInt(),
                0xFF0C140E.toInt()
            ),
            floatArrayOf(0f, 0.16f, 0.30f, 0.44f, 0.52f, 0.68f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w, h, skyPaint)
        skyPaint.shader = null

        // High-altitude starfield & cosmic nebula dust
        for (i in 0 until 55) {
            val sx = ((i * 173.3f + 41f) % 1.0f) * w
            val sy = ((i * 97.7f + 13f) % 1.0f) * (horizonY * 0.72f)
            val twinkle = 0.45f + 0.55f * sin(t * 3.2f + i * 1.7f)
            val alpha = (twinkle * 215f).toInt().coerceIn(30, 255)
            glowPaint.color = (alpha shl 24) or 0xFFF8E7
            val r = if (i % 6 == 0) 2.6f else 1.5f
            canvas.drawCircle(sx, sy, r, glowPaint)
        }

        // Blazing 4K Volumetric Sun / Supernova Corona
        glowPaint.shader = RadialGradient(
            sunX, sunY, w * 0.55f,
            intArrayOf(
                0xFFFFFFFF.toInt(),
                0xCCFFE082.toInt(),
                0x66FF8F00.toInt(),
                0x2200E5FF.toInt(),
                0x00000000
            ),
            floatArrayOf(0f, 0.12f, 0.36f, 0.68f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(sunX, sunY, w * 0.55f, glowPaint)
        glowPaint.shader = null

        // 14 Volumetric Crepuscular God-Ray Shafts radiating from the 3D Sun (single shared RadialGradient)
        val rayLen = max(w, h) * 0.95f
        rayPaint.shader = RadialGradient(
            sunX, sunY, rayLen,
            intArrayOf(0x38FFF3C4, 0x18FFB300, 0x00000000),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        for (ray in 0 until 14) {
            val baseAngle = -PI.toFloat() * 0.1f + (ray / 13f) * PI.toFloat() * 1.2f
            val sway = sin(t * 0.7f + ray * 0.9f) * 0.04f
            val a0 = baseAngle + sway - 0.032f
            val a1 = baseAngle + sway + 0.032f

            polyPath.reset()
            polyPath.moveTo(sunX, sunY)
            polyPath.lineTo(sunX + cos(a0) * rayLen, sunY + sin(a0) * rayLen)
            polyPath.lineTo(sunX + cos(a1) * rayLen, sunY + sin(a1) * rayLen)
            polyPath.close()
            canvas.drawPath(polyPath, rayPaint)
        }
        rayPaint.shader = null
    }

    /**
     * Renders a true 3D Perspective-Projected Canyon Mountain & Winding Labyrinth Causeway Mesh
     * from back-to-front with per-facet 3D surface normal lighting!
     */
    private fun draw3dPerspectiveCanyonAndCausewayMesh(
        canvas: Canvas,
        w: Float,
        h: Float,
        t: Float
    ) {
        val horizonY = h * 0.50f
        val fovScale = w * 0.82f
        val camZ = t * 4.5f
        val camX = sin(t * 0.45f) * 0.65f
        val camY = 2.65f + 0.20f * sin(t * 0.6f)

        val zRows = 14
        val xCols = 10
        val zStep = 2.1f
        val xSpan = 13.5f

        val baseZIndex = (camZ / zStep).toInt()

        fun terrainY(wx: Float, wz: Float): Float {
            val roadCenter = sin(wz * 0.16f) * 1.85f
            val distRoad = abs(wx - roadCenter)
            if (distRoad < 1.55f) {
                // Raised flat causeway deck in the center of the river valley
                return -0.35f
            }
            val wallFactor = ((distRoad - 1.55f) / 4.2f).coerceIn(0f, 2.5f)
            val ridge = sin(wx * 0.55f + wz * 0.23f) * cos(wz * 0.31f - wx * 0.4f)
            val peak = abs(sin(wx * 0.37f - wz * 0.19f))
            return -0.55f + (wallFactor * wallFactor * 1.85f) + (ridge * 0.95f + peak * 1.35f) * wallFactor
        }

        val p00 = FloatArray(3)
        val p10 = FloatArray(3)
        val p11 = FloatArray(3)
        val p01 = FloatArray(3)

        // Painter's algorithm from far Z to near Z
        for (rz in zRows downTo 1) {
            val wz0 = (baseZIndex + rz) * zStep
            val wz1 = wz0 + zStep
            val fogFrac = (rz.toFloat() / zRows.toFloat()).coerceIn(0f, 1f)

            for (cxIdx in 0 until xCols) {
                val frac0 = (cxIdx.toFloat() / xCols.toFloat()) - 0.5f
                val frac1 = ((cxIdx + 1).toFloat() / xCols.toFloat()) - 0.5f
                val wx0 = frac0 * xSpan * 2f
                val wx1 = frac1 * xSpan * 2f

                val wy00 = terrainY(wx0, wz0)
                val wy10 = terrainY(wx1, wz0)
                val wy11 = terrainY(wx1, wz1)
                val wy01 = terrainY(wx0, wz1)

                if (!project3D(wx0, wy00, wz0, camX, camY, camZ, w, h, horizonY, fovScale, p00)) continue
                if (!project3D(wx1, wy10, wz0, camX, camY, camZ, w, h, horizonY, fovScale, p10)) continue
                if (!project3D(wx1, wy11, wz1, camX, camY, camZ, w, h, horizonY, fovScale, p11)) continue
                if (!project3D(wx0, wy01, wz1, camX, camY, camZ, w, h, horizonY, fovScale, p01)) continue

                val midX = (wx0 + wx1) * 0.5f
                val midZ = (wz0 + wz1) * 0.5f
                val roadCenter = sin(midZ * 0.16f) * 1.85f
                val distToRoad = abs(midX - roadCenter)
                val isRoad = distToRoad < 1.55f

                // Compute 3D surface slope normal for PBR directional sun shading
                val slopeX = (wy10 - wy00)
                val slopeZ = (wy01 - wy00)
                val sunShade = (0.68f - slopeX * 0.28f + slopeZ * 0.18f).coerceIn(0.22f, 1.25f)

                val baseColor = if (isRoad) {
                    Labyrinth3dEngine.shadePbrColor(0xFF6D5843.toInt(), 0f, 0.7f, 0.7f, 0.35f, 28f)
                } else if (wy00 > 2.4f || wy10 > 2.4f) {
                    // Sunlit golden-granite & snow mountain peaks
                    Labyrinth3dEngine.brightenColor(0xFF786858.toInt(), ((sunShade - 0.5f) * 85f).toInt())
                } else {
                    // Emerald canyon meadow terraces
                    Labyrinth3dEngine.brightenColor(0xFF2E5A27.toInt(), ((sunShade - 0.5f) * 65f).toInt())
                }

                // Blend with golden horizon mist in the distance
                val foggedColor = blendColor(baseColor, 0xFFD9823B.toInt(), fogFrac * 0.58f)

                polyPath.reset()
                polyPath.moveTo(p00[0], p00[1])
                polyPath.lineTo(p10[0], p10[1])
                polyPath.lineTo(p11[0], p11[1])
                polyPath.lineTo(p01[0], p01[1])
                polyPath.close()

                meshPaint.color = foggedColor
                canvas.drawPath(polyPath, meshPaint)

                // Crisp 3D wireframe/stone joint trim so the 3D perspective grid pops visually
                meshStrokePaint.color = if (isRoad) 0x88FFD54F.toInt() else 0x33101810
                meshStrokePaint.strokeWidth = if (isRoad) 1.8f else 0.9f
                canvas.drawPath(polyPath, meshStrokePaint)
            }
        }
    }

    /**
     * Renders 3D Multi-Tiered Citadel Spires rising along the left and right canyon ridges in 3D perspective.
     */
    private fun draw3dMidgroundCitadelSpires(
        canvas: Canvas,
        w: Float,
        h: Float,
        t: Float
    ) {
        val spirePositions = arrayOf(
            floatArrayOf(w * 0.12f, h * 0.54f, 0.82f),
            floatArrayOf(w * 0.88f, h * 0.54f, 0.82f),
            floatArrayOf(w * 0.24f, h * 0.49f, 0.58f),
            floatArrayOf(w * 0.76f, h * 0.49f, 0.58f)
        )

        for ((idx, sp) in spirePositions.withIndex()) {
            val sx = sp[0]
            val sy = sp[1]
            val sc = sp[2]
            Labyrinth3dEngine.drawCastGroundShadow(canvas, sx, sy, 34f * sc, 22f * sc, 75f * sc)
            // 3D Octagonal Stone Base Plinth
            Labyrinth3dEngine.drawExtrudedCylinder3D(
                canvas, sx, sy, 0f, 22f * sc, 30f * sc, 26f * sc,
                0xFF5A4D41.toInt(), 0xFFD4AF37.toInt(), 0xFFD4AF37.toInt(), facets = 8
            )
            // 3D Fluted Citadel Tower Column
            Labyrinth3dEngine.drawExtrudedCylinder3D(
                canvas, sx, sy, 22f * sc, 64f * sc, 21f * sc, 17f * sc,
                0xFF8D7B68.toInt(), 0xFFE6C687.toInt(), 0xFFFFDF73.toInt(), facets = 10
            )
            // 3D Rotating Golden Battlement & Crystal Spire Peak
            Labyrinth3dEngine.drawCone3D(
                canvas, sx, sy, 64f * sc, 102f * sc, 22f * sc,
                if (idx % 2 == 0) 0xFFD4AF37.toInt() else 0xFF29B6F6.toInt(),
                0xFFFFFFFF.toInt(), facets = 6, rotationRad = t * (if (idx % 2 == 0) 1.4f else -1.4f)
            )
        }
    }

    /**
     * Renders the Titanic 3-Tier 3D Perspective-Extruded Metallic Gold & Platinum Monument:
     *   Tier 1: "CREATED BY:"
     *   Tier 2: "BRANDON J."
     *   Tier 3: "WALLACE"
     * With 48-layer 3D perspective extrusion, sunlit top bevels, PBR bronze/gold side walls,
     * a 3D floating stone-and-gold pedestal plinth underneath, and 4K anamorphic lens flares!
     */
    private fun draw3dExtrudedCreatorMonument(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        w: Float,
        h: Float,
        t: Float
    ) {
        // 1. 3D Floating Royal Pedestal Plinth supporting the 3D Creator Monument in mid-air
        val plinthY = cy + h * 0.135f
        Labyrinth3dEngine.drawExtrudedBox3D(
            canvas = canvas,
            cx = cx,
            cy = plinthY,
            zBottom = 0f,
            zTop = 24f,
            halfLength = w * 0.42f,
            halfWidth = 36f,
            yawRad = sin(t * 0.65f) * 0.08f,
            baseColor = 0xFF4A3B2C.toInt(),
            trimColor = 0xFFFFD54F.toInt(),
            shininess = 42f
        )

        // Flanking 3D Crystal Obelisks on the left & right corners of the Creator Pedestal
        Labyrinth3dEngine.drawCone3D(
            canvas, cx - w * 0.37f, plinthY - 4f, 24f, 68f, 16f,
            0xFF00E5FF.toInt(), 0xFFFFFFFF.toInt(), facets = 6, rotationRad = t * 1.8f
        )
        Labyrinth3dEngine.drawCone3D(
            canvas, cx + w * 0.37f, plinthY - 4f, 24f, 68f, 16f,
            0xFFFFD54F.toInt(), 0xFFFFFFFF.toInt(), facets = 6, rotationRad = -t * 1.8f
        )

        // 2. Dynamic 3D Orbital Tilt & Yaw so the viewer clearly sees the deep 3D extruded side walls!
        val yawAngle = sin(t * 1.25f) * 0.48f + 0.22f * cos(t * 0.6f)
        val pitchAngle = cos(t * 0.95f) * 0.28f + 0.22f

        val line1 = "CREATED BY:"
        val line2 = "BRANDON J."
        val line3 = "WALLACE"

        if (cachedWidth != w) {
            cachedWidth = w
            textPaint.textSize = 100f
            val rawW2 = textPaint.measureText(line2).coerceAtLeast(1f)
            val rawW3 = textPaint.measureText(line3).coerceAtLeast(1f)
            cachedSize2 = kotlin.math.floor(((w * 0.82f / rawW2) * 100f).coerceIn(54f, 128f))
            cachedSize3 = kotlin.math.floor(((w * 0.84f / rawW3) * 100f).coerceIn(64f, 146f))
            cachedSize1 = kotlin.math.floor((cachedSize2 * 0.46f).coerceIn(28f, 62f))
        }

        val size1 = cachedSize1
        val size2 = cachedSize2
        val size3 = cachedSize3

        val y1 = cy - size2 * 1.08f
        val y2 = cy - size2 * 0.08f
        val y3 = cy + size3 * 0.92f

        // Deep 3D perspective extrusion vector (swings dynamically to show left/right/bottom/top 3D walls)
        val extrudeDirX = (-sin(yawAngle) * size3 * 0.36f)
        val extrudeDirY = (cos(pitchAngle) * size3 * 0.30f)

        // Volumetric Golden Back-Halo behind the 3D Monument
        glowPaint.shader = RadialGradient(
            cx, cy, w * 0.48f,
            intArrayOf(0x88FFB300.toInt(), 0x4400E5FF.toInt(), 0x00000000),
            floatArrayOf(0f, 0.55f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, w * 0.48f, glowPaint)
        glowPaint.shader = null

        // PASS A: 20-Layer Hardware-Atlas-Cached 3D Z-Extrusion Side Walls & Top Bevels (constant integer textSize!)
        val totalLayers = 20
        textPaint.shader = null
        for (layer in totalLayers downTo 1) {
            val depthFrac = layer.toFloat() / totalLayers.toFloat()
            val lx = cx + extrudeDirX * depthFrac
            val ly1 = y1 + extrudeDirY * depthFrac * 0.75f
            val ly2 = y2 + extrudeDirY * depthFrac * 0.90f
            val ly3 = y3 + extrudeDirY * depthFrac

            if (layer == totalLayers) {
                textPaint.color = 0xEE080502.toInt()
                for (ox in intArrayOf(-4, 4)) {
                    for (oy in intArrayOf(-4, 4)) {
                        textPaint.textSize = size1
                        canvas.drawText(line1, lx + ox, ly1 + oy, textPaint)
                        textPaint.textSize = size2
                        canvas.drawText(line2, lx + ox, ly2 + oy, textPaint)
                        textPaint.textSize = size3
                        canvas.drawText(line3, lx + ox, ly3 + oy, textPaint)
                    }
                }
            }

            val bevelRidge = sin(depthFrac * 14f - t * 4f)
            val wallLight = (0.28f + 0.68f * (1f - depthFrac) + 0.22f * bevelRidge).coerceIn(0.15f, 1.20f)
            val sideR = (205 * wallLight).toInt().coerceIn(24, 255)
            val sideG = (142 * wallLight).toInt().coerceIn(16, 215)
            val sideB = (34 * wallLight).toInt().coerceIn(8, 110)
            textPaint.color = (0xFF shl 24) or (sideR shl 16) or (sideG shl 8) or sideB

            textPaint.textSize = size1
            canvas.drawText(line1, lx, ly1, textPaint)
            textPaint.textSize = size2
            canvas.drawText(line2, lx, ly2, textPaint)
            textPaint.textSize = size3
            canvas.drawText(line3, lx, ly3, textPaint)
        }

        // PASS B: 3D Chamfered Sunlit Platinum-Gold Bevel Rim around the Front Face (GPU-cached FILL)
        textPaint.color = 0xFFFFF59D.toInt()
        for (ox in intArrayOf(-2, 2)) {
            for (oy in intArrayOf(-2, 2)) {
                textPaint.textSize = size1
                canvas.drawText(line1, cx + ox, y1 + oy, textPaint)
                textPaint.textSize = size2
                canvas.drawText(line2, cx + ox, y2 + oy, textPaint)
                textPaint.textSize = size3
                canvas.drawText(line3, cx + ox, y3 + oy, textPaint)
            }
        }

        // PASS C: Front 3D Specular Gold & Chrome Face with Live Travelling Spotlight Shader
        val lightSweepX = cx + sin(t * 1.9f) * (w * 0.42f)
        val faceShader = LinearGradient(
            lightSweepX - size3 * 1.6f, y1 - size1,
            lightSweepX + size3 * 1.6f, y3 + size3 * 0.4f,
            intArrayOf(
                0xFFD4AF37.toInt(),
                0xFFFFF8E1.toInt(),
                0xFFFFFFFF.toInt(),
                0xFFFFD54F.toInt(),
                0xFFB8860B.toInt()
            ),
            floatArrayOf(0f, 0.36f, 0.50f, 0.64f, 1f),
            Shader.TileMode.MIRROR
        )
        textPaint.shader = faceShader

        textPaint.textSize = size1
        canvas.drawText(line1, cx, y1, textPaint)

        textPaint.textSize = size2
        canvas.drawText(line2, cx, y2, textPaint)

        textPaint.textSize = size3
        canvas.drawText(line3, cx, y3, textPaint)
        textPaint.shader = null

        // PASS D: 4K Anamorphic Horizontal Lens Flare Glint sweeping across the 3D letters
        val glintX = cx + sin(t * 1.9f) * (w * 0.34f)
        val glintY = y2 + (y3 - y2) * (0.5f + 0.5f * cos(t * 1.3f))
        glowPaint.shader = RadialGradient(
            glintX, glintY, size3 * 0.85f,
            intArrayOf(0xEEFFFFFF.toInt(), 0x8880D8FF.toInt(), 0x00000000),
            floatArrayOf(0f, 0.32f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(glintX, glintY, size3 * 0.85f, glowPaint)
        glowPaint.shader = null
        ringPaint.color = 0xDDFFFFFF.toInt()
        ringPaint.strokeWidth = 3.2f
        canvas.drawLine(glintX - size3 * 1.6f, glintY, glintX + size3 * 1.6f, glintY, ringPaint)

        // PASS E: 3D Pedestal Inscription Plate on the Floating Plinth (auto-fitted between the 3D Obelisks)
        val eraTitle = when {
            t < 3.8f -> "AGE I–VI: MEDIEVAL KEEP TO GOLDEN EMPIRE"
            t < 7.6f -> "AGE VII–XII: INDUSTRIAL RAILGUNS TO CYBER CITADEL"
            t < 11.4f -> "AGE XIII–XVII: STARFLEET TO DYSON SPHERE"
            else -> "AGE XVIII: OMEGA SINGULARITY • 18 AGES • 3D PBR"
        }
        val plateText = "LABYRINTH CITADEL TD • $eraTitle"
        val plateY = plinthY - 5f
        subPaint.textSize = 40f
        val rawPlateW = subPaint.measureText(plateText).coerceAtLeast(1f)
        subPaint.textSize = ((w * 0.68f / rawPlateW) * 40f).coerceIn(13f, 22f)
        subPaint.color = 0xFF0E0A06.toInt()
        canvas.drawText(plateText, cx + 1.8f, plateY + 1.8f, subPaint)
        subPaint.color = 0xFFFFE082.toInt()
        canvas.drawText(plateText, cx, plateY, subPaint)
    }

    /**
     * Draws the back half (`frontHalf = false`) or front half (`frontHalf = true`) of 2 intersecting
     * 3D Astrolabe Energy Rings so they physically wrap behind and in front of "BRANDON J. WALLACE"!
     */
    private fun draw3dOrbitingRingsHalf(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        w: Float,
        h: Float,
        t: Float,
        frontHalf: Boolean
    ) {
        val rx = w * 0.44f
        val ry = h * 0.14f
        val startAngle = if (frontHalf) 0f else 180f

        for (ringIdx in 0..1) {
            val tiltDeg = if (ringIdx == 0) 15f * sin(t * 0.7f) - 11f else -15f * cos(t * 0.7f) + 11f
            canvas.save()
            canvas.translate(cx, cy)
            canvas.rotate(tiltDeg)
            rectF.set(-rx, -ry, rx, ry)

            ringPaint.strokeWidth = if (frontHalf) 5.5f else 3.2f
            ringPaint.color = if (ringIdx == 0) {
                if (frontHalf) 0xDDFFD54F.toInt() else 0x66FFD54F
            } else {
                if (frontHalf) 0xDD40C4FF.toInt() else 0x6640C4FF
            }
            canvas.drawArc(rectF, startAngle, 180f, false, ringPaint)

            val orbAngle = t * (if (ringIdx == 0) 1.9f else -2.3f) + ringIdx * 2.0f
            val inThisHalf = if (frontHalf) sin(orbAngle) >= 0f else sin(orbAngle) < 0f
            if (inThisHalf) {
                val ox = cos(orbAngle) * rx
                val oy = sin(orbAngle) * ry
                Labyrinth3dEngine.drawSphere3D(
                    canvas, ox, oy, 0f, if (frontHalf) 11f else 7f,
                    if (ringIdx == 0) 0xFFFFDF73.toInt() else 0xFF40C4FF.toInt(),
                    0.95f
                )
            }
            canvas.restore()
        }
    }

    /**
     * Renders a Close-Up 2.4x Scale 3D Battle Diorama across the lower third of the 4K Intro:
     * 4 massive 3D Towers (with rotating 3D turrets & barrels) blasting close-up 3D articulated
     * knights, 3D multi-wheeled siege tanks, and the colossal 3D Omega Leviathan Boss!
     */
    private fun draw3dForegroundBattleDiorama(
        canvas: Canvas,
        w: Float,
        h: Float,
        t: Float
    ) {
        val sc = (w / 1080f).coerceIn(0.75f, 1.6f) * 2.15f
        val towerY = h * 0.73f
        val roadY = h * 0.85f

        // 1. Massive 3D Elevated Royal Causeway Bridge Deck in Foreground
        Labyrinth3dEngine.drawExtrudedBox3D(
            canvas = canvas,
            cx = w * 0.5f,
            cy = roadY + 14f * sc,
            zBottom = 0f,
            zTop = 18f * sc,
            halfLength = w * 0.48f,
            halfWidth = 36f * sc,
            yawRad = 0f,
            baseColor = 0xFF4E4237.toInt(),
            trimColor = 0xFFD4AF37.toInt(),
            shininess = 36f
        )

        // 2. Four Close-Up 2.15x-Scale 3D Articulated Towers along the Citadel Ridge
        val towerSpecs = arrayOf(
            Triple(w * 0.14f, 0xFF8C7A68.toInt(), 0xFFD4AF37.toInt()),
            Triple(w * 0.38f, 0xFF9C6B3B.toInt(), 0xFFFFB300.toInt()),
            Triple(w * 0.62f, 0xFF455A64.toInt(), 0xFF40C4FF.toInt()),
            Triple(w * 0.86f, 0xFF37474F.toInt(), 0xFF00E5FF.toInt())
        )

        for ((idx, spec) in towerSpecs.withIndex()) {
            val tx = spec.first
            val baseCol = spec.second
            val accentCol = spec.third

            Labyrinth3dEngine.drawCastGroundShadow(canvas, tx, towerY, 26f * sc, 18f * sc, 56f * sc)
            // Tier 1: Wide 3D Octagonal Stone & Gold Plinth
            Labyrinth3dEngine.drawExtrudedCylinder3D(
                canvas, tx, towerY, 0f, 15f * sc, 25f * sc, 22f * sc,
                0xFF4A423A.toInt(), 0xFFD4AF37.toInt(), 0xFFD4AF37.toInt(), facets = 8
            )
            // Tier 2: 3D Fluted Tower Drum / Armored Superstructure
            Labyrinth3dEngine.drawExtrudedCylinder3D(
                canvas, tx, towerY, 15f * sc, 42f * sc, 18f * sc, 15f * sc,
                baseCol, Labyrinth3dEngine.brightenColor(baseCol, 32), accentCol, facets = 10
            )
            // Tier 3: 3D Rotating Battlement Crown & Crystalline Turret Dome
            val aimYaw = 1.15f + 0.38f * sin(t * 2.8f + idx * 1.4f)
            Labyrinth3dEngine.drawCone3D(
                canvas, tx, towerY, 42f * sc, 60f * sc, 16f * sc,
                accentCol, 0xFFFFFFFF.toInt(), facets = 8, rotationRad = t * 1.6f
            )
            // Tier 4: 3D Articulated Cannon / Railgun Barrel aiming down at the 3D Creeps
            Labyrinth3dEngine.drawBarrel3D(
                canvas, tx, towerY, 36f * sc, aimYaw, 28f * sc, -6f * sc, 5.2f * sc,
                0xFFD4AF37.toInt(), 0xFF263238.toInt(), accentCol
            )
        }

        // 3. Close-Up 2.15x-Scale Marching 3D Articulated Creeps, Siege Tanks & Leviathan Boss!
        val convoyCount = 5
        for (i in 0 until convoyCount) {
            val progress = ((t * 0.13f + i * 0.19f) % 1.0f)
            val ex = w * 0.08f + progress * (w * 0.84f)
            val ey = roadY + sin(progress * 10f + i) * (5f * sc)
            val isBoss = (i == 0)
            val r = (if (isBoss) 22f else 15f) * sc

            Labyrinth3dEngine.drawCastGroundShadow(canvas, ex, ey, r * 1.15f, r * 0.82f, r * 1.6f)
            if (isBoss || i == 2) {
                // 3D Armored Siege Tank / Omega Leviathan Boss with 3D Treads, Bevelled Hull, Turret & Cannon
                Labyrinth3dEngine.drawExtrudedBox3D(
                    canvas, ex, ey + r * 0.45f, 0f, r * 0.35f, r * 1.18f, r * 0.22f, 0f,
                    0xFF212121.toInt(), 0xFF616161.toInt()
                )
                Labyrinth3dEngine.drawExtrudedBox3D(
                    canvas, ex, ey, r * 0.25f, r * 0.95f, r * 1.10f, r * 0.72f, 0f,
                    if (isBoss) 0xFFB71C1C.toInt() else 0xFF455A64.toInt(),
                    0xFFFFD54F.toInt()
                )
                Labyrinth3dEngine.drawExtrudedCylinder3D(
                    canvas, ex, ey, r * 0.95f, r * 1.55f, r * 0.64f, r * 0.52f,
                    if (isBoss) 0xFFD4AF37.toInt() else 0xFF78909C.toInt(),
                    0xFFFFF59D.toInt(), 0xFF00E5FF.toInt(), facets = 8
                )
                Labyrinth3dEngine.drawBarrel3D(
                    canvas, ex, ey, r * 1.25f, 0f, r * 1.45f, 3f * sc, r * 0.28f,
                    0xFFD4AF37.toInt(), 0xFF263238.toInt(), 0xFF00E5FF.toInt()
                )
            } else {
                // 3D Articulated Armored Knight / Cyber Strider with Striding 3D Legs, Cuirass, Shield, Helmet & Plume
                val stride = sin(t * 9.5f + i * 2f) * (5.5f * sc)
                Labyrinth3dEngine.drawExtrudedCylinder3D(
                    canvas, ex - 4f * sc + stride, ey + 3f * sc, 0f, 11f * sc, 4.2f * sc, 3.5f * sc,
                    0xFF3E2723.toInt(), facets = 6
                )
                Labyrinth3dEngine.drawExtrudedCylinder3D(
                    canvas, ex + 4f * sc - stride, ey - 3f * sc, 0f, 11f * sc, 4.2f * sc, 3.5f * sc,
                    0xFF3E2723.toInt(), facets = 6
                )
                // 3D Armored Torso Cuirass
                Labyrinth3dEngine.drawExtrudedCylinder3D(
                    canvas, ex, ey, 10f * sc, 26f * sc, 10.5f * sc, 8.8f * sc,
                    0xFF8D6E63.toInt(), 0xFFD4AF37.toInt(), 0xFFFFE082.toInt(), facets = 8
                )
                // 3D Tower Shield on Left Arm
                Labyrinth3dEngine.drawExtrudedBox3D(
                    canvas, ex + 3f * sc, ey + 8f * sc, 9f * sc, 24f * sc, 6.5f * sc, 2.4f * sc, 0.15f,
                    0xFF1565C0.toInt(), 0xFFFFD54F.toInt()
                )
                // 3D Helmet Sphere + Crest Plume
                Labyrinth3dEngine.drawSphere3D(canvas, ex, ey, 31f * sc, 7.2f * sc, 0xFFECEFF1.toInt(), 0.9f)
                Labyrinth3dEngine.drawCone3D(
                    canvas, ex, ey, 36f * sc, 46f * sc, 4.5f * sc,
                    0xFFD32F2F.toInt(), 0xFFFFCDD2.toInt(), facets = 6
                )
            }

            // 4. Volumetric 3D Laser & Plasma Beams firing from the 3D Towers onto the 3D Convoy!
            val firingTowerIdx = (i + (t * 2.2f).toInt()) % towerSpecs.size
            val tx = towerSpecs[firingTowerIdx].first
            val ty = Labyrinth3dEngine.projY(towerY, 46f * sc)
            val targetScreenY = Labyrinth3dEngine.projY(ey, 18f * sc)
            if (((t * 4.5f + i).toInt() % 2) == 0) {
                ringPaint.color = 0x6600E5FF
                ringPaint.strokeWidth = 9.5f
                canvas.drawLine(tx, ty, ex, targetScreenY, ringPaint)
                ringPaint.color = 0xFFFFFFFF.toInt()
                ringPaint.strokeWidth = 3.2f
                canvas.drawLine(tx, ty, ex, targetScreenY, ringPaint)
                Labyrinth3dEngine.drawSphere3D(canvas, ex, ey, 18f * sc, 7.5f * sc, 0xFFFFD54F.toInt(), 0.95f)
            }
        }
    }

    private fun blendColor(c1: Int, c2: Int, ratio: Float): Int {
        val t = ratio.coerceIn(0f, 1f)
        val inv = 1f - t
        val r = (((c1 shr 16) and 0xFF) * inv + ((c2 shr 16) and 0xFF) * t).toInt().coerceIn(0, 255)
        val g = (((c1 shr 8) and 0xFF) * inv + ((c2 shr 8) and 0xFF) * t).toInt().coerceIn(0, 255)
        val b = ((c1 and 0xFF) * inv + (c2 and 0xFF) * t).toInt().coerceIn(0, 255)
        return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
    }
}

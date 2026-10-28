package com.brandonwallace.labyrinthtd.render

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.brandonwallace.labyrinthtd.game.GameEngine
import com.brandonwallace.labyrinthtd.game.LabyrinthMap
import com.brandonwallace.labyrinthtd.game.WeatherFront

/**
 * FIXED, NON-SCROLLABLE 3D BATTLEFIELD VIEW (v1.6)
 *
 * - Locks the map in place (no scrolling, no panning, no pinch-zoom drift) and fits the
 *   3D Terraced Valley, River Canyon, Arched Stone Bridges, and Cobblestone Labyrinth Road
 *   cleanly across the full playable viewport between the 2D Top HUD and 2D Bottom Dock.
 * - Uses [Labyrinth3dEngine.getOrBuildStaticMapBitmap] so the ultra-detailed 3D terrain,
 *   3D stone pavers, 3D curbstones, 3D bridges, and 3D forest render in < 0.1ms per frame.
 */
class LabyrinthCanvasView(
    context: Context,
    private val engine: GameEngine,
    private val onSelectionChanged: () -> Unit
) : View(context) {

    private var lastFrameNanos: Long = System.nanoTime()
    private var animTimeSec: Float = 0f

    // Fixed viewport transform (non-scrollable, non-moveable, fitted cleanly between top & bottom 2D HUD)
    private var baseScaleX: Float = 1f
    private var baseScaleY: Float = 1f
    private var baseOffsetX: Float = 0f
    private var baseOffsetY: Float = 0f

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val dashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.4f
        pathEffect = DashPathEffect(floatArrayOf(10f, 8f), 0f)
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val roadPath = Path()

    private val gestureDetector = GestureDetector(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean = true

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                handleWorldTap(e.x, e.y)
                return true
            }
        }
    )

    init {
        buildRoadPath()
    }

    private fun buildRoadPath() {
        roadPath.reset()
        val pts = LabyrinthMap.waypoints
        if (pts.isNotEmpty()) {
            roadPath.moveTo(pts[0].x, pts[0].y)
            for (i in 1 until pts.size) {
                roadPath.lineTo(pts[i].x, pts[i].y)
            }
        }
    }

    fun resetCamera() {
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val density = resources.displayMetrics.density
        // Fit cleanly between the 2-row 2D Top HUD + status bar (116dp) and 2D Bottom Dock + nav bar (100dp)
        // Zoomed out slightly (VIEW_SPAN_X = 1112, VIEW_SPAN_Y = 1464) so all outer-edge towers & 3D keeps are 100% visible!
        val topHudPx = 116f * density
        val bottomHudPx = 100f * density
        val usableH = (h - topHudPx - bottomHudPx).coerceAtLeast(260f)

        baseScaleX = (w * 0.994f) / LabyrinthMap.VIEW_SPAN_X
        baseScaleY = (usableH * 0.990f) / LabyrinthMap.VIEW_SPAN_Y
        baseOffsetX = ((w - LabyrinthMap.VIEW_SPAN_X * baseScaleX) * 0.5f) - LabyrinthMap.VIEW_MIN_X * baseScaleX
        baseOffsetY = topHudPx + ((usableH - LabyrinthMap.VIEW_SPAN_Y * baseScaleY) * 0.5f) - LabyrinthMap.VIEW_MIN_Y * baseScaleY
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return true
    }

    /**
     * Free-Placement World Tap on Fixed Map:
     * Tapping an existing tower selects it; tapping open grass places the selected building right there!
     */
    private fun handleWorldTap(screenX: Float, screenY: Float) {
        val worldX = (screenX - baseOffsetX) / baseScaleX.coerceAtLeast(0.01f)
        val worldY = (screenY - baseOffsetY) / baseScaleY.coerceAtLeast(0.01f)

        engine.handleMeadowTap(worldX, worldY)
        onSelectionChanged()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val now = System.nanoTime()
        val rawDt = ((now - lastFrameNanos) / 1_000_000_000f).coerceIn(0.001f, 0.08f)
        lastFrameNanos = now
        animTimeSec += rawDt

        engine.stepSimulation(rawDt)

        engine.executeLockedRenderRead { eng ->
            val ageNum = eng.effectiveAgeLevel()

            // 1. Blit Offscreen-Cached Ultra-Detailed 3D Terrain, River, Bridges, Cobblestone Road & Forest (< 0.1ms!)
            val staticMapBmp = Labyrinth3dEngine.getOrBuildStaticMapBitmap(
                viewW = width,
                viewH = height,
                scaleX = baseScaleX,
                scaleY = baseScaleY,
                offsetX = baseOffsetX,
                offsetY = baseOffsetY,
                ageNum = ageNum,
                roadPath = roadPath
            )
            canvas.drawBitmap(staticMapBmp, 0f, 0f, null)

            // 2. Apply Fixed World Transform for Dynamic 3D Entities & 3D Attacks
            canvas.save()
            canvas.translate(baseOffsetX, baseOffsetY)
            canvas.scale(baseScaleX, baseScaleY)

            Labyrinth3dEngine.drawLiveMapAtmosphere3D(canvas, animTimeSec)
            Labyrinth3dEngine.drawRaiderGateCastleKeepAndCreatorMonument3D(canvas, eng, ageNum, animTimeSec)
            drawDeployedRoadTraps(canvas, eng)
            if (eng.showSynergyOverlay) {
                drawSynergyLinks(canvas, eng)
            }
            drawSelectedTowerRangeOnly(canvas, eng)
            Labyrinth3dEngine.drawTowers3D(canvas, eng, ageNum, animTimeSec)
            Labyrinth3dEngine.drawEnemies3D(canvas, eng, animTimeSec)
            Labyrinth3dEngine.drawProjectilesAndShockwaves3D(
                canvas = canvas,
                projectiles = eng.projectiles,
                shockwaves = eng.shockwaves,
                animTimeSec = animTimeSec
            )
            if (eng.showFloatingCallouts) {
                drawCallouts(canvas, eng)
            }
            drawWeatherAccents(canvas, eng.currentWeather)

            canvas.restore()
        }

        postInvalidateDelayed(16L)
    }

    private fun drawDeployedRoadTraps(canvas: Canvas, eng: GameEngine) {
        if (eng.trackGadgets.isEmpty()) return
        for ((slotId, deployed) in eng.trackGadgets) {
            val slot = LabyrinthMap.gadgetSlots.getOrNull(slotId) ?: continue
            Labyrinth3dEngine.drawExtrudedCylinder3D(
                canvas, slot.x, slot.y, 0f, 5.5f, 13.5f, 11.5f,
                0xFF3E352B.toInt(), deployed.colorArgb.toInt(), 0xFFD4AF37.toInt(), facets = 8
            )
        }
    }

    private fun drawSynergyLinks(canvas: Canvas, eng: GameEngine) {
        for (link in eng.synergyLinks) {
            dashPaint.color = if (link.isFusionReady) 0xCCD4AF37.toInt() else 0x77D4AF37
            dashPaint.strokeWidth = if (link.isFusionReady) 3.2f else 2.0f
            canvas.drawLine(link.fromX, link.fromY, link.toX, link.toY, dashPaint)
        }
    }

    private fun drawSelectedTowerRangeOnly(canvas: Canvas, eng: GameEngine) {
        val selId = eng.selectedPadId ?: return
        val tower = eng.towers[selId] ?: return
        val effRange = eng.effectiveTowerRange(tower)

        fillPaint.color = 0x18D4AF37
        canvas.drawCircle(tower.x, tower.y, effRange, fillPaint)
        dashPaint.color = 0xBBECD078.toInt()
        dashPaint.strokeWidth = 2.4f
        canvas.drawCircle(tower.x, tower.y, effRange, dashPaint)

        strokePaint.color = 0xFFFFDF73.toInt()
        strokePaint.strokeWidth = 3.6f
        canvas.drawCircle(tower.x, tower.y, 31f, strokePaint)
    }

    private fun drawCallouts(canvas: Canvas, eng: GameEngine) {
        for (c in eng.callouts) {
            val alpha = ((c.life / c.maxLife) * 255).toInt().coerceIn(0, 255)
            textPaint.textSize = if (c.isCombo) 13.5f else 11.0f
            textPaint.color = (0xFF1A120B.toInt() and 0x00FFFFFF) or (alpha shl 24)
            canvas.drawText(c.text, c.x + 1.5f, c.y + 1.5f, textPaint)
            textPaint.color = (c.colorArgb.toInt() and 0x00FFFFFF) or (alpha shl 24)
            canvas.drawText(c.text, c.x, c.y, textPaint)
        }
    }

    private fun drawWeatherAccents(canvas: Canvas, weather: WeatherFront) {
        if (weather == WeatherFront.MONSOON_SQUALL) {
            strokePaint.color = 0x288FBCCF
            strokePaint.strokeWidth = 1.6f
            val offset = (animTimeSec * 400f) % 200f
            for (i in 0..18) {
                val rx = (i * 65f + offset) % LabyrinthMap.WORLD_WIDTH
                val ry = (i * 95f + offset * 1.6f) % LabyrinthMap.WORLD_HEIGHT
                canvas.drawLine(rx, ry, rx - 8f, ry + 20f, strokePaint)
            }
        }
    }
}

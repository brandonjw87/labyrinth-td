package com.brandonwallace.labyrinthtd

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.brandonwallace.labyrinthtd.audio.SoundEngine
import com.brandonwallace.labyrinthtd.game.GameEngine
import com.brandonwallace.labyrinthtd.render.LabyrinthCanvasView
import com.brandonwallace.labyrinthtd.ui.BrandonWallaceIntroOverlay
import com.brandonwallace.labyrinthtd.ui.LabyrinthHudOverlay
import com.brandonwallace.labyrinthtd.ui.LabyrinthTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private lateinit var soundEngine: SoundEngine
    private lateinit var gameEngine: GameEngine
    private var canvasViewRef: LabyrinthCanvasView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        soundEngine = SoundEngine()
        gameEngine = GameEngine(soundEngine)

        val prefs = getSharedPreferences(GameEngine.PREFS_NAME, MODE_PRIVATE)
        gameEngine.load(prefs)

        setContent {
            LabyrinthTheme {
                var uiSnap by remember { mutableStateOf(gameEngine.captureUiSnapshot()) }
                var showIntro by remember { mutableStateOf(true) }

                // Continuous UI telemetry sync + periodic auto-save
                LaunchedEffect(Unit) {
                    var ticks = 0
                    while (true) {
                        delay(300L)
                        uiSnap = gameEngine.captureUiSnapshot()
                        ticks++
                        if (ticks % 60 == 0) {
                            gameEngine.save(prefs)
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    if (showIntro) {
                        BrandonWallaceIntroOverlay(
                            onFinished = {
                                showIntro = false
                            }
                        )
                    } else {
                        AndroidView(
                            factory = { ctx ->
                                LabyrinthCanvasView(
                                    context = ctx,
                                    engine = gameEngine,
                                    onSelectionChanged = {
                                        uiSnap = gameEngine.captureUiSnapshot()
                                    }
                                ).also { cv ->
                                    canvasViewRef = cv
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        LabyrinthHudOverlay(
                            snap = uiSnap,
                            engine = gameEngine,
                            onResetCamera = {
                                canvasViewRef?.resetCamera()
                            },
                            onAction = {
                                uiSnap = gameEngine.captureUiSnapshot()
                            },
                            onReplayIntro = {
                                showIntro = true
                            }
                        )
                    }
                }

                DisposableEffect(Unit) {
                    onDispose {
                        gameEngine.save(prefs)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        soundEngine.resume()
        soundEngine.setAmbientPlaying(true)
    }

    override fun onPause() {
        val prefs = getSharedPreferences(GameEngine.PREFS_NAME, MODE_PRIVATE)
        gameEngine.save(prefs)
        soundEngine.setAmbientPlaying(false)
        soundEngine.pause()
        super.onPause()
    }

    override fun onDestroy() {
        val prefs = getSharedPreferences(GameEngine.PREFS_NAME, MODE_PRIVATE)
        gameEngine.save(prefs)
        soundEngine.release()
        super.onDestroy()
    }
}

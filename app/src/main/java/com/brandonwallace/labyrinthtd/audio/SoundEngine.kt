package com.brandonwallace.labyrinthtd.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

class SoundEngine {
    enum class Sfx {
        GATLING,
        CRYO,
        PYRO,
        TESLA,
        VENOM,
        SNIPER,
        MORTAR,
        PRISM,
        CHRONO,
        DRONE,
        HYDRO,
        HUB_MINT,
        REACTION_COMBO,
        FUSION_APEX,
        GADGET_DEPLOY,
        UPGRADE,
        WAVE_START,
        CORE_SURGE,
        CHEAT_ACTIVATE
    }

    private data class Voice(
        val sfx: Sfx,
        var samplePos: Int = 0,
        val totalSamples: Int,
        val freqStart: Float,
        val freqEnd: Float,
        val volume: Float,
        val noiseMix: Float = 0f
    )

    @Volatile
    var isMuted: Boolean = false

    @Volatile
    private var running: Boolean = true

    @Volatile
    private var paused: Boolean = false

    @Volatile
    private var ambientEnabled: Boolean = true

    private val activeVoices = ConcurrentLinkedQueue<Voice>()
    private var workerThread: Thread? = null

    init {
        startAudioThread()
    }

    fun toggleMute(): Boolean {
        isMuted = !isMuted
        return isMuted
    }

    fun setAmbientPlaying(play: Boolean) {
        ambientEnabled = play
    }

    fun pause() {
        paused = true
    }

    fun resume() {
        paused = false
    }

    fun release() {
        running = false
        workerThread?.interrupt()
    }

    fun play(sfx: Sfx) {
        if (isMuted || paused) return
        if (activeVoices.size > 14) return
        val sr = 22050
        val voice = when (sfx) {
            Sfx.GATLING -> Voice(sfx, 0, (sr * 0.045f).toInt(), 360f, 110f, 0.16f, 0.35f)
            Sfx.CRYO -> Voice(sfx, 0, (sr * 0.11f).toInt(), 1250f, 1850f, 0.18f, 0.1f)
            Sfx.PYRO -> Voice(sfx, 0, (sr * 0.12f).toInt(), 220f, 95f, 0.20f, 0.75f)
            Sfx.TESLA -> Voice(sfx, 0, (sr * 0.09f).toInt(), 920f, 380f, 0.20f, 0.45f)
            Sfx.VENOM -> Voice(sfx, 0, (sr * 0.10f).toInt(), 440f, 260f, 0.17f, 0.25f)
            Sfx.SNIPER -> Voice(sfx, 0, (sr * 0.16f).toInt(), 680f, 90f, 0.28f, 0.40f)
            Sfx.MORTAR -> Voice(sfx, 0, (sr * 0.24f).toInt(), 150f, 42f, 0.32f, 0.55f)
            Sfx.PRISM -> Voice(sfx, 0, (sr * 0.14f).toInt(), 660f, 1320f, 0.20f, 0.05f)
            Sfx.CHRONO -> Voice(sfx, 0, (sr * 0.18f).toInt(), 980f, 240f, 0.22f, 0.08f)
            Sfx.DRONE -> Voice(sfx, 0, (sr * 0.07f).toInt(), 520f, 740f, 0.14f, 0.15f)
            Sfx.HYDRO -> Voice(sfx, 0, (sr * 0.12f).toInt(), 310f, 520f, 0.19f, 0.50f)
            Sfx.HUB_MINT -> Voice(sfx, 0, (sr * 0.14f).toInt(), 880f, 1760f, 0.18f, 0.0f)
            Sfx.REACTION_COMBO -> Voice(sfx, 0, (sr * 0.18f).toInt(), 540f, 1080f, 0.25f, 0.12f)
            Sfx.FUSION_APEX -> Voice(sfx, 0, (sr * 0.42f).toInt(), 330f, 1320f, 0.35f, 0.08f)
            Sfx.GADGET_DEPLOY -> Voice(sfx, 0, (sr * 0.16f).toInt(), 420f, 840f, 0.24f, 0.10f)
            Sfx.UPGRADE -> Voice(sfx, 0, (sr * 0.16f).toInt(), 523f, 1046f, 0.24f, 0.0f)
            Sfx.WAVE_START -> Voice(sfx, 0, (sr * 0.30f).toInt(), 196f, 392f, 0.26f, 0.08f)
            Sfx.CORE_SURGE -> Voice(sfx, 0, (sr * 0.45f).toInt(), 480f, 75f, 0.36f, 0.35f)
            Sfx.CHEAT_ACTIVATE -> Voice(sfx, 0, (sr * 0.25f).toInt(), 600f, 1500f, 0.28f, 0.05f)
        }
        activeVoices.offer(voice)
    }

    private fun startAudioThread() {
        workerThread = thread(start = true, isDaemon = true, name = "LabyrinthAudio") {
            val sampleRate = 22050
            val chunkSamples = 512
            val buffer = ShortArray(chunkSamples)
            var track: AudioTrack? = null
            try {
                val minBuf = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(chunkSamples * 2)

                track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBuf * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                track.play()

                while (running) {
                    if (paused || isMuted || activeVoices.isEmpty()) {
                        Thread.sleep(25)
                        continue
                    }

                    for (i in 0 until chunkSamples) {
                        var mix = 0f

                        val iter = activeVoices.iterator()
                        while (iter.hasNext()) {
                            val v = iter.next()
                            val progress = v.samplePos.toFloat() / v.totalSamples.toFloat()
                            if (progress >= 1f) {
                                iter.remove()
                                continue
                            }
                            val freq = v.freqStart + (v.freqEnd - v.freqStart) * progress
                            val env = exp(-3.2f * progress) * (1f - progress)
                            val phase = (2.0 * PI * freq * v.samplePos) / sampleRate
                            val tone = sin(phase).toFloat()
                            val noise = (Random.nextFloat() * 2f - 1f)
                            val sig = tone * (1f - v.noiseMix) + noise * v.noiseMix
                            mix += sig * v.volume * env
                            v.samplePos++
                        }

                        val clamped = mix.coerceIn(-0.95f, 0.95f)
                        buffer[i] = (clamped * Short.MAX_VALUE).toInt().toShort()
                    }
                    track.write(buffer, 0, chunkSamples, AudioTrack.WRITE_NON_BLOCKING)
                    Thread.sleep(18)
                }
            } catch (_: Throwable) {
                // Ignore audio hardware unavailability in unit test JVM or headless runtimes
            } finally {
                try {
                    track?.stop()
                    track?.release()
                } catch (_: Throwable) {}
            }
        }
    }
}

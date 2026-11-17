package com.example.ui.avs

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.audiofx.Visualizer
import androidx.core.content.ContextCompat
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sin

/**
 * Аудио-контроллер реального времени для AVS визуализатора:
 * 1. Захватывает спектр (FFT) и форму волны (Waveform) через аппаратный android.media.audiofx.Visualizer.
 * 2. При отсутствии сессии или отсутствии разрешений — автоматически генерирует богатый органический
 *    синтетический спектр в ритме играющей музыки, гарантируя стабильные 60 FPS без лагов.
 * 3. Рассчитывает энергии полос (Bass, Mid, Treble) и детектирует пиковый бас (Beat Detection).
 */
class AudioVisualizerController(private val context: Context) {

    private var visualizer: Visualizer? = null
    private var currentSessionId: Int = 0

    val fftBands = FloatArray(64)
    val waveform = FloatArray(128)

    var bassEnergy: Float = 0f
        private set
    var midEnergy: Float = 0f
        private set
    var trebleEnergy: Float = 0f
        private set
    var isBeat: Boolean = false
        private set

    private var beatThreshold = 0.45f
    private var prevBass = 0f
    private var animPhase = 0f

    @Synchronized
    fun attachSession(sessionId: Int) {
        if (sessionId == currentSessionId && visualizer != null) return
        releaseVisualizer()
        currentSessionId = sessionId

        if (sessionId <= 0) return

        try {
            val hasRecordPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasRecordPermission) {
                // Работаем в безопасном синтетическом режиме
                return
            }

            val vis = Visualizer(sessionId).apply {
                captureSize = Visualizer.getCaptureSizeRange()[1].coerceAtMost(512)
                setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(
                            v: Visualizer?,
                            data: ByteArray?,
                            rate: Int
                        ) {
                            processWaveform(data)
                        }

                        override fun onFftDataCapture(
                            v: Visualizer?,
                            data: ByteArray?,
                            rate: Int
                        ) {
                            processFft(data)
                        }
                    },
                    Visualizer.getMaxCaptureRate(),
                    true,
                    true
                )
                enabled = true
            }
            visualizer = vis
        } catch (e: Exception) {
            e.printStackTrace()
            visualizer = null
        }
    }

    private fun processWaveform(data: ByteArray?) {
        if (data == null || data.isEmpty()) return
        val step = max(1, data.size / waveform.size)
        for (i in waveform.indices) {
            val srcIdx = (i * step).coerceAtMost(data.size - 1)
            // Unsigned 8-bit to normalized float [-1.0f .. 1.0f]
            val b = data[srcIdx].toInt() and 0xFF
            waveform[i] = (b - 128) / 128f
        }
    }

    private fun processFft(data: ByteArray?) {
        if (data == null || data.isEmpty()) return
        val n = (data.size / 2).coerceAtMost(fftBands.size)
        var sumBass = 0f
        var sumMid = 0f
        var sumTreble = 0f

        for (i in 0 until n) {
            val r = data[i * 2].toFloat()
            val im = data[i * 2 + 1].toFloat()
            val mag = (hypot(r.toDouble(), im.toDouble()) / 128.0).toFloat().coerceIn(0f, 1f)

            // Сглаживание пиков (decay 0.82)
            fftBands[i] = max(mag, fftBands[i] * 0.82f)

            if (i < 8) sumBass += fftBands[i]
            else if (i < 28) sumMid += fftBands[i]
            else sumTreble += fftBands[i]
        }

        bassEnergy = (sumBass / 8f).coerceIn(0f, 1f)
        midEnergy = (sumMid / 20f).coerceIn(0f, 1f)
        trebleEnergy = (sumTreble / 36f).coerceIn(0f, 1f)

        // Beat detection
        val bassDelta = bassEnergy - prevBass
        isBeat = bassEnergy > beatThreshold && bassDelta > 0.12f
        prevBass = bassEnergy
    }

    /**
     * Вызывается каждый кадр анимации (60 FPS).
     * Если аппаратный визуализатор не активен или звук на паузе,
     * обновляет синтетический спектр и фазу анимации.
     */
    fun tick(isPlaying: Boolean, dt: Float = 0.016f) {
        animPhase += dt * (if (isPlaying) 2.5f else 0.5f)

        if (visualizer == null || !isPlaying) {
            // Генерация органического живого спектра
            val amp = if (isPlaying) 0.85f else 0.12f
            var sumBass = 0f
            var sumMid = 0f
            var sumTreble = 0f

            for (i in fftBands.indices) {
                val f = i / fftBands.size.toFloat()
                val wave1 = sin(animPhase * 3.2f + i * 0.28f)
                val wave2 = cos(animPhase * 5.1f - i * 0.15f)
                val wave3 = sin(animPhase * 1.7f + i * 0.65f)

                val synth = ((wave1 * 0.5f + wave2 * 0.35f + wave3 * 0.25f + 1f) / 2.1f) * amp
                val falloff = (1f - f * 0.65f)
                val target = (synth * falloff).coerceIn(0.02f, 1f)
                fftBands[i] = max(target, fftBands[i] * 0.86f)

                if (i < 8) sumBass += fftBands[i]
                else if (i < 28) sumMid += fftBands[i]
                else sumTreble += fftBands[i]
            }

            for (i in waveform.indices) {
                val wave = sin(animPhase * 4f + i * 0.12f) * amp
                waveform[i] = wave.coerceIn(-1f, 1f)
            }

            bassEnergy = (sumBass / 8f).coerceIn(0f, 1f)
            midEnergy = (sumMid / 20f).coerceIn(0f, 1f)
            trebleEnergy = (sumTreble / 36f).coerceIn(0f, 1f)

            val bassDelta = bassEnergy - prevBass
            isBeat = isPlaying && (bassEnergy > 0.45f && bassDelta > 0.08f)
            prevBass = bassEnergy
        }
    }

    @Synchronized
    fun releaseVisualizer() {
        try {
            visualizer?.enabled = false
            visualizer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        visualizer = null
        currentSessionId = 0
    }
}

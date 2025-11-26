package it.progmob.esame1

import kotlin.math.*

object AudioAnalyzer {

    // -------- PITCH (YIN algorithm) --------
    fun detectPitch(pcm: FloatArray, sampleRate: Int): Float {
        val size = min(pcm.size, 2048)
        val yin = FloatArray(size / 2)
        var minVal = Float.MAX_VALUE
        var bestTau = -1

        for (tau in 1 until yin.size) {
            var sum = 0f
            for (i in 0 until size - tau) {
                val diff = pcm[i] - pcm[i + tau]
                sum += diff * diff
            }
            yin[tau] = sum
            if (sum < minVal) {
                minVal = sum
                bestTau = tau
            }
        }

        return if (bestTau > 0) sampleRate.toFloat() / bestTau else 0f
    }

    // -------- BPM (autocorrelation) --------
    fun detectBPM(pcm: FloatArray, sampleRate: Int): Int {
        val step = sampleRate / 2
        val buffer = pcm.take(step * 4).toFloatArray()

        val autocorr = FloatArray(step)
        for (lag in 1 until step) {
            var sum = 0f
            for (i in 0 until buffer.size - lag) {
                sum += buffer[i] * buffer[i + lag]
            }
            autocorr[lag] = sum
        }

        var maxLag = 0
        var maxVal = 0f
        for (lag in 20..step / 2) {
            if (autocorr[lag] > maxVal) {
                maxVal = autocorr[lag]
                maxLag = lag
            }
        }

        if (maxLag == 0) return 0

        val freq = sampleRate.toFloat() / maxLag
        val bpm = freq * 60f

        return bpm.roundToInt()
    }

    // -------- NOTE NAME --------
    fun pitchToNoteName(freq: Float): String {
        if (freq <= 0) return "--"
        val noteNames = arrayOf(
            "C", "C#", "D", "D#", "E", "F",
            "F#", "G", "G#", "A", "A#", "B"
        )
        val midi = (69 + 12 * ln(freq / 440f) / ln(2.0)).roundToInt()
        return noteNames[(midi % 12 + 12) % 12]
    }
}

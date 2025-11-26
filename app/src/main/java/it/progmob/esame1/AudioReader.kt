package it.progmob.esame1

import android.media.*
import java.nio.ByteBuffer
import kotlin.math.min

object AudioReader {

    fun decodeToPCM(path: String): Pair<FloatArray, Int> {
        val extractor = MediaExtractor()
        extractor.setDataSource(path)

        // Trova track AUDIO
        var trackIndex = -1
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            if (format.getString(MediaFormat.KEY_MIME)?.startsWith("audio") == true) {
                trackIndex = i
                break
            }
        }
        if (trackIndex == -1) throw Exception("No audio track found")

        extractor.selectTrack(trackIndex)
        val format = extractor.getTrackFormat(trackIndex)

        val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val mime = format.getString(MediaFormat.KEY_MIME)!!

        val decoder = MediaCodec.createDecoderByType(mime)
        decoder.configure(format, null, null, 0)
        decoder.start()

        val output = ArrayList<Float>()

        val info = MediaCodec.BufferInfo()

        var isEOS = false
        while (true) {

            if (!isEOS) {
                val inIndex = decoder.dequeueInputBuffer(10_000)
                if (inIndex >= 0) {
                    val inputBuffer = decoder.getInputBuffer(inIndex)!!
                    val sampleSize = extractor.readSampleData(inputBuffer, 0)
                    if (sampleSize < 0) {
                        decoder.queueInputBuffer(
                            inIndex, 0, 0, 0,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        )
                        isEOS = true
                    } else {
                        decoder.queueInputBuffer(
                            inIndex, 0, sampleSize, extractor.sampleTime, 0
                        )
                        extractor.advance()
                    }
                }
            }

            val outIndex = decoder.dequeueOutputBuffer(info, 10_000)
            if (outIndex >= 0) {
                val outBuffer = decoder.getOutputBuffer(outIndex)!!
                val chunk = FloatArray(info.size / 2)

                outBuffer.order(java.nio.ByteOrder.LITTLE_ENDIAN)
                for (i in chunk.indices) {
                    chunk[i] = outBuffer.short.toFloat() / Short.MAX_VALUE
                }
                output.addAll(chunk.toList())

                decoder.releaseOutputBuffer(outIndex, false)

                if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) break
            }
        }

        decoder.stop()
        decoder.release()
        extractor.release()

        return Pair(output.toFloatArray(), sampleRate)
    }
}

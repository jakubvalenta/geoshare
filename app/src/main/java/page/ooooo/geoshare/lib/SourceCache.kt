package page.ooooo.geoshare.lib

import kotlinx.io.Buffer
import kotlinx.io.RawSource
import kotlinx.io.buffered

class SourceCache(private val source: RawSource) {

    /**
     * Reads from [source] while copying all bytes that have been read to [cache].
     */
    class Writer(private val source: RawSource, private val cache: Buffer) : RawSource {
        override fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
            val bytesRead = source.readAtMostTo(cache, byteCount)
            if (bytesRead == -1L) {
                return -1L
            }
            cache.copyTo(sink, cache.size - bytesRead, cache.size)
            return bytesRead
        }

        override fun close() = source.close()
    }

    /**
     * Reads from [cache] until it's exhausted, then continues reading from [source].
     */
    class Reader(private val source: RawSource, private val cache: Buffer) : RawSource {
        override fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
            val bytesRead = cache.readAtMostTo(sink, byteCount)
            if (bytesRead == -1L) {
                return source.readAtMostTo(sink, byteCount)
            }
            return bytesRead
        }

        override fun close() = source.close()
    }

    private val cache: Buffer = Buffer()

    val writer get() = Writer(source, cache).buffered()

    val reader get() = Reader(source, cache).buffered()
}

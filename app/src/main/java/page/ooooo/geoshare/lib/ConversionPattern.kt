package page.ooooo.geoshare.lib

import kotlin.collections.flatten
import com.google.re2j.Pattern as Pattern_

abstract class ConversionPattern<I, M> {

    companion object {
        const val MAX_COORD_PRECISION = 17
        const val LAT_NUM = """-?\d{1,2}(\.\d{1,$MAX_COORD_PRECISION})?"""
        const val LON_NUM = """-?\d{1,3}(\.\d{1,$MAX_COORD_PRECISION})?"""
        const val LAT = """[\+ ]?(?P<lat>$LAT_NUM)"""
        const val LON = """[\+ ]?(?P<lon>$LON_NUM)"""
        const val Z = """(?P<z>\d{1,2}(\.\d{1,$MAX_COORD_PRECISION})?)"""
        const val Q_PARAM = """(?P<q>.+)"""
        const val Q_PATH = """(?P<q>[^/]+)"""

        val LAT_PATTERN: Pattern_ = Pattern_.compile(LAT)
        val LON_PATTERN: Pattern_ = Pattern_.compile(LON)
        val LAT_LON_PATTERN: Pattern_ = Pattern_.compile("$LAT,$LON")
        val LON_LAT_PATTERN: Pattern_ = Pattern_.compile("$LON,$LAT")
        val Z_PATTERN: Pattern_ = Pattern_.compile(Z)
        val Q_PARAM_PATTERN: Pattern_ = Pattern_.compile(Q_PARAM)

        fun <I, M> first(init: First<I, M>.() -> Unit): First<I, M> {
            val conversionPattern = First<I, M>()
            conversionPattern.init()
            return conversionPattern
        }
    }

    abstract fun match(input: I): List<M>?

    class Pattern<I, M>(val block: I.() -> M?) : ConversionPattern<I, M>() {
        override fun match(input: I): List<M>? = input.block()?.let { listOf(it) }
    }

    class ListPattern<I, M>(val block: I.() -> List<M>) : ConversionPattern<I, M>() {
        override fun match(input: I): List<M>? = input.block().takeIf { it.isNotEmpty() }
    }

    abstract class Group<I, M> : ConversionPattern<I, M>() {
        val children: MutableList<ConversionPattern<I, M>> = mutableListOf()

        fun all(init: All<I, M>.() -> Unit) = initMatcher(All(), init)

        fun first(init: First<I, M>.() -> Unit) = initMatcher(First(), init)

        fun pattern(block: I.() -> M?) = initMatcher(Pattern(block))

        fun listPattern(block: I.() -> List<M>) = initMatcher(ListPattern(block))

        fun optional(init: Optional<I, M>.() -> Unit) = initMatcher(Optional(), init)

        private fun <P : ConversionPattern<I, M>> initMatcher(conversionPattern: P, init: P.() -> Unit = {}): P {
            conversionPattern.init()
            children.add(conversionPattern)
            return conversionPattern
        }
    }

    class Optional<I, M> : Group<I, M>() {
        override fun match(input: I): List<M> = children.mapNotNull { it.match(input) }.flatten()
    }

    class All<I, M> : Group<I, M>() {
        override fun match(input: I): List<M>? =
            children.mapNotNull { it.match(input) }.takeIf { it.size == children.size }?.flatten()
    }

    class First<I, M> : Group<I, M>() {
        override fun match(input: I): List<M>? = children.firstNotNullOfOrNull { it.match(input) }
    }
}

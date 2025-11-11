package page.ooooo.geoshare.lib

import com.google.re2j.Matcher

interface ConversionPattern<I, M> {
    fun matches(input: I): List<M>?
}

class ConversionInputPattern<I, M>(val block: I.() -> M?) : ConversionPattern<I, M> {
    override fun matches(input: I): List<M>? = input.block()?.let { listOf(it) }
}

class ConversionInputEachPattern<I, M>(
    val condition: I.() -> Sequence<Matcher>,
    val result: (matcher: Matcher) -> M,
) : ConversionPattern<I, M> {
    class Builder<I, M>(
        val condition: I.() -> Sequence<Matcher>,
        val block: (conversionUriPattern: ConversionInputEachPattern<I, M>) -> Unit,
    ) {
        infix fun doReturn(result: (matcher: Matcher) -> M) = block(ConversionInputEachPattern(condition, result))
    }

    override fun matches(input: I): List<M>? =
        input.condition().map(result).toList().takeIf { it.isNotEmpty() }?.reversed()
}

abstract class ConversionGroupPattern<I, M> : ConversionPattern<I, M> {
    val children: MutableList<ConversionPattern<I, M>> = mutableListOf()

    fun all(init: ConversionAllPattern<I, M>.() -> Unit) = initMatcher(ConversionAllPattern(), init)

    fun first(init: ConversionFirstPattern<I, M>.() -> Unit) = initMatcher(ConversionFirstPattern(), init)

    fun on(condition: I.() -> M?) = ConversionInputPattern.Builder(condition) { initMatcher(it) }

    fun onEach(condition: I.() -> Sequence<Matcher>) =
        ConversionInputEachPattern.Builder(condition) { initMatcher(it) }

    fun optional(init: ConversionOptionalPattern<I, M>.() -> Unit) = initMatcher(ConversionOptionalPattern(), init)

    private fun <P : ConversionPattern<I, M>> initMatcher(conversionPattern: P, init: P.() -> Unit = {}): P {
        conversionPattern.init()
        children.add(conversionPattern)
        return conversionPattern
    }
}

class ConversionOptionalPattern<I, M> : ConversionGroupPattern<I, M>() {
    override fun matches(input: I): List<M> = children.mapNotNull { it.matches(input) }.flatten()
}

class ConversionAllPattern<I, M> : ConversionGroupPattern<I, M>() {
    override fun matches(input: I): List<M>? =
        children.mapNotNull { it.matches(input) }.takeIf { it.size == children.size }?.flatten()
}

class ConversionFirstPattern<I, M> : ConversionGroupPattern<I, M>() {
    override fun matches(input: I): List<M>? = children.firstNotNullOfOrNull { it.matches(input) }
}

fun <I, M> conversionPattern(init: ConversionFirstPattern<I, M>.() -> Unit): ConversionFirstPattern<I, M> {
    val conversionPattern = ConversionFirstPattern<I, M>()
    conversionPattern.init()
    return conversionPattern
}

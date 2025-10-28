package page.ooooo.geoshare.lib

interface ConversionUriPattern<M> {
    fun matches(uri: Uri): List<M>?
}

class ConversionHostUriPattern<M, R : ConversionRegex<M>>(val conversionRegex: R) : ConversionUriPattern<M> {
    override fun matches(uri: Uri): List<M>? = conversionRegex.matches(uri.host)?.let { listOf(it) }
}

class ConversionPathUriPattern<M, R : ConversionRegex<M>>(val conversionRegex: R) : ConversionUriPattern<M> {
    override fun matches(uri: Uri): List<M>? = conversionRegex.matches(uri.path)?.let { listOf(it) }
}

class ConversionQueryParamUriPattern<M, R : ConversionRegex<M>>(
    val name: String,
    val conversionRegex: R,
) : ConversionUriPattern<M> {

    override fun matches(uri: Uri): List<M>? =
        uri.queryParams[name]?.let { value -> conversionRegex.matches(value) }?.let { listOf(it) }
}

class ConversionFragmentUriPattern<M, R : ConversionRegex<M>>(val conversionRegex: R) : ConversionUriPattern<M> {
    override fun matches(uri: Uri): List<M>? = conversionRegex.matches(uri.fragment)?.let { listOf(it) }
}

abstract class ConversionGroupUriPattern<M, R : ConversionRegex<M>> : ConversionUriPattern<M> {
    val children: MutableList<ConversionUriPattern<M>> = mutableListOf()

    fun all(init: ConversionAllUriPattern<M, R>.() -> Unit) = initMatcher(ConversionAllUriPattern(), init)

    fun first(init: ConversionFirstUriPattern<M, R>.() -> Unit) = initMatcher(ConversionFirstUriPattern(), init)

    fun optional(init: ConversionOptionalUriPattern<M, R>.() -> Unit) =
        initMatcher(ConversionOptionalUriPattern(), init)

    fun host(conversionRegex: R) = initMatcher(ConversionHostUriPattern(conversionRegex))

    fun path(conversionRegex: R) = initMatcher(ConversionPathUriPattern(conversionRegex))

    fun query(name: String, conversionRegex: R) = initMatcher(ConversionQueryParamUriPattern(name, conversionRegex))

    fun fragment(conversionRegex: R) = initMatcher(ConversionFragmentUriPattern(conversionRegex))

    private fun <P : ConversionUriPattern<M>> initMatcher(conversionPattern: P, init: P.() -> Unit = {}): P {
        conversionPattern.init()
        children.add(conversionPattern)
        return conversionPattern
    }
}

class ConversionOptionalUriPattern<M, R : ConversionRegex<M>> : ConversionGroupUriPattern<M, R>() {
    override fun matches(uri: Uri): List<M> = children.mapNotNull { it.matches(uri) }.flatten()
}

class ConversionAllUriPattern<M, R : ConversionRegex<M>> : ConversionGroupUriPattern<M, R>() {
    override fun matches(uri: Uri): List<M>? =
        children.mapNotNull { it.matches(uri) }.takeIf { it.size == children.size }?.flatten()
}

class ConversionFirstUriPattern<M, R : ConversionRegex<M>> : ConversionGroupUriPattern<M, R>() {
    override fun matches(uri: Uri): List<M>? = children.firstNotNullOfOrNull { it.matches(uri) }
}

fun <M, R : ConversionRegex<M>> uriPattern(init: ConversionFirstUriPattern<M, R>.() -> Unit): ConversionFirstUriPattern<M, R> {
    val conversionPattern = ConversionFirstUriPattern<M, R>()
    conversionPattern.init()
    return conversionPattern
}

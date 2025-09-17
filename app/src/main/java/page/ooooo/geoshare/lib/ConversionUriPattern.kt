package page.ooooo.geoshare.lib

interface ConversionUriPattern<T : ConversionRegex> {
    fun matches(uri: Uri): List<T>?
}

class ConversionHostUriPattern<T : ConversionRegex>(val conversionRegex: T) : ConversionUriPattern<T> {
    override fun matches(uri: Uri): List<T>? =
        conversionRegex.takeIf { it.matches(uri.host) }?.let { listOf(it) }
}

class ConversionPathUriPattern<T : ConversionRegex>(val conversionRegex: T) : ConversionUriPattern<T> {
    override fun matches(uri: Uri): List<T>? =
        conversionRegex.takeIf { it.matches(uri.path) }?.let { listOf(it) }
}

class ConversionQueryParamUriPattern<T : ConversionRegex>(
    val name: String,
    val conversionRegex: T,
) : ConversionUriPattern<T> {
    override fun matches(uri: Uri): List<T>? =
        uri.queryParams[name]?.let { value ->
            conversionRegex.takeIf { it.matches(value) }
        }?.let { listOf(it) }
}

class ConversionFragmentUriPattern<T : ConversionRegex>(val conversionRegex: T) : ConversionUriPattern<T> {
    override fun matches(uri: Uri): List<T>? =
        conversionRegex.takeIf { it.matches(uri.fragment) }?.let { listOf(it) }
}

abstract class ConversionGroupUriPattern<T : ConversionRegex> : ConversionUriPattern<T> {
    val children: MutableList<ConversionUriPattern<T>> = mutableListOf()

    fun all(init: ConversionAllUriPattern<T>.() -> Unit) =
        initMatcher(ConversionAllUriPattern(), init)

    fun first(init: ConversionFirstUriPattern<T>.() -> Unit) =
        initMatcher(ConversionFirstUriPattern(), init)

    fun optional(init: ConversionOptionalUriPattern<T>.() -> Unit) =
        initMatcher(ConversionOptionalUriPattern(), init)

    fun host(conversionRegex: T) =
        initMatcher(ConversionHostUriPattern(conversionRegex))

    fun path(conversionRegex: T) =
        initMatcher(ConversionPathUriPattern(conversionRegex))

    fun query(name: String, conversionRegex: T) =
        initMatcher(ConversionQueryParamUriPattern(name, conversionRegex))

    fun fragment(conversionRegex: T) =
        initMatcher(ConversionFragmentUriPattern(conversionRegex))

    private fun <U : ConversionUriPattern<T>> initMatcher(conversionPattern: U, init: U.() -> Unit = {}): U {
        conversionPattern.init()
        children.add(conversionPattern)
        return conversionPattern
    }
}

class ConversionOptionalUriPattern<T : ConversionRegex> : ConversionGroupUriPattern<T>() {
    override fun matches(uri: Uri): List<T> =
        children.mapNotNull { it.matches(uri) }.flatten()
}

class ConversionAllUriPattern<T : ConversionRegex> : ConversionGroupUriPattern<T>() {
    override fun matches(uri: Uri): List<T>? =
        children.mapNotNull { it.matches(uri) }.takeIf { it.size == children.size }?.flatten()
}

class ConversionFirstUriPattern<T : ConversionRegex> : ConversionGroupUriPattern<T>() {
    override fun matches(uri: Uri): List<T>? =
        children.firstNotNullOfOrNull { it.matches(uri) }
}

fun <T : ConversionRegex> uriPattern(init: ConversionFirstUriPattern<T>.() -> Unit): ConversionFirstUriPattern<T> {
    val conversionPattern = ConversionFirstUriPattern<T>()
    conversionPattern.init()
    return conversionPattern
}

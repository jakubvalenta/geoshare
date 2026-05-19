package page.ooooo.geoshare.lib.inputs

import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.extensions.groupOrNull
import page.ooooo.geoshare.lib.extensions.matchEntire
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleMapsHtmlInputImpl @Inject constructor(
    private val googleMapsAddressApiInput: dagger.Lazy<GoogleMapsAddressApiInput>,
    private val googleMapsPlaceApiInput: dagger.Lazy<GoogleMapsPlaceApiInput>,
    private val uriQuote: UriQuote,
) : GoogleMapsHtmlInput<Uri>, BasicInput<Uri> {

    override suspend fun fetch(match: String, block: suspend (Uri) -> ParseResult) =
        block(Uri.parse(match, uriQuote))

    override suspend fun parse(data: Uri, match: String, prevResult: ParseResult?) = buildParseResult {
        data.run {
            // API directions
            // https://www.google.com/maps/dir/?origin={name}&destination={name}
            // API search
            // https://maps.google.com/?q={name}
            listOf(
                "destination",
                @Suppress("SpellCheckingInspection") "daddr",
                "q",
                "query",
            ).forEach { key ->
                Q_PARAM_PATTERN.matchEntire(queryParams[key])?.groupOrNull()?.let {
                    nextStep = NextStep(googleMapsAddressApiInput.get(), it)
                    return@buildParseResult
                }
            }

            val parts = pathParts.dropWhile { it.isEmpty() || it == "maps" }
            val firstPart = parts.firstOrNull()
            when (firstPart) {
                // Directions
                // https://www.google.com/maps/place/{point}/{point}/@{centerX},{centerY},{centerZ}
                "dir" ->
                    // Take as query the last path part that isn't a map center or data parameter
                    parts.drop(1).lastOrNull { !it.startsWith('@') && !it.startsWith("data=") }?.let {
                        nextStep = NextStep(googleMapsAddressApiInput.get(), it)
                        return@buildParseResult
                    }

                // Place
                // https://www.google.com/maps/place/{name}/@{centerX},{centerY},{centerZ}
                "place" ->
                    // TODO Try to get place id
                    // Take as query the second path part
                    parts.getOrNull(1)?.let {
                        nextStep = NextStep(googleMapsAddressApiInput.get(), it)
                        return@buildParseResult
                    }

                // Place list
                // https://www.google.com/maps/placelists/list/{id}
                // https://www.google.com/maps/@/data=!3m1!4b1!...!2s{id}
                // https://www.google.com/maps/d/edit?mid={id}
                // https://www.google.com/maps/d/view?mid={id}
                "placelists", "@", "d" -> {
                    // TODO Show an error that place lists are not supported
                }

                // Search
                // https://www.google.com/maps/search/{query}
                "search" ->
                    // Take as query the second path part
                    parts.getOrNull(1)?.let {
                        nextStep = NextStep(googleMapsAddressApiInput.get(), it)
                        return@buildParseResult
                    }
            }
        }
    }

    override fun toString() = "GoogleMapsHtmlInput"
}

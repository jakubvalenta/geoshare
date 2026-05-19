package page.ooooo.geoshare.lib.inputs

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleMapsPlaceListInput @Inject constructor() : BasicInput<String> {
    override suspend fun fetch(
        match: String,
        block: suspend (String) -> ParseResult,
    ) = block(match)

    override suspend fun parse(
        data: String,
        match: String,
        prevResult: ParseResult?,
    ) = buildParseResult {
        // TODO Return specific error that place lists are not supported
    }
}

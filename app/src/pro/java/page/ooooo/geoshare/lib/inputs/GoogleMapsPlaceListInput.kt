package page.ooooo.geoshare.lib.inputs

import io.ktor.client.engine.HttpClientEngine
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.UriQuote
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class GoogleMapsPlaceListInput @Inject constructor() : BasicInput<String> {
    override suspend fun fetch(
        match: String,
        engine: HttpClientEngine,
        log: Log,
        uriQuote: UriQuote,
        coroutineContext: CoroutineContext,
        block: suspend (String) -> ParseResult,
    ) = block(match)

    override suspend fun parse(
        data: String,
        match: String,
        prevResult: ParseResult?,
        uriQuote: UriQuote,
        log: Log,
    ) = buildParseResult {
        // TODO Return specific error that place lists are not supported
    }
}

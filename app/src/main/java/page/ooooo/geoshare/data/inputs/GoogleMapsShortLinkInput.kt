package page.ooooo.geoshare.data.inputs

import android.content.res.Resources
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.cookies.ConstantCookiesStorage
import io.ktor.http.Cookie
import kotlinx.collections.immutable.persistentListOf
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.geo.Point
import page.ooooo.geoshare.data.log.Log
import page.ooooo.geoshare.data.network.DESKTOP_USER_AGENT
import page.ooooo.geoshare.data.network.FetchTools
import page.ooooo.geoshare.data.uri.UriQuote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleMapsShortLinkInput @Inject constructor(
    private val googleMapsUriInput: dagger.Lazy<GoogleMapsUriInput>,
    private val engine: HttpClientEngine,
    private val log: Log,
    private val uriQuote: UriQuote,
) : BasicOnlineInput, Input.HasPattern, Input.HasRandomUri {
    override fun getName(resources: Resources) = resources.getString(R.string.input_google_maps_short_link_name)
    override val group = InputGroup.GOOGLE_MAPS
    override val changelog = persistentListOf(
        InputChangelogItem.Url(10, "https://g.co/kgs"),
        InputChangelogItem.Url(5, "https://app.goo.gl/maps"),
        InputChangelogItem.Url(5, "https://goo.gl/maps"),
        InputChangelogItem.Url(5, "https://maps.app.goo.gl"),
    )

    override val pattern = Regex("""((?:https?://)?(?:(?:maps\.)?(?:app\.)?goo\.gl|g\.co)/[/A-Za-z0-9_-]+)""")

    override suspend fun parse(match: String, resources: Resources, fetchTools: FetchTools) =
        fetchTools.headLocationHeader(
            match,
            engine,
            log,
            uriQuote,
            cookies = COOKIES,
            userAgent = USER_AGENT,
        ).run {
            parseResult {
                // Google Maps Go
                // https://maps.app.goo.gl/?link={url}
                queryParams["link"]?.firstOrNull()?.takeIf { it.isNotEmpty() }?.let {
                    next = MatchedInput(googleMapsUriInput.get(), it)
                    return@parseResult
                }

                next = MatchedInput(googleMapsUriInput.get(), this@run.toString())
            }
        }

    override fun genRandomUri(point: Point) = "https://maps.app.goo.gl/Q6ZugPBVWvuiVb8e8"

    override fun toString() = "GoogleMapsShortLinkInput"

    companion object {
        // Bypass consent page https://stackoverflow.com/a/78115353
        val COOKIES = ConstantCookiesStorage(
            Cookie(
                name = "CONSENT",
                value = "PENDING+987",
                domain = "www.google.com",
            ),
            Cookie(
                name = "SOCS",
                value = "CAESHAgBEhJnd3NfMjAyMzA4MTAtMF9SQzIaAmRlIAEaBgiAo_CmBg",
                domain = "www.google.com",
            ),
        )

        // Set custom User-Agent, so that we don't receive Google Lite HTML, which doesn't contain coordinates in
        // case of Google Maps or maps link in case of Google Search.
        const val USER_AGENT = DESKTOP_USER_AGENT
    }
}

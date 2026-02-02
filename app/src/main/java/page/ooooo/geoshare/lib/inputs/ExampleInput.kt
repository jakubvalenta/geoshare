package page.ooooo.geoshare.lib.inputs

import androidx.annotation.StringRes
import kotlinx.collections.immutable.ImmutableList
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.ILog
import page.ooooo.geoshare.lib.Uri
import page.ooooo.geoshare.lib.point.Point

// TODO Enable example input only in debug mode
object ExampleInput : Input.HasWeb {
    override val uriPattern = Regex("""https?://(?:www\.)?example\.com/\S+""")
    override val documentation = InputDocumentation(
        id = InputDocumentationId.EXAMPLE,
        nameResId = R.string.converter_example_name,
        items = emptyList(),
    )

    override suspend fun parseUri(uri: Uri) = buildParseUriResult {
        webUriString = uri.toString()
    }

    override fun onUrlChange(
        urlString: String,
        pointsFromUri: ImmutableList<Point>,
        log: ILog,
    ): ParseWebResult? {
        return null
    }

    override fun shouldInterceptRequest(
        requestUrlString: String,
        log: ILog,
    ) = false

    @StringRes
    override val permissionTitleResId = R.string.converter_example_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_example_loading_indicator_title
}

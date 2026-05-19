package page.ooooo.geoshare.lib.inputs

import android.webkit.WebSettings
import androidx.annotation.StringRes
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.Log
import page.ooooo.geoshare.lib.geo.GCJ02MainlandChinaPoint
import page.ooooo.geoshare.lib.geo.Source
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleMapsPlaceListInput @Inject constructor(
    private val log: Log,
) : WebViewInput {

    @Serializable
    private data class ExtractedPoint(val lat: Double?, val lon: Double?)

    @StringRes
    override val permissionTitleResId = R.string.converter_google_maps_permission_title

    @StringRes
    override val loadingIndicatorTitleResId = R.string.converter_google_maps_loading_indicator_title

    /**
     * Parse APP_INITIALIZATION_STATE, which has this structure:
     *
     * ```
     * [
     *    { Uf: ..., Oa: ... },
     *    { Uf: ... },
     *    null,
     *    { Uf: [
     *        null,
     *        null,
     *        ...
     *        "...[null,[null,null,"",null,"",[null,null,52.50918,13.40685],..."
     *    ] },
     *    ...
     * ]
     * ```
     */
    // language=JavaScript
    override val unsafeExtractionJavascript = $$"""
        () => {
            function findPointsInAppInitState(obj) {
                const MAX_PRECISION = 17;
                const LAT_NUM = `-?\\d{1,2}(?:\\.\\d{1,${MAX_PRECISION}})?`;
                const LON_NUM = `-?\\d{1,3}(?:\\.\\d{1,${MAX_PRECISION}})?`;
                const LAT = `[+ ]?(${LAT_NUM})`;
                const LON = `[+ ]?(${LON_NUM})`;
                const regexp = new RegExp(`\\[(?:null,null,|null,\\[)${LAT},${LON}]`, 'g');
                if (Array.isArray(obj)) {
                    for (const level1 of obj) {
                        if (level1 !== null && typeof(level1) === 'object' && !Array.isArray(level1)) {
                            for (const prop in level1) {
                                if (Object.hasOwn(level1, prop)) {
                                    const level2 = level1[prop];
                                    if (Array.isArray(level2)) {
                                        for (const level3 of level2) {
                                            if (typeof level3 === 'string') {
                                                const matches = [...level3.matchAll(regexp)];
                                                if (matches.length) {
                                                    return matches.map(m => ({ lat: m[1], lon: m[2] }));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                return undefined;
            }
            const points = findPointsInAppInitState(window.APP_INITIALIZATION_STATE);
            if (points && points.length) {
                return JSON.stringify(points);
            }
            return undefined;
        }
    """.trimIndent()

    override suspend fun parse(data: String, match: String, prevResult: ParseResult?) = buildParseResult {
        val json = Json {
            explicitNulls = false
        }
        try {
            json.decodeFromString<List<ExtractedPoint>>(data)
        } catch (tr: IllegalArgumentException) {
            log.e(TAG, "Deserialization error", tr)
            null
        }?.run {
            points = map { GCJ02MainlandChinaPoint(it.lat, it.lon, source = Source.JAVASCRIPT) }
                .toImmutableList()
        }
    }

    override fun extendWebSettings(settings: WebSettings) =
        GoogleMapsWebViewInput.extendWebSettings(settings)

    override fun shouldInterceptRequest(requestUrlString: String) =
        GoogleMapsWebViewInput.shouldInterceptRequest(requestUrlString)

    override fun toString() = TAG

    private companion object {
        private const val TAG = "GoogleMapsPlaceListWebViewInput"
    }
}

package page.ooooo.geoshare.lib.converters

import android.content.pm.PackageManager
import page.ooooo.geoshare.R
import page.ooooo.geoshare.lib.IntentTools

data class UrlConverterDocumentation(val nameResId: Int, val inputs: List<UrlConverterDocumentationInput>)

sealed class UrlConverterDocumentationInput(val addedInVersionCode: Int) {
    class Text(val descriptionResId: Int, addedInVersionCode: Int) : UrlConverterDocumentationInput(addedInVersionCode)
    class Url(val urlString: String, addedInVersionCode: Int) : UrlConverterDocumentationInput(addedInVersionCode)
}

data class UrlConverterDocumentations(
    val documentations: List<UrlConverterDocumentation>,
    val defaultHandlersEnabled: Map<String, Boolean>,
)

sealed class UrlConverterDocumentationFilter(val titleResId: Int) {
    class All : UrlConverterDocumentationFilter(R.string.url_converters_filter_all)
    class Recent : UrlConverterDocumentationFilter(R.string.url_converters_filter_recent)
    class Enabled : UrlConverterDocumentationFilter(R.string.url_converters_default_handler_enabled)
    class Disabled : UrlConverterDocumentationFilter(R.string.url_converters_default_handler_disabled)
}

val urlConverterDocumentationFilters = listOf(
    UrlConverterDocumentationFilter.All(),
    UrlConverterDocumentationFilter.Recent(),
    UrlConverterDocumentationFilter.Enabled(),
    UrlConverterDocumentationFilter.Disabled(),
)

fun getUrlConverterDocumentations(
    urlConverters: List<UrlConverter>,
    filter: UrlConverterDocumentationFilter,
    intentTools: IntentTools,
    changelogShownForVersionCode: Int?,
    packageManager: PackageManager,
): UrlConverterDocumentations {
    val defaultHandlersEnabled = mutableMapOf<String, Boolean>()
    val filteredUrlConverterDocumentations = urlConverters.mapNotNull { urlConverter ->
        val filteredInputs = urlConverter.documentation.inputs.filter { input ->
            if (filter is UrlConverterDocumentationFilter.Recent && (changelogShownForVersionCode == null || input.addedInVersionCode > changelogShownForVersionCode)) {
                return@filter false
            }
            if (input is UrlConverterDocumentationInput.Url) {
                val defaultHandlerEnabled = intentTools.isDefaultHandlerEnabled(packageManager, input.urlString)
                if (filter is UrlConverterDocumentationFilter.Enabled) {
                    if (!defaultHandlerEnabled) {
                        return@filter false
                    }
                } else if (filter is UrlConverterDocumentationFilter.Disabled) {
                    if (defaultHandlerEnabled) {
                        return@filter false
                    }
                }
                defaultHandlersEnabled[input.urlString] = defaultHandlerEnabled
            } else if (filter is UrlConverterDocumentationFilter.Disabled) {
                return@filter false
            }
            true
        }
        if (filteredInputs.isEmpty()) {
            return@mapNotNull null
        }
        urlConverter.documentation.copy(inputs = filteredInputs)
    }
    return UrlConverterDocumentations(
        documentations = filteredUrlConverterDocumentations,
        defaultHandlersEnabled = defaultHandlersEnabled.toMap(),
    )
}

package page.ooooo.geoshare.data

import page.ooooo.geoshare.data.android.AppActivity
import page.ooooo.geoshare.data.android.FileActivity
import page.ooooo.geoshare.data.android.FileType
import page.ooooo.geoshare.data.android.MimeType
import page.ooooo.geoshare.data.android.PackageNames
import page.ooooo.geoshare.data.android.TextActivity
import page.ooooo.geoshare.data.android.UriActivity
import page.ooooo.geoshare.data.android.UriScheme
import page.ooooo.geoshare.data.android.isMessagingApp
import page.ooooo.geoshare.data.android.sorted
import page.ooooo.geoshare.data.database.Link
import page.ooooo.geoshare.data.geo.CoordinateConverter
import page.ooooo.geoshare.data.geo.Point
import page.ooooo.geoshare.data.geo.Points
import page.ooooo.geoshare.data.log.DefaultLog
import page.ooooo.geoshare.data.log.Log
import page.ooooo.geoshare.data.outputs.CopyCoordsDecOutput
import page.ooooo.geoshare.data.outputs.CopyCoordsDegMinSecOutput
import page.ooooo.geoshare.data.outputs.CopyGeoUriOutput
import page.ooooo.geoshare.data.outputs.CopyLinkUriOutput
import page.ooooo.geoshare.data.outputs.CopyNameOutput
import page.ooooo.geoshare.data.outputs.CopyStringOutput
import page.ooooo.geoshare.data.outputs.NoopOutput
import page.ooooo.geoshare.data.outputs.OpenDisplayCartesIGNUrlOutput
import page.ooooo.geoshare.data.outputs.OpenDisplayGeoUriOutput
import page.ooooo.geoshare.data.outputs.OpenDisplayMagicEarthUriOutput
import page.ooooo.geoshare.data.outputs.OpenNavigationGoogleUriOutput
import page.ooooo.geoshare.data.outputs.OpenNavigationMagicEarthUriOutput
import page.ooooo.geoshare.data.outputs.OpenPointsGpxOutput
import page.ooooo.geoshare.data.outputs.OpenRouteGpxOutput
import page.ooooo.geoshare.data.outputs.OpenRouteOnePointGpxOutput
import page.ooooo.geoshare.data.outputs.OpenStreetViewGoogleUriOutput
import page.ooooo.geoshare.data.outputs.OpenUnknownUriOutput
import page.ooooo.geoshare.data.outputs.Output
import page.ooooo.geoshare.data.outputs.PointOutput
import page.ooooo.geoshare.data.outputs.SavePointGpxOutput
import page.ooooo.geoshare.data.outputs.SavePointToContactOutput
import page.ooooo.geoshare.data.outputs.SavePointsGpxOutput
import page.ooooo.geoshare.data.outputs.SaveRouteGpxOutput
import page.ooooo.geoshare.data.outputs.SendPointOutput
import page.ooooo.geoshare.data.outputs.SendStringOutput
import page.ooooo.geoshare.data.outputs.ShareDisplayGeoUriOutput
import page.ooooo.geoshare.data.outputs.ShareLinkUriOutput
import page.ooooo.geoshare.data.outputs.ShareNavigationGoogleUriOutput
import page.ooooo.geoshare.data.outputs.SharePointsGpxOutput
import page.ooooo.geoshare.data.outputs.ShareRouteGpxOutput
import page.ooooo.geoshare.data.outputs.ShareStreetViewGoogleUriOutput
import page.ooooo.geoshare.data.preferences.ActivityAutomation
import page.ooooo.geoshare.data.preferences.BasicAutomation
import page.ooooo.geoshare.data.preferences.CopyCoordsDecAutomation
import page.ooooo.geoshare.data.preferences.CopyCoordsDegMinSecAutomation
import page.ooooo.geoshare.data.preferences.CopyGeoUriAutomation
import page.ooooo.geoshare.data.preferences.CopyLinkDisplayAppleMapsUriAutomation
import page.ooooo.geoshare.data.preferences.CopyLinkDisplayGoogleMapsUriAutomation
import page.ooooo.geoshare.data.preferences.CopyLinkDisplayMagicEarthUriAutomation
import page.ooooo.geoshare.data.preferences.CopyLinkNavigationAppleMapsUriAutomation
import page.ooooo.geoshare.data.preferences.CopyLinkNavigationGoogleUriAutomation
import page.ooooo.geoshare.data.preferences.CopyLinkNavigationMagicEarthUriAutomation
import page.ooooo.geoshare.data.preferences.CopyLinkStreetViewGoogleUriAutomation
import page.ooooo.geoshare.data.preferences.CopyLinkUriAutomation
import page.ooooo.geoshare.data.preferences.CopyNameAutomation
import page.ooooo.geoshare.data.preferences.LinkAutomation
import page.ooooo.geoshare.data.preferences.NoopAutomation
import page.ooooo.geoshare.data.preferences.OpenDisplayCartesIGNUrlAutomation
import page.ooooo.geoshare.data.preferences.OpenDisplayGeoUriAutomation
import page.ooooo.geoshare.data.preferences.OpenDisplayMagicEarthUriAutomation
import page.ooooo.geoshare.data.preferences.OpenNavigationGoogleUriAutomation
import page.ooooo.geoshare.data.preferences.OpenNavigationMagicEarthUriAutomation
import page.ooooo.geoshare.data.preferences.OpenPointsGpxAutomation
import page.ooooo.geoshare.data.preferences.OpenRouteGpxAutomation
import page.ooooo.geoshare.data.preferences.OpenRouteOnePointGpxAutomation
import page.ooooo.geoshare.data.preferences.OpenStreetViewGoogleUriAutomation
import page.ooooo.geoshare.data.preferences.SavePointGpxAutomation
import page.ooooo.geoshare.data.preferences.SavePointToContactAutomation
import page.ooooo.geoshare.data.preferences.SavePointsGpxAutomation
import page.ooooo.geoshare.data.preferences.SaveRouteGpxAutomation
import page.ooooo.geoshare.data.preferences.SendPointAutomation
import page.ooooo.geoshare.data.preferences.ShareDisplayGeoUriAutomation
import page.ooooo.geoshare.data.preferences.ShareLinkUriAutomation
import page.ooooo.geoshare.data.preferences.ShareNavigationGoogleUriAutomation
import page.ooooo.geoshare.data.preferences.SharePointsGpxAutomation
import page.ooooo.geoshare.data.preferences.ShareRouteGpxAutomation
import page.ooooo.geoshare.data.preferences.ShareStreetViewGoogleUriAutomation
import page.ooooo.geoshare.data.uri.DefaultUriQuote
import page.ooooo.geoshare.data.uri.UriQuote
import javax.inject.Inject
import javax.inject.Singleton

data class AppOutputs<T>(
    val packageName: String,
    val defaultOutput: T,
    val allOutputs: List<T>,
)

data class AppsOutputs(
    val mapAppsOutputs: List<AppOutputs<Output<*>>> = emptyList(),
    val messagingAppsOutputs: List<AppOutputs<Output<*>>> = emptyList(),
    val exampleAppOutputs: AppOutputs<Output<*>>? = null,
    val shareOutputs: ShareOutputs? = null,
)

data class LinkOutputs(
    val group: String,
    val defaultOutput: Output<Point>,
    val allOutputs: List<Output<Point>>,
)

data class ShareOutputs(
    val defaultOutput: Output<*>,
    val allOutputs: List<Output<*>>,
)

@Singleton
class OutputRepository @Inject constructor(
    private val coordinateConverter: CoordinateConverter,
    private val log: Log,
    private val uriQuote: UriQuote,
) {
    fun getPointCopyOutputs(links: List<Link>): List<Output<Point>> =
        listOf(
            CopyCoordsDecOutput(coordinateConverter, uriQuote),
            CopyCoordsDegMinSecOutput(coordinateConverter, uriQuote),
            CopyNameOutput(uriQuote),
            CopyGeoUriOutput(coordinateConverter, uriQuote),
            *links
                .filter { it.sheetEnabled }
                .groupBy { it.groupOrName }
                .toSortedMap()
                .values
                .flatten()
                .map { CopyLinkUriOutput(it, coordinateConverter, uriQuote) }
                .toTypedArray(),
        )

    fun getPointOpenOutputs(activities: List<AppActivity>, hiddenApps: Set<String>?): List<AppOutputs<Output<Point>>> =
        activities
            .groupBy { it.packageName }
            .filterKeys { packageName -> hiddenApps?.contains(packageName) != true }
            .values
            .mapNotNull { activities ->
                activities.firstNotNullOfOrNull { activity ->
                    activity
                        .toOutputs()
                        .firstNotNullOfOrNull { it as? PointOutput }
                        ?.let { defaultOutput ->
                            AppOutputs(
                                packageName = activity.packageName,
                                defaultOutput = defaultOutput,
                                allOutputs = emptyList(),
                            )
                        }
                }
            }

    val pointSaveOutputs: List<Output<Point>> by lazy {
        listOf(
            SavePointGpxOutput(coordinateConverter),
            SavePointToContactOutput(coordinateConverter),
        )
    }

    val pointsSaveOutputs: List<Output<Points>> by lazy {
        listOf(
            SaveRouteGpxOutput(coordinateConverter),
            SavePointsGpxOutput(coordinateConverter),
        )
    }

    val pointsShareOutputs: List<Output<Points>> by lazy {
        listOf(
            ShareRouteGpxOutput(coordinateConverter),
            SharePointsGpxOutput(coordinateConverter),
        )
    }

    fun getAppsOutputs(activities: List<AppActivity>, hiddenApps: Set<String>?): AppsOutputs =
        activities
            .partition { activity -> !activity.isMessagingApp() }
            .let { (mapActivities, messagingActivities) ->
                val mapAppsOutputs = mapActivities.toOutputsForApps(hiddenApps)
                AppsOutputs(
                    mapAppsOutputs = mapAppsOutputs,
                    messagingAppsOutputs = messagingActivities.toOutputsForApps(hiddenApps),
                    shareOutputs = shareOutputs,
                    exampleAppOutputs = PackageNames.examples.firstNotNullOfOrNull { packageName ->
                        mapAppsOutputs.firstOrNull { appOutputs -> appOutputs.packageName == packageName }
                    },
                )
            }

    val shareOutputs by lazy {
        ShareOutputs(
            defaultOutput = ShareDisplayGeoUriOutput(coordinateConverter, uriQuote),
            allOutputs = listOf(
                ShareDisplayGeoUriOutput(coordinateConverter, uriQuote),
                ShareNavigationGoogleUriOutput(coordinateConverter, uriQuote),
                ShareStreetViewGoogleUriOutput(coordinateConverter, uriQuote),
                ShareRouteGpxOutput(coordinateConverter),
                SharePointsGpxOutput(coordinateConverter),
                SavePointToContactOutput(coordinateConverter),
            ),
        )
    }

    fun getLinksOutputs(links: List<Link>): List<LinkOutputs> =
        links
            .filter { it.appEnabled }
            .groupBy { it.groupOrName }
            .toSortedMap()
            .mapNotNull { (group, links) ->
                listOf(
                    *links.map { ShareLinkUriOutput(it, coordinateConverter, uriQuote) }.toTypedArray(),
                    *links.map { CopyLinkUriOutput(it, coordinateConverter, uriQuote) }.toTypedArray(),
                ).let { all ->
                    all.firstOrNull()?.let { default ->
                        LinkOutputs(group = group, defaultOutput = default, allOutputs = all)
                    }
                }
            }

    fun getPointChipsOutputs(links: List<Link>): List<Output<Point>> =
        listOf(
            CopyGeoUriOutput(coordinateConverter, uriQuote),
            *links
                .filter { it.chipEnabled }
                .sortedBy { it.name }
                .map { CopyLinkUriOutput(it, coordinateConverter, uriQuote) }
                .toTypedArray(),
        )

    val pointsChipsOutputs: List<Output<Points>> by lazy {
        listOf(
            ShareRouteGpxOutput(coordinateConverter),
            SaveRouteGpxOutput(coordinateConverter),
            SavePointsGpxOutput(coordinateConverter),
        )
    }

    val sourceCopyOutput: Output<String> by lazy {
        CopyStringOutput
    }

    fun getSourceAppsOutputs(activities: List<AppActivity>): List<AppOutputs<Output<String>>> =
        activities.mapNotNull { activity ->
            when (activity) {
                // Don't show an item for a file activity, because we don't know how to create a file from the
                // source, which is a URI or a text
                is FileActivity -> null
                is TextActivity -> AppOutputs(
                    packageName = activity.packageName,
                    defaultOutput = SendStringOutput(activity),
                    allOutputs = emptyList(),
                )

                is UriActivity -> AppOutputs(
                    packageName = activity.packageName,
                    defaultOutput = OpenUnknownUriOutput(activity),
                    allOutputs = emptyList(),
                )
            }
        }

    private fun List<AppActivity>.toOutputsForApps(hiddenApps: Set<String>?): List<AppOutputs<Output<*>>> =
        groupBy { it.packageName }
            .filterKeys { packageName -> hiddenApps?.contains(packageName) != true }
            .mapNotNull { (packageName, activities) ->
                activities
                    .sorted()
                    .flatMap { activity -> activity.toOutputs() }
                    .let { all ->
                        all.firstOrNull()?.let { default ->
                            AppOutputs(packageName = packageName, defaultOutput = default, allOutputs = all)
                        }
                    }
            }

    private fun AppActivity.toOutputs(): List<Output<*>> =
        when (this) {
            is FileActivity ->
                when (fileType) {
                    FileType.GPX -> listOf(
                        OpenRouteGpxOutput(this, coordinateConverter, log),
                        OpenPointsGpxOutput(this, coordinateConverter, log),
                    )

                    FileType.GPX_ONE_POINT -> listOf(
                        OpenRouteOnePointGpxOutput(this, coordinateConverter, log),
                    )
                }

            is TextActivity -> listOf(
                SendPointOutput(this, coordinateConverter, uriQuote),
            )

            is UriActivity ->
                when (uriScheme) {
                    UriScheme.CARTES_IGN -> listOf(
                        OpenDisplayCartesIGNUrlOutput(this, coordinateConverter, uriQuote),
                    )

                    UriScheme.GEO -> listOf(
                        OpenDisplayGeoUriOutput(this, coordinateConverter, uriQuote),
                    )

                    UriScheme.GOOGLE_NAVIGATION -> listOf(
                        OpenNavigationGoogleUriOutput(this, coordinateConverter, uriQuote),
                    )

                    UriScheme.GOOGLE_STREET_VIEW -> listOf(
                        OpenStreetViewGoogleUriOutput(this, coordinateConverter, uriQuote),
                    )

                    UriScheme.MAGIC_EARTH -> listOf(
                        OpenDisplayMagicEarthUriOutput(this, coordinateConverter, uriQuote),
                        OpenNavigationMagicEarthUriOutput(this, coordinateConverter, uriQuote),
                    )

                    UriScheme.UNKNOWN -> listOf(
                        OpenUnknownUriOutput(this),
                    )
                }
        }
}

fun BasicAutomation.toOutput(
    coordinateConverter: CoordinateConverter,
    uriQuote: UriQuote = DefaultUriQuote,
): Output<*> =
    when (this) {
        is CopyCoordsDecAutomation ->
            CopyCoordsDecOutput(coordinateConverter, uriQuote)

        is CopyCoordsDegMinSecAutomation ->
            CopyCoordsDegMinSecOutput(coordinateConverter, uriQuote)

        is CopyGeoUriAutomation ->
            CopyGeoUriOutput(coordinateConverter, uriQuote)

        is CopyNameAutomation ->
            CopyNameOutput(uriQuote)

        is NoopAutomation ->
            NoopOutput

        is SavePointGpxAutomation ->
            SavePointGpxOutput(coordinateConverter)

        is SavePointsGpxAutomation ->
            SavePointsGpxOutput(coordinateConverter)

        is SaveRouteGpxAutomation ->
            SaveRouteGpxOutput(coordinateConverter)

        is SavePointToContactAutomation ->
            SavePointToContactOutput(coordinateConverter)

        is ShareDisplayGeoUriAutomation ->
            ShareDisplayGeoUriOutput(coordinateConverter, uriQuote)

        is ShareRouteGpxAutomation ->
            ShareRouteGpxOutput(coordinateConverter)

        is SharePointsGpxAutomation ->
            SharePointsGpxOutput(coordinateConverter)

        is ShareNavigationGoogleUriAutomation ->
            ShareNavigationGoogleUriOutput(coordinateConverter, uriQuote)

        is ShareStreetViewGoogleUriAutomation ->
            ShareStreetViewGoogleUriOutput(coordinateConverter, uriQuote)
    }

fun ActivityAutomation.toOutput(
    coordinateConverter: CoordinateConverter,
    log: Log = DefaultLog,
    uriQuote: UriQuote = DefaultUriQuote,
): Output<*>? =
    when (this) {
        is OpenDisplayGeoUriAutomation ->
            packageName?.let { packageName ->
                OpenDisplayGeoUriOutput(UriActivity(packageName, UriScheme.GEO), coordinateConverter, uriQuote)
            }

        is OpenDisplayCartesIGNUrlAutomation ->
            packageName?.let { packageName ->
                OpenDisplayCartesIGNUrlOutput(
                    UriActivity(packageName, UriScheme.CARTES_IGN),
                    coordinateConverter,
                    uriQuote,
                )
            }

        is OpenDisplayMagicEarthUriAutomation ->
            packageName?.let { packageName ->
                OpenDisplayMagicEarthUriOutput(
                    UriActivity(packageName, UriScheme.MAGIC_EARTH),
                    coordinateConverter,
                    uriQuote,
                )
            }

        is OpenNavigationGoogleUriAutomation ->
            packageName?.let { packageName ->
                OpenNavigationGoogleUriOutput(
                    UriActivity(packageName, UriScheme.GOOGLE_NAVIGATION),
                    coordinateConverter,
                    uriQuote,
                )
            }

        is OpenNavigationMagicEarthUriAutomation ->
            packageName?.let { packageName ->
                OpenNavigationMagicEarthUriOutput(
                    UriActivity(packageName, UriScheme.MAGIC_EARTH),
                    coordinateConverter,
                    uriQuote,
                )
            }

        is OpenStreetViewGoogleUriAutomation ->
            packageName?.let { packageName ->
                OpenStreetViewGoogleUriOutput(
                    UriActivity(packageName, UriScheme.GOOGLE_STREET_VIEW),
                    coordinateConverter,
                    uriQuote,
                )
            }

        is OpenPointsGpxAutomation ->
            packageName?.let { packageName ->
                OpenPointsGpxOutput(FileActivity(packageName, FileType.GPX), coordinateConverter, log)
            }

        is OpenRouteGpxAutomation ->
            packageName?.let { packageName ->
                OpenRouteGpxOutput(FileActivity(packageName, FileType.GPX), coordinateConverter, log)
            }

        is OpenRouteOnePointGpxAutomation ->
            packageName?.let { packageName ->
                OpenRouteOnePointGpxOutput(
                    FileActivity(packageName, FileType.GPX_ONE_POINT),
                    coordinateConverter,
                    log,
                )
            }

        is SendPointAutomation ->
            packageName?.let { packageName ->
                SendPointOutput(TextActivity(packageName, MimeType.TEXT_PLAIN), coordinateConverter, uriQuote)
            }
    }

@Suppress("DEPRECATION")
fun LinkAutomation.toOutput(
    coordinateConverter: CoordinateConverter,
    link: Link,
    uriQuote: UriQuote = DefaultUriQuote,
): Output<*>? =
    when (this) {
        is CopyLinkUriAutomation -> CopyLinkUriOutput(link, coordinateConverter, uriQuote)
        is CopyLinkDisplayAppleMapsUriAutomation -> CopyLinkUriOutput(link, coordinateConverter, uriQuote)
        is CopyLinkDisplayGoogleMapsUriAutomation -> CopyLinkUriOutput(link, coordinateConverter, uriQuote)
        is CopyLinkDisplayMagicEarthUriAutomation -> CopyLinkUriOutput(link, coordinateConverter, uriQuote)
        is CopyLinkNavigationAppleMapsUriAutomation -> CopyLinkUriOutput(link, coordinateConverter, uriQuote)
        is CopyLinkNavigationGoogleUriAutomation -> CopyLinkUriOutput(link, coordinateConverter, uriQuote)
        is CopyLinkNavigationMagicEarthUriAutomation -> CopyLinkUriOutput(link, coordinateConverter, uriQuote)
        is CopyLinkStreetViewGoogleUriAutomation -> CopyLinkUriOutput(link, coordinateConverter, uriQuote)
        is ShareLinkUriAutomation -> ShareLinkUriOutput(link, coordinateConverter, uriQuote)
    }

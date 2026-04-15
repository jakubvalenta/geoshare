package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.FakeGoogleMapsStreetViewLink
import page.ooooo.geoshare.data.local.database.Link
import page.ooooo.geoshare.data.local.database.LinkType
import page.ooooo.geoshare.lib.android.AndroidTools
import page.ooooo.geoshare.lib.android.AppDetails
import page.ooooo.geoshare.lib.formatters.UriFormatter
import page.ooooo.geoshare.lib.geo.Geometries
import page.ooooo.geoshare.lib.outputs.CopyLinkUriOutput
import page.ooooo.geoshare.lib.outputs.ShareLinkUriOutput
import page.ooooo.geoshare.lib.geo.CoordinateConverter
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.Srs
import page.ooooo.geoshare.lib.geo.WGS84Point
import page.ooooo.geoshare.ui.theme.AppTheme
import page.ooooo.geoshare.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkForm(
    appEnabled: Boolean,
    chipEnabled: Boolean,
    coordinateConverter: CoordinateConverter,
    coordsUriTemplate: String,
    group: String,
    name: String,
    nameUriTemplate: String,
    sheetEnabled: Boolean,
    srs: Srs,
    type: LinkType,
    onSaveForm: () -> Unit,
    onSetAppEnabled: (Boolean) -> Unit,
    onSetChipEnabled: (Boolean) -> Unit,
    onSetCoordsUriTemplate: (String) -> Unit,
    onSetGroup: (String) -> Unit,
    onSetName: (String) -> Unit,
    onSetNameUriTemplate: (String) -> Unit,
    onSetSheetEnabled: (Boolean) -> Unit,
    onSetSrs: (Srs) -> Unit,
    onSetType: (LinkType) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    initialExpanded: Boolean = false,
) {
    val context = LocalContext.current
    val spacing = LocalSpacing.current

    var expanded by retain { mutableStateOf(initialExpanded) }
    val appDetails: AppDetails = emptyMap()
    val isValid = remember(name, coordsUriTemplate) {
        name.isNotEmpty() && coordsUriTemplate.isNotEmpty()
    }
    val link = remember(
        appEnabled,
        chipEnabled,
        coordsUriTemplate,
        group,
        name,
        nameUriTemplate,
        sheetEnabled,
        srs,
        type,
    ) {
        Link(
            group = group,
            name = name,
            srs = srs,
            type = type,
            appEnabled = appEnabled,
            chipEnabled = chipEnabled,
            sheetEnabled = sheetEnabled,
            coordsUriTemplate = coordsUriTemplate,
            nameUriTemplate = nameUriTemplate,
        )
    }
    val copyOutput = remember(link) { CopyLinkUriOutput(link, coordinateConverter) }
    val shareOutput = remember(link) { ShareLinkUriOutput(link, coordinateConverter) }

    Column(modifier) {
        TextField(
            value = name,
            onValueChange = onSetName,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.windowPadding)
                .testTag("geoShareLinkFormName"),
            enabled = enabled,
            label = {
                Text(stringResource(R.string.links_form_name))
            },
            isError = name.isEmpty(),
            singleLine = true,
        )
        TextField(
            value = coordsUriTemplate,
            onValueChange = onSetCoordsUriTemplate,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.windowPadding)
                .padding(top = spacing.medium)
                .testTag("geoShareLinkFormCoordsUriTemplate"),
            enabled = enabled,
            label = {
                Text(stringResource(R.string.links_form_uri_template_coords))
            },
            supportingText = {
                Text(
                    buildAnnotatedString {
                        append(stringResource(R.string.example, ""))
                        append("https://maps.apple.com/?ll=")
                        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                            append("{lat}")
                        }
                        append("%2C")
                        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                            append("{lon}")
                        }
                        append("&z=")
                        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                            append("{z}")
                        }
                        append("&q=")
                        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                            append("{name}")
                        }
                    })
            },
            isError = coordsUriTemplate.isEmpty(),
        )
        listOf(
            WGS84Point(
                -3.075833,
                37.353333,
                source = Source.GENERATED,
            ) to stringResource(R.string.links_form_test_world),
            WGS84Point(
                39.915833,
                116.390833,
                source = Source.GENERATED,
            ) to stringResource(R.string.links_form_test_china),
        ).let { testPointsWithName ->
            Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
                ScrollableChips(PaddingValues(horizontal = LocalSpacing.current.windowPadding)) {
                    testPointsWithName.forEach { (point, name) ->
                        item {
                            SuggestionChip(
                                onClick = {
                                    UriFormatter.formatUriString(
                                        coordinateConverter.toSrs(point.copy(name = name), srs),
                                        coordsUriTemplate,
                                        nameUriTemplate,
                                    )?.let {
                                        AndroidTools.openWebUri(context, it)
                                    }
                                },
                                label = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(name)
                                        Icon(
                                            painterResource(R.drawable.arrow_outward_24px),
                                            contentDescription = null,
                                            Modifier.size(16.dp),
                                        )
                                    }
                                },
                                enabled = enabled,
                                contentPadding = PaddingValues(start = 8.dp, end = 2.dp),
                            )
                        }
                    }
                }
            }
        }
        Column(
            Modifier
                .padding(top = spacing.medium)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            verticalArrangement = Arrangement.spacedBy(spacing.tiny / 2)
        ) {
            LinkFormEnabledCheckbox(
                value = appEnabled,
                onCheckedChange = onSetAppEnabled,
                label = stringResource(R.string.links_form_enabled_option_app),
                enabled = enabled,
                testTag = "geoShareLinkFormAppEnabled",
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.extraSmall)
                        .padding(vertical = spacing.tiny)
                ) {
                    AppIcon(Modifier.width(100.dp), link.groupOrName, enabled = false) {
                        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.tertiaryContainer) {
                            IconFromDescriptor(
                                shareOutput.getIcon(appDetails),
                                contentDescription = null,
                                size = 46.dp,
                                inverseContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            )
                        }
                    }
                }
            }
            HorizontalDivider()
            LinkFormEnabledCheckbox(
                value = sheetEnabled,
                onCheckedChange = onSetSheetEnabled,
                label = stringResource(R.string.links_form_enabled_option_sheet),
                enabled = enabled,
                testTag = "geoShareLinkFormSheetEnabled",
            ) {
                Surface(shape = BottomSheetDefaults.ExpandedShape) {
                    Column {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            BottomSheetDefaults.DragHandle()
                        }
                        ResultSuccessSheetItem(
                            headlineText = copyOutput.label(appDetails),
                            supportingText = copyOutput.getDescription(WGS84Point.example),
                            icon = copyOutput.getIcon(appDetails),
                        )
                    }
                }
            }
            HorizontalDivider()
            LinkFormEnabledCheckbox(
                value = chipEnabled,
                onCheckedChange = onSetChipEnabled,
                label = stringResource(R.string.links_form_enabled_option_chip),
                enabled = enabled,
                testTag = "geoShareLinkFormChipEnabled",
            ) {
                Surface(
                    Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    // Row prevents the chip from filling the whole container width
                    Row(Modifier.padding(horizontal = spacing.small)) {
                        StyledChip(
                            label = copyOutput.label(appDetails),
                            modifier = Modifier.widthIn(min = 150.dp),
                            icon = {
                                IconFromDescriptor(
                                    link.icon,
                                    contentDescription = null,
                                )
                            },
                            onClick = { onSetChipEnabled(!chipEnabled) },
                        )
                    }
                }
            }
        }
        Row(
            modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.windowPadding, vertical = spacing.medium)
                .toggleable(
                    value = expanded,
                    onValueChange = { expanded = it },
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.links_form_advanced),
                style = MaterialTheme.typography.bodyLarge,
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
            )
        }
        AnimatedVisibility(expanded) {
            Column {
                TextField(
                    value = group,
                    onValueChange = onSetGroup,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.windowPadding),
                    enabled = enabled,
                    label = {
                        Text(stringResource(R.string.links_form_group))
                    },
                    singleLine = true,
                )
                DropdownField(
                    value = srs,
                    options = Srs.entries.associateWith { srs ->
                        when (srs) {
                            Srs.WGS84 -> stringResource(R.string.srs_wgs84)

                            Srs.GCJ02 -> stringResource(R.string.srs_gcj02)

                            Srs.GCJ02_MAINLAND_CHINA -> stringResource(
                                R.string.srs_description,
                                stringResource(R.string.srs_gcj02),
                                stringResource(R.string.srs_description_mainland_china),
                            )

                            Srs.GCJ02_GREATER_CHINA_AND_TAIWAN -> stringResource(
                                R.string.srs_description,
                                stringResource(R.string.srs_gcj02),
                                stringResource(R.string.srs_description_greater_china_and_taiwan),
                            )
                        }
                    },
                    onValueChange = onSetSrs,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.windowPadding)
                        .padding(top = spacing.medium),
                    enabled = enabled,
                    label = { Text(stringResource(R.string.links_form_srs)) },
                )
                TextField(
                    value = nameUriTemplate,
                    onValueChange = onSetNameUriTemplate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.windowPadding)
                        .padding(top = spacing.medium),
                    enabled = enabled,
                    label = {
                        Text(stringResource(R.string.links_form_uri_template_name))
                    },
                    supportingText = {
                        Text(
                            buildAnnotatedString {
                                append(stringResource(R.string.example, ""))
                                append("https://maps.apple.com/?q=")
                                withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                                    append("{q}")
                                }
                            })
                    },
                )
                Column(
                    Modifier
                        .padding(top = spacing.medium)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = spacing.windowPadding)
                        .padding(bottom = spacing.tiny),
                ) {
                    Text(
                        stringResource(R.string.links_form_type),
                        Modifier.padding(top = spacing.small, bottom = spacing.tiny),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    RadioButtonGroup(
                        selectedValue = type,
                        onSelect = onSetType,
                        values = listOf(LinkType.DISPLAY, LinkType.NAVIGATION, LinkType.STREET_VIEW),
                        enabled = enabled,
                    ) { value ->
                        Surface(
                            Modifier.fillMaxWidth(),
                            shape = ShapeDefaults.Large,
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        ) {
                            DropdownMenuItem(
                                text = { Text(shareOutput.label(appDetails)) },
                                onClick = { onSetType(value) },
                                leadingIcon = {
                                    IconFromDescriptor(
                                        ShareLinkUriOutput(link.copy(type = value), coordinateConverter)
                                            .getMenuIcon(appDetails),
                                        contentDescription = null,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
        HorizontalDivider()
        LargeButton(
            stringResource(R.string.links_form_save),
            Modifier
                .padding(top = spacing.medium)
                .testTag("geoShareLinkFormSave"),
            enabled = enabled && isValid,
        ) {
            onSaveForm()
        }
    }
}

@Composable
private fun LinkFormEnabledCheckbox(
    value: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    enabled: Boolean = true,
    testTag: String? = null,
    content: @Composable () -> Unit,
) {
    val spacing = LocalSpacing.current

    Row(
        Modifier
            .padding(spacing.small)
            .toggleable(value = value, role = Role.Checkbox, onValueChange = onCheckedChange),
        horizontalArrangement = Arrangement.spacedBy(spacing.small),
    ) {
        Checkbox(
            checked = value,
            onCheckedChange = null,
            modifier = Modifier.semantics {
                if (testTag != null) {
                    this.testTag = if (value) "${testTag}_checked" else "${testTag}_unchecked"
                }
            },
            enabled = enabled,
        )
        Column(verticalArrangement = Arrangement.spacedBy(spacing.tiny)) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
            )
            content()
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            LinkForm(
                appEnabled = false,
                chipEnabled = false,
                coordinateConverter = coordinateConverter,
                coordsUriTemplate = "",
                group = "",
                name = "",
                nameUriTemplate = "",
                sheetEnabled = false,
                srs = Srs.WGS84,
                type = LinkType.DISPLAY,
                onSaveForm = {},
                onSetAppEnabled = {},
                onSetChipEnabled = {},
                onSetCoordsUriTemplate = {},
                onSetGroup = {},
                onSetName = {},
                onSetNameUriTemplate = {},
                onSetSheetEnabled = {},
                onSetSrs = {},
                onSetType = {},
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            LinkForm(
                appEnabled = false,
                chipEnabled = false,
                coordinateConverter = coordinateConverter,
                coordsUriTemplate = "",
                group = "",
                name = "",
                nameUriTemplate = "",
                sheetEnabled = false,
                srs = Srs.WGS84,
                type = LinkType.DISPLAY,
                onSaveForm = {},
                onSetAppEnabled = {},
                onSetChipEnabled = {},
                onSetCoordsUriTemplate = {},
                onSetGroup = {},
                onSetName = {},
                onSetNameUriTemplate = {},
                onSetSheetEnabled = {},
                onSetSrs = {},
                onSetType = {},
            )
        }
    }
}

@Preview(showBackground = true, device = "spec:width=1080px,height=4000px,dpi=440")
@Composable
private fun UpdatePreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val link = FakeGoogleMapsStreetViewLink
            LinkForm(
                appEnabled = link.appEnabled,
                chipEnabled = link.chipEnabled,
                coordinateConverter = coordinateConverter,
                coordsUriTemplate = link.coordsUriTemplate,
                group = link.group,
                name = link.name,
                nameUriTemplate = link.nameUriTemplate,
                sheetEnabled = link.sheetEnabled,
                srs = link.srs,
                type = link.type,
                onSaveForm = {},
                onSetAppEnabled = {},
                onSetChipEnabled = {},
                onSetCoordsUriTemplate = {},
                onSetGroup = {},
                onSetName = {},
                onSetNameUriTemplate = {},
                onSetSheetEnabled = {},
                onSetSrs = {},
                onSetType = {},
            )
        }
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=1080px,height=4000px,dpi=440",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun DarkUpdatePreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val link = FakeGoogleMapsStreetViewLink
            LinkForm(
                appEnabled = link.appEnabled,
                chipEnabled = link.chipEnabled,
                coordinateConverter = coordinateConverter,
                coordsUriTemplate = link.coordsUriTemplate,
                group = link.group,
                name = link.name,
                nameUriTemplate = link.nameUriTemplate,
                sheetEnabled = link.sheetEnabled,
                srs = link.srs,
                type = link.type,
                onSaveForm = {},
                onSetAppEnabled = {},
                onSetChipEnabled = {},
                onSetCoordsUriTemplate = {},
                onSetGroup = {},
                onSetName = {},
                onSetNameUriTemplate = {},
                onSetSheetEnabled = {},
                onSetSrs = {},
                onSetType = {},
            )
        }
    }
}

@Preview(showBackground = true, device = "spec:width=1080px,height=4000px,dpi=440")
@Composable
private fun UpdateExpandedPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val link = FakeGoogleMapsStreetViewLink
            LinkForm(
                appEnabled = link.appEnabled,
                chipEnabled = link.chipEnabled,
                coordinateConverter = coordinateConverter,
                coordsUriTemplate = link.coordsUriTemplate,
                group = link.group,
                name = link.name,
                nameUriTemplate = link.nameUriTemplate,
                sheetEnabled = link.sheetEnabled,
                srs = link.srs,
                type = link.type,
                onSaveForm = {},
                onSetAppEnabled = {},
                onSetChipEnabled = {},
                onSetCoordsUriTemplate = {},
                onSetGroup = {},
                onSetName = {},
                onSetNameUriTemplate = {},
                onSetSheetEnabled = {},
                onSetSrs = {},
                onSetType = {},
                initialExpanded = true,
            )
        }
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=1080px,height=4000px,dpi=440",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun DarkUpdateExpandedPreview() {
    AppTheme {
        Surface {
            val context = LocalContext.current
            val geometries = Geometries(context)
            val coordinateConverter = CoordinateConverter(geometries)
            val link = FakeGoogleMapsStreetViewLink
            LinkForm(
                appEnabled = link.appEnabled,
                chipEnabled = link.chipEnabled,
                coordinateConverter = coordinateConverter,
                coordsUriTemplate = link.coordsUriTemplate,
                group = link.group,
                name = link.name,
                nameUriTemplate = link.nameUriTemplate,
                sheetEnabled = link.sheetEnabled,
                srs = link.srs,
                type = link.type,
                onSaveForm = {},
                onSetAppEnabled = {},
                onSetChipEnabled = {},
                onSetCoordsUriTemplate = {},
                onSetGroup = {},
                onSetName = {},
                onSetNameUriTemplate = {},
                onSetSheetEnabled = {},
                onSetSrs = {},
                onSetType = {},
                initialExpanded = true,
            )
        }
    }
}

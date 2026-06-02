package page.ooooo.geoshare.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.MutablePreferences
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.local.database.Server
import page.ooooo.geoshare.data.local.preferences.OptionsPreference
import page.ooooo.geoshare.data.local.preferences.TextPreference
import page.ooooo.geoshare.data.local.preferences.UserPreferencesValues
import page.ooooo.geoshare.ui.theme.LocalSpacing

@Composable
fun UserPreferenceControls(
    titleResId: Int,
    billingAppNameResId: Int,
    wide: Boolean,
    description: (@Composable () -> String)? = null,
    featureNotPurchased: Boolean = false,
    onBack: () -> Unit,
    onNavigateToBillingScreen: () -> Unit,
    content: LazyListScope.() -> Unit,
) {
    val spacing = LocalSpacing.current
    val maxWidth = 500.dp

    Box(Modifier.widthIn(max = maxWidth)) {
        ScrollablePane(
            title = {
                Text(stringResource(titleResId))
            },
            onBack = onBack.takeUnless { wide },
            modifier = Modifier
                .padding(horizontal = spacing.windowPadding)
                .testTag("geoShareUserPreferencesControlsPane"),
        ) {
            item {
                description?.let { description ->
                    ParagraphHtml(
                        description(),
                        Modifier
                            .padding(top = spacing.tiny, bottom = spacing.mediumAdaptive)
                            .run {
                                if (featureNotPurchased) {
                                    alpha(0.7f)
                                } else {
                                    this
                                }
                            },
                    )
                } ?: Spacer(Modifier.height(spacing.tiny))
            }
            content()
        }
        if (featureNotPurchased) {
            FeatureWall(
                billingAppNameResId = billingAppNameResId,
                modifier = Modifier.testTag("geoShareAutomationFeatureWall"),
                onNavigateToBillingScreen = onNavigateToBillingScreen,
            )
        }
    }
}

fun <T> LazyListScope.userPreferenceOptionsControl(
    userPreference: OptionsPreference<T>,
    values: UserPreferencesValues,
    optionGroups: List<List<T>>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    itemTestTag: ((option: T) -> String)? = null,
    onValueChange: ((MutablePreferences) -> Unit) -> Unit,
    option: @Composable RowScope.(option: T, modifier: Modifier) -> Unit,
) {
    val value = if (enabled) {
        userPreference.getValue(values)
    } else {
        userPreference.default
    }
    optionGroups.forEachIndexed { i, values ->
        item {
            RadioButtonGroup(
                selectedValue = value,
                onSelect = {
                    onValueChange { preferences ->
                        userPreference.setValue(preferences, it)
                    }
                },
                values = values,
                enabled = enabled,
                modifier = modifier.run {
                    if (i == 0) {
                        padding(top = LocalSpacing.current.tinyAdaptive)
                    } else {
                        this
                    }
                },
                itemTestTag = itemTestTag,
                option = option,
            )
        }
        if (i < optionGroups.size - 1) {
            item {
                HorizontalDivider(
                    modifier.padding(vertical = LocalSpacing.current.tinyAdaptive),
                    thickness = Dp.Hairline,
                )
            }
        }
    }
}

fun LazyListScope.userPreferenceServerControls(
    selected: Server?,
    servers: List<Server>,
    itemNoneDescription: @Composable () -> String,
    itemTestTag: ((item: Server?) -> String)? = null,
    onSelect: (Server?) -> Unit,
) {
    item {
        RadioButtonGroup(
            selectedValue = selected,
            onSelect = onSelect,
            values = listOf(null) + servers,
            modifier = Modifier.padding(top = LocalSpacing.current.tinyAdaptive),
            itemEnabled = { item -> item?.isValid() != false },
            itemTestTag = itemTestTag,
        ) { item, modifier ->
            Column(modifier.weight(1f)) {
                Text(item?.name ?: stringResource(R.string.server_none))
                if (item == null) {
                    Text(itemNoneDescription(), style = MaterialTheme.typography.bodySmall)
                } else if (item.description.isNotEmpty()) {
                    Text(item.description, style = MaterialTheme.typography.bodySmall)
                }
            }
            if (item?.isValid() == false) {
                Text(
                    stringResource(R.string.server_invalid),
                    modifier,
                    fontStyle = FontStyle.Italic,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

fun <T> LazyListScope.userPreferenceTextControl(
    userPreference: TextPreference<T>,
    values: UserPreferencesValues,
    onValueChange: ((MutablePreferences) -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    error: (@Composable () -> String)? = null,
    suffix: (@Composable () -> String)? = null,
) {
    item {
        val value = userPreference.getValue(values)
        val (inputValue, setInputValue) = remember { mutableStateOf(userPreference.serialize(value)) }
        val isValid = userPreference.isValid(inputValue)

        TextField(
            value = inputValue,
            onValueChange = {
                setInputValue(it)
                onValueChange { preferences ->
                    userPreference.setValue(preferences, userPreference.deserialize(it))
                }
            },
            modifier = modifier.fillMaxWidth(),
            enabled = enabled,
            suffix = suffix?.let { suffix ->
                {
                    Text(
                        suffix(),
                        color = if (isValid) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                    )
                }
            },
            trailingIcon = {
                IconButton({
                    setInputValue(userPreference.serialize(userPreference.default))
                    onValueChange { preferences ->
                        userPreference.setValue(preferences, userPreference.default)
                    }
                }) {
                    Icon(
                        Icons.Default.Refresh,
                        stringResource(R.string.reset),
                    )
                }
            },
            supportingText = if (!isValid && error != null) {
                {
                    Text(error())
                }
            } else {
                null
            },
            isError = !isValid,
            singleLine = true,
        )
    }
}

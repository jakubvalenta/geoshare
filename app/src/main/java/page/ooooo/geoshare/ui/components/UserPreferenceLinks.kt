package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.di.defaultFakeLinks
import page.ooooo.geoshare.data.local.database.Link
import page.ooooo.geoshare.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UserPreferenceLinksListItem(
    index: Int,
    count: Int,
    links: StateFlow<List<Link>>,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val links by links.collectAsStateWithLifecycle()

    SegmentedListItem(
        selected = selected,
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(index, count),
        modifier = modifier,
        supportingContent = links.takeIf { it.isNotEmpty() }?.let { links ->
            {
                Text(
                    links.filter { it.enabled }.map { it.groupOrName }.toSortedSet().joinToString(),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            }
        },
        colors = segmentedListColors(),
    ) {
        Text(
            stringResource(R.string.links_title),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ListItemPreview() {
    AppTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                UserPreferenceLinksListItem(
                    index = 0,
                    count = 1,
                    links = MutableStateFlow(defaultFakeLinks),
                    selected = false,
                    onClick = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkListItemPreview() {
    AppTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                UserPreferenceLinksListItem(
                    index = 0,
                    count = 1,
                    links = MutableStateFlow(defaultFakeLinks),
                    selected = false,
                    onClick = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyListItemPreview() {
    AppTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                UserPreferenceLinksListItem(
                    index = 0,
                    count = 1,
                    links = MutableStateFlow(emptyList()),
                    selected = false,
                    onClick = {},
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkEmptyListItemPreview() {
    AppTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                UserPreferenceLinksListItem(
                    index = 0,
                    count = 1,
                    links = MutableStateFlow(emptyList()),
                    selected = false,
                    onClick = {},
                )
            }
        }
    }
}

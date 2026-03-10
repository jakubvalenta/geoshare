package page.ooooo.geoshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import page.ooooo.geoshare.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CharIcon(
    char: Char?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    inverseContentColor: Color = MaterialTheme.colorScheme.surface,
    shape: Shape = MaterialShapes.Slanted.toShape(),
) {
    BoxWithConstraints(
        modifier.background(LocalContentColor.current, shape),
        contentAlignment = Alignment.Center,
    ) {
        char?.let { char ->
            Text(
                char.toString(),
                Modifier.semantics {
                    if (contentDescription != null) {
                        this.contentDescription = contentDescription
                    }
                    role = Role.Image
                },
                color = inverseContentColor,
                fontSize = with(LocalDensity.current) { (maxHeight * 0.6f).toSp() },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Surface {
            CharIcon('s', contentDescription = null, Modifier.size(24.dp))
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkPreview() {
    AppTheme {
        Surface {
            CharIcon('s', contentDescription = null, Modifier.size(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LargePreview() {
    AppTheme {
        Surface {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.tertiaryContainer) {
                CharIcon(
                    'L',
                    contentDescription = null,
                    Modifier.size(46.dp),
                    inverseContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkLargePreview() {
    AppTheme {
        Surface {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.tertiaryContainer) {
                CharIcon(
                    'L',
                    contentDescription = null,
                    Modifier.size(46.dp),
                    inverseContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyPreview() {
    AppTheme {
        Surface {
            CharIcon(null, contentDescription = null, Modifier.size(24.dp))
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DarkEmptyPreview() {
    AppTheme {
        Surface {
            CharIcon(null, contentDescription = null, Modifier.size(24.dp))
        }
    }
}

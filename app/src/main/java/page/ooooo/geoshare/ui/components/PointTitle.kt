package page.ooooo.geoshare.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import page.ooooo.geoshare.R
import page.ooooo.geoshare.data.geo.Point

@Composable
fun pointTitle(point: Point, pointIndex: Int, totalPoints: Int): String? =
    point.cleanName.let { cleanName ->
        if (!cleanName.isNullOrEmpty()) {
            cleanName
        } else if (totalPoints > 1) {
            if (pointIndex == totalPoints - 1) {
                stringResource(R.string.conversion_succeeded_point_last)
            } else {
                stringResource(R.string.conversion_succeeded_point_number, pointIndex + 1)
            }
        } else {
            null
        }
    }

package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.Point
import page.ooooo.geoshare.lib.Position
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.Action as Action_

// TODO Output feels too complicated
sealed interface Output {
    interface HasLabel : Output {
        @Composable
        fun label(): String
    }

    interface HasPositionText : Output {
        fun getText(position: Position, uriQuote: UriQuote = DefaultUriQuote()): String?
    }

    interface HasPointText : Output {
        fun getText(point: Point, uriQuote: UriQuote = DefaultUriQuote()): String?
    }

    interface HasPositionAction : Output {
        fun getAction(position: Position, uriQuote: UriQuote = DefaultUriQuote()): Action_
    }

    interface HasPointAction : Output {
        fun getAction(point: Point, uriQuote: UriQuote = DefaultUriQuote()): Action_
    }

    interface Action : HasLabel, HasPositionAction

    interface AppAction : HasLabel, HasPositionAction {
        val packageName: String
    }

    interface Chip : HasLabel, HasPositionAction

    interface OpenChooserAction : HasLabel, HasPositionAction

    interface SaveGpxAction : HasLabel, HasPositionAction {
        override fun getAction(position: Position, uriQuote: UriQuote): Action_.SaveGpx
    }

    interface PointText : HasPointText

    interface PointAction : HasLabel, HasPointAction

    interface SupportingText : HasPositionText

    interface Text : HasPositionText
}

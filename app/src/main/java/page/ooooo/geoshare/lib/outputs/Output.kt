package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.IntentTools
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.outputs.Action as Action_

sealed interface Output<T> {

    interface Action<T, U : Action_> {
        fun getAction(value: T, uriQuote: UriQuote = DefaultUriQuote()): U

        @Composable
        fun label(): String
    }

    interface App<T> : Output<T> {
        val packageName: String

        fun getAction(value: T, uriQuote: UriQuote = DefaultUriQuote()): Action_.OpenApp

        @Composable
        fun label(app: IntentTools.App): String
    }

    interface Text<T> : Output<T> {
        fun getText(value: T, uriQuote: UriQuote = DefaultUriQuote()): String
    }

    interface PointLabel<T> : Output<T> {
        @Composable
        fun getText(value: T, i: Int, pointCount: Int, uriQuote: UriQuote = DefaultUriQuote()): String?
    }
}

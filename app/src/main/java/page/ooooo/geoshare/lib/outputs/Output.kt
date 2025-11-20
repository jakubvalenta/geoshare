package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.AndroidTools
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.outputs.Action as Action_

sealed interface Output<T> {

    interface Action<T, U : Action_> {
        fun getAction(value: T, uriQuote: UriQuote = DefaultUriQuote()): U

        @Composable
        fun label(): String

        fun isEnabled(value: T): Boolean
    }

    interface App<T> : Output<T> {
        val packageName: String

        fun getAction(value: T, uriQuote: UriQuote = DefaultUriQuote()): Action_.OpenApp

        @Composable
        fun label(app: AndroidTools.App): String

        fun isEnabled(value: T): Boolean
    }

    interface Text<T> : Output<T> {
        fun getText(value: T, uriQuote: UriQuote = DefaultUriQuote()): String
    }

    interface PointLabel<T> : Output<T> {
        @Composable
        fun getText(value: T, i: Int, pointCount: Int, uriQuote: UriQuote = DefaultUriQuote()): String?
    }
}

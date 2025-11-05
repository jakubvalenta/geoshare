package page.ooooo.geoshare.lib.outputs

import androidx.compose.runtime.Composable
import page.ooooo.geoshare.lib.DefaultUriQuote
import page.ooooo.geoshare.lib.UriQuote
import page.ooooo.geoshare.lib.outputs.Action as Action_

sealed interface Output<T> {

    interface Action<T, U : Action_> {
        fun getAction(value: T, uriQuote: UriQuote = DefaultUriQuote()): Action_

        @Composable
        fun label(): String
    }

    interface App<T> : Action<T, Action_.OpenApp> {
        val packageName: String
    }

    interface Text<T> : Output<T> {
        fun getText(value: T, uriQuote: UriQuote = DefaultUriQuote()): String
    }
}

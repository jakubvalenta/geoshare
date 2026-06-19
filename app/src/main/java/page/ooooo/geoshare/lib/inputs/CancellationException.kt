package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import page.ooooo.geoshare.R
import kotlin.coroutines.cancellation.CancellationException

sealed class RecoverableCancellationException : CancellationException() {
    abstract fun getMessage(resources: Resources): String
}

sealed class UnrecoverableCancellationException : CancellationException() {
    abstract fun getMessage(resources: Resources): String
}

class WebViewException : RecoverableCancellationException() {
    override fun getMessage(resources: Resources) = resources.getString(R.string.network_exception_unknown)
}

class MaxAttemptsReachedCancellationException(override val cause: RecoverableCancellationException) :
    UnrecoverableCancellationException() {
    override fun getMessage(resources: Resources) = cause.getMessage(resources)
}

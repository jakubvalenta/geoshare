package page.ooooo.geoshare.lib.network

sealed class NetworkException(val messageResId: Int, override val cause: Throwable) : Exception(cause)

class RecoverableNetworkException(messageResId: Int, cause: Throwable) : NetworkException(messageResId, cause)

class UnrecoverableNetworkException(messageResId: Int, cause: Throwable) : NetworkException(messageResId, cause)

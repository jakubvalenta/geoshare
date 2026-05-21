package page.ooooo.geoshare.lib.network

import android.content.res.Resources
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request
import io.ktor.http.HttpStatusCode
import page.ooooo.geoshare.R

sealed class NetworkException(cause: Throwable) : Exception(cause) {
    abstract fun getMessage(resources: Resources): String

    open fun getDetails(): String? = null
}

sealed class RecoverableNetworkException(cause: Throwable) : NetworkException(cause)

sealed class UnrecoverableNetworkException(cause: Throwable) : NetworkException(cause)

class UnresolvedAddressNetworkException(cause: Throwable) : RecoverableNetworkException(cause) {
    override fun getMessage(resources: Resources) = resources.getString(R.string.network_exception_unresolved_address)
}

class RequestTimeoutNetworkException(cause: Throwable) : RecoverableNetworkException(cause) {
    override fun getMessage(resources: Resources) = resources.getString(R.string.network_exception_request_timeout)
}

class SocketTimeoutNetworkException(cause: Throwable) : RecoverableNetworkException(cause) {
    override fun getMessage(resources: Resources) = resources.getString(R.string.network_exception_socket_timeout)
}

class ConnectTimeoutNetworkException(cause: Throwable) : RecoverableNetworkException(cause) {
    override fun getMessage(resources: Resources) = resources.getString(R.string.network_exception_connect_timeout)
}

class ConnectionClosedNetworkException(cause: Throwable) : RecoverableNetworkException(cause) {
    override fun getMessage(resources: Resources) = resources.getString(R.string.network_exception_eof)
}

class ConnectionRefusedNetworkException(cause: Throwable) : RecoverableNetworkException(cause) {
    override fun getMessage(resources: Resources) = resources.getString(R.string.network_exception_connect_exception)
}

class ServerResponseNetworkException(val response: HttpResponse, cause: Throwable) :
    RecoverableNetworkException(cause) {
    override fun getMessage(resources: Resources) =
        resources.getString(R.string.network_exception_server_response_error, response.status.value)
}

class ResponseNetworkException(val response: HttpResponse, cause: Throwable) : UnrecoverableNetworkException(cause) {
    override fun getMessage(resources: Resources) =
        when (response.status) {
            HttpStatusCode.TooManyRequests -> resources.getString(R.string.network_exception_too_many_requests)
            else -> resources.getString(R.string.network_exception_response_error, response.status.value)
        }

    override fun getDetails() = "Request URL: ${response.request.url}"
}

class UnknownNetworkException(cause: Throwable) : UnrecoverableNetworkException(cause) {
    override fun getMessage(resources: Resources) = resources.getString(R.string.network_exception_unknown)
}

class MissingHeaderNetworkException : UnrecoverableNetworkException(Throwable()) {
    override fun getMessage(resources: Resources) =
        resources.getString(R.string.conversion_failed_reason_missing_header)
}

class MaxAttemptsReachedNetworkException(override val cause: RecoverableNetworkException) :
    UnrecoverableNetworkException(cause) {
    override fun getMessage(resources: Resources) = cause.getMessage(resources)
}

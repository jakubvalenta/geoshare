package page.ooooo.geoshare.lib.android

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.location.LocationListenerCompat
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Source
import page.ooooo.geoshare.lib.geo.WGS84Point
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

private const val TAG = "LocationExtensions"

fun Context.hasLocationPermission(): Boolean =
    checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

@RequiresApi(Build.VERSION_CODES.S)
@RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
private suspend fun LocationManager.getCurrentLocation(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
): Point? = withContext(dispatcher) {
    suspendCancellableCoroutine { cont ->
        val cancellationSignal = CancellationSignal()
        try {
            getCurrentLocation(
                LocationManager.GPS_PROVIDER,
                cancellationSignal,
                dispatcher.asExecutor(),
            ) { location: Location? ->
                cont.resume(location?.let {
                    WGS84Point(it.latitude, it.longitude, source = Source.GPS_SENSOR)
                })
            }
        } catch (e: Exception) {
            cont.resumeWithException(e)
        }
        cont.invokeOnCancellation {
            cancellationSignal.cancel()
        }
    }
}

/**
 * See https://stackoverflow.com/a/71710276
 */
@RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
private suspend fun LocationManager.getCurrentLocationPreS(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
): Point? = withContext(dispatcher) {
    withTimeoutOrNull(30.seconds) {
        suspendCancellableCoroutine { cont ->
            try {
                @Suppress("DEPRECATION")
                requestSingleUpdate(
                    LocationManager.GPS_PROVIDER,
                    object : LocationListenerCompat {
                        // Use LocationListenerCompat instead of LocationListener or lambda, so that we don't have
                        // to override onStatusChanged on Android Q and older.
                        override fun onLocationChanged(location: Location) {
                            cont.resume(location.let {
                                WGS84Point(it.latitude, it.longitude, source = Source.GPS_SENSOR)
                            })
                        }
                    },
                    Looper.getMainLooper(),
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error when getting location", e)
                cont.resumeWithException(e)
            }
        }
    }
}

@RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
private fun LocationManager.getLastKnownLocation(maxAge: Duration = 1.minutes): Point? =
    getLastKnownLocation(LocationManager.GPS_PROVIDER)
        ?.takeIf { (SystemClock.elapsedRealtimeNanos() - it.elapsedRealtimeNanos).nanoseconds <= maxAge }
        ?.let { WGS84Point(it.latitude, it.longitude, source = Source.GPS_SENSOR) }

suspend fun Context.getLocation(): Point? {
    val locationManager = getSystemService(LocationManager::class.java)
    return try {
        val lastKnownLocation = locationManager.getLastKnownLocation()
        if (lastKnownLocation != null) {
            lastKnownLocation
        } else {
            // Use a small delay to prevent Android from asking for location permission twice, once for
            // getLastKnownLocation() and once for getCurrentLocation()
            delay(500.milliseconds)
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
                locationManager.getCurrentLocation()
            } else {
                locationManager.getCurrentLocationPreS()
            }
        }
    } catch (e: SecurityException) {
        Log.e(TAG, "Security error when getting location", e)
        null
    }
}

package page.ooooo.geoshare.lib

import android.os.Build
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object XiaomiTools {

    /**
     * See [GitHub Gist](https://gist.github.com/starry-shivam/901267c26eb030eb3faf1ccd4d2bdd32)
     */
    fun isMiuiDevice(): Boolean =
        setOf("xiaomi", "redmi", "poco").contains(Build.BRAND.lowercase()) &&
                (!getRuntimeProperty("ro.miui.ui.version.name").isNullOrBlank() ||
                        !getRuntimeProperty("ro.mi.os.version.name").isNullOrBlank())

    private fun getRuntimeProperty(property: String): String? = try {
        @Suppress("SpellCheckingInspection")
        Runtime.getRuntime().exec("getprop $property").inputStream.use { input ->
            BufferedReader(InputStreamReader(input), 1024).readLine()
        }
    } catch (_: IOException) {
        null
    }
}

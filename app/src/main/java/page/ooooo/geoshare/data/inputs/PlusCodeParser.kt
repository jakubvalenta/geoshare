package page.ooooo.geoshare.data.inputs

import com.google.openlocationcode.OpenLocationCode
import page.ooooo.geoshare.data.geo.NaivePoint
import page.ooooo.geoshare.data.geo.Source

object PlusCodeParser {
    /**
     * See https://github.com/google/open-location-code
     */
    fun decodePlusCode(codeString: String): NaivePoint? =
        try {
            OpenLocationCode(codeString)
        } catch (_: IllegalArgumentException) {
            // Invalid code
            null
        }
            ?.run {
                if (isFull) {
                    // The code is full
                    this
                } else {
                    // Complete the code using a geocoding service (not implemented yet)
                    null
                }
            }
            ?.decode()
            ?.run { NaivePoint(lat = centerLatitude, lon = centerLongitude, source = Source.HASH) }

    /**
     * See https://github.com/google/open-location-code/blob/main/Documentation/Reference/App_Developers.md#supporting-global-codes
     */
    val globalCodePattern by lazy { Regex(GLOBAL_CODE, RegexOption.IGNORE_CASE) }

    const val GLOBAL_CODE =
        // language=regexp
        """[23456789C][23456789CFGHJMPQRV][23456789CFGHJMPQRVWX]{6}(?:\+|%2B)[23456789CFGHJMPQRVWX]{2,7}"""
}

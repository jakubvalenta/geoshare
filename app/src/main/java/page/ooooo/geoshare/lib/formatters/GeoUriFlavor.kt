package page.ooooo.geoshare.lib.formatters

data class GeoUriFlavor(
    val pin: PinFlavor,
    val zoom: ZoomFlavor,
) {
    enum class PinFlavor {
        /**
         * Pin coords are in the 'q' param with name in parentheses, e.g. 'geo:50.123,-11.123?q=50.123,-11.123(foo%20bar)'
         */
        COORDS_AND_NAME_IN_Q,

        /**
         * Pin coords are in the 'q' param, pin name is not supported, e.g. 'geo:50.123,-11.123?q=50.123,-11.123'
         */
        COORDS_ONLY_IN_Q,

        /**
         * Pin name is in the 'q' param, pin coords are supported, e.g. 'geo:50.123,-11.123?q=foo%20bar'
         */
        NAME_ONLY_IN_Q,

        NOT_AVAILABLE,
    }

    enum class ZoomFlavor {
        /**
         * The 'z' param is supported but not when other params are set, e.g. 'geo:50.123,-11.123?z=3.14'
         */
        ALONE_ONLY,

        /**
         * The 'z' param is supported and other params can be set too, e.g. 'geo:50.123,-11.123?z=3.14&q=foo%20bar'
         */
        ANY,

        NOT_AVAILABLE,
    }

    companion object {
        val Safe = GeoUriFlavor(pin = PinFlavor.NOT_AVAILABLE, zoom = ZoomFlavor.NOT_AVAILABLE)
        val Best = GeoUriFlavor(pin = PinFlavor.COORDS_AND_NAME_IN_Q, zoom = ZoomFlavor.ANY)
    }
}

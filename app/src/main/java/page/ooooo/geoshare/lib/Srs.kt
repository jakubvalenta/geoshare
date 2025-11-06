package page.ooooo.geoshare.lib

sealed interface Srs {
    val name: String

    object WGS84 : Srs {
        override val name = "WGS 84"
    }

    object GCJ02 : Srs {
        override val name = "GCJ-02"
    }
}

package page.ooooo.geoshare.lib.features

interface Feature {
    suspend fun validate(): Boolean?
}

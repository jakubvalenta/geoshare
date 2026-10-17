package page.ooooo.geoshare.data.inputs

/**
 * Not available in this build flavor.
 */
object GoogleMapsHtmlInputImpl : GoogleMapsHtmlInput, NoopInput {
    override val group = GOOGLEInputGroup_MAPS

    override fun toString() = "GoogleMapsHtmlInput"
}

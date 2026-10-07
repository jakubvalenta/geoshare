package page.ooooo.geoshare.lib.inputs

/**
 * Not available in this build flavor.
 */
object GoogleMapsHtmlInputImpl : GoogleMapsHtmlInput, NoopInput {
    override val group = InputGroup.GOOGLE_MAPS

    override fun toString() = "GoogleMapsHtmlInput"
}

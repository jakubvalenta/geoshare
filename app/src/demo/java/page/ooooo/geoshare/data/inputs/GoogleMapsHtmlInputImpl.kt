package page.ooooo.geoshare.data.inputs

/**
 * Not available in this build flavor.
 */
object GoogleMapsHtmlInputImpl : GoogleMapsHtmlInput, NoopInput {
    override val group = GoogleMapsInputGroup

    override fun toString() = "GoogleMapsHtmlInput"
}

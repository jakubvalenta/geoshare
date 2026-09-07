package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources

interface GoogleMapsHtmlInput : NoopInput {
    override fun getName(resources: Resources) = "Google Maps HTML"
}

package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources

interface GoogleMapsPlaceListInput : NoopInput {
    override fun getName(resources: Resources) = "Google Maps Place List"
}

package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import page.ooooo.geoshare.R

interface GoogleMapsHtmlInput : NoopInput {
    override fun getName(resources: Resources) = resources.getString(R.string.input_google_maps_html_name)
}

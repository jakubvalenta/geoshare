package page.ooooo.geoshare.lib.inputs

import android.content.res.Resources
import page.ooooo.geoshare.lib.network.FakeFetchTools
import page.ooooo.geoshare.lib.network.FetchTools

interface InputTest {
    val resources: Resources

    suspend fun BasicOfflineInput.parse(match: String) = parse(match, resources)

    suspend fun BasicOnlineInput.parse(match: String) = parse(match, resources, FakeFetchTools(""))

    suspend fun BasicOnlineInput.parse(match: String, fetchTools: FetchTools) = parse(match, resources, fetchTools)

    suspend fun WebViewInput.parse(data: String, match: String) = parse(data, match, resources)
}

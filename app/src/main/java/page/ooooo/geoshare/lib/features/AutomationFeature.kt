package page.ooooo.geoshare.lib.features

import page.ooooo.geoshare.BuildConfig

object AutomationFeature : Feature {
    override suspend fun validate(): Boolean {
        @Suppress("KotlinConstantConditions", "SimplifyBooleanWithConstants")
        if (BuildConfig.BUILD_TYPE != "paid") {
            return true
        }
        // TODO Call Google Play API
        return true
    }
}

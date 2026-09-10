package page.ooooo.geoshare.tests.inputs

import page.ooooo.geoshare.BuildConfig

interface InputBehaviorTest {
    /**
     * Stores whether the current build variant supports HTML parsing or not. This way we can have one test class for
     * both build variants and all the tested links in one function.
     */
    @Suppress("KotlinConstantConditions", "SimplifyBooleanWithConstants")
    val htmlParsingSupported get() = BuildConfig.FLAVOR == "free"
}

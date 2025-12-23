package page.ooooo.geoshare.lib.features

import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.NullableLongUserPreference
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds

class Features(
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    private companion object {
        private val MAX_AGE = 24.hours
    }

    suspend fun validate(
        feature: Feature,
        userPreference: NullableLongUserPreference,
        now: Long = System.currentTimeMillis(),
    ): Boolean {
        val automationFeatureValidatedAt = userPreferencesRepository.getValue(userPreference)
        return if (automationFeatureValidatedAt == null) {
            // Validation info not found -- validate synchronously
            if (feature.validate()) {
                userPreferencesRepository.setValue(userPreference, now)
                true
            } else {
                false
            }
        } else if (automationFeatureValidatedAt.milliseconds + MAX_AGE <= now.milliseconds) {
            // Validation info cached -- consider the feature valid
            true
        } else {
            // Validation info expired -- consider the feature valid and start asynchronous validation
            // TODO Check if this gets cancelled
            supervisorScope {
                launch {
                    val automationValid = feature.validate()
                    userPreferencesRepository.setValue(userPreference, if (automationValid) now else null)
                }
            }
            true
        }
    }
}

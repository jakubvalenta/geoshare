package page.ooooo.geoshare.lib.billing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.BillingStatusPreference
import page.ooooo.geoshare.lib.DefaultLog
import page.ooooo.geoshare.lib.ILog
import kotlin.time.Duration.Companion.hours

class Billing(
    private val coroutineScope: CoroutineScope,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val billingProvider: BillingProvider = BillingProviderImpl,
    initialStatus: BillingStatus = BillingStatus.Loading,
    private val log: ILog = DefaultLog,
) {
    private companion object {
        private val MAX_AGE = 24.hours.inWholeMilliseconds
    }

    private val _status: MutableStateFlow<BillingStatus> = MutableStateFlow(initialStatus)
    val status: StateFlow<BillingStatus> = _status

    /**
     * Refresh billing status by calling BillingProvider, which might call an web API.
     *
     * Cache the resulting status in UserPreferences. On subsequent calls, if a cached status is found, return it
     * immediately and do the refreshing in the background. This means that the user doesn't have to wait for a web API
     * every time they use a paid feature, but for us it means we allow the user to use an expired subscription for
     * a bit longer.
     */
    suspend fun refresh(now: Long = System.currentTimeMillis()): Job? =
        when (val cachedStatus = userPreferencesRepository.getValue(BillingStatusPreference)) {
            is BillingStatus.Loading -> {
                _status.value = queryStatus()
                null
            }

            is BillingStatus.Done -> {
                if (cachedStatus.validatedAt + MAX_AGE <= now) {
                    coroutineScope.launch {
                        _status.value = queryStatus()
                    }
                } else {
                    _status.value = cachedStatus
                    null
                }
            }
        }

    private suspend fun queryStatus(): BillingStatus =
        try {
            val newStatus = billingProvider.queryStatus()
            userPreferencesRepository.setValue(BillingStatusPreference, newStatus)
            newStatus
        } catch (e: Exception) {
            log.e("Billing", "Error when querying billing status", e)
            BillingStatus.Loading
        }
}

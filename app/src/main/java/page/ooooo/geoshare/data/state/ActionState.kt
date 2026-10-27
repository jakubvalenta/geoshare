package page.ooooo.geoshare.data.state

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.timeout
import page.ooooo.geoshare.data.LinkRepository
import page.ooooo.geoshare.data.UserPreferenceRepository
import page.ooooo.geoshare.data.billing.AutomationFeature
import page.ooooo.geoshare.data.billing.Billing
import page.ooooo.geoshare.data.billing.BillingStatus
import page.ooooo.geoshare.data.geo.CoordinateConverter
import page.ooooo.geoshare.data.geo.Point
import page.ooooo.geoshare.data.geo.Points
import page.ooooo.geoshare.data.log.DefaultLog
import page.ooooo.geoshare.data.log.Log
import page.ooooo.geoshare.data.outputs.Action
import page.ooooo.geoshare.data.outputs.ActionResult
import page.ooooo.geoshare.data.outputs.BasicAction
import page.ooooo.geoshare.data.outputs.FileAction
import page.ooooo.geoshare.data.outputs.LocationAction
import page.ooooo.geoshare.data.outputs.NoopAction
import page.ooooo.geoshare.data.outputs.Output
import page.ooooo.geoshare.data.outputs.PointOutput
import page.ooooo.geoshare.data.outputs.PointsOutput
import page.ooooo.geoshare.data.outputs.StringOutput
import page.ooooo.geoshare.data.preferences.ActivityAutomation
import page.ooooo.geoshare.data.preferences.AutomationDelayPreference
import page.ooooo.geoshare.data.preferences.AutomationPreference
import page.ooooo.geoshare.data.preferences.BasicAutomation
import page.ooooo.geoshare.data.preferences.CachedPurchase
import page.ooooo.geoshare.data.preferences.CachedPurchasePreference
import page.ooooo.geoshare.data.preferences.LinkAutomation
import page.ooooo.geoshare.data.preferences.NoopAutomation
import page.ooooo.geoshare.data.toOutput
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

sealed interface ActionState : State<ActionStateContext> {
    interface HasPermission : State.HasPermission<ActionStateContext> {
        override suspend fun grant(stateContext: ActionStateContext, doNotAsk: Boolean): ActionState
        override suspend fun deny(stateContext: ActionStateContext, doNotAsk: Boolean): ActionState
    }

    data object Initial : ActionState {
        override fun toString() = "Initial"
    }
}

data class ActionStateContext(
    val billing: Billing,
    val context: Context,
    val coordinateConverter: CoordinateConverter,
    val linkRepository: LinkRepository,
    val log: Log = DefaultLog,
    val userPreferenceRepository: UserPreferenceRepository,
)

data class AutomationRequested(
    val points: Points,
    val billingStatusTimeout: Duration = 3.seconds,
) : ActionState {
    @OptIn(FlowPreview::class)
    override suspend fun transition(stateContext: ActionStateContext): ActionState? {
        val lastPoint = points.lastOrNull() ?: return null
        val automation = stateContext.userPreferenceRepository.getValue(AutomationPreference)
        if (automation is NoopAutomation) {
            return null
        }

        val billingStatus: BillingStatus = try {
            // Wait for billing status to appear; it should appear, because we call Billing.startConnection() in onCreate
            stateContext.billing.status
                .filter {
                    when (it) {
                        is BillingStatus.Loading -> false

                        is BillingStatus.Pending, is BillingStatus.NotPurchased -> true

                        is BillingStatus.Purchased -> {
                            // If billing status appeared within timeout, cache it
                            stateContext.userPreferenceRepository.setValue(
                                CachedPurchasePreference,
                                CachedPurchase(productId = it.product.id, token = it.token),
                            )
                            true
                        }
                    }
                }
                .timeout(billingStatusTimeout)
                .first()
        } catch (_: TimeoutCancellationException) {
            // If billing status didn't appear, try to read it from cache
            stateContext.log.w(TAG, "Billing status didn't appear within $billingStatusTimeout")
            stateContext.userPreferenceRepository.getValue(CachedPurchasePreference)
                ?.let { cachedPurchase ->
                    stateContext.billing.products.firstOrNull { product -> cachedPurchase.productId == product.id }
                        ?.let { product ->
                            stateContext.log.w(TAG, "Found cached billing status")
                            BillingStatus.Purchased(
                                product,
                                expired = false,
                                refundable = true,
                                token = cachedPurchase.token,
                            )
                        }
                }
                ?: run {
                    stateContext.log.w(TAG, "Didn't find cached billing status")
                    BillingStatus.Loading
                }
        }

        if (billingStatus is BillingStatus.Purchased && stateContext.billing.features.contains(AutomationFeature)) {
            val output = when (automation) {
                is BasicAutomation -> automation.toOutput(stateContext.coordinateConverter)
                is ActivityAutomation -> automation.toOutput(stateContext.coordinateConverter, stateContext.log)
                is LinkAutomation -> stateContext.linkRepository.getByUUID(automation.linkUUID)?.let { link ->
                    automation.toOutput(stateContext.coordinateConverter, link)
                }
            } ?: return null
            val action = when (output) {
                is PointOutput -> output.toAction(lastPoint)
                is PointsOutput -> output.toAction(points)
                is StringOutput -> NoopAction
            }
            if (output is Output.HasAutomationDelay) {
                val delay = stateContext.userPreferenceRepository.getValue(AutomationDelayPreference)
                return ActionWaiting(action, output, isAutomation = true, delay = delay)
            }
            return ActionReady(action, isAutomation = true)
        }
        return null
    }

    override fun toString() = "$TAG(points=$points)"

    private companion object {
        private const val TAG = "AutomationRequested"
    }
}

data class ActionWaiting(
    val action: Action<*>,
    val output: Output.HasAutomationDelay,
    @Suppress("SameParameterValue") val isAutomation: Boolean,
    val delay: Duration,
) : ActionState {
    override suspend fun transition(stateContext: ActionStateContext): ActionState = try {
        if (delay.isPositive()) {
            delay(delay)
        }
        ActionReady(action, isAutomation)
    } catch (_: CancellationException) {
        ActionCompleted(ActionResult.FAILED)
    }

    override fun toString() = "ActionWaiting(action=$action, output=$output, isAutomation=$isAutomation, delay=$delay)"
}

data class ActionReady(
    val action: Action<*>,
    val isAutomation: Boolean,
) : ActionState {
    override suspend fun transition(stateContext: ActionStateContext): ActionState = when (action) {
        is BasicAction -> BasicActionReady(action, isAutomation)
        is FileAction -> FileUriRequested(action, isAutomation)
        is LocationAction -> LocationRationaleRequested(action, isAutomation)
    }

    override fun toString() = "ActionReady(action=$action, isAutomation=$isAutomation)"
}

data class BasicActionReady(
    val action: BasicAction<*>,
    val isAutomation: Boolean,
) : ActionState {
    suspend fun execute(context: Context): ActionState =
        ActionRan(action = action, actionResult = action.execute(context), isAutomation = isAutomation)

    override fun toString() = "BasicActionReady(action=$action, isAutomation=$isAutomation)"
}

data class FileActionReady(
    val action: FileAction<*>,
    val isAutomation: Boolean,
    val uri: Uri,
) : ActionState {
    suspend fun execute(context: Context): ActionState =
        ActionRan(action = action, actionResult = action.execute(uri, context), isAutomation = isAutomation)

    override fun toString() = "FileActionReady(action=$action, isAutomation=$isAutomation, uri=$uri)"
}

data class LocationActionReady(
    val action: LocationAction<*>,
    val isAutomation: Boolean,
    val location: Point,
) : ActionState {
    suspend fun execute(context: Context): ActionState =
        ActionRan(action = action, actionResult = action.execute(location, context), isAutomation = isAutomation)

    override fun toString() = "LocationActionReady(action=$action, isAutomation=$isAutomation, location=$location)"
}

data class ActionRan(
    val action: Action<*>,
    val actionResult: ActionResult,
    val isAutomation: Boolean,
) : ActionState {
    override suspend fun transition(stateContext: ActionStateContext): ActionState =
        action.output.let { output ->
            if (!isAutomation) {
                when (actionResult) {
                    ActionResult.SUCCEEDED, ActionResult.SUCCEEDED_AND_OPENED_APP ->
                        if (output is Output.HasSuccessText) {
                            ActionSucceeded(actionResult, output)
                        } else {
                            ActionCompleted(actionResult)
                        }

                    ActionResult.FAILED ->
                        if (output is Output.HasErrorText) {
                            ActionFailed(output)
                        } else {
                            ActionCompleted(actionResult)
                        }
                }
            } else {
                when (actionResult) {
                    ActionResult.SUCCEEDED, ActionResult.SUCCEEDED_AND_OPENED_APP ->
                        if (output is Output.HasAutomationSuccessText) {
                            ActionAutomationSucceeded(actionResult, output)
                        } else {
                            ActionCompleted(actionResult)
                        }

                    ActionResult.FAILED ->
                        if (output is Output.HasAutomationErrorText) {
                            ActionAutomationFailed(output)
                        } else {
                            ActionCompleted(actionResult)
                        }
                }
            }
        }

    override fun toString() = "ActionRan(action=$action, actionResult=$actionResult, isAutomation=$isAutomation)"
}

data class ActionSucceeded(
    val actionResult: ActionResult,
    val output: Output.HasSuccessText,
) : ActionState {
    override suspend fun transition(stateContext: ActionStateContext): ActionState {
        try {
            delay(3.seconds)
        } catch (_: CancellationException) {
            // Do nothing
        }
        return ActionCompleted(actionResult)
    }

    override fun toString() = "ActionSucceeded(actionResult=$actionResult, output=$output)"
}

data class ActionAutomationSucceeded(
    val actionResult: ActionResult,
    val output: Output.HasAutomationSuccessText,
) : ActionState {
    override suspend fun transition(stateContext: ActionStateContext): ActionState {
        try {
            delay(3.seconds)
        } catch (_: CancellationException) {
            // Do nothing
        }
        return ActionCompleted(actionResult)
    }

    override fun toString() = "ActionAutomationSucceeded(actionResult=$actionResult, output=$output)"
}

data class ActionFailed(
    val output: Output.HasErrorText,
) : ActionState {
    override suspend fun transition(stateContext: ActionStateContext): ActionState {
        try {
            delay(3.seconds)
        } catch (_: CancellationException) {
            // Do nothing
        }
        return ActionCompleted(ActionResult.FAILED)
    }

    override fun toString() = "ActionFailed(output=$output)"
}

data class ActionAutomationFailed(
    val output: Output.HasAutomationErrorText,
) : ActionState {
    override suspend fun transition(stateContext: ActionStateContext): ActionState {
        try {
            delay(3.seconds)
        } catch (_: CancellationException) {
            // Do nothing
        }
        return ActionCompleted(ActionResult.FAILED)
    }

    override fun toString() = "ActionAutomationFailed(output=$output)"
}

data class ActionCompleted(
    val actionResult: ActionResult,
) : ActionState {
    override fun toString() = "ActionCompleted(actionResult=$actionResult)"
}

data class FileUriRequested(
    val action: FileAction<*>,
    val isAutomation: Boolean,
) : ActionState {
    override fun toString() = "FileUriRequested(action=$action, isAutomation=$isAutomation)"
}

data class LocationRationaleRequested(
    val action: LocationAction<*>,
    val isAutomation: Boolean,
) : ActionState {
    override fun toString() = "LocationRationaleRequested(action=$action, isAutomation=$isAutomation)"
}

data class LocationRationaleShown(
    val action: LocationAction<*>,
    val isAutomation: Boolean,
) : ActionState, ActionState.HasPermission {
    override suspend fun grant(stateContext: ActionStateContext, doNotAsk: Boolean): ActionState =
        LocationRationaleConfirmed(action, isAutomation)

    override suspend fun deny(stateContext: ActionStateContext, doNotAsk: Boolean): ActionState =
        ActionCompleted(ActionResult.FAILED)

    override fun toString() = "LocationRationaleShown(action=$action, isAutomation=$isAutomation)"
}

data class LocationRationaleConfirmed(
    val action: LocationAction<*>,
    val isAutomation: Boolean,
) : ActionState {
    override fun toString() = "LocationRationaleConfirmed(action=$action, isAutomation=$isAutomation)"
}

data class LocationPermissionReceived(
    val action: LocationAction<*>,
    val isAutomation: Boolean,
) : ActionState {
    override fun toString() = "LocationPermissionReceived(action=$action, isAutomation=$isAutomation)"
}

data class LocationReceived(
    val action: LocationAction<*>,
    val isAutomation: Boolean,
    val location: Point?,
) : ActionState {
    override suspend fun transition(stateContext: ActionStateContext): ActionState = if (location == null) {
        LocationFindingFailed(ActionResult.FAILED)
    } else {
        LocationActionReady(action, isAutomation, location)
    }

    override fun toString() = "LocationReceived(action=$action, isAutomation=$isAutomation, location=$location)"
}

data class LocationFindingFailed(
    val actionResult: ActionResult,
) : ActionState {
    override suspend fun transition(stateContext: ActionStateContext): ActionState {
        try {
            delay(3.seconds)
        } catch (_: CancellationException) {
            // Do nothing
        }
        return ActionCompleted(actionResult)
    }

    override fun toString() = "LocationFindingFailed(actionResult=$actionResult)"
}

data object ActivityFinishingRequested : ActionState {
    override fun toString() = "ActivityFinishingRequested"
}

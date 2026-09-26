package page.ooooo.geoshare.lib.outputs

import android.net.Uri
import kotlinx.coroutines.delay
import page.ooooo.geoshare.data.UserPreferencesRepository
import page.ooooo.geoshare.data.local.preferences.AutomationDelayPreference
import page.ooooo.geoshare.lib.State
import page.ooooo.geoshare.lib.geo.Point
import page.ooooo.geoshare.lib.geo.Points
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface ActionState : State<ActionStateContext> {
    interface HasPermission : State.HasPermission<ActionStateContext> {
        override suspend fun deny(stateContext: ActionStateContext, doNotAsk: Boolean): ActionState
        override suspend fun grant(stateContext: ActionStateContext, doNotAsk: Boolean): ActionState
    }

    object Initial : ActionState {
        override fun toString() = "Initial"
    }
}

interface ActionStateContext {
    val userPreferencesRepository: UserPreferencesRepository
}

class DefaultActionStateContext(
    val userPreferencesRepository: UserPreferencesRepository,
)

data class AutomationReceived(
    val points: Points,
    val output: Output,
) : ActionState {
    override suspend fun transition(stateContext: ActionStateContext): ActionState? {
        val lastPoint = points.lastOrNull() ?: return null
        val action = when (output) {
            is PointOutput -> output.toAction(lastPoint)
            is PointsOutput -> output.toAction(points)
            is StringOutput -> NoopAction
        }
        if (output is Output.HasAutomationDelay) {
            val delay = stateContext.userPreferencesRepository.getValue(AutomationDelayPreference)
            return ActionWaiting(action, output, isAutomation = true, delay = delay)
        }
        return ActionReady(action, isAutomation = true)
    }

    override fun toString() = "AutomationReceived(points=$points, output=$output)"
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

    override fun toString() = "$TAG(saction=$action, isAutomation=$isAutomation)"

    private companion object {
        private const val TAG = "ActionWaiting"
    }
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

    override fun toString() = "$TAG(action=$action, isAutomation=$isAutomation)"

    private companion object {
        private const val TAG = "ActionReady"
    }
}

data class BasicActionReady(
    val action: BasicAction<*>,
    val isAutomation: Boolean,
) : ActionState {
    override fun toString() = "$TAG(action=$action, isAutomation=$isAutomation)"

    private companion object {
        private const val TAG = "BasicActionReady"
    }
}

data class FileActionReady(
    val action: FileAction<*>,
    val isAutomation: Boolean,
    val uri: Uri,
) : ActionState {
    override fun toString() =
        "$TAG(action=$action, isAutomation=$isAutomation, uri=$uri)"

    private companion object {
        private const val TAG = "FileActionReady"
    }
}

data class LocationActionReady(
    val action: LocationAction<*>,
    val isAutomation: Boolean,
    val location: Point,
) : ActionState {
    override fun toString() =
        "$TAG(action=$action, isAutomation=$isAutomation, location=$location)"

    private companion object {
        private const val TAG = "LocationActionReady"
    }
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

    override fun toString() =
        "$TAG(action=$action, actionResult=$actionResult, isAutomation=$isAutomation)"

    private companion object {
        private const val TAG = "ActionRan"
    }
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

    override fun toString() = "$TAG(actionResult=$actionResult)"

    private companion object {
        private const val TAG = "ActionSucceeded"
    }
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

    override fun toString() = "$TAG(actionResult=$actionResult)"

    private companion object {
        private const val TAG = "ActionAutomationSucceeded"
    }
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

    override fun toString() = "ActionFailed"
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

    override fun toString() = "ActionAutomationFailed"
}

data class ActionCompleted(
    val actionResult: ActionResult,
) : ActionState {
    override fun toString() = "$TAG(actionResult=$actionResult)"

    private companion object {
        private const val TAG = "ActionCompleted"
    }
}

data class FileUriRequested(
    val action: FileAction<*>,
    val isAutomation: Boolean,
) : ActionState {
    override fun toString() = "$TAG(action=$action, isAutomation=$isAutomation)"

    private companion object {
        private const val TAG = "FileUriRequested"
    }
}

data class LocationRationaleRequested(
    val action: LocationAction<*>,
    val isAutomation: Boolean,
) : ActionState {
    override fun toString() = "$TAG(action=$action, isAutomation=$isAutomation)"

    private companion object {
        private const val TAG = "LocationRationaleRequested"
    }
}

data class LocationRationaleShown(
    val action: LocationAction<*>,
    val isAutomation: Boolean,
) : ActionState, ActionState.HasPermission {
    override suspend fun grant(stateContext: ActionStateContext, doNotAsk: Boolean): ActionState =
        LocationRationaleConfirmed(action, isAutomation)

    override suspend fun deny(stateContext: ActionStateContext, doNotAsk: Boolean): ActionState =
        ActionCompleted(ActionResult.FAILED)

    override fun toString() = "$TAG(action=$action, isAutomation=$isAutomation)"

    private companion object {
        private const val TAG = "LocationRationaleShown"
    }
}

data class LocationRationaleConfirmed(
    val action: LocationAction<*>,
    val isAutomation: Boolean,
) : ActionState {
    override fun toString() = "$TAG(action=$action, isAutomation=$isAutomation)"

    private companion object {
        private const val TAG = "LocationRationaleConfirmed"
    }
}

data class LocationPermissionReceived(
    val action: LocationAction<*>,
    val isAutomation: Boolean,
) : ActionState {
    override fun toString() = "$TAG(action=$action, isAutomation=$isAutomation)"

    private companion object {
        private const val TAG = "LocationPermissionReceived"
    }
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

    override fun toString() =
        "$TAG(action=$action, isAutomation=$isAutomation, location=$location)"

    private companion object {
        private const val TAG = "LocationReceived"
    }
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

    override fun toString() = "$TAG(actionResult=$actionResult)"

    private companion object {
        private const val TAG = "LocationFindingFailed"
    }
}

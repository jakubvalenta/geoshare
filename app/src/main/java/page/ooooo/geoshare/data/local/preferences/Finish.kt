package page.ooooo.geoshare.data.local.preferences

import page.ooooo.geoshare.lib.outputs.ActionResult

enum class Finish {
    AFTER_ACTION_SUCCEEDED,
    AFTER_ACTION_SUCCEEDED_AND_OPENED_APP,
    NEVER,
}

fun Finish.shouldAppFinish(actionResult: ActionResult): Boolean =
    when (this) {
        Finish.AFTER_ACTION_SUCCEEDED -> when (actionResult) {
            ActionResult.SUCCEEDED -> true
            ActionResult.SUCCEEDED_AND_OPENED_APP -> true
            ActionResult.FAILED -> false
        }

        Finish.AFTER_ACTION_SUCCEEDED_AND_OPENED_APP -> when (actionResult) {
            ActionResult.SUCCEEDED -> false
            ActionResult.SUCCEEDED_AND_OPENED_APP -> true
            ActionResult.FAILED -> false
        }

        Finish.NEVER -> false
    }

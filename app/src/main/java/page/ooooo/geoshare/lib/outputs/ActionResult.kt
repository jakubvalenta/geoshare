package page.ooooo.geoshare.lib.outputs

enum class ActionResult {
    SUCCEEDED,
    SUCCEEDED_AND_OPENED_APP,
    FAILED,
}

fun Boolean?.toActionResult(openedApp: Boolean = false): ActionResult =
    if (this == true) {
        if (openedApp) {
            ActionResult.SUCCEEDED_AND_OPENED_APP
        } else {
            ActionResult.SUCCEEDED
        }
    } else {
        ActionResult.FAILED
    }

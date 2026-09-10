package page.ooooo.geoshare.data.local.preferences

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import page.ooooo.geoshare.lib.outputs.ActionResult

class FinishTest {
    @Test
    fun shouldAppFinish_whenItIsAfterActionSucceeded() {
        assertTrue(Finish.AFTER_ACTION_SUCCEEDED.shouldAppFinish(ActionResult.SUCCEEDED))
        assertTrue(Finish.AFTER_ACTION_SUCCEEDED.shouldAppFinish(ActionResult.SUCCEEDED_AND_OPENED_APP))
        assertFalse(Finish.AFTER_ACTION_SUCCEEDED.shouldAppFinish(ActionResult.FAILED))
    }

    @Test
    fun shouldAppFinish_whenItIsAfterActionSucceededAndOpenedApp() {
        assertFalse(Finish.AFTER_ACTION_SUCCEEDED_AND_OPENED_APP.shouldAppFinish(ActionResult.SUCCEEDED))
        assertTrue(Finish.AFTER_ACTION_SUCCEEDED_AND_OPENED_APP.shouldAppFinish(ActionResult.SUCCEEDED_AND_OPENED_APP))
        assertFalse(Finish.AFTER_ACTION_SUCCEEDED_AND_OPENED_APP.shouldAppFinish(ActionResult.FAILED))
    }

    @Test
    fun shouldAppFinish_whenItIsNever() {
        assertFalse(Finish.NEVER.shouldAppFinish(ActionResult.SUCCEEDED))
        assertFalse(Finish.NEVER.shouldAppFinish(ActionResult.SUCCEEDED_AND_OPENED_APP))
        assertFalse(Finish.NEVER.shouldAppFinish(ActionResult.FAILED))
    }
}

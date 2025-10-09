package page.ooooo.geoshare.ui.components

sealed class AutomationState {
    class Nothing : AutomationState()
    class Running(val messageResId: Int) : AutomationState()
    class Succeeded(val messageResId: Int) : AutomationState()
    class Failed(val messageResId: Int) : AutomationState()
}

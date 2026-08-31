package page.ooooo.geoshare.data.local.preferences

enum class HelpMessage {
    OPEN_BY_DEFAULT,
    SHARE_SOURCE,
    WELCOME,
}

/**
 * Returns true if the message is dismissed.
 *
 * When [dismissedHelpMessages] is null, it means the set of dismissed messages hasn't finished loading yet. All
 * messages are considered dismissed in this state.
 */
fun HelpMessage.isDismissed(dismissedHelpMessages: Set<HelpMessage>?): Boolean =
    dismissedHelpMessages?.contains(this) != false

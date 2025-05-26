package page.ooooo.geoshare.lib

data class Message(
    val resId: Int,
    val type: Type = Type.SUCCESS,
) {
    enum class Type { SUCCESS, ERROR }
}

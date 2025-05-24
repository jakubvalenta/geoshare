package page.ooooo.geoshare.lib

data class Message(
    val resId: Int,
    val type: Type = Type.SUCCESS,
    val formatArgs: List<String> = emptyList(),
) {
    enum class Type { SUCCESS, ERROR }
}

package page.ooooo.geoshare.lib

interface Log {
    fun d(tag: String?, msg: String): Int
    fun d(tag: String?, msg: String, tr: Throwable): Int
    fun e(tag: String?, msg: String): Int
    fun e(tag: String?, msg: String, tr: Throwable): Int
    fun i(tag: String?, msg: String): Int
    fun i(tag: String?, msg: String, tr: Throwable): Int
    fun w(tag: String?, msg: String): Int
    fun w(tag: String?, msg: String, tr: Throwable): Int
}

object DefaultLog : Log {
    override fun d(tag: String?, msg: String) = android.util.Log.d(tag, msg)
    override fun d(tag: String?, msg: String, tr: Throwable) = android.util.Log.d(tag, msg, tr)
    override fun e(tag: String?, msg: String) = android.util.Log.e(tag, msg)
    override fun e(tag: String?, msg: String, tr: Throwable) = android.util.Log.e(tag, msg, tr)
    override fun i(tag: String?, msg: String) = android.util.Log.i(tag, msg)
    override fun i(tag: String?, msg: String, tr: Throwable) = android.util.Log.i(tag, msg, tr)
    override fun w(tag: String?, msg: String) = android.util.Log.w(tag, msg)
    override fun w(tag: String?, msg: String, tr: Throwable) = android.util.Log.w(tag, msg, tr)
}

object FakeLog : Log {
    override fun d(tag: String?, msg: String) = log(msg)
    override fun d(tag: String?, msg: String, tr: Throwable) = log(msg, tr)
    override fun e(tag: String?, msg: String) = log(msg)
    override fun e(tag: String?, msg: String, tr: Throwable) = log(msg, tr)
    override fun i(tag: String?, msg: String) = log(msg)
    override fun i(tag: String?, msg: String, tr: Throwable) = log(msg, tr)
    override fun w(tag: String?, msg: String) = log(msg)
    override fun w(tag: String?, msg: String, tr: Throwable) = log(msg, tr)

    @Suppress("SameReturnValue")
    private fun log(msg: String): Int {
        println(msg)
        return 1
    }

    @Suppress("SameReturnValue")
    private fun log(msg: String, tr: Throwable): Int {
        println("$msg, ${tr.stackTraceToString()}")
        return 1
    }
}

package page.ooooo.geoshare.lib

import android.util.Log

interface ILog {
    fun d(tag: String?, msg: String): Int
    fun d(tag: String?, msg: String, tr: Throwable): Int
    fun e(tag: String?, msg: String): Int
    fun e(tag: String?, msg: String, tr: Throwable): Int
    fun i(tag: String?, msg: String): Int
    fun i(tag: String?, msg: String, tr: Throwable): Int
    fun w(tag: String?, msg: String): Int
    fun w(tag: String?, msg: String, tr: Throwable): Int
}

object DefaultLog : ILog {
    override fun d(tag: String?, msg: String) = Log.d(tag, msg)
    override fun d(tag: String?, msg: String, tr: Throwable) = Log.d(tag, msg, tr)
    override fun e(tag: String?, msg: String) = Log.e(tag, msg)
    override fun e(tag: String?, msg: String, tr: Throwable) = Log.e(tag, msg, tr)
    override fun i(tag: String?, msg: String) = Log.i(tag, msg)
    override fun i(tag: String?, msg: String, tr: Throwable) = Log.i(tag, msg, tr)
    override fun w(tag: String?, msg: String) = Log.w(tag, msg)
    override fun w(tag: String?, msg: String, tr: Throwable) = Log.w(tag, msg, tr)
}

object FakeLog : ILog {
    override fun d(tag: String?, msg: String) = log(msg)
    override fun d(tag: String?, msg: String, tr: Throwable) = log(msg, tr)
    override fun e(tag: String?, msg: String) = log(msg)
    override fun e(tag: String?, msg: String, tr: Throwable) = log(msg, tr)
    override fun i(tag: String?, msg: String) = log(msg)
    override fun i(tag: String?, msg: String, tr: Throwable) = log(msg, tr)
    override fun w(tag: String?, msg: String) = log(msg)
    override fun w(tag: String?, msg: String, tr: Throwable) = log(msg, tr)

    private fun log(msg: String): Int {
        println(msg)
        return 1
    }

    private fun log(msg: String, tr: Throwable): Int {
        println("$msg, ${tr.stackTraceToString()}")
        return 1
    }
}

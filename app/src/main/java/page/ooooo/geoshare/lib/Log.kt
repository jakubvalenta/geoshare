package page.ooooo.geoshare.lib

import android.util.Log

interface ILog {
    fun e(tag: String?, msg: String): Int
    fun e(tag: String?, msg: String, tr: Throwable): Int
    fun i(tag: String?, msg: String): Int
    fun i(tag: String?, msg: String, tr: Throwable): Int
    fun w(tag: String?, msg: String): Int
    fun w(tag: String?, msg: String, tr: Throwable): Int
}

class DefaultLog : ILog {
    override fun e(tag: String?, msg: String) = Log.e(tag, msg)
    override fun e(tag: String?, msg: String, tr: Throwable) = Log.e(tag, msg, tr)
    override fun i(tag: String?, msg: String) = Log.i(tag, msg)
    override fun i(tag: String?, msg: String, tr: Throwable) = Log.i(tag, msg, tr)
    override fun w(tag: String?, msg: String) = Log.w(tag, msg)
    override fun w(tag: String?, msg: String, tr: Throwable) = Log.w(tag, msg, tr)
}

class FakeLog : ILog {
    override fun e(tag: String?, msg: String): Int {
        println(msg)
        return 1
    }

    override fun e(tag: String?, msg: String, tr: Throwable): Int {
        println("$msg, ${tr.stackTraceToString()}")
        return 1
    }

    override fun i(tag: String?, msg: String): Int {
        println(msg)
        return 1
    }

    override fun i(tag: String?, msg: String, tr: Throwable): Int {
        println("$msg, ${tr.stackTraceToString()}")
        return 1
    }

    override fun w(tag: String?, msg: String): Int {
        println(msg)
        return 1
    }

    override fun w(tag: String?, msg: String, tr: Throwable): Int {
        println("$msg, ${tr.stackTraceToString()}")
        return 1
    }
}

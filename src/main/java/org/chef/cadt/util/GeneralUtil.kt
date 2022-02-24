package org.chef.cadt.util

/**
 * Created by Chef.Xie
 */
object GeneralUtil {
    @JvmStatic
    fun sleep(millis: Number) = Thread.sleep(millis.toLong())

    @JvmStatic
    fun color(text: String, color: ConsoleColor) = "\u001b[${color.num};4m$text\u001b[0m"

    @JvmStatic
    fun <T> merge(vararg lists: List<T>): List<T> {
        val collection = ArrayList<T>()
        lists.forEach { collection.addAll(it) }
        return collection
    }

    @JvmStatic
    fun getOS(): OperateSystem {
        val os = System.getProperty("os.name").toLowerCase()
        if (os.indexOf("win") >= 0) return OperateSystem.WINDOWS
        else if (os.indexOf("mac") >= 0) return OperateSystem.MAC
        else if (os.indexOf("linux") >= 0) return OperateSystem.LINUX
        else return OperateSystem.UNIX
    }
}

enum class ConsoleColor(val num: Int) {
    RED(31), YELLOW(33), BLUE(34), PURPLE(35),
    CYAN(36), GREEN(32), GREY(37), BLACK(38)
}

enum class OperateSystem {
    WINDOWS, MAC, LINUX, UNIX
}
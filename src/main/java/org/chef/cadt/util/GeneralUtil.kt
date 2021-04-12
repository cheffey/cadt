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
}

enum class ConsoleColor(val num: Int) {
    RED(31), YELLOW(33), BLUE(34), PURPLE(35),
    CYAN(36), GREEN(32), GREY(37), BLACK(38)
}

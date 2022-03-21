package org.chef.cadt.model

import org.chef.cadt.exception.WithUselessTraceException
import org.chef.cadt.util.ConsoleColor
import org.chef.cadt.util.ExceptionUtil
import org.chef.cadt.util.GeneralUtil
import org.openqa.selenium.WebElement

/**
 * Created by Chef.Xie
 */

class CachedElements private constructor(private val targetList: MutableList<CachedElement>) :
    List<CachedElement> by targetList {

    constructor() : this(mutableListOf())

    fun wrap(ele: WebElement) = CachedElement(this, ele)

    fun append(element: CachedElement) {
        targetList.add(element)
    }

    fun clear() {
        targetList.removeIf { true }
    }

    fun print(sortBy: String? = null) {
        val elementView = when (sortBy) {
            "id", "i", "I" -> sortedBy { it.cacheTestID }
            "text", "t", "T" -> sortedBy { it.cacheText }
            else -> this
        }
        println("cached element count: ${elementView.size}")
        for (element in elementView) {
            element.print()
        }
    }
}

class CachedElement(private val container: CachedElements, private val element: WebElement) : WebElement by element {
    val cacheText: String? by lazy { ExceptionUtil.tryOrNull { (element.text) } }
    val cacheClass: String? by lazy { ExceptionUtil.tryOrNull { getAttribute("class") } }
    val cacheTestID: String? by lazy { ExceptionUtil.tryOrNull { getAttribute(testIDProperty) } }
    private val testIDProperty = "id"

    fun print() = println("idx: ${container.indexOf(this)} ${colorClass()}\t\t${colorID()}\t\t${colorText()}")

    private fun colorID(): String {
        val idStart = GeneralUtil.color("{id: ", ConsoleColor.YELLOW)
        val idEnd = GeneralUtil.color("}", ConsoleColor.YELLOW)
        val id = cacheTestID?.replace("\n", "\\n")
        return "$idStart$id$idEnd"
    }

    private fun colorText(): String {
        val textStart = GeneralUtil.color("[text: ", ConsoleColor.GREEN)
        val textEnd = GeneralUtil.color("]", ConsoleColor.GREEN)
        val text = cacheText?.replace("\n", "\\n")
        return "$textStart$text$textEnd"
    }

    private fun colorClass(): String {
        val textStart = GeneralUtil.color("[class: ", ConsoleColor.PURPLE)
        val textEnd = GeneralUtil.color("]", ConsoleColor.PURPLE)
        val text = cacheClass?.replace("\n", "\\n")
        return "$textStart$text$textEnd"
    }
}
package org.chef.cadt.model

import org.chef.cadt.exception.WithUselessTraceException
import org.chef.cadt.util.ConsoleColor
import org.chef.cadt.util.GeneralUtil
import org.chef.cadt.util.StringUtil

/**
 * Created by Chef.Xie
 */
data class CommandLine(var keyword: String, var args: List<String>)

class CommandMapper(vararg val commandNames: String) {
    private var desc: String? = null
    val strategies: MutableList<CommandStrategy> = ArrayList()

    fun nonArgMap(runnable: () -> Unit): CommandMapper {
        strategies.add(commandStrategy(0) { runnable.invoke() })
        return this
    }

    fun oneArgMap(consumer: (String) -> Unit): CommandMapper {
        strategies.add(commandStrategy(1) { consumer.invoke(it[0]) })
        return this
    }

    fun twoArgsMap(biConsumer: (String, String) -> Unit): CommandMapper {
        strategies.add(commandStrategy(2) { biConsumer.invoke(it[0], it[1]) })
        return this
    }

    fun threeArgsMap(triConsumer: (String, String, String) -> Unit): CommandMapper {
        strategies.add(commandStrategy(3) { triConsumer.invoke(it[0], it[1], it[2]) })
        return this
    }

    fun desc(desc: String): CommandMapper {
        this.desc = desc
        return this
    }

    fun printHelp() {
        val BLOCK_TAB_COUNT = 4
        val commandNamesDesc = commandNames.map { GeneralUtil.color(it, ConsoleColor.BLUE) }.toString()
        val commandNamesTabCount = commandNames.contentToString().length / 4
        val tabEstimation = (commandNamesTabCount / BLOCK_TAB_COUNT + 1) * BLOCK_TAB_COUNT
        val tabCount = tabEstimation - commandNamesTabCount
        println(commandNamesDesc + StringUtil.repeat("\t", tabCount) + desc)
    }
}

interface CommandStrategy {
    fun execute(args: List<String>)
    fun argsCount(): Int
}
private fun commandStrategy(expectedArgsSize: Int, executable: (List<String>) -> Unit) =
    object : CommandStrategy {
        override fun execute(args: List<String>) {
            checkArgsCount(args, expectedArgsSize)
            executable.invoke(args)
        }

        override fun argsCount() = expectedArgsSize
    }

private fun checkArgsCount(args: List<String>, expectedSize: Int) {
    if (args.size == expectedSize)
        return
    val message = "expect $expectedSize argument, actual arguments: $args"
    if (args.size < expectedSize) {
        throw WithUselessTraceException(message)
    } else
        System.err.println(message)
}
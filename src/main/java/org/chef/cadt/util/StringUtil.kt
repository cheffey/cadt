package org.chef.cadt.util

import org.chef.cadt.model.CommandLine
import java.lang.StringBuilder
import java.util.regex.Pattern

/**
 * Created by Chef.Xie
 */
object StringUtil {
    fun repeat(unit: String, times: Int): String {
        val sb = StringBuilder()
        for (i in 0..times) {
            sb.append(unit)
        }
        return sb.toString()
    }

    fun cutArgs(line: String): CommandLine? {
        val args = cutArgs0(line)
        return if (args.isEmpty()) null
        else CommandLine(args[0], args.subList(1, args.size))
    }

    private fun cutArgs0(line: String): List<String> {
        val trimmedLine = line.trim()
        if (trimmedLine.isEmpty()) {
            return listOf()
        }
        val pattern = Pattern.compile("([^{}]*)\\{([^}]*)}(.*)")
        val matcher = pattern.matcher(trimmedLine)
        if (matcher.find()) {
            val list = ArrayList<String>()
            list.addAll(cutArgs0(matcher.group(1)))
            list.add(matcher.group(2).trim())
            list.addAll(cutArgs0(matcher.group(3)))
            return list
        }
        return trimmedLine.split(" ")
    }
}
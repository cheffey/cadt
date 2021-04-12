package org.chef.cadt.util

/**
 * Created by Chef.Xie
 */
object ShellUtil {
    fun runCommands(commands: String) {
        Runtime.getRuntime().exec(arrayOf("/bin/sh", "-c", commands))
    }
}
package org.chef.cadt.util

import org.chef.cadt.util.GeneralUtil.getOS
import java.io.File

/**
 * Created by Chef.Xie
 */
object ShellUtil {
    fun runCommands(commands: String) {
        val shellCommands = when (getOS()) {
            OperateSystem.WINDOWS -> arrayOf("cmd.exe", "/c", commands)
            else -> arrayOf("/bin/sh", "-c", commands)
        }
        Runtime.getRuntime().exec(shellCommands)
    }

    fun open(path: String) {
        when (getOS()) {
            OperateSystem.WINDOWS -> runCommands("start $path")
            else -> runCommands("open $path")
        }
    }
}
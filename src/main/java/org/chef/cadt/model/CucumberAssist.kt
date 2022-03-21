package org.chef.cadt.model

import io.cucumber.plugin.event.Status
import org.chef.cadt.util.ThrowableRunnable
import org.openqa.selenium.WebDriver

/**
 * Created by Chef.Xie
 */

class Device(val driver: WebDriver) {
    val url = driver.currentUrl
    fun reconnect() {
        TODO()
    }
}

enum class AutomationType {
    XCUITEST, UIAUTOMATOR2, ESPRESSO
}

data class DebugToolExecutionResult(var status: Status, var error: Throwable?)

class StepInfo(val description: String, private val testStep: ThrowableRunnable) {
    fun execute() {
        testStep.run()
    }

    override fun toString(): String {
        return "description: $description"
    }
}

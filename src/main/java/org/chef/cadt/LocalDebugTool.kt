package org.chef.cadt

import io.appium.java_client.AppiumDriver
import io.appium.java_client.MobileBy
import io.appium.java_client.android.AndroidDriver
import io.cucumber.core.runner.CaseFlow
import org.chef.cadt.exception.WithUselessTraceException
import org.chef.cadt.model.AutomationType
import org.chef.cadt.model.CachedElement
import org.chef.cadt.model.CachedElements
import org.chef.cadt.model.CommandMapper
import org.chef.cadt.model.CommandStrategy
import org.chef.cadt.model.Device
import org.chef.cadt.model.ErrorHandleResponse
import org.chef.cadt.util.ConsoleColor
import org.chef.cadt.util.ExceptionUtil.tryOrNull
import org.chef.cadt.util.ExceptionUtil.tryRunHandleException
import org.chef.cadt.util.GeneralUtil.color
import org.chef.cadt.util.GeneralUtil.merge
import org.chef.cadt.util.GeneralUtil.sleep
import org.chef.cadt.util.ShellUtil
import org.chef.cadt.util.StringUtil.cutArgs
import org.openqa.selenium.By
import org.openqa.selenium.OutputType
import org.openqa.selenium.TakesScreenshot
import org.openqa.selenium.WebElement
import java.util.*
import java.util.function.Supplier
import java.util.regex.Pattern

/**
 * Created by Chef.Xie
 */

class LocalDebugTool {
    companion object {
        @JvmField
        var driverLoader: Supplier<List<AppiumDriver<out WebElement>>>? = null

        @JvmField
        var debugMode = false
    }

    private var caseFlow: CaseFlow? = null
    private var currentDevice: Device? = null
    private val loadedDevices: MutableList<Device> = mutableListOf()
    private var cachedElements: CachedElements = CachedElements()
    private var errorHandleResponse: ErrorHandleResponse? = null

    private val FLOW_CONTROL: List<CommandMapper> = listOf(
        CommandMapper("redo").desc(CommandDescriptions.REDO_HELP)
            .nonArgMap { errorHandleResponse = ErrorHandleResponse.REDO }
            .oneArgMap { sleep(it.toLong() * 1000); errorHandleResponse = ErrorHandleResponse.REDO },
        CommandMapper("end", "exit", "bye").desc(CommandDescriptions.END_HELP)
            .nonArgMap { errorHandleResponse = ErrorHandleResponse.END },
        CommandMapper("endstep").desc(CommandDescriptions.END_STEP_HELP)
            .nonArgMap { errorHandleResponse = ErrorHandleResponse.END_STEP },
        CommandMapper("skip", "resume").desc(CommandDescriptions.SKIP_HELP)
            .nonArgMap { errorHandleResponse = ErrorHandleResponse.RESUME },
        CommandMapper("closeDebug").desc(CommandDescriptions.CLOSE_DEBUG)
            .nonArgMap { System.setProperty("LocalDebug", "false") }
    )

    private val BASICS: List<CommandMapper> = listOf(
        CommandMapper("help").nonArgMap { help() }.desc("print help"),
        CommandMapper("cache", "ca").desc(CommandDescriptions.CACHE_HELP)
            .nonArgMap { cachedElements.print() }
            .oneArgMap { cachedElements.print(it) },
        CommandMapper("step", "history").desc(CommandDescriptions.STEP_HELP)
            .nonArgMap { printStepHistory() }
            .oneArgMap { runStep(it) },
        CommandMapper("device", "de").desc(CommandDescriptions.DEVICE_HELP)
            .nonArgMap { listDevice() }
            .oneArgMap { device(it) },
        CommandMapper("reconnect").desc(CommandDescriptions.RECONNECT_HELP).nonArgMap { reconnect() }
    )

    private val DRIVER_ACTION: List<CommandMapper> = listOf(
        CommandMapper("source").desc(CommandDescriptions.SOURCE_HELP)
            .nonArgMap { source() },
        CommandMapper("click").desc(CommandDescriptions.CLICK_HELP)
            .nonArgMap { click("0") }
            .oneArgMap { click(it) },
        CommandMapper("find").desc(CommandDescriptions.FIND_HELP)
            .oneArgMap { findContains(it) }
            .twoArgsMap { by, using -> find(by, using) },
        CommandMapper("send").desc(CommandDescriptions.SEND_HELP)
            .oneArgMap { send("0", it) }
            .twoArgsMap { indexOrID, text -> send(indexOrID, text) },
        CommandMapper("list", "ls").desc(CommandDescriptions.LIST_HELP)
            .nonArgMap { list() },
        CommandMapper("info").desc(CommandDescriptions.INFO_HELP)
            .nonArgMap { info("0") }
            .oneArgMap { info(it) },
        CommandMapper("ss", "screenshot").desc(CommandDescriptions.SCREENSHOT_HELP)
            .nonArgMap { screenshot(requiredCurrentDriver()) }
            .oneArgMap { screenshot(matchElement(it)) }
    )

    private val COMMANDS: List<CommandMapper> =
        merge(FLOW_CONTROL, BASICS, DRIVER_ACTION)

    init {
        driverLoader?.let { loader ->
            loadedDevices.addAll(loader.get().map { Device(it) })
        }
        if (loadedDevices.isNotEmpty()) {
            currentDevice = loadedDevices[0]
        }
    }

    fun run(): ErrorHandleResponse {
        println("cmd debug tool injected.")
        //(use appium desktop 1.12 if sessions won't be loaded automatically)
        // Attach this session with appium desktop if needed.
        listDevice()
        while (true) {
            sleep(100)//allow printStackTrace finish first
            print(">>>>")
            val scanner = Scanner(System.`in`)
            val cmdline = scanner.nextLine()
            val (desiredKeyword, args) = cutArgs(cmdline) ?: continue
            tryRunHandleException { execute(desiredKeyword, args, cmdline) }
            errorHandleResponse?.let {
                return it
            }
        }
    }

    fun withCaseFlow(caseFlow: CaseFlow): LocalDebugTool {
        this.caseFlow = caseFlow
        return this
    }

    private fun reconnect() {
        val device = requiredCurrentDevice()
        device.reconnect()
    }

    private fun screenshot(target: TakesScreenshot) {
        val screenshot = target.getScreenshotAs(OutputType.FILE)
        println("screenshot path: ${color(screenshot.absolutePath, ConsoleColor.BLUE)}")
        sleep(500)
        ShellUtil.open(screenshot.absoluteFile.toString())
    }

    private fun listDevice() {
        println("connection device count: ${loadedDevices.size}")
        for ((idx, device) in loadedDevices.withIndex()) {
            val appiumUrl = device.appiumUrl
            val sessionId = color(device.sessionId(), ConsoleColor.CYAN)
            val message = "idx: $idx, appiumUrl: $appiumUrl, sessionID: $sessionId"
            if (device == currentDevice)
                println(color(message, ConsoleColor.PURPLE))
            else
                println(message)
        }
    }

    private fun device(idxAsString: String) {
        val idx = idxAsString.toInt()
        if (idx >= loadedDevices.size) {
            listDevice()
            throw WithUselessTraceException("Out of Bounds")
        }
        currentDevice = loadedDevices[idx]
        listDevice()
    }

    private fun runStep(conjoinedSteps: String) {
        conjoinedSteps.split(",").map { it.trim() }
            .forEach { runStep0(it) }
    }

    private fun runStep0(idxAsString: String) {
        val steps = (caseFlow ?: throw WithUselessTraceException
            ("caseFlow is NOT loaded, which means step can NOT be loaded")).allStepInfos()
        val pattern = Pattern.compile("[\\d]+")
        if (pattern.matcher(idxAsString).matches()) {
            val idx = idxAsString.toInt()
            if (steps.size <= idx) {
                throw WithUselessTraceException("idx $idx out of limit: " + steps.size)
            }
            steps[idx].execute()
        } else {
            throw WithUselessTraceException("invalid index: $idxAsString")
        }
    }

    private fun printStepHistory() {
        val caseFlow = this.caseFlow ?: throw WithUselessTraceException(
            "caseFlow is NOT loaded, " +
                "which means step can NOT be loaded"
        )
        val steps = caseFlow.allStepInfos()
        for ((idx, it) in steps.withIndex()) {
            val message = "idx: $idx, description: ${it.description}"
            var color =
                if (idx % 2 == 0) ConsoleColor.BLUE else ConsoleColor.BLACK// mix color for easier distinguishing
            if (caseFlow.currentStepIndex == idx) {
                color = ConsoleColor.PURPLE// color current step
            }
            println(color(message, color))
        }
    }

    private fun list() {
        val by = when (requiredCurrentDevice().automationType()) {
            AutomationType.UIAUTOMATOR2, AutomationType.ESPRESSO ->
                MobileBy.xpath("//*[@content-desc or @resource-id or @text]")
            AutomationType.XCUITEST -> MobileBy.iOSNsPredicateString("value LIKE '*' OR name LIKE '*'")
        }
        val elements = findElements(by)
        cachedElements.clear()
        elements.forEach { collectThenPrint(it) }
    }

    private fun collectThenPrint(ele: WebElement) {
        val element = cachedElements.wrap(ele)
        cachedElements.append(element)
        element.print()
    }

    private fun findElements(by: By): List<WebElement> {
        val currentDriver = requiredCurrentDriver()
        val elements = currentDriver.findElements(by)
        println("Got raw ${elements.size} element(s).")
        return elements
    }

    private fun help() {
        println("Help:")
        println(color("flow control commands:", ConsoleColor.RED))
        for (commandMapper in FLOW_CONTROL) {
            commandMapper.printHelp()
        }
        println(color("device action commands:", ConsoleColor.YELLOW))
        for (commandMapper in DRIVER_ACTION) {
            commandMapper.printHelp()
        }
        println(color("basic commands:", ConsoleColor.PURPLE))
        for (commandMapper in BASICS) {
            commandMapper.printHelp()
        }
    }

    private fun findContains(keyword: String) {
        val by = when (requiredCurrentDevice().automationType()) {
            AutomationType.XCUITEST ->
                MobileBy.iOSNsPredicateString("name CONTAINS '$keyword' OR value CONTAINS '$keyword'")
            AutomationType.ESPRESSO,
            AutomationType.UIAUTOMATOR2 ->
                MobileBy.xpath(
                    "//*[contains(@content-desc,'$keyword')" +
                        " or contains(@resource-id,'$keyword')" +
                        " or contains(@text,'$keyword')]"
                )
        }
        findAndCollect(by)
    }

    private fun find(byType: String, using: String) {
        val by = when (byType) {
            "i", "I" -> MobileBy.AccessibilityId(using)
            "u", "U" -> MobileBy.AndroidUIAutomator(using)
            "x", "X" -> MobileBy.xpath(using)
            "p", "P" -> MobileBy.iOSNsPredicateString(using)
            "c", "C" -> MobileBy.iOSClassChain(using)
            "t", "T" -> MobileBy.tagName(using)
            else -> throw WithUselessTraceException("Unsupported byType: $byType")
        }
        findAndCollect(by)
    }

    private fun findAndCollect(by: By) {
        try {
            val elements = findElements(by)
            println("found ${elements.size} elements:")
            elements.forEach { collectThenPrint(it) }
        } catch (e: Throwable) {
            System.err.println("unable to find element by: $by")
            throw e
        }
    }

    private fun info(indexOrID: String) {
        val element = matchElement(indexOrID)
        val byMethod = if (isAndroid()) "UiSelector" else "NsPredicate"

        element.cacheTestID?.let {
            val findByIDSuggestion =
                if (isAndroid()) "new UiSelector().resourceId(\"$it\")"
                else "name == '$it'"
            println("id: $it")
            println("findByID $byMethod suggestion: $findByIDSuggestion")
        }

        element.cacheText?.let {
            val findByTextSuggestion =
                if (isAndroid()) "new UiSelector().text(\"$it\")"
                else "value == '$it'"
            println("text: $it")
            println("findByText $byMethod suggestion: $findByTextSuggestion")
        }

        val location = tryOrNull { element.location }
        println("location: [${location?.x},${location?.y}]")

        val size = tryOrNull { element.size }
        println("size: [${size?.width},${size?.height}]")

        println("displayed: " + element.isDisplayed)
        println("tagName: " + element.tagName)
    }

    private fun click(index: String) {
        val element = matchElement(index)
        element.click()
    }

    private fun send(index: String, text: String) {
        val element = matchElement(index)
        element.sendKeys(text)
    }

    private fun matchElement(idxAsString: String): CachedElement {
        val pattern = Pattern.compile("[\\d]+")
        val idx = if (pattern.matcher(idxAsString).matches()) {
            idxAsString.toInt()
        } else {
            throw WithUselessTraceException("Invalid index: $idxAsString")
        }
        if (idx >= cachedElements.size) {
            cachedElements.print()
            System.err.println("the idx is out of limited")
            throw WithUselessTraceException()
        }
        val element = cachedElements[idx]
        println("focusedElement:")
        element.print()
        return element
    }

    private fun source() {
        val pageSource = requiredCurrentDriver().pageSource
        println(pageSource)
    }

    private fun execute(desiredKeyword: String, args: List<String>, cmdline: String?) {
        println("match command with keyword: $desiredKeyword, args: $args")
        if (!foundMatchCommandExecute(desiredKeyword, args)) {
            help()
            println()
            println("unable to find match cmd with: $cmdline")
        }
    }

    private fun foundMatchCommandExecute(desiredCommandName: String, args: List<String>): Boolean {
        for (commandMapper in COMMANDS) {
            for (commandName in commandMapper.commandNames) {
                if (commandName.equals(desiredCommandName, true)) {
                    val bestMatchExecutor = commandMapper.strategies.filter { it.argsCount() <= args.size }
                        .maxBy(CommandStrategy::argsCount)
                    if (bestMatchExecutor != null) bestMatchExecutor.execute(args)
                    else commandMapper.printHelp()
                    return true
                }
            }
        }
        return false
    }

    private fun isAndroid() = currentDevice?.driver is AndroidDriver

    private fun requiredCurrentDevice() =
        currentDevice ?: (throw WithUselessTraceException("Invalid current device: null"))

    private fun requiredCurrentDriver() = requiredCurrentDevice().driver
}
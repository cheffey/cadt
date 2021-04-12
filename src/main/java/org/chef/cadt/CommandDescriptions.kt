package org.chef.cadt

/**
 * Created by Chef.Xie
 */
object CommandDescriptions {
    const val REDO_HELP =
        "redo the current failed step, you may fix test code and build to hot reload script before rerun"
    const val END_HELP =
        "report the caught failed step as failed, and it WON'T start LocalDebugTool for next failed step"
    const val END_STEP_HELP =
        "report the caught failed step as failed, and it WILL start LocalDebugTool for next failed step"
    const val SKIP_HELP = "reports the caught failed step as SUCCESS, it will continue the test flow and run next step"
    const val CLOSE_DEBUG =
        "close the current debug mode, when you don't want to interact with app at next step failure"
    const val STEP_HELP = "without arg it browses all history steps; with arg match by idx or " +
        "find the only one with (matching keyword) to execute. Support multiply steps, eg: step 1,2,3"
    const val CACHE_HELP = "print CachedElements(found by 'list' and 'find'), option arg: sortBy: i->id, t->text"
    const val DEVICE_HELP = "without arg: list all loaded devices; with index as arg, " +
        "select loaded device as driver interactive command's target"
    const val RECONNECT_HELP = "reconnect current selected device"
    const val DOODLE_HELP =
        "Execute 'doodle' method in LDTDoodleBoard, you can modify LDTDoodleBoard and reload the class by build," +
            " then use 'doodle' command to run desired code"

    const val SOURCE_HELP = "print the source of selected device"
    const val CLICK_HELP = "click cached element with arg: index to select cache element"
    const val FIND_HELP = "find element by arguments(byMethod, usingString) then append to current element cache, " +
        " by methods: u-> uiauotmator, p->NSPredicate, x->xpath, i->id, c->iosClassChain, t->tagName"
    const val SEND_HELP = "send cached element text with args(index, text)"
    const val LIST_HELP = "cache and print all elements found by selected strategy, " +
        "optional arg strategy by default is id, strategies: t->text, i->accessibilityId/uiAutomatorResourceId"
    const val INFO_HELP = "print element info, take the element screenshot then open it"
    const val SCREENSHOT_HELP = "Take a screenshot of current device and open it"
}
package org.chef.cadt.model


/**
 * Created by Chef.Xie
 */

enum class ErrorHandleResponse {
    REDO,     //redo the caught failed step, if the step pass, it will continue the test flow and run next step
    END,      //report the caught failed step as failed, and it WON'T start LocalDebugTool for next failed step
    END_STEP, //report the caught failed step as failed, and it WILL start LocalDebugTool for next failed step
    RESUME    //report the caught failed step as success, it will continue the test flow and run next step
}








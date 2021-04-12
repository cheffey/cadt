##CucumberAppiumDebugTool

###Purpose:
Handle step failure, interact with device through console when STEP FAILS in cucumber test.
*  1. use INTERACTIVE method to find out the state of app. e.q. list, find (element), click, screenshot, info, source
*  2. use REACTION method. Close interaction, return one command to proceed the case run. e.q. redo, resume, end, skipAll
###Setup:
*  1. make IDEA support console input: Help->Edit Custom VM Options->append a new line: **"-Deditable.java.test.console=true"**, restart IDEA
*  2. Support hot reload in IDEA: Preference->search hotswap->select "reload classes after compilation" always or ask
###Usage:
*  1. Start the existing example, with org.chef.example.Runner. When a step is failed, type 'help' then enter. Then try commands listed in instruction.
### WARNING!!!!!
* Keep "TestStep, TestCase" locally, DO NOT merge those customized cucumber java file, it altered cucumber code which may affect CI.

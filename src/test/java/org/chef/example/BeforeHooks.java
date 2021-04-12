package org.chef.example;

import org.chef.Context;
import org.chef.ContextKt;
import org.chef.cadt.LocalDebugTool;

import io.cucumber.java.Before;

public class BeforeHooks {
    @Before(order = 1)
    public void preset() {
        LocalDebugTool.debugMode = ContextKt.DEBUG_MODE;
        LocalDebugTool.driverLoader = Context::getDrivers;
    }

    @Before(order = 2)
    public void before2() {
        System.out.println("before2 fail");
        throw new RuntimeException("before2 fail");
    }

    @Before(order = 3)
    public void before3() {
        System.out.println("before3");
    }

    @Before(order = 4)
    public void before4() {
        System.out.println("before4");
    }
}

package org.chef.example;

import io.cucumber.java.After;

public class AfterHooks {
    @After(order = 1)
    public void after1() {
        System.out.println("after1 fail");
        throw new RuntimeException("after1 fail");
    }

    @After(order = 2)
    public void after2() {
        System.out.println("after2");
    }

    @After(order = 3)
    public void after3() {
        System.out.println("after3");
    }

    @After(order = 4)
    public void after4() {
        System.out.println("after4");
    }
}

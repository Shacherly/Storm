package com.jdbc.util;

import java.time.LocalDate;

public class Test {
    public static void main(String[] args) {
        LocalDate date = LocalDate.now();
        ThreadLocal<LocalDate> th1 = (ThreadLocal<LocalDate>) ThLocalManager.getThLocal("th1");
        th1.set(date);
        // new Thread(() -> {
        //     ThreadLocal<?> th11 = ThLocalManager.getThLocal("th1");
        //     Object o = th11.get();
        //     System.out.println(o);
        // }).start();
        ThLocalUtil.set("th1", date);
        new Thread(() -> {
            Object th11 = ThLocalUtil.get("th1");
            System.out.println(th11);
        }).start();
    }
}

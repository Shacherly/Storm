package com.jdbc.util;

import java.util.HashMap;

public class ThLocalManager {
    private ThLocalManager(){}

    private static HashMap<String, ThreadLocal<?>> localMap = new HashMap<>();

    public static ThreadLocal<?> getThLocal(String name) {
        ThreadLocal<?> threadLocal = localMap.get(name);
        if (threadLocal == null) {
            threadLocal = new ThreadLocal<>();
            localMap.put(name, threadLocal);
        }
        return threadLocal;
    }
}

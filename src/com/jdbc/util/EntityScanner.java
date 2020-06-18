package com.jdbc.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @auther shiZehao
 * @date 09:16 02/01/2020
 * @class EntityScanner
 * @description domian实体对象扫描器，用于生成table
 */
public class EntityScanner {

    private static Map<String, Class<?>> entityMap = new HashMap<>();

    public static Class<?> getEntityClass(String className) {
        return entityMap.get(className);
    }

    public static void loadClass() {
        SelfClassLoader selfClassLoader = new SelfClassLoader(System.getProperty("user.dir"));
        List<String> classNameList = new ArrayList<>();

    }

}

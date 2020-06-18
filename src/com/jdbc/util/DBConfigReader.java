package com.jdbc.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * 用于读取配置文件
 */
public class DBConfigReader {
    // 设为静态  只加载一次
    private static Properties dbConfig;

    static {
        try (InputStream inputStream = Thread.currentThread()
                .getContextClassLoader().getResourceAsStream("dbconfig.properties")) {
            dbConfig = new Properties();
            // 通过当前线程去加载一个文件
            // InputStream inputStream = new FileInputStream("D://Chrome Downloads/LEVEL-ONE/My_Blog/dbconfig.properties");
            dbConfig.load(new InputStreamReader(inputStream));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getConfig(String key) {
        return dbConfig.getProperty(key, "");
    }

    public static String getConfig(String key, String defaultValue) {
        return dbConfig.getProperty(key, defaultValue);
    }

    public static Integer getIntegerValue(String key) {
        return Integer.parseInt(dbConfig.getProperty(key, ""));
    }

    public static Integer getIntegerValue(String key, String defaultValue) {
        return Integer.parseInt(dbConfig.getProperty(key, defaultValue));
    }
}

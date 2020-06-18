package com.jdbc.util;

public class DateUtil {
    private DateUtil() {

    }

    // 获取Unix时间，10位长度的整数
    public static int now() {
        long timestamp = System.currentTimeMillis();
        return (int) (timestamp / 1000);
    }
}

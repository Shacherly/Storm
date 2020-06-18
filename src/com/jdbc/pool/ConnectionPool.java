package com.jdbc.pool;

import com.jdbc.util.DBConfigReader;

import java.sql.Connection;
import java.util.Arrays;

public class ConnectionPool {
    private static final int STATUS_FREE = 0;
    private static final int STATUS_BUSY = 1;
    private static final int STATUS_NULL = -1;
    // 设置一个容器，选数组 占用空间小
    private static Connection[] connectionList = new Connection[DBConfigReader.getIntegerValue("minPoolSize", "1")];
    // 设置另一个容器  存放连接对应的状态  占用 释放  空置
    private static byte[] connectionBitMap = new byte[DBConfigReader.getIntegerValue("minPoolSize", "1")];// 位图
    // 记录个数
    private static int total = 0;

    // 块就是用来初始化的
    static {
        // 初始状态-1
        Arrays.fill(connectionBitMap, (byte) -1);
    }

    // 设计分配的方法，给出索引，看该位置是否能被使用
    private static Connection distribute(int index) {
        Connection connection = null;
        if (connectionBitMap[index] == STATUS_BUSY) {// 如果被占用那就没有连接供使用了
            return null;
        }
        if (connectionBitMap[index] == STATUS_NULL) {// 目前为止没有创建过为空状态null
            connection = new ConnectionProxy(index);
            connectionList[index] = connection;
            total++;
        } else if (connectionBitMap[index] == STATUS_FREE) {// 已经被创建，但处于可被使用的状态
            connection = connectionList[index];
        }
        connectionBitMap[index] = STATUS_BUSY;// 占有连接后切换状态
        return connection;
    }

    // 释放连接
    protected static synchronized void giveBack(ConnectionProxy connectionProxy) {
        connectionBitMap[connectionProxy.getIndex()] = STATUS_FREE;
    }

    // 给用户提供一个从连接池中获取连接的方法，加上锁
    public static synchronized Connection getConnection() {
        int freeIndex = getFreeIndex();
        if (freeIndex > -1) {// 闲置连接STATUS_FREE的位置为-1，表示5个连接都被占用了
            return distribute(freeIndex);
        } else if (total < DBConfigReader.getIntegerValue("maxPoolSize", "10")) {// 初始容量是5个，如果连接数超过5个但是没有超过10个可以进行扩容
            int nullIndex = getNullIndex();
            if (nullIndex == -1) {// 空连接STATUS_NULL的位置为-1，表示5个连接都被创建过了
                nullIndex = gorw();
            }
            return distribute(nullIndex);
        }
        return null;// 容量扩为2倍后不够就不再扩了
    }

    // 扩容，加载因子2
    private static int gorw() {
        Connection[] newConnectionList = new Connection[connectionList.length * 2];
        byte[] newConnectionBitMap = new byte[connectionBitMap.length * 2];
        try {
            System.arraycopy(connectionList, 0, newConnectionList, 0, newConnectionList.length);
            System.arraycopy(connectionBitMap, 0, newConnectionBitMap, 0, newConnectionBitMap.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int firstNullIndex = connectionList.length;
        // 换一个新的指向
        connectionList = newConnectionList;// 长度已经增加了
        connectionBitMap = newConnectionBitMap;
        for (int i = firstNullIndex; i < connectionBitMap.length; i++) {
            connectionBitMap[i] = -1;// 初始化
        }
        return firstNullIndex;
    }

    // 返回 处于空闲状态的连接的位置
    private static int getFreeIndex() {
        for (int i = 0; i < connectionBitMap.length; i++) {
            if (connectionBitMap[i] == STATUS_FREE) {
                return i;
            }
        }
        return -1;
    }

    // 返回 尚未创建过连接的位置
    private static int getNullIndex() {
        for (int i = 0; i < connectionBitMap.length; i++) {
            if (connectionBitMap[i] == STATUS_NULL) {
                return i;
            }
        }
        return -1;
    }

}

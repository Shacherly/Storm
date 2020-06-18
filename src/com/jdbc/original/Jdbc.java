package com.jdbc.original;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Properties;

public class Jdbc {
    private static Connection connection;

    static {
        try {
            // Class.forName("com.mysql.cj.jdbc.Driver");
            // new Driver();
            // 上面两种都行  ******
            System.setProperty("jdbc.driver", "com.mysql.cj.jdbc.Driver");
            Properties properties = System.getProperties();
            Enumeration<?> en = properties.propertyNames();
            while (en.hasMoreElements()) {
                Object o = en.nextElement();
                System.out.print(o + " = ");
                System.out.println(properties.get(o));
            }
            String url = "jdbc:mysql://localhost:3306/myblog?" +
                    "characterEncoding=UTF-8&useSSL=true&serverTimezone=UTC";
            String user = "root";
            String pass = "111111";
            connection = DriverManager.getConnection(url, user, pass);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws SQLException {
        // insert();
        update();
    }

    public static void update() throws SQLException {
        String sql = "update atm set abalance = 8080 where aname = \"u1\"";
        connection.setAutoCommit(false);
        connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        Statement statement = connection.createStatement();
        int affectedRows = statement.executeUpdate(sql);
        System.out.println("影响行数：" + affectedRows);
        connection.commit();
        close(statement, connection);
    }

    public static void insert() throws SQLException {
        String sql = "insert into atm values(\"u7\", \"s7\", 5555)";
        Statement statement = connection.createStatement();
        int affectedRows = statement.executeUpdate(sql);
        System.out.println("影响行数：" + affectedRows);
        close(statement, connection);
    }


    public static void close(Statement statement, Connection connection) {
        try {
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

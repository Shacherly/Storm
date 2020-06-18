package com.jdbc.sqlsession;

import com.jdbc.pool.ConnectionPool;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("all")
public class SqlSessionFactory {

    private static volatile SqlSessionFactory sessionFactory;

    private SqlSessionFactory() {

    }

    // 真正的单例模式
    public static SqlSessionFactory getInstance() {
        if (sessionFactory == null) {
            synchronized (SqlSessionFactory.class) {
                if (sessionFactory == null) {
                    sessionFactory = new SqlSessionFactory();
                }
            }
        }
        return sessionFactory;
    }

    /**
     * 增
     * @param sql
     * @param values
     * @return 新增的记录主键ID，Long型的
     * @throws SQLException
     */
    public long insert(String sql, Object[] values) {
        System.out.println("SQL   :" + sql);
        System.out.println("Params:" + Arrays.toString(values));
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = ConnectionPool.getConnection();
            // 注意这里如果没有去适配器中实现对应的方法会出现空指针
            preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            // 将sql和？信息拼接完整
            for (int i = 0; i < values.length; i++) {
                // 设置问号的索引位置对应的values值。
                preparedStatement.setObject(i + 1, values[i]);
            }
            preparedStatement.executeUpdate();

            // 存储临时新增的数据
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                preparedStatement.close();
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    /**
     * 删、改
     * @param sql
     * @param values
     * @return
     * @throws SQLException
     */
    // 方法只需要传递sql语句和“问号”占位符对应的一组参数数组即可
    public int update(String sql, Object[] values) {
        System.out.println("SQL   :" + sql);
        System.out.println("Params:" + Arrays.toString(values));
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        int affectedRows = 0;
        try {
            connection = ConnectionPool.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            // 将sql和？信息拼接完整
            for (int i = 0; i < values.length; i++) {
                preparedStatement.setObject(i + 1, values[i]);
            }
            affectedRows = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return affectedRows;
    }

    /**
     * @auther shiZehao
     * @date 10:00 02/18/2020
     * @param returnType Class<?> 方法返回值类型，？是因为返回值不确定是哪种类型的集合
     * @param resultType 查询的结果集类型
     * @description 查询方法，返回单条或者集合。
     *
     */
    public <E> Object select(String sql, Object[] values, Class<?> returnType, Class<E> resultType) {
        System.out.println("SQL   :" + sql);
        System.out.println("Params:" + Arrays.toString(values));
        Connection connection = ConnectionPool.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;// 将结果集信息取出来
        try {
            preparedStatement = connection.prepareStatement(sql);

            if (values != null) {
                // 将sql和？信息拼接完整
                for (int i = 0; i < values.length; i++) {
                    preparedStatement.setObject(i + 1, values[i]);
                }
            }

            resultSet = preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Collection<E> collection = null;
        E obj = null;

        try {// 正常为集合类型，catch中异常为非集合类型：如实体类、String、Integer、Date等
            if (returnType.isInterface()) {// List Set之类的
                collection = collecton(returnType);
            } else {// ArrayList、HashSet之类的具体实现类
                collection = (Collection<E>) returnType.newInstance();
            }

            try {
                while (resultSet.next()) {
                    E e = constructByResultSet(resultSet, resultType);
                    collection.add(e);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return collection;
        } catch (Exception e) {

            try {
                if (resultSet.next()) {
                    obj = constructByResultSet(resultSet, resultType);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return obj;

        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 未使用
     */
    private <T> T selectOne(String sql, Object[] values, Class<?> returnType, Class<T> resultType) throws SQLException, InstantiationException, IllegalAccessException {
        System.out.println("SQL:   " + sql);
        System.out.println("Params:" + Arrays.toString(values));
        Connection connection = ConnectionPool.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        if (values != null) {
            // 将sql和？信息拼接完整
            for (int i = 0; i < values.length; i++) {
                preparedStatement.setObject(i + 1, values[i]);
            }
        }

        ResultSet resultSet = preparedStatement.executeQuery();// 将结果集信息取出来
        T obj = null;
        if (resultSet.next()) {
            obj = constructByResultSet(resultSet, resultType);
        }
        return obj;
    }

    // 构建对象（其中有充填属性值的过程）
    private <T> T constructByResultSet(ResultSet resultSet, Class<T> resultTypeClass) throws InstantiationException, IllegalAccessException, SQLException {
        T t = constructByResultType(resultTypeClass);

        for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
            String columnName = resultSet.getMetaData().getColumnName(i);
            if (t == null) {
                // 证明是简单类型
                return resultSet.getObject(columnName, resultTypeClass);
            }
            setFieldValue(t, columnName, resultSet);
        }
        return t;
    }

    // 填充类对象的属性值

    /**
     * @auther shiZehao
     * @date 22:22 02/18/2020
     * @param obj 需要被构建的对象，此参数传入就不会再变
     * @param columnName 当前列名，对应对象的字段名
     * @param resultSet 结果集
     * @description 遍历对象的所有属性，复合属性也会被分解
     */
    private void setFieldValue(Object obj, String columnName, ResultSet resultSet) throws SQLException, IllegalAccessException {
        if (obj == null) {
            obj = resultSet.getObject(columnName);
            return;
        }

        String fieldName = formatAsField(columnName);
        // 寻找所有类字段
        Field[] declaredFields = obj.getClass().getDeclaredFields();
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            // 基本类型或者包装类
            if (isGenericType(declaredField.getType())) {
                if (declaredField.getName().equals(fieldName)) {
                    // 获取字段的类型String Integer Long Date等等
                    Class<?> fieldType = declaredField.getType();
                    // 通过反射为字段设置该字段的值
                    declaredField.set(obj, resultSet.getObject(columnName, fieldType));
                }
            } else {// 分解设置复合属性，此举的返回值无意义，因此下面返回null
                setFieldValue(declaredField.get(obj), columnName, resultSet);
            }
        }
    }

    // 下划线转驼峰  student_name_area --->>> studentNameArea
    private String formatAsField(String columnName) {
        columnName = columnName.toLowerCase();

        // 生成匹配格式
        Pattern pattern = Pattern.compile("_(\\w)");
        Matcher matcher = pattern.matcher(columnName);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            // matcher.group(1)该格式匹配到之后从找到索引为1的变大写，0号索引是下划线
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        // 把最后一次匹配到内容   之后的字符串  追加到 StringBuffer 中
        matcher.appendTail(sb);
        return sb.toString();
    }

    // 驼峰转下划线 studentNameArea  --->>>   STUDENT_NAME_AREA
    private String formatAsColumn(String fieldName) {
        Pattern pattern = Pattern.compile("[A-Z]");
        Matcher matcher = pattern.matcher(fieldName);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            // 找到第一个大写的替换为前面加下划线
            matcher.appendReplacement(sb, "_" + matcher.group(0));
        }
        matcher.appendTail(sb);
        return sb.toString().toUpperCase();
    }

    private String formatAsTable(String className) {
        return formatAsColumn(className).substring(1, formatAsColumn(className).length());
    }

    // insert时根据对象自动拼串
    public long insert(Object[] args) {
        Class<?> aClass = args[0].getClass();
        StringBuilder sql = new StringBuilder("INSERT INTO " + formatAsTable(aClass.getSimpleName()).toUpperCase() + " (");
        Field[] declaredFields = aClass.getDeclaredFields();
        // 使用LinkedHashSet保证迭代顺序
        Set<Field> fieldSet = new LinkedHashSet<>();
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            // Type genericType = declaredField.getGenericType();
            try {
                if (declaredField.get(args[0]) != null && declaredField.getModifiers() == Modifier.PRIVATE) {
                    fieldSet.add(declaredField);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        Iterator<Field> iterator1 = fieldSet.iterator();
        do {
            sql.append(formatAsColumn(iterator1.next().getName()));
            if (iterator1.hasNext()) {
                sql.append(", ");
            }
        } while (iterator1.hasNext());
        sql.append(") VALUES (");

        // 不用Set防止重复的值被覆盖
        List<Object> fieldValueList = new LinkedList<>();
        // Set<Object> fieldValueSet = new LinkedHashSet<>();
        Iterator<Field> iterator2 = fieldSet.iterator();
        do {
            try {
                fieldValueList.add(iterator2.next().get(args[0]));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            sql.append("?");
            if (iterator2.hasNext()) {
                sql.append(", ");
            }
        } while (iterator2.hasNext());
        sql.append(")");

        return insert(sql.toString(), fieldValueList.toArray());
    }

    /**
     * 根据select方法的返回值集合类型创建对应的集合
     * @param returnTypeClass
     * @param <E>
     * @return
     * @throws Exception
     */
    private Collection collecton(Class returnTypeClass) throws Exception {
        if (returnTypeClass == List.class) {
            return new ArrayList<>();
        }
        if (returnTypeClass == Set.class) {
            return new HashSet<>();
        }
        if (returnTypeClass == Collection.class) {
            return new ArrayList<>();
        }
        throw new Exception("不支持的集合类型:" + returnTypeClass.getName());
    }


    /**
     * 根据注解中的ResultType构建对象，如果属性不是基本类型则递归
     * @param resultTypeClass
     * @param <T>
     * @return
     */
    private <T> T constructByResultType(Class<T> resultTypeClass) throws IllegalAccessException, InstantiationException {
        /*
        如果是基本类型则不需要构建对象，直接返回null从ResultSet中取值,
        就是当select的是String就不用下面的递归构建对象了
         */
        if (resultTypeClass == null || isGenericType(resultTypeClass)) {
            return null;
        }
        T t = resultTypeClass.newInstance();// 这样new出来的对象字段全部为null
        Field[] declaredFields = resultTypeClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            if (!isGenericType(declaredField.getType())) {
                declaredField.setAccessible(true);
                // 为对象t赋予该字段的值，只是赋予一个引用。
                declaredField.set(t, constructByResultType(declaredField.getType()));
            }
        }
        return t;
    }

    /**
     * 是否为通用类型，包装类、基本类、String、Date
     * @param fieldTypeClass
     * @return
     */
    private boolean isGenericType(Class<?> fieldTypeClass) {
        return isWrapper(fieldTypeClass) || isPrimitive(fieldTypeClass)
                || fieldTypeClass == Date.class || fieldTypeClass == String.class;
    }

    /**
     * 判断字段类型是否为包装类型
     * @param fieldTypeClass
     * @return
     */
    private boolean isWrapper(Class<?> fieldTypeClass) {
        try {
            return ((Class<?>) fieldTypeClass.getField("TYPE").get(null)).isPrimitive();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断字段类型是否为基本类型
     * @param fieldTypeClass
     * @return
     */
    private boolean isPrimitive(Class<?> fieldTypeClass) {
        return fieldTypeClass.isPrimitive();
    }


}

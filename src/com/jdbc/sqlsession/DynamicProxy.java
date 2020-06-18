package com.jdbc.sqlsession;

import com.jdbc.annotation.SQL;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;

// Dao类的代理对象，不用DAO来调用方法，直接变接口，使用代理类去调用
@SuppressWarnings("unchecked")
public class DynamicProxy {

    // 代理对象不确定,传递真实对象的clas，返回Object更通用
    public static <T> T getProxyInstance(Class<T> clazz) {
        // 三个参数，类加载器、代理类的数组、代理的方法
        // 使用JDk原生的Proxy
        /*
         * 需要三个参数：
         * 1、委托类的类加载器
         * 2、委托类的Class数组
         * 3、InvocationHandler类型的委托类的某个方法代理
         */
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class[]{clazz},
                new MethodProxy()
        );

    }

    // inner static class
    private static class MethodProxy implements InvocationHandler {
        /**
         * 这才是代理对象处理逻辑的方法，帮助委托类操作原本该他操作的方法。
         * 反正以下三个参数就是这么个意思……
         * @param proxy 代理对象
         * @param method 代理对象要调用的方法的Menthod对象
         * @param args 调用方法需要传入的参数
         * @return Obj
         * @throws Throwable
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (Object.class.equals(method.getDeclaringClass())) {// 传进来的是实例
                // 此时的this就是执行method方法的对象，而method就是原来那个本该在Dao中被执行的方法
                return method.invoke(this, args);
            }
            // 表明委托类是接口，代理需要干活了
            // 读取sql语句、告诉SqlSessionFactory去执行

            return execute(method, args);
        }

        private Object execute(Method method, Object[] args) throws Exception {
            SQL sqlAnno = method.getAnnotation(SQL.class);
            if (sqlAnno == null) {
                throw new Exception("Sql Syntax Error");
            }
            SqlSessionFactory sessionFactory = SqlSessionFactory.getInstance();
            // switch有return就不用写break了，return后面的语句从来不会到达
            switch (sqlAnno.type()) {
                case INSERT:
                    return sessionFactory.insert(args);
                case DELETE:
                    return null;
                case UPDATE:
                    return sessionFactory.update(sqlAnno.sql(), args);
                case SELECT:
                    return sessionFactory.select(sqlAnno.sql(), args, method.getReturnType(), sqlAnno.resultType());
            }
            return new ArrayList<>();
        }
    }
}
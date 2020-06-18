package com.jdbc.annotation;

import com.jdbc.sqlsession.SqlExecutorEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SQL {

    String sql() default "";

    SqlExecutorEnum type();

    Class<?> resultType() default Object.class;
}

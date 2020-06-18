package com.jdbc.sqlsession;

/**
 * @auther shiZehao
 * @date 15:57 01/31/2020
 * @class SqlExecutorEnum
 * @description 描述sql的增删改查类型
 */
public enum SqlExecutorEnum {

    INSERT,
    DELETE,
    UPDATE,
    SELECT,

    /**
     * 可以不写，直接在sql句首判断
     */
    UNDEFINED

}

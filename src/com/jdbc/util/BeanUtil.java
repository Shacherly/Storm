package com.jdbc.util;


import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BeanUtil {
    private BeanUtil() {

    }

    public static final Map<String, Function<String, ?>> CONVERTER_MAP = new HashMap<>(32);

    static {
        /**
         * Gson 解析带小数点的都是Double
         * 不带小数点的纯数字是String
         *
         * 传入需要转换的类型，就可以获取到类型转换器
         */
        CONVERTER_MAP.put("byte", variable -> new Double(variable).byteValue());
        CONVERTER_MAP.put("short", variable -> new Double(variable).shortValue());
        CONVERTER_MAP.put("int", variable -> new Double(variable).intValue());
        CONVERTER_MAP.put("integer", variable -> new Double(variable).intValue());
        CONVERTER_MAP.put("long", variable -> new Double(variable).longValue());
        CONVERTER_MAP.put("float", variable -> new Double(variable).floatValue());
        CONVERTER_MAP.put("double", Double::parseDouble);
        CONVERTER_MAP.put("boolean", Boolean::parseBoolean);
        CONVERTER_MAP.put("bigdecimal", BigDecimal::new);
    }

    /**
     * Determines if the specified {@code typeClass} is assignment-compatible
     * with those genetic Class
     * @see     java.lang.Boolean
     * @see     java.lang.Character
     * @see     java.lang.Byte
     * @see     java.lang.Short
     * @see     java.lang.Integer
     * @see     java.lang.Long
     * @see     java.lang.Float
     * @see     java.lang.Double
     * @see     java.lang.Void
     * @see     java.util.Date
     * @see     java.lang.String
     * @param typeClass
     * @return
     */
    public static boolean isGeneric(Class<?> typeClass) {
        try {
            return isPrimitive(typeClass) || typeClass == Date.class
                    || typeClass == String.class || typeClass == BigDecimal.class
                    || ((Class<?>) typeClass.getField("TYPE").get(typeClass)).isPrimitive();
        } catch (IllegalAccessException | NoSuchFieldException e) {
            // e.printStackTrace();
            return false;
        }
    }

    /**
     * is primitive?
     * @param typeClass
     * @return
     */
    public static boolean isPrimitive(Class<?> typeClass) {
        return typeClass.isPrimitive();
    }

    /**
     * get a simple name
     * @param typeClass
     * @return
     * @throws Exception
     */
    public static String getPrimitiveName(Class<?> typeClass) throws Exception {
        // 直接返回String
        if (isPrimitive(typeClass))
            return typeClass.toString();
        // 解析其他类型
        if (isGeneric(typeClass))
            return typeClass.getSimpleName().toLowerCase();
        // BigDecimal
        return "UnSupportedType";
    }

    /**
     * 构建嵌套对象的方法
     * @param tClass
     * @param item
     * @param <T>
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static <T> T build(Class<T> tClass, Map<String, Object> item) throws IllegalAccessException, InstantiationException {
        T t = tClass.newInstance();
        /**
         * 获取转换器，当从item的Map中取值String值时转换成需要的值。
         */
        Map<String, Function<String, ?>> converter = BeanUtil.CONVERTER_MAP;

        List<Field> declaredFields = Arrays.asList(tClass.getDeclaredFields());
        declaredFields.forEach(field -> field.setAccessible(true));
        declaredFields.stream().filter(FieldUtil::serializableField)
                .collect(Collectors.toList())
                .forEach(field -> {
                    try {
                        // 将属性名转为 a_b这样的形式获取item中的值
                        String fieldName = field.getName();
                        String key = FieldUtil.underLine(fieldName);
                        /**
                         * 获取字段类型，如果是嵌套其他类型，还要递归构建，但是现在已经没有使用嵌套属性了。
                         */
                        Class<?> fieldType = field.getType();
                        String fieldTypeName = getPrimitiveName(fieldType);
                        boolean isGeneric = isGeneric(fieldType);

                        if (isGeneric) {
                            if (Objects.equals(fieldName, "traderId"))
                                field.set(t, FieldUtil.convertType(((Map<String, Object>) item.get("owner")).get("id"), fieldType));
                            else if (Objects.equals(fieldName, "traderNickname"))
                                field.set(t, FieldUtil.convertType(((Map<String, Object>) item.get("owner")).get("nickname"), fieldType));
                            else
                                field.set(t, FieldUtil.convertType(item.get(key), fieldType));

                        } else {
                            // Object o = fieldType.newInstance();
                            Object innerType = build(fieldType, (Map<String, Object>) item.get(key));
                            field.set(t, innerType);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        return t;
    }
}


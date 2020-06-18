package com.jdbc.util;

import com.jdbc.bean.Hero;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FieldUtil implements Serializable {

    private String s = "123";
    private static final long serialVersionUID = 3137919628786415627L;

    private FieldUtil() {

    }

    /**
     * 将字符串转换为需要的类型
     * @param source
     * @param target
     * @param <T>
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static <T> T convertType(Object source, Class<T> target) throws Exception {
        Optional<Object> optional = Optional.ofNullable(source);
        String src = optional.map(String::valueOf).orElseThrow(NullPointerException::new);
        if (target == String.class) return (T) src;
        // 获取转换器的key
        String primitiveName = BeanUtil.getPrimitiveName(target);
        // 去Map中藏key对应的转换器
        return (T) BeanUtil.CONVERTER_MAP.get(primitiveName).apply(src);
    }

    /**
     * 将驼峰的属性转为下划线
     * @param property
     */
    // public static String convertFiledName(String property) {
    //
    // }
    public static String underLine(String src) {
        Optional<String> optional = Optional.ofNullable(src);
        Function<String, String> strHandler = copy -> {
            Matcher matcher = Pattern.compile("[A-Z]").matcher(copy);
            for (; ; ) {
                if (matcher.hitEnd()) break;
                if (matcher.find())
                    copy = copy.replaceFirst(matcher.group(), "_" + matcher.group().toLowerCase());
            }
            return copy;
        };
        return optional.map(strHandler).orElseGet(String::new);
    }

    /**
     * 判断是否是正常的可序列化属性，排除serialVersionUID属性
     * @param field
     * @return
     */
    public static boolean serializableField(Field field) {
        field.setAccessible(true);
        return field.getModifiers() == (Modifier.PRIVATE);
    }

    public static void main(String[] args) throws NoSuchFieldException {
        // String reg = "[A-Z]";
        // System.out.println("A111".matches(reg));
        String src = "asdFhjKll";
        String copy = new String(src);
        Matcher matcher = Pattern.compile("[A-Z]").matcher(src);
        for (; ; ) {

            if (matcher.hitEnd()) break;
            if (matcher.find()) {
                System.out.println(matcher.start());
                System.out.println(matcher.group());
                src = src.replaceFirst(matcher.group(), "_" + matcher.group().toLowerCase());
                // matcher.reset();
            }
        }
        System.out.println(underLine(null) + "?");

        Field s = FieldUtil.class.getDeclaredField("s");
        s.setAccessible(true);
        System.out.println(s.getModifiers() == Modifier.PRIVATE);

        // try {
        //     BeanUtil.build(Hero.class, null);
        // } catch (IllegalAccessException | InstantiationException e) {
        //     e.printStackTrace();
        // }


    }
}

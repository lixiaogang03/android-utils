package com.wif.baseservice.reflex;

import java.lang.reflect.Field;

public class Reflex {

    /**
     * 通过反射获取
     *
     * @param object Object
     * @param name   字段名
     * @return 对应的字段
     * @throws Exception 获取失败, 抛出异常
     */
    public static Object getField(final Object object, final String name) throws Exception {
        Field field = object.getClass().getField(name);
        return field.get(object);
    }

    /**
     * 通过反射获取
     *
     * @param object Object
     * @param name   字段名
     * @return 对应的字段
     * @throws Exception 获取失败, 抛出异常
     */
    public static Object getDeclaredField(final Object object, final String name) throws Exception {
        Field field = object.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(object);
    }

    /**
     * 通过反射枚举类, 进行设置
     *
     * @param object Object
     * @param value  设置参数值
     * @param name   字段名
     * @throws Exception 设置失败, 抛出异常
     */
    public static void setEnumField(final Object object, final String value, final String name) throws Exception {
        Field field = object.getClass().getField(name);
        field.set(object, Enum.valueOf((Class<Enum>) field.getType(), value));
    }

    /**
     * 通过反射, 进行设置
     *
     * @param object Object
     * @param val    设置参数值
     * @param name   字段名
     * @throws Exception 设置失败, 抛出异常
     */
    public static void setValueField(final Object object, final Object val, final String name) throws Exception {
        Field field = object.getClass().getField(name);
        field.set(object, val);
    }

}

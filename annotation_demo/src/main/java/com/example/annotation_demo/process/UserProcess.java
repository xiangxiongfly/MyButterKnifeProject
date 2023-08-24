package com.example.annotation_demo.process;

import com.example.annotation_demo.annotation.Formatter;
import com.example.annotation_demo.annotation.Label;
import com.example.annotation_demo.bean.User;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UserProcess {
    public static String parse(User user) {
        Class<? extends User> clz = user.getClass();
        StringBuilder builder = new StringBuilder();
        //获取所有成员变量
        Field[] declaredFields = clz.getDeclaredFields();
        for (Field field : declaredFields) {
            //设置访问权限
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            //是否被@Label注解
            if (field.isAnnotationPresent(Label.class)) {
                //获取Label注解
                Label label = field.getAnnotation(Label.class);
                //获取注解值
                String name = label.value();
                try {
                    //获取变量值
                    Object value = field.get(user);
                    if (value != null) {
                        //变量类型为String
                        if (field.getType() == String.class) {
                            builder.append(name).append(":").append(value).append("\n");
                        }
                        //变量类型为Date
                        else if (field.getType() == Date.class) {
                            value = formatDate(field, value);
                            builder.append(name).append(":").append(value).append("\n");
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return builder.toString();
    }

    private static Object formatDate(Field field, Object value) {
        //是否被@Formatter注解
        if (field.isAnnotationPresent(Formatter.class)) {
            Formatter formatter = field.getAnnotation(Formatter.class);
            String format = formatter.format();
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.format(value);
        }
        return value;
    }
}

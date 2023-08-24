package com.example.lib_reflection;

import android.app.Activity;
import android.view.View;

import java.lang.reflect.Field;

public class MyButterKnife {
    public static void bind(Activity activity) {
        //获取类的所有成员
        Field[] fields = activity.getClass().getDeclaredFields();
        for (Field field : fields) {
            //判断字段是否有BindView注解
            if (field.isAnnotationPresent(BindView.class)) {
                //获取注解
                BindView annotation = field.getAnnotation(BindView.class);
                if (annotation != null) {
                    //设置访问权限
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    //获取注解值
                    int id = annotation.value();
                    //获取View
                    View view = activity.findViewById(id);
                    try {
                        //通过反射设置值
                        field.set(activity, view);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

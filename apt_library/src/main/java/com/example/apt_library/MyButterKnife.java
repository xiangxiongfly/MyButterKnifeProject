package com.example.apt_library;

import android.app.Activity;

import java.lang.reflect.Constructor;

public class MyButterKnife {
    public static void bind(Activity activity) {
        //获取Activity的Class
        Class<?> clz = activity.getClass();
        //拼接获取ViewBinding的类型
        String className = clz.getName() + "_ViewBinding";
        try {
            //获取ViewBinding的Class
            Class<?> viewBinderClz = Class.forName(className);
            //通过反射构造函数创建
            Constructor<?> constructor = viewBinderClz.getConstructor(activity.getClass());
            constructor.newInstance(activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

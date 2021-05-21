package com.example.apt_library;

import android.app.Activity;

import java.lang.reflect.Constructor;

public class MyButterKnife {
    public static void bind(Activity activity) {
        Class<?> aClass = activity.getClass();
        String binderName = aClass.getName() + "ViewBinding";
        try {
            Class<?> viewBinderClz = Class.forName(binderName);
            Constructor<?> constructor = viewBinderClz.getConstructor(activity.getClass());
            constructor.newInstance(activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

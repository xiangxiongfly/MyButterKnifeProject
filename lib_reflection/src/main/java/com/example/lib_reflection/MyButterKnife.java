package com.example.lib_reflection;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class MyButterKnife {
    public static void bind(Activity activity) {
        //获取类中所有变量
        Field[] fields = activity.getClass().getDeclaredFields();
        //获取类中所有方法
        Method[] methods = activity.getClass().getDeclaredMethods();

        bindFields(activity, fields);
        bindMethods(activity, methods);
    }

    /**
     * 绑定变量
     *
     * @param activity
     * @param fields
     */
    private static void bindFields(Activity activity, Field[] fields) {
        for (Field field : fields) {
            //判断是否被@BindView注解
            if (field.isAnnotationPresent(BindView.class)) {
                //获取@BindView注解
                BindView bindView = field.getAnnotation(BindView.class);
                if (bindView != null) {
                    //设置访问权限
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    //获取注解值
                    int id = bindView.value();
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

    /**
     * 绑定方法
     *
     * @param activity
     * @param methods
     */
    private static void bindMethods(Activity activity, Method[] methods) {
        for (Method method : methods) {
            //判断是否被@OnClick或@OnLongClick注解
            if (method.isAnnotationPresent(OnClick.class) || method.isAnnotationPresent(OnLongClick.class)) {
                //获取方法上的所有注解
                Annotation[] annotations = method.getAnnotations();
                //遍历注解
                for (Annotation annotation : annotations) {
                    //获取注解的注解类型
                    Class<? extends Annotation> annotationType = annotation.annotationType();
                    //判断注解是否为@EventType
                    if (annotationType.isAnnotationPresent(EventType.class)) {
                        //获取EventType注解
                        EventType eventType = annotationType.getAnnotation(EventType.class);
                        //获取事件类的Class
                        assert eventType != null;
                        Class methodClass = eventType.methodClass();
                        //获取事件方法
                        String methodName = eventType.methodName();
                        //设置访问权限
                        method.setAccessible(true);

                        try {
                            //获取OnClick或OnLongClick的value值
                            Method valueMethod = annotationType.getDeclaredMethod("value");
                            //获取绑定的id
                            int[] viewIds = (int[]) valueMethod.invoke(annotation);
                            //代理对象
                            Object proxy = Proxy.newProxyInstance(methodClass.getClassLoader(),
                                    new Class[]{methodClass},
                                    new InvocationHandler() {
                                        @Override
                                        public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
                                            return method.invoke(activity, args);
                                        }
                                    });
                            assert viewIds != null;
                            //遍历id并绑定事件
                            for (int id : viewIds) {
                                //获取Activity的View
                                View view = activity.findViewById(id);
                                //获取指定方法，如setOnClickListener方法，参数类型是OnClickListener
                                Method clickMethod = view.getClass().getMethod(methodName, methodClass);
                                //执行方法
                                clickMethod.invoke(view, proxy);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}

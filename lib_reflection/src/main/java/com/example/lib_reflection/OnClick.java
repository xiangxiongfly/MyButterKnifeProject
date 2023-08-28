package com.example.lib_reflection;

import android.view.View;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import androidx.annotation.IdRes;

/**
 * 绑定点击事件
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@EventType(methodClass = View.OnClickListener.class, methodName = "setOnClickListener")
public @interface OnClick {
    @IdRes int[] value() default {};
}

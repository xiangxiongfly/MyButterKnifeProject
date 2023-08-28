package com.example.lib_reflection;

import android.view.View;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import androidx.annotation.IdRes;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@EventType(methodClass = View.OnLongClickListener.class, methodName = "setOnLongClickListener")
public @interface OnLongClick {
    @IdRes int[] value() default {};
}

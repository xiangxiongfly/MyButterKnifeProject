package com.example.annotation_demo;

import com.example.annotation_demo.bean.User;
import com.example.annotation_demo.process.UserProcess;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Demo {
    public static void main(String[] args) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date birthday = sdf.parse("2008-09-10");
        User user = new User("小明", birthday, new Date());
        String userInfo = UserProcess.parse(user);
        System.out.println(userInfo);
    }
}
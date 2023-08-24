package com.example.annotation_demo.bean;

import com.example.annotation_demo.annotation.Formatter;
import com.example.annotation_demo.annotation.Label;

import java.util.Date;

public class User {
    @Label("姓名")
    private String name;

    @Label("生日")
    @Formatter(format = "yyyy年MM月dd日")
    private Date birthday;

    @Label("现在时间")
    @Formatter
    private Date nowDate;

    public User(String name, Date birthday, Date nowDate) {
        this.name = name;
        this.birthday = birthday;
        this.nowDate = nowDate;
    }
}

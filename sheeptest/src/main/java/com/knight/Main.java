package com.knight;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        String s = "13899538726";
        String regex = "1[38]\\d{9}";//定义手机好规则
        boolean flag = s.matches(regex);//判断功能
        s.matches(regex);

        Pattern pattern = Pattern.compile(".*.*.*abc");
        pattern.split("xxxabc");

        System.out.println("flag:" + flag);
    }
}

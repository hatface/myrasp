package com.knight;

public class Main {
    public static void main(String[] args)
    {
        String s = "13899538726";
        String regex = "1[38]\\d{9}";//定义手机好规则
        boolean flag = s.matches(regex);//判断功能
        System.out.println("flag:"+flag);
    }
}

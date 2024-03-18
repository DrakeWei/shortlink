package com.drake.shortlink.admin.common.constant;

public class RedisCacheConstant {

    //用户注册分布式锁KEY
    public static final String USER_REGISTER_LOCK="lock:register:";
    //用户登录token存储KEY
    public static final String USER_LOGIN_TOKEN="user:login:";
    //用户新建分组分布式锁
    public static final String GROUP_CREATE_LOCK="lock:group:create:";
}
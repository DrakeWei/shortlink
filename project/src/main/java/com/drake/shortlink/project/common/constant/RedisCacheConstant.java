package com.drake.shortlink.project.common.constant;

public class RedisCacheConstant {

    //短链接跳转KEY
    public static final String LINK_GOTO_KEY="link:goto:";
    //短链接跳转分布式锁
    public static final String LINK_GOTO_LOCK="lock:link:goto:";
    //短链接判空KEY
    public static final String LINK_NULL_KEY="link:null:";
    //短链接UV数据监控KEY
    public static final String LINK_STATS_UV="link:stats:uv:";
    //短链接IP数据监控KEY
    public static final String LINK_STATS_IP="link:stats:ip:";
}
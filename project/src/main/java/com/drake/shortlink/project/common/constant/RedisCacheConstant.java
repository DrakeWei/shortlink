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
    //短链接编辑查询时的读写锁
    public static final String LINK_UPDATE_LOCK="lock:link:update:";
    //存储访问数据幂等KEY
    public static final String MQ_STATS_CONSUME_KEY="mq:stats:consume:";
}
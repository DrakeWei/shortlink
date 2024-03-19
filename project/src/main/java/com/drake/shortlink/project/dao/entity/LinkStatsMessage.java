package com.drake.shortlink.project.dao.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LinkStatsMessage {
    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 新/老访客
     */
    private Integer firstFlag;

    /**
     * 请求ip
     */
    private String ip;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 设备
     */
    private String device;

    /**
     * 网络
     */
    private String network;

    /**
     * cookie
     */
    private String cookie;

    /**
     * 日期
     */
    private Date date;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}

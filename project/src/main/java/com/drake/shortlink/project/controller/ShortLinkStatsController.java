package com.drake.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.drake.shortlink.project.common.convention.result.Result;
import com.drake.shortlink.project.common.convention.result.Results;
import com.drake.shortlink.project.dto.req.ShortLinkGroupStatsAccessRecordReqDTO;
import com.drake.shortlink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.drake.shortlink.project.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.drake.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import com.drake.shortlink.project.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import com.drake.shortlink.project.dto.resp.ShortLinkStatsRespDTO;
import com.drake.shortlink.project.service.ShortLinkStatsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShortLinkStatsController {

    @Resource
    private ShortLinkStatsService shortLinkStatsService;

    /**
     * 访问单个短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/v1/stats")
    public Result<ShortLinkStatsRespDTO> shortLinkStats(ShortLinkStatsReqDTO requestParam) {
        return Results.success(shortLinkStatsService.oneShortLinkStats(requestParam));
    }

    /**
     * 访问单个短链接指定时间内访问记录监控数据
     */
    @GetMapping("/api/short-link/v1/stats/access-record")
    public Result<IPage<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam) {
        return Results.success(shortLinkStatsService.shortLinkStatsAccessRecord(requestParam));
    }

    /**
     * 访问分组短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/v1/stats/group")
    public Result<ShortLinkStatsRespDTO> groupShortLinkStats(ShortLinkGroupStatsReqDTO requestParam) {
        return Results.success(shortLinkStatsService.groupShortLinkStats(requestParam));
    }

    /**
     * 访问分组短链接指定时间内访问记录监控数据
     */
    @GetMapping("/api/short-link/v1/stats/access-record/group")
    public Result<IPage<ShortLinkStatsAccessRecordRespDTO>> groupShortLinkStatsAccessRecord(ShortLinkGroupStatsAccessRecordReqDTO requestParam) {
        return Results.success(shortLinkStatsService.groupShortLinkStatsAccessRecord(requestParam));
    }
}

package com.drake.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.drake.shortlink.project.common.convention.result.Result;
import com.drake.shortlink.project.common.convention.result.Results;
import com.drake.shortlink.project.dto.req.RecycleBinPageReqDTO;
import com.drake.shortlink.project.dto.req.RecycleBinRecoverReqDTO;
import com.drake.shortlink.project.dto.req.RecycleBinRemoveReqDTO;
import com.drake.shortlink.project.dto.req.RecycleBinSaveReqDTO;
import com.drake.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.drake.shortlink.project.service.RecycleBinService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RecycleBinController {

    @Resource
    private RecycleBinService recycleBinService;

    @PostMapping("/api/short-link/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam){
        recycleBinService.saveRecycleBin(requestParam);
        return Results.success();
    }

    @GetMapping("/api/short-link/v1/recycle-bin/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(@RequestBody RecycleBinPageReqDTO requestParam) {
        return Results.success(recycleBinService.pageQuery(requestParam));
    }

    @PostMapping("/api/short-link/v1/recycle-bin/recover")
    public Result<Void> recoverShortLink(@RequestBody RecycleBinRecoverReqDTO requestParam){
        recycleBinService.recoverShortLink(requestParam);
        return Results.success();
    }

    @PostMapping("/api/short-link/v1/recycle-bin/remove")
    public Result<Void> recoverShortLink(@RequestBody RecycleBinRemoveReqDTO requestParam){
        recycleBinService.removeShortLink(requestParam);
        return Results.success();
    }
}

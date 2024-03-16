package com.drake.shortlink.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.drake.shortlink.admin.common.biz.user.UserContext;
import com.drake.shortlink.admin.common.convention.result.Result;
import com.drake.shortlink.admin.common.convention.result.Results;
import com.drake.shortlink.admin.remote.dto.ShortLinkRemoteService;
import com.drake.shortlink.admin.remote.dto.req.*;
import com.drake.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.drake.shortlink.admin.service.RecycleBinService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RecycleBinController {

    ShortLinkRemoteService shortLinkRemoteService=new ShortLinkRemoteService() {};

    @Resource
    private RecycleBinService recycleBinService;

    @PostMapping("/api/short-link/admin/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam){
        shortLinkRemoteService.saveRecycleBin(requestParam);
        return Results.success();
    }

    @GetMapping("/api/short-link/admin/v1/recycle-bin/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(@RequestBody ShortLinkPageReqDTO requestParam) {
        List<String> gidList = recycleBinService.getGidList(UserContext.getUsername());
        RecycleBinPageReqDTO recycleBinPageReqDTO=new RecycleBinPageReqDTO();
        BeanUtil.copyProperties(requestParam,recycleBinPageReqDTO);
        recycleBinPageReqDTO.setGidList(gidList);
        return shortLinkRemoteService.pageShortLinkBin(recycleBinPageReqDTO);
    }

    @PostMapping("/api/short-link/admin/v1/recycle-bin/recover")
    public Result<Void> recoverShortLink(@RequestBody RecycleBinRecoverReqDTO requestParam){
        shortLinkRemoteService.recoverShortLink(requestParam);
        return Results.success();
    }

    @PostMapping("/api/short-link/admin/v1/recycle-bin/remove")
    public Result<Void> recoverShortLink(@RequestBody RecycleBinRemoveReqDTO requestParam){
        shortLinkRemoteService.removeShortLink(requestParam);
        return Results.success();
    }
}

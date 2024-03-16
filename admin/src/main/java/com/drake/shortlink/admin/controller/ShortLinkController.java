package com.drake.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.drake.shortlink.admin.common.convention.result.Result;
import com.drake.shortlink.admin.common.convention.result.Results;
import com.drake.shortlink.admin.remote.dto.ShortLinkRemoteService;
import com.drake.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import com.drake.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.drake.shortlink.admin.remote.dto.req.ShortLinkUpdateReqDTO;
import com.drake.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import com.drake.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ShortLinkController {

    ShortLinkRemoteService shortLinkRemoteService=new ShortLinkRemoteService() {};

    @GetMapping("/api/short-link/admin/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        return shortLinkRemoteService.pageShortLink(requestParam);
    }

    @PostMapping("/api/short-link/admin/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        return shortLinkRemoteService.createShortLink(requestParam);
    }

    @PostMapping("/api/short-link/admin/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam){
        shortLinkRemoteService.update(requestParam);
        return Results.success();
    }
}

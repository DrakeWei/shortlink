package com.drake.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.drake.shortlink.project.common.convention.result.Result;
import com.drake.shortlink.project.common.convention.result.Results;
import com.drake.shortlink.project.dto.req.ShortLinkBatchCreateReqDTO;
import com.drake.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.drake.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.drake.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.drake.shortlink.project.dto.resp.ShortLinkBatchCreateRespDTO;
import com.drake.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.drake.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.drake.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.drake.shortlink.project.service.ShortLinkService;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
public class ShortLinkController {

    @Resource
    private ShortLinkService shortLinkService;

    /**
     * 创建短链接
     */
    @PostMapping("/api/short-link/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) throws IOException {
        return Results.success(shortLinkService.create(requestParam));
//        HttpRequest httpRequest = HttpUtil.createPost("http://127.0.0.1:8001/api/short-link/v1/create").body(JSON.toJSONString(requestParam));
//        HttpResponse execute = httpRequest.execute();
//        return Results.success(JSON.parseObject(execute.body(), ShortLinkCreateRespDTO.class));
    }

    /**
     * 分页查询短链接
     */
    @GetMapping("/api/short-link/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        return Results.success(shortLinkService.pageQuery(requestParam));
    }

    @GetMapping("/api/short-link/v1/count")
    public Result<List<ShortLinkGroupCountQueryRespDTO>> listShortLinkGroup(@RequestParam List<String> gidList){
        return Results.success(shortLinkService.listShortLinkGroup(gidList));
    }

    @PostMapping("/api/short-link/v1/update")
    public Result<Void> update(@RequestBody ShortLinkUpdateReqDTO requestParam){
        shortLinkService.updateShortLink(requestParam);
        return Results.success();
    }

    @GetMapping("/{short-uri}")
    public Result<Void> restoreUrl(@PathVariable("short-uri") String shortUri, ServletRequest servletRequest, ServletResponse servletResponse) throws IOException {
        shortLinkService.restoreUrl(shortUri,servletRequest,servletResponse);
        return Results.success();
    }

    @PostMapping("/api/short-link/v1/create/batch")
    public Result<ShortLinkBatchCreateRespDTO> batchCreateShortLink(@RequestBody ShortLinkBatchCreateReqDTO requestParam) {
        return Results.success(shortLinkService.batchCreateShortLink(requestParam));
    }
}

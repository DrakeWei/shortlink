package com.drake.shortlink.project.controller;

import com.drake.shortlink.project.common.convention.result.Result;
import com.drake.shortlink.project.common.convention.result.Results;
import com.drake.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.drake.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.drake.shortlink.project.service.ShortLinkService;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class ShortLinkController {

    @Resource
    private ShortLinkService shortLinkService;

    /**
     * 有效期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date validDate;

    /**
     * 有效期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    /**
     * 创建短链接
     */
    @PostMapping("/api/short-link/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        return Results.success(shortLinkService.create(requestParam));
//        HttpRequest httpRequest = HttpUtil.createPost("http://127.0.0.1:8001/api/short-link/v1/create").body(JSON.toJSONString(requestParam));
//        HttpResponse execute = httpRequest.execute();
//        return Results.success(JSON.parseObject(execute.body(), ShortLinkCreateRespDTO.class));
    }

    /**
     * 分页查询短链接
     */
//    @GetMapping("/api/short-link/v1/page")
//    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
//        Map<String, Object> requestMap = new HashMap<>();
//        requestMap.put("gid", requestParam.getGid());
//        requestMap.put("current", requestParam.getCurrent());
//        requestMap.put("size", requestParam.getSize());
//        String actualUrl = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/page", requestMap);
//        Type type = new TypeReference<Result<IPage<ShortLinkPageRespDTO>>>() {
//        }.getType();
//        return JSON.parseObject(actualUrl, type);
//    }
}

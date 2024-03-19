package com.drake.shortlink.admin.controller;

import com.drake.shortlink.admin.common.convention.result.Result;
import com.drake.shortlink.admin.remote.ShortLinkActualRemoteService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UrlTitleController {

    @Resource
    private ShortLinkActualRemoteService shortLinkActualRemoteService;

    @GetMapping("/api/short-link/admin/v1/title")
    public Result<String> getTitleByUrl(@RequestParam String url){
        return shortLinkActualRemoteService.getTitleByUrl(url);
    }
}

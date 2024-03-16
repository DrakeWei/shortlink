package com.drake.shortlink.project.controller;

import com.drake.shortlink.project.common.convention.result.Result;
import com.drake.shortlink.project.common.convention.result.Results;
import com.drake.shortlink.project.service.UrlTitleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
public class UrlTitleController {

    @Resource
    private UrlTitleService urlTitleService;

    @GetMapping("/api/short-link/v1/title")
    public Result<String> getTitleByUrl(@RequestParam String url) throws IOException {
        return Results.success(urlTitleService.getTitleByUrl(url));
    }
}

package com.drake.shortlink.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 短链接控制层2
 */
@Controller // @Controller注解返回MVC视图， @RestController注解返回JSON数据
public class ShortLinkNotFoundController {

    /**
     * 返回页面不存在的视图
     * @return
     */
    @GetMapping("/page/notfound")
    public String notfound(){
        return "notfound";
    }
}

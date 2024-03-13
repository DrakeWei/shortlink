package com.drake.shortlink.admin.controller;

import com.drake.shortlink.admin.common.convention.result.Result;
import com.drake.shortlink.admin.common.convention.result.Results;
import com.drake.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.drake.shortlink.admin.dto.resp.UserRespDTO;
import com.drake.shortlink.admin.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制层
 */
@RestController
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("/api/short-link/v1/user/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable String username){
        UserRespDTO userRespDTO = userService.getUserByUsername(username);
        if(userRespDTO==null){
            return Results.fail(null);
        }
        return Results.success(userRespDTO);
    }

    @GetMapping("/api/short-link/v1/user/has-username")
    public Result<Boolean> hasUsername(@RequestParam String username){
        return Results.success(userService.hasUsername(username));
    }

    @PostMapping("/api/short-link/v1/user/register")
    public Result<Void> register(@RequestBody UserRegisterReqDTO requestParam){
        userService.register(requestParam);
        return Results.success();
    }
}

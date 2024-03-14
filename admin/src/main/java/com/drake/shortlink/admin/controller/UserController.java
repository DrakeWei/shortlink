package com.drake.shortlink.admin.controller;

import com.drake.shortlink.admin.common.convention.result.Result;
import com.drake.shortlink.admin.common.convention.result.Results;
import com.drake.shortlink.admin.dto.req.UserLoginReqDTO;
import com.drake.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.drake.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.drake.shortlink.admin.dto.resp.UserLoginRespDTO;
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

    @GetMapping("/api/short-link/admin/v1/user/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable String username){
        UserRespDTO userRespDTO = userService.getUserByUsername(username);
        if(userRespDTO==null){
            return Results.fail(null);
        }
        return Results.success(userRespDTO);
    }

    @GetMapping("/api/short-link/admin/v1/user/has-username")
    public Result<Boolean> hasUsername(@RequestParam String username){
        return Results.success(userService.hasUsername(username));
    }

    @PostMapping("/api/short-link/admin/v1/user")
    public Result<Void> register(@RequestBody UserRegisterReqDTO requestParam){
        userService.register(requestParam);
        return Results.success();
    }

    @PutMapping("/api/short-link/admin/v1/user")
    public Result<Void> update(@RequestBody UserUpdateReqDTO requestParam){
        userService.updateInfo(requestParam);
        return Results.success();
    }

    @GetMapping("/api/short-link/admin/v1/user/login")
    public Result<UserLoginRespDTO> login(@RequestBody UserLoginReqDTO requestParam){
        return Results.success(userService.login(requestParam));
    }

    @GetMapping("/api/short-link/admin/v1/user/check-login")
    public Result<Boolean> checkLogin(@RequestParam String username,@RequestParam String token){
        return Results.success(userService.checkLogin(username,token));
    }

    @DeleteMapping("/api/short-link/admin/v1/user/logout")
    public Result<Void> logout(@RequestParam String username,@RequestParam String token){
        userService.logout(username,token);
        return Results.success();
    }
}

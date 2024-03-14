package com.drake.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.drake.shortlink.admin.dao.entity.UserDO;
import com.drake.shortlink.admin.dto.req.UserLoginReqDTO;
import com.drake.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.drake.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.drake.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.drake.shortlink.admin.dto.resp.UserRespDTO;

/**
 * 用户接口层
 */
public interface UserService extends IService<UserDO> {

    UserRespDTO getUserByUsername(String username);

    Boolean hasUsername(String username);

    void register(UserRegisterReqDTO requestParam);

    void updateInfo(UserUpdateReqDTO requestParam);

    UserLoginRespDTO login(UserLoginReqDTO requestParam);

    Boolean checkLogin(String username, String token);

    void logout(String username, String token);
}

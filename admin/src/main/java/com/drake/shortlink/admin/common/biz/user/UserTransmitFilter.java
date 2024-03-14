package com.drake.shortlink.admin.common.biz.user;

import cn.hutool.json.JSONUtil;
import com.drake.shortlink.admin.common.convention.exception.ClientException;
import com.drake.shortlink.admin.dao.entity.UserDO;
import com.google.common.collect.Lists;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.util.List;

import static com.drake.shortlink.admin.common.constant.RedisCacheConstant.USER_LOGIN_TOKEN;
import static com.drake.shortlink.admin.common.convention.errorcode.BaseErrorCode.USER_NOT_LOGIN;

public class UserTransmitFilter implements Filter {

    private StringRedisTemplate stringRedisTemplate;

    public UserTransmitFilter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private static final List<String> ignoreURI= Lists.newArrayList("/api/short-link/v1/user/login","/api/short-link/v1/user/has-username");

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String uri = httpServletRequest.getRequestURI();
        if(!(uri.contains(ignoreURI.get(0))||uri.contains(ignoreURI.get(1)))){
            String username = httpServletRequest.getHeader("username");
            String token = httpServletRequest.getHeader("token");
            Object object = stringRedisTemplate.opsForHash().get(USER_LOGIN_TOKEN+username, token);
            if(object==null){
                throw new ClientException(USER_NOT_LOGIN);
            }
            UserDO userDO = JSONUtil.toBean(object.toString(), UserDO.class);
            UserInfoDTO userInfoDTO = UserInfoDTO.builder().userId(userDO.getId().toString()).username(username).realName(userDO.getRealName()).build();
            UserContext.setUser(userInfoDTO);
        }
        try {
            filterChain.doFilter(servletRequest,servletResponse);
        }
        finally {
            UserContext.removeUser();
        }
    }
}

package com.drake.shortlink.admin.common.biz.user;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.drake.shortlink.admin.common.convention.exception.ClientException;
import com.drake.shortlink.admin.dao.entity.UserDO;
import com.google.common.collect.Lists;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static com.drake.shortlink.admin.common.constant.RedisCacheConstant.USER_LOGIN_TOKEN;
import static com.drake.shortlink.admin.common.convention.errorcode.BaseErrorCode.IDEMPOTENT_TOKEN_NULL_ERROR;

public class UserTransmitFilter implements Filter {

    private final StringRedisTemplate stringRedisTemplate;

    public UserTransmitFilter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private static final List<String> ignoreURI= Lists.newArrayList("/api/short-link/admin/v1/user/login","/api/short-link/admin/v1/user/has-username");

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String uri = httpServletRequest.getRequestURI();
        if(!(uri.contains(ignoreURI.get(0))||uri.contains(ignoreURI.get(1)))){
            String method = ((HttpServletRequest) servletRequest).getMethod();
            if(!(uri.contains("/api/short-link/admin/v1/user") && Objects.equals(method,"POST"))){
                String username = httpServletRequest.getHeader("username");
                String token = httpServletRequest.getHeader("token");
                if(!StrUtil.isAllNotBlank(username,token)){
                    throw new ClientException(IDEMPOTENT_TOKEN_NULL_ERROR);
                }
                try {
                    Object object = stringRedisTemplate.opsForHash().get(USER_LOGIN_TOKEN+username, token);
                    if(object==null){
                        throw new ClientException(IDEMPOTENT_TOKEN_NULL_ERROR);
                    }
                    UserDO userDO = JSONUtil.toBean(object.toString(), UserDO.class);
                    UserInfoDTO userInfoDTO = UserInfoDTO.builder().userId(userDO.getId().toString()).username(username).realName(userDO.getRealName()).build();
                    UserContext.setUser(userInfoDTO);
                }
                catch (Exception e){
                    throw new ClientException(IDEMPOTENT_TOKEN_NULL_ERROR);
                }
            }
        }
        try {
            filterChain.doFilter(servletRequest,servletResponse);
        }
        finally {
            UserContext.removeUser();
        }
    }
}

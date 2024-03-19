package com.drake.shortlink.admin.common.biz.user;

import com.alibaba.fastjson2.JSON;
import com.drake.shortlink.admin.common.config.UserFlowRiskControlConfiguration;
import com.drake.shortlink.admin.common.convention.exception.ClientException;
import com.drake.shortlink.admin.common.convention.result.Results;
import com.google.common.collect.Lists;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.io.IOException;
import java.io.PrintWriter;

import static com.drake.shortlink.admin.common.convention.errorcode.BaseErrorCode.FLOW_LIMIT_ERROR;

@Slf4j
public class UserFlowRiskControlFilter implements Filter {

    private final StringRedisTemplate stringRedisTemplate;

    private final UserFlowRiskControlConfiguration userFlowRiskControlConfiguration;

    private static final String USER_FLOW_RISK_CONTROL_LUA_SCRIPT_PATH = "lua/user_flow_risk_control.lua";

    public UserFlowRiskControlFilter(StringRedisTemplate stringRedisTemplate, UserFlowRiskControlConfiguration userFlowRiskControlConfiguration) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.userFlowRiskControlConfiguration = userFlowRiskControlConfiguration;
    }

    @SneakyThrows
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        DefaultRedisScript<Long> redisScript=new DefaultRedisScript<>();
        redisScript.setLocation(new ClassPathResource(USER_FLOW_RISK_CONTROL_LUA_SCRIPT_PATH));
        redisScript.setResultType(Long.class);
        String username = UserContext.getUsername();
        if(username!=null){
            Long result = null;
            try {
                String url = ((HttpServletRequest) request).getRequestURI() + ((HttpServletRequest) request).getMethod();
                result = stringRedisTemplate.execute(redisScript, Lists.newArrayList(username,url), userFlowRiskControlConfiguration.getTimeWindow());
            } catch (Throwable ex) {
                log.error("执行用户请求流量限制LUA脚本出错", ex);
                returnJson((HttpServletResponse) response, JSON.toJSONString(Results.failure(new ClientException(FLOW_LIMIT_ERROR))));
            }
            if (result == null || result > userFlowRiskControlConfiguration.getMaxAccessCount()) {
                returnJson((HttpServletResponse) response, JSON.toJSONString(Results.failure(new ClientException(FLOW_LIMIT_ERROR))));
            }
        }
        filterChain.doFilter(request, response);
    }

    private void returnJson(HttpServletResponse response, String json) throws Exception {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.print(json);
        }
    }
}

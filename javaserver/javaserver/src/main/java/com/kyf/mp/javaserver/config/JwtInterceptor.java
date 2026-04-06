package com.kyf.mp.javaserver.config;

import com.kyf.mp.javaserver.common.ResultModel;
import com.kyf.mp.javaserver.utils.JwtUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String token = request.getHeader("token");

        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        try {
            if (token == null || token.isEmpty()) {
                throw new RuntimeException("Token 不能为空");
            }
            JwtUtils.parseToken(token);
            return true;
        } catch (Exception e) {
            response.setContentType("application/json;charset=utf-8");
            ResultModel<Object> error = ResultModel.error("登录已过期，请重新登录", 401);
            String json = new ObjectMapper().writeValueAsString(error);
            response.getWriter().write(json);
            return false;
        }
    }
}

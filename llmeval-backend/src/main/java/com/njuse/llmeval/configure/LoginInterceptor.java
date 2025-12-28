package com.njuse.llmeval.configure;

import com.njuse.llmeval.exception.LLMEvalException;
import com.njuse.llmeval.po.User;
import com.njuse.llmeval.util.TokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private TokenUtil tokenUtil;

    // 请求前的处理，验证 token 是否有效
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 从请求头中获取 token
        String token = request.getHeader("token");

        // 如果没有 token，返回 401 错误
        if (token == null) {
            throw LLMEvalException.notLogin();
        }

        // 验证 token 是否有效
        User user = tokenUtil.decodeToken(token);

        // 如果 token 无效，返回 401 错误
        if (user == null) {
            throw LLMEvalException.invalidToken();
        }

        return true;
    }
}


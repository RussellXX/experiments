package com.njuse.llmeval.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.njuse.llmeval.po.User;
import com.njuse.llmeval.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class TokenUtil {

    @Autowired
    UserRepository userRepository;

    private static final String SECRET_KEY = "your-secret-key";  // 密钥
    private static final long EXPIRATION_TIME = 86400000L;  // 24小时过期时间

    public String generateToken(User user) {
        return JWT.create()
                .withSubject(user.getPhone())  // 使用用户手机号作为 subject
                .withClaim("username", user.getUsername())  // 添加用户名作为 claim
                .withIssuedAt(new Date())  // 设置签发时间
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))  // 设置过期时间
                .sign(Algorithm.HMAC512(SECRET_KEY));  // 使用 HMAC512 算法和密钥签名
    }

    public User decodeToken(String token) {
        // 使用相同的密钥解码 Token
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC512(SECRET_KEY))
                .build()
                .verify(token);  // 验证并解码 Token

        // 获取解析后的用户名和手机号
        String phone = decodedJWT.getSubject();

        // 返回解析后的用户对象
        return userRepository.findByPhone(phone);
    }
}



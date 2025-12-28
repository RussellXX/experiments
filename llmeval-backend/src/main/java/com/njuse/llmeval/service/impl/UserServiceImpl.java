package com.njuse.llmeval.service.impl;

import com.njuse.llmeval.exception.LLMEvalException;
import com.njuse.llmeval.po.User;
import com.njuse.llmeval.repository.UserRepository;
import com.njuse.llmeval.service.UserService;
import com.njuse.llmeval.util.TokenUtil;
import com.njuse.llmeval.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenUtil tokenUtil;

    @Override
    public UserVO register(UserVO userVO) {
        // 检查手机号是否已被注册
        if (userRepository.existsByPhone(userVO.getPhone())) {
            throw LLMEvalException.phoneAlreadyExist();
        }

        // 创建新的用户
        User user = userVO.toPO();

        // 保存用户到数据库
        userRepository.save(user);

        // 转换 PO 到 VO 并返回
        return user.toVO();
    }

    @Override
    public String login(String phone, String password) {
        // 根据手机号查找用户
        User user = userRepository.findByPhone(phone);

        // 验证密码
        if (user == null || !user.getPassword().equals(password)) {
            throw LLMEvalException.phoneOrPasswordError();
        }

        // 根据用户生成 JWT Token
        return tokenUtil.generateToken(user);
    }

    @Override
    public UserVO getCurrentUser(String token) {
        // 解析 Token 获取用户信息
        User user = tokenUtil.decodeToken(token);

        // 转换 PO 到 VO 并返回
        return user.toVO();
    }
}
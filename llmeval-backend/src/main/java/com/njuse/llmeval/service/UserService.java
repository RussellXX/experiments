package com.njuse.llmeval.service;

import com.njuse.llmeval.vo.UserVO;

public interface UserService {

    UserVO register(UserVO userVO);

    String login(String phone, String password);

    UserVO getCurrentUser(String token);
}

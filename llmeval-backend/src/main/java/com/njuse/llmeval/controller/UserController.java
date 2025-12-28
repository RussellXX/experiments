package com.njuse.llmeval.controller;

import com.njuse.llmeval.service.UserService;
import com.njuse.llmeval.vo.ResultVO;
import com.njuse.llmeval.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // 用户注册接口
    @PostMapping("/register")
    public ResultVO<UserVO> register(@RequestBody UserVO userVO) {
        UserVO user = userService.register(userVO);
        return ResultVO.buildSuccess(user);
    }

    // 用户登录接口
    @PostMapping("/login")
    public ResultVO<String> login(@RequestParam String phone, @RequestParam String password) {
        String token = userService.login(phone, password);
        return ResultVO.buildSuccess(token);
    }

    // 获取当前用户信息接口
    @GetMapping("/current")
    public ResultVO<UserVO> getCurrentUser(@RequestHeader("token") String token) {
        UserVO user = userService.getCurrentUser(token);
        return ResultVO.buildSuccess(user);
    }
}


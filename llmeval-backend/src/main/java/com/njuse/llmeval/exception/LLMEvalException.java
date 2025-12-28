package com.njuse.llmeval.exception;

public class LLMEvalException extends RuntimeException {
    public LLMEvalException(String message) {
        super(message);
    }

    public static LLMEvalException notLogin() {
        return new LLMEvalException("未登陆！");
    }

    public static LLMEvalException invalidToken() {
        return new LLMEvalException("无效的 token！");
    }
    public static LLMEvalException phoneAlreadyExist() {
        return new LLMEvalException("手机号已存在！");
    }

    public static LLMEvalException phoneOrPasswordError() {
        return new LLMEvalException("手机号或密码错误！");
    }

    public static LLMEvalException taskNotFound() {
        return new LLMEvalException("任务不存在！");
    }

    public static LLMEvalException taskNotBelongToUser() {
        return new LLMEvalException("该评估任务不属于当前用户！");
    }

    public static LLMEvalException evalNotFound() {
        return new LLMEvalException("评估不存在！");
    }
}

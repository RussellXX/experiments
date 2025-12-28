package com.njuse.llmeval.exception;

import com.njuse.llmeval.vo.ResultVO;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = LLMEvalException.class)
    public ResultVO<String> handleAIExternalException(LLMEvalException e) {
        return ResultVO.buildFailure(e.getMessage());
    }
}

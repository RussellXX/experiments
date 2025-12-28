package com.njuse.llmeval.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class ResultVO<T> implements Serializable {

    private String code;

    private String msg;

    private T result;

    public static <T> ResultVO<T> buildSuccess(T result) {
        return new ResultVO<>("200", null, result);
    }

    public static <T> ResultVO<T> buildFailure(String msg) {
        return new ResultVO<>("400", msg, null);
    }
}
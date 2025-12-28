package com.njuse.llmeval.vo.rpc;

import lombok.Data;

@Data
public class PromptEvalResult {
    private Integer evalId;  // 本次评估在整个后端项目中的 ID
    private String status;   // 评估完成状态，可能为 DONE 或 ERROR
    private Float score;     // 评估得分
    private String generatedAnswer;  // 生成的回答
    private String errorMsg;  // 错误信息，当 status 为 ERROR 时使用
}


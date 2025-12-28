package com.njuse.llmeval.vo.rpc;

import lombok.Data;

@Data
public class PromptEvalRequest {
    private Integer evalId;  // 本次评估在整个后端项目中的 ID
    private String metric;   // 评估的指标
    private String prompt;   // 要评估的提示词
    private String groundTruth;  // 标准答案
}


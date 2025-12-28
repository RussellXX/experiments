package com.njuse.llmeval.vo;

import com.njuse.llmeval.enums.EvalStatus;
import com.njuse.llmeval.enums.PromptMetric;
import com.njuse.llmeval.po.PromptEval;
import lombok.Data;

@Data
public class PromptEvalVO {
    private Integer id;
    private Integer taskId;
    private String prompt;
    private String groundTruth;
    private PromptMetric metric;
    private Float score;
    private String generatedAnswer;
    private EvalStatus status;

    public PromptEval toPO() {
        PromptEval eval = new PromptEval();
        eval.setId(this.id);
        eval.setTaskId(this.taskId);
        eval.setPrompt(this.prompt);
        eval.setGroundTruth(this.groundTruth);
        eval.setMetric(this.metric);
        eval.setScore(this.score);
        eval.setGeneratedAnswer(this.generatedAnswer);
        eval.setStatus(this.status);
        return eval;
    }
}


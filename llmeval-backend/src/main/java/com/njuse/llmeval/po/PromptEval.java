package com.njuse.llmeval.po;

import com.njuse.llmeval.enums.EvalStatus;
import com.njuse.llmeval.enums.PromptMetric;
import com.njuse.llmeval.vo.PromptEvalVO;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class PromptEval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer taskId;
    private Float score;
    private EvalStatus status;
    private PromptMetric metric;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String prompt;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String groundTruth;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String generatedAnswer;

    public PromptEvalVO toVO() {
        PromptEvalVO vo = new PromptEvalVO();
        vo.setId(this.id);
        vo.setTaskId(this.taskId);
        vo.setPrompt(this.prompt);
        vo.setGroundTruth(this.groundTruth);
        vo.setMetric(this.metric);
        vo.setScore(this.score);
        vo.setStatus(this.status);
        vo.setGeneratedAnswer(this.generatedAnswer);
        return vo;
    }
}

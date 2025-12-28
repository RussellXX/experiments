package com.njuse.llmeval;


import com.njuse.llmeval.enums.EvalStatus;
import com.njuse.llmeval.enums.PromptMetric;
import com.njuse.llmeval.po.PromptEval;
import com.njuse.llmeval.repository.PromptEvalRepository;
import com.njuse.llmeval.util.PromptEvalUtil;
import com.njuse.llmeval.vo.rpc.PromptEvalResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PromptEvalUtilTest {
    @InjectMocks
    private PromptEvalUtil promptEvalUtil;

    @Mock
    private PromptEvalRepository promptEvalRepo;

    @Test
    public void normalProcessEvalResult() {
        int evalId = 1, taskId = 1;
        float score = 1.0f;
        String GA = "This is the GA.";
        String prompt = "This is a prompt.";
        String GT = "This is the GT.";
        PromptMetric metric = PromptMetric.BLEU_SCORE;

        PromptEvalResult evalResult = new PromptEvalResult();
        evalResult.setEvalId(evalId);
        evalResult.setStatus("DONE");
        evalResult.setGeneratedAnswer(GA);
        evalResult.setScore(score);

        PromptEval promptEval = new PromptEval();
        promptEval.setId(evalId);
        promptEval.setPrompt(prompt);
        promptEval.setGroundTruth(GT);
        promptEval.setMetric(metric);
        promptEval.setTaskId(taskId);
        promptEval.setStatus(EvalStatus.DOING);

        when(promptEvalRepo.findById(evalResult.getEvalId()))
                .thenAnswer(invocation -> {
                    PromptEval temp = new PromptEval();
                    temp.setId(evalId);
                    temp.setPrompt(promptEval.getPrompt());
                    temp.setGroundTruth(promptEval.getGroundTruth());
                    temp.setMetric(promptEval.getMetric());
                    temp.setTaskId(promptEval.getTaskId());
                    temp.setStatus(promptEval.getStatus());
                    return Optional.of(temp);
                });
        when(promptEvalRepo.save(any(PromptEval.class)))
                .thenAnswer(invocation -> {
                    PromptEval arg = invocation.getArgument(0);
                    assertEquals(evalId, arg.getId());
                    promptEval.setId(arg.getId());
                    promptEval.setStatus(arg.getStatus());
                    promptEval.setScore(arg.getScore());
                    promptEval.setPrompt(arg.getPrompt());
                    promptEval.setMetric(arg.getMetric());
                    promptEval.setGroundTruth(arg.getGroundTruth());
                    promptEval.setGeneratedAnswer(arg.getGeneratedAnswer());
                    promptEval.setTaskId(arg.getTaskId());
                    return promptEval;
                });
        assertDoesNotThrow(() -> promptEvalUtil.processEvalResult(evalResult));
        assertEquals(evalId, promptEval.getId());
        assertEquals(EvalStatus.DONE, promptEval.getStatus());
        assertEquals(score, promptEval.getScore());
        assertEquals(prompt, promptEval.getPrompt());
        assertEquals(GT, promptEval.getGroundTruth());
        assertEquals(GA, promptEval.getGeneratedAnswer());
        assertEquals(taskId, promptEval.getTaskId());
        assertEquals(metric, promptEval.getMetric());
    }
}

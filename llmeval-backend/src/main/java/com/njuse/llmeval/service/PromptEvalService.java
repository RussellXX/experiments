package com.njuse.llmeval.service;

import com.njuse.llmeval.vo.PromptEvalVO;
import com.njuse.llmeval.vo.PromptTaskVO;

import java.util.List;

public interface PromptEvalService {
    PromptTaskVO createPromptTask(PromptTaskVO promptTaskVO, String token);

    List<PromptTaskVO> getAllPromptTasks(String token);

    boolean deletePromptTask(Integer taskId, String token);

    Integer createPromptEval(PromptEvalVO promptEvalVO, String token);

    PromptEvalVO getPromptEvalResult(Integer evalId, String token);

    List<PromptEvalVO> getEvaluationsByTask(Integer taskId, String token);
}

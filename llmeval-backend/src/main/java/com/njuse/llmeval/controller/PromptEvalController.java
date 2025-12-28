package com.njuse.llmeval.controller;

import com.njuse.llmeval.service.PromptEvalService;
import com.njuse.llmeval.vo.PromptEvalVO;
import com.njuse.llmeval.vo.PromptTaskVO;
import com.njuse.llmeval.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prompteval")
public class PromptEvalController {

    @Autowired
    PromptEvalService promptEvalService;

    @PostMapping
    public ResultVO<PromptTaskVO> createPromptTask(@RequestBody PromptTaskVO promptTaskVO, @RequestHeader String token) {
        PromptTaskVO result = promptEvalService.createPromptTask(promptTaskVO, token);
        return ResultVO.buildSuccess(result);
    }

    @GetMapping
    public ResultVO<List<PromptTaskVO>> getAllPromptTasks(@RequestHeader String token) {
        List<PromptTaskVO> result = promptEvalService.getAllPromptTasks(token);
        return ResultVO.buildSuccess(result);
    }

    @DeleteMapping("/{taskId}")
    public ResultVO<Boolean> deletePromptTask(@PathVariable Integer taskId, @RequestHeader String token) {
        Boolean result = promptEvalService.deletePromptTask(taskId, token);
        return ResultVO.buildSuccess(result);
    }

    @PostMapping("/eval")
    public ResultVO<Integer> createPromptEval(@RequestBody PromptEvalVO promptEvalVO, @RequestHeader String token) {
        Integer evalId = promptEvalService.createPromptEval(promptEvalVO, token);
        return ResultVO.buildSuccess(evalId);
    }

    @GetMapping("/eval/{evalId}")
    public ResultVO<PromptEvalVO> getPromptEvalResult(@PathVariable Integer evalId, @RequestHeader String token) {
        PromptEvalVO result = promptEvalService.getPromptEvalResult(evalId, token);
        return ResultVO.buildSuccess(result);
    }

    @GetMapping("/eval/task/{taskId}")
    public ResultVO<List<PromptEvalVO>> getAllEvaluationsByTask(@PathVariable Integer taskId, @RequestHeader String token) {
        List<PromptEvalVO> result = promptEvalService.getEvaluationsByTask(taskId, token);
        return ResultVO.buildSuccess(result);
    }
}

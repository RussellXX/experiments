package com.njuse.llmeval.service.impl;

import com.njuse.llmeval.enums.EvalStatus;
import com.njuse.llmeval.exception.LLMEvalException;
import com.njuse.llmeval.po.PromptEval;
import com.njuse.llmeval.po.PromptTask;
import com.njuse.llmeval.po.User;
import com.njuse.llmeval.repository.PromptEvalRepository;
import com.njuse.llmeval.repository.PromptTaskRepository;
import com.njuse.llmeval.service.PromptEvalService;
import com.njuse.llmeval.util.RabbitMQUtil;
import com.njuse.llmeval.util.TokenUtil;
import com.njuse.llmeval.vo.PromptEvalVO;
import com.njuse.llmeval.vo.PromptTaskVO;
import com.njuse.llmeval.vo.rpc.PromptEvalRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PromptEvalServiceImpl implements PromptEvalService {

    @Autowired
    private PromptTaskRepository promptTaskRepository;
    @Autowired
    private PromptEvalRepository promptEvalRepository;
    @Autowired
    private RabbitMQUtil rabbitMQUtil;
    @Autowired
    private TokenUtil tokenUtil;

    @Override
    public PromptTaskVO createPromptTask(PromptTaskVO promptTaskVO, String token) {
        User user = getUserFromToken(token);
        PromptTask task = promptTaskVO.toPO();
        task.setUserId(user.getId());
        PromptTask savedTask = promptTaskRepository.save(task);
        return savedTask.toVO();
    }

    @Override
    public List<PromptTaskVO> getAllPromptTasks(String token) {
        User user = getUserFromToken(token);
        List<PromptTask> taskList = promptTaskRepository.findByUserId(user.getId());
        return taskList.stream().map(PromptTask::toVO).toList();
    }

    @Override
    public boolean deletePromptTask(Integer taskId, String token) {
        User user = getUserFromToken(token);
        PromptTask task = promptTaskRepository.findById(taskId).orElseThrow(LLMEvalException::taskNotFound);
        if (!task.getUserId().equals(user.getId())) {
            throw LLMEvalException.taskNotBelongToUser();
        }
        promptTaskRepository.deleteById(taskId);
        return true;
    }

    @Override
    public Integer createPromptEval(PromptEvalVO promptEvalVO, String token) {
        PromptTask task = promptTaskRepository.findById(promptEvalVO.getTaskId()).orElseThrow(LLMEvalException::taskNotFound);

        // 检查该 task 是否属于当前用户
        User currentUser = tokenUtil.decodeToken(token);
        if (!Objects.equals(currentUser.getId(), task.getUserId())) throw LLMEvalException.taskNotBelongToUser();

        PromptEval eval = promptEvalVO.toPO();
        eval.setTaskId(task.getId());
        eval.setStatus(EvalStatus.DOING);
        PromptEval savedEval = promptEvalRepository.save(eval);

        // 构造发送给 python 进程的评估请求
        PromptEvalRequest promptEvalRequest = new PromptEvalRequest();
        promptEvalRequest.setEvalId(savedEval.getId());
        promptEvalRequest.setPrompt(savedEval.getPrompt());
        promptEvalRequest.setGroundTruth(savedEval.getGroundTruth());
        promptEvalRequest.setMetric(savedEval.getMetric().name());

        rabbitMQUtil.sendEvalRequest(promptEvalRequest);
        return savedEval.getId();
    }

    @Override
    public PromptEvalVO getPromptEvalResult(Integer evalId, String token) {
        PromptEval eval = promptEvalRepository.findById(evalId).orElseThrow(LLMEvalException::evalNotFound);
        PromptTask task = promptTaskRepository.findById(eval.getTaskId()).orElseThrow(LLMEvalException::taskNotFound);

        // 检查该 task 是否属于当前用户
        User currentUser = tokenUtil.decodeToken(token);
        if (!Objects.equals(currentUser.getId(), task.getUserId())) throw LLMEvalException.taskNotBelongToUser();

        return eval.toVO();
    }

    @Override
    public List<PromptEvalVO> getEvaluationsByTask(Integer taskId, String token) {
        PromptTask task = promptTaskRepository.findById(taskId).orElseThrow(LLMEvalException::taskNotFound);

        // 检查该 task 是否属于当前用户
        User currentUser = tokenUtil.decodeToken(token);
        if (!Objects.equals(currentUser.getId(), task.getUserId())) throw LLMEvalException.taskNotBelongToUser();

        List<PromptEval> evaluations = promptEvalRepository.findByTaskId(taskId);
        return evaluations.stream().map(PromptEval::toVO).toList();
    }

    private User getUserFromToken(String token) {
        return tokenUtil.decodeToken(token);
    }
}

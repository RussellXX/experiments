package com.njuse.llmeval.util;

import com.njuse.llmeval.enums.EvalStatus;
import com.njuse.llmeval.exception.LLMEvalException;
import com.njuse.llmeval.repository.PromptEvalRepository;
import com.njuse.llmeval.vo.rpc.PromptEvalRequest;
import com.njuse.llmeval.vo.rpc.PromptEvalResult;
import com.njuse.llmeval.po.PromptEval;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PromptEvalUtil {

    @Autowired
    private PromptEvalRepository promptEvalRepository;

    @Autowired
    private RabbitMQUtil rabbitMQUtil;

    @PostConstruct
    public void init() {
        rabbitMQUtil.setConsumer(this::processEvalResult);
    }

    // 缺少了错误处理的分支
    public void processEvalResult(PromptEvalResult evalResult) {
        PromptEval eval = promptEvalRepository.findById(evalResult.getEvalId()).orElseThrow(LLMEvalException::evalNotFound);
        eval.setStatus(EvalStatus.DONE);
        eval.setScore(evalResult.getScore());
        eval.setGeneratedAnswer(evalResult.getGeneratedAnswer());
        promptEvalRepository.save(eval);
    }

    public void sendEvalRequest(PromptEvalRequest request) {
        rabbitMQUtil.sendEvalRequest(request);
    }
}

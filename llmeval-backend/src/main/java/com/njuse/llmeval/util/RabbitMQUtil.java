package com.njuse.llmeval.util;

import com.njuse.llmeval.vo.rpc.PromptEvalRequest;
import com.njuse.llmeval.vo.rpc.PromptEvalResult;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class RabbitMQUtil {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private Consumer<PromptEvalResult> evalResultConsumer;

    // 设置结果队列的处理函数
    public void setConsumer(Consumer<PromptEvalResult> consumer) {
        this.evalResultConsumer = consumer;
    }

    // 发送评估请求消息到队列
    public void sendEvalRequest(PromptEvalRequest request) {
        // 将评估请求对象转换为 JSON 并发送到队列
        rabbitTemplate.convertAndSend("promptEvalRequestQueue", request);
    }

    // 评估结果监听器，监听结果队列的消息
    @RabbitListener(queues = "promptEvalResultQueue")
    public void onEvalResultReceived(PromptEvalResult result) {
        // 调用外部设置的 Consumer 函数处理评估结果
        if (evalResultConsumer != null) {
            evalResultConsumer.accept(result);
        }
    }

}


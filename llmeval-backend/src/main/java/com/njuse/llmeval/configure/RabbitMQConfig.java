package com.njuse.llmeval.configure;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue promptEvalRequestQueue() {
        return new Queue("promptEvalRequestQueue", true);
    }

    @Bean
    public Queue promptEvalResultQueue() {
        return new Queue("promptEvalResultQueue", true);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}


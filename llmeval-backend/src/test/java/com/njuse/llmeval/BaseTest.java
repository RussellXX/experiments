package com.njuse.llmeval;

import com.njuse.llmeval.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

public class BaseTest {

    @Autowired
    protected TestRestTemplate restTemplate;


    /**
     * 注册并登录，获取token
     *
     * @return token
     */
    protected String loginAndGetToken(String phone, String username, String password) {
        String registerUrl = "/api/users/register";
        String loginUrl = "/api/users/login";
        // 注册
        UserVO user = new UserVO(0, username, phone, password);
        HttpEntity<UserVO> entity = new HttpEntity<>(user);
        restTemplate
                .exchange(registerUrl, HttpMethod.POST, entity, new ParameterizedTypeReference<ResultVO<UserVO>>() {})
                .getBody();
        // 登陆
        String params = "?phone=" + phone + "&password=" + password;
        ResultVO<String> result = restTemplate.exchange(loginUrl + params,
                HttpMethod.POST, null, new ParameterizedTypeReference<ResultVO<String>>() {}).getBody();
        assert result != null;
        return result.getResult();
    }


    /**
     * 创建prompt任务
     *
     * @param task 任务对象
     * @return 创建的任务对象
     */
    public PromptTaskVO createPromptTask(PromptTaskVO task) {
        String createTaskUrl = "/api/prompteval";
        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        jsonHeaders.add("token", loginAndGetToken("12345678900", "test", "123"));

        HttpEntity<PromptTaskVO> taskEntity = new HttpEntity<>(task, jsonHeaders);
        ResultVO<PromptTaskVO> taskResult = restTemplate.exchange(createTaskUrl, HttpMethod.POST, taskEntity,
                new ParameterizedTypeReference<ResultVO<PromptTaskVO>>() {}).getBody();
        assert taskResult != null;
        return taskResult.getResult();
    }

    /**
     * 创建rag评估
     *
     * @param promptEvalVO 评估任务对象
     * @return 评估结果ID
     */
    public Integer createPromptEval(PromptEvalVO promptEvalVO) {
        String evalUrl = "/api/prompteval/eval";
        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        jsonHeaders.add("token", loginAndGetToken("12345678900", "test", "123"));

        HttpEntity<PromptEvalVO> evalEntity = new HttpEntity<>(promptEvalVO, jsonHeaders);
        ResultVO<Integer> evalResult = restTemplate
                .exchange(evalUrl, HttpMethod.POST, evalEntity, new ParameterizedTypeReference<ResultVO<Integer>>() {})
                .getBody();
        assert evalResult != null;
        return evalResult.getResult();
    }

    /**
     * 获取prompt评估结果
     *
     * @param evalId 评估ID
     * @return 评估结果对象
     */
    public PromptEvalVO getPromptEvalResult(Integer evalId) {
        String evalResultUrl = "/api/prompteval/eval/" + evalId;
        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        jsonHeaders.add("token", loginAndGetToken("12345678900", "test", "123"));

        HttpEntity<Void> evalResultEntity = new HttpEntity<>(jsonHeaders);
        ResultVO<PromptEvalVO> evalResult = restTemplate.exchange(evalResultUrl, HttpMethod.GET, evalResultEntity,
                new ParameterizedTypeReference<ResultVO<PromptEvalVO>>() {}).getBody();
        assert evalResult != null;
        assert evalResult.getCode().equals("200");
        assert evalResult.getResult() != null;
        return evalResult.getResult();
    }


}

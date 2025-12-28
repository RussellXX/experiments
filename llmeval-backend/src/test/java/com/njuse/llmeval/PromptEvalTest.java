package com.njuse.llmeval;

import com.njuse.llmeval.enums.EvalStatus;
import com.njuse.llmeval.enums.PromptMetric;
import com.njuse.llmeval.vo.PromptEvalVO;
import com.njuse.llmeval.vo.PromptTaskVO;
import com.njuse.llmeval.vo.ResultVO;
import com.njuse.llmeval.vo.UserVO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/scripts/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class PromptEvalTest extends BaseTest {

    // 用户模块测试用例

    @Test
    public void testRegisterAndLogin() {
        String token = loginAndGetToken("12345678900", "test", "123");
        assertNotNull(token);
    }

    @Test
    public void testRegisterWithDuplicatedPhone() {
        String registerUrl = "/api/users/register";
        String phone = "11111111111";
        // 第一次注册
        UserVO user = new UserVO(0, "test1", phone, "123");
        HttpEntity<UserVO> entity = new HttpEntity<>(user);
        ResultVO<UserVO> result = restTemplate
                .exchange(registerUrl, HttpMethod.POST, entity, new ParameterizedTypeReference<ResultVO<UserVO>>() {})
                .getBody();
        assertNotNull(result);
        assertEquals(result.getCode(), "200");

        // 通过相同的手机号注册
        user = new UserVO(1, "test2", phone, "123");
        entity = new HttpEntity<>(user);
        result = restTemplate
                .exchange(registerUrl, HttpMethod.POST, entity, new ParameterizedTypeReference<ResultVO<UserVO>>() {})
                .getBody();
        assertNotNull(result);
        assertEquals(result.getCode(), "400");
    }

    @Test
    public void testLoginWithoutRegister() {
        String loginUrl = "/api/users/login";
        // 直接登陆
        ResultVO<String> result = restTemplate.exchange(loginUrl + "?phone=12345678900&password=123",
                HttpMethod.POST, null, new ParameterizedTypeReference<ResultVO<String>>() {}).getBody();
        assertNotNull(result);
        assertEquals(result.getCode(), "400");
    }

    @Test
    public void testLoginWIthIncorrectPassword() {
        String registerUrl = "/api/users/register";
        String loginUrl = "/api/users/login";
        // 注册
        UserVO user = new UserVO(0, "test", "12345678900", "123");
        HttpEntity<UserVO> entity = new HttpEntity<>(user);
        restTemplate
                .exchange(registerUrl, HttpMethod.POST, entity, new ParameterizedTypeReference<ResultVO<UserVO>>() {})
                .getBody();
        // 登陆，但是用错误密码
        ResultVO<String> result = restTemplate.exchange(loginUrl + "?phone=12345678900&password=12345",
                HttpMethod.POST, null, new ParameterizedTypeReference<ResultVO<String>>() {}).getBody();
        assertNotNull(result);
        assertEquals(result.getCode(), "400");
    }

    @Test
    public void testGetCurrentUser() {
        String phone = "12345678900";
        String username = "test";
        String password = "123";
        String token = loginAndGetToken(phone, username, password);
        String getCurrentUserUrl = "/api/users/current";
        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        jsonHeaders.add("token", token);

        HttpEntity<Void> httpEntity = new HttpEntity<>(jsonHeaders);
        ResultVO<UserVO> currentUserResult = restTemplate.exchange(getCurrentUserUrl, HttpMethod.GET, httpEntity,
                new ParameterizedTypeReference<ResultVO<UserVO>>() {}).getBody();
        assertNotNull(currentUserResult);
        assertEquals(currentUserResult.getCode(), "200");
        UserVO currentUser = currentUserResult.getResult();
        assertEquals(currentUser.getPhone(), phone);
        assertEquals(currentUser.getUsername(), username);
    }

    // 提示词评估模块测试用例

    @Test
    public void testCreateAndDeletePromptTask() {
        int taskCount = 5; // 共需创建的任务个数
        for (int i = 0; i < taskCount; ++i) {
            PromptTaskVO task = new PromptTaskVO();
            task.setName("dummy");
            task.setDescription("This is a dummy task.");
            PromptTaskVO newTask = createPromptTask(task);
            assertNotNull(newTask);
        }

        // 获取属于当前用户的所有评估任务
        String getPromptTasksUrl = "/api/prompteval";
        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        jsonHeaders.add("token", loginAndGetToken("12345678900", "test", "123"));

        HttpEntity<Void> httpEntity = new HttpEntity<>(jsonHeaders);
        ResultVO<List<PromptTaskVO>> getPromptTasksResult = restTemplate.exchange(getPromptTasksUrl, HttpMethod.GET, httpEntity,
                new ParameterizedTypeReference<ResultVO<List<PromptTaskVO>>>() {}).getBody();
        assertNotNull(getPromptTasksResult);
        assertEquals(getPromptTasksResult.getCode(), "200");
        List<PromptTaskVO> tasks = getPromptTasksResult.getResult();
        assertEquals(tasks.size(), taskCount);

        // 逐一删除创建的评估任务
        String deletePromptTaskUrlPrefix = "/api/prompteval/";
        for (PromptTaskVO task : tasks) {
            String currentUrl = deletePromptTaskUrlPrefix + task.getId();
            ResultVO<Boolean> deleteResult = restTemplate.exchange(currentUrl, HttpMethod.DELETE, httpEntity,
                    new ParameterizedTypeReference<ResultVO<Boolean>>() {}).getBody();
            assertNotNull(deleteResult);
            assertEquals(deleteResult.getCode(), "200");
            assertTrue(deleteResult.getResult());
        }

        // 删除后检查现存评估任务个数是否为 0
        getPromptTasksResult = restTemplate.exchange(getPromptTasksUrl, HttpMethod.GET, httpEntity,
                new ParameterizedTypeReference<ResultVO<List<PromptTaskVO>>>() {}).getBody();
        assertNotNull(getPromptTasksResult);
        assertEquals(getPromptTasksResult.getCode(), "200");
        tasks = getPromptTasksResult.getResult();
        assertEquals(tasks.size(), 0);
    }

    @Test
    public void testOperationsOnAnotherUser() {
        // 1.创建 prompt 任务
        PromptTaskVO task = new PromptTaskVO();
        task.setName("taskForTest");
        task.setDescription("This is a task to test operations on other users.");
        PromptTaskVO newTask = createPromptTask(task);
        assertNotNull(newTask);

        // 2.开启评估
        PromptMetric metric = PromptMetric.BLEU_SCORE;
        PromptEvalVO eval = new PromptEvalVO();
        eval.setTaskId(newTask.getId());
        eval.setPrompt("When was Einstein born?");
        eval.setGroundTruth("Albert Einstein was born in 1879.");
        eval.setMetric(metric);

        Integer evalId = createPromptEval(eval);
        assertTrue(evalId > 0);

        // 3.此时注册并登录另一个用户
        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        jsonHeaders.add("token", loginAndGetToken("11111111111", "newUser", "123"));

        // 尝试开启另一用户的评估
        String evalUrl = "/api/prompteval/eval";
        HttpEntity<PromptEvalVO> evalEntity = new HttpEntity<>(eval, jsonHeaders);
        ResultVO<Integer> createEvalResult = restTemplate
                .exchange(evalUrl, HttpMethod.POST, evalEntity, new ParameterizedTypeReference<ResultVO<Integer>>() {})
                .getBody();
        assertNotNull(createEvalResult);
        assertEquals(createEvalResult.getCode(), "400");

        // 尝试获取评估结果
        String evalResultUrl = "/api/prompteval/eval/" + evalId;
        HttpEntity<Void> httpEntity = new HttpEntity<>(jsonHeaders);
        ResultVO<PromptEvalVO> evalResult = restTemplate.exchange(evalResultUrl, HttpMethod.GET, httpEntity,
                new ParameterizedTypeReference<ResultVO<PromptEvalVO>>() {}).getBody();
        assertNotNull(evalResult);
        assertEquals(evalResult.getCode(), "400");

        // 尝试获取另一用户的某个任务下的评估结果
        String getTaskEvalUrl = "/api/prompteval/eval/task/" + newTask.getId();
        ResultVO<List<PromptEvalVO>> taskEvalResult = restTemplate.exchange(getTaskEvalUrl, HttpMethod.GET, httpEntity,
                new ParameterizedTypeReference<ResultVO<List<PromptEvalVO>>>() {}).getBody();
        assertNotNull(taskEvalResult);
        assertEquals(taskEvalResult.getCode(), "400");

        // 尝试删除另一用户的评估任务
        String deletePromptTaskUrl = "/api/prompteval/" + newTask.getId();
        ResultVO<Boolean> deleteResult = restTemplate.exchange(deletePromptTaskUrl, HttpMethod.DELETE, httpEntity,
                new ParameterizedTypeReference<ResultVO<Boolean>>() {}).getBody();
        assertNotNull(deleteResult);
        assertEquals(deleteResult.getCode(), "400");

        // 需要等待评估完成
        PromptEvalVO evalRes = null;
        for (int i = 0; i < 20; i++) {
            try {
                Thread.sleep(5000); // 等待 5s
            } catch (InterruptedException e) {
                return;
            }
            evalRes = getPromptEvalResult(evalId);
            if (evalRes.getStatus() == EvalStatus.DONE) {
                break;
            }
        }

        System.out.println("Prompt evaluation result: " + evalRes);
        assertEquals(evalId, evalRes.getId());
        assertEquals(evalRes.getStatus(), EvalStatus.DONE);
        assertEquals(newTask.getId(), evalRes.getTaskId());
    }

    @Test
    public void testBuiltinLLMBasedMetrics() {
        PromptMetric[] builtInMetrics = new PromptMetric[] {
                PromptMetric.ANSWER_ACCURACY,
                PromptMetric.SEMANTIC_SIMILARITY,
                PromptMetric.FACTUAL_CORRECTNESS,
                PromptMetric.RESPONSE_RELEVANCY,
        };

        for (PromptMetric metric : builtInMetrics) {
            // 1.创建 prompt 任务
            PromptTaskVO task = new PromptTaskVO();
            task.setName("Built-in LLM-based metric test");
            task.setDescription("This is a built-in LLM-based metric test.");
            PromptTaskVO newTask = createPromptTask(task);
            assertNotNull(newTask);

            // 2.开启评估
            PromptEvalVO eval = new PromptEvalVO();
            eval.setTaskId(newTask.getId());
            eval.setPrompt("When was Einstein born?");
            eval.setGroundTruth("Albert Einstein was born in 1879.");
            eval.setMetric(metric);

            Integer evalId = createPromptEval(eval);
            assertTrue(evalId > 0);

            // 3. 等待评估完成
            PromptEvalVO evalResult = null;
            for (int i = 0; i < 20; i++) {
                try {
                    Thread.sleep(5000); // 等待 5s
                } catch (InterruptedException e) {
                    return;
                }
                evalResult = getPromptEvalResult(evalId);
                if (evalResult.getStatus() == EvalStatus.DONE) {
                    break;
                }
            }

            // 4. 获取评估结果
            System.out.println("Prompt evaluation result: " + evalResult);
            assertEquals(evalId, evalResult.getId());
            assertEquals(evalResult.getStatus(), EvalStatus.DONE);
            assertEquals(newTask.getId(), evalResult.getTaskId());
        }
    }

    @Test
    public void testBuiltinNonLLMBasedMetrics() {
        PromptMetric[] builtInMetrics = new PromptMetric[] {
                PromptMetric.BLEU_SCORE,
                PromptMetric.ROUGE_SCORE
        };

        for (PromptMetric metric : builtInMetrics) {
            // 1.创建 prompt 任务
            PromptTaskVO task = new PromptTaskVO();
            task.setName("Built-in non-LLM-based metric test");
            task.setDescription("This is a built-in non-LLM-based metric test.");
            PromptTaskVO newTask = createPromptTask(task);
            assertNotNull(newTask);

            // 2.开启评估
            PromptEvalVO eval = new PromptEvalVO();
            eval.setTaskId(newTask.getId());
            eval.setPrompt("When was Einstein born?");
            eval.setGroundTruth("Albert Einstein was born in 1879.");
            eval.setMetric(metric);

            Integer evalId = createPromptEval(eval);
            assertTrue(evalId > 0);

            // 3. 等待评估完成
            PromptEvalVO evalResult = null;
            for (int i = 0; i < 20; i++) {
                try {
                    Thread.sleep(5000); // 等待 5s
                } catch (InterruptedException e) {
                    return;
                }
                evalResult = getPromptEvalResult(evalId);
                if (evalResult.getStatus() == EvalStatus.DONE) {
                    break;
                }
            }

            // 4. 获取评估结果
            System.out.println("Prompt evaluation result: " + evalResult);
            assertEquals(evalId, evalResult.getId());
            assertEquals(evalResult.getStatus(), EvalStatus.DONE);
            assertEquals(newTask.getId(), evalResult.getTaskId());
        }
    }
}
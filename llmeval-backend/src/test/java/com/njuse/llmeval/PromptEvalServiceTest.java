package com.njuse.llmeval;

import com.njuse.llmeval.enums.EvalStatus;
import com.njuse.llmeval.enums.PromptMetric;
import com.njuse.llmeval.exception.LLMEvalException;
import com.njuse.llmeval.po.PromptEval;
import com.njuse.llmeval.po.PromptTask;
import com.njuse.llmeval.po.User;
import com.njuse.llmeval.repository.PromptEvalRepository;
import com.njuse.llmeval.repository.PromptTaskRepository;
import com.njuse.llmeval.service.impl.PromptEvalServiceImpl;
import com.njuse.llmeval.util.RabbitMQUtil;
import com.njuse.llmeval.util.TokenUtil;
import com.njuse.llmeval.vo.PromptEvalVO;
import com.njuse.llmeval.vo.PromptTaskVO;
import com.njuse.llmeval.vo.rpc.PromptEvalRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PromptEvalServiceTest {
    @InjectMocks
    private PromptEvalServiceImpl promptEvalService;

    @Mock
    private TokenUtil tokenUtil;

    @Mock
    private PromptTaskRepository promptTaskRepo;

    @Mock
    private PromptEvalRepository promptEvalRepo;

    @Mock
    private RabbitMQUtil rabbitMQUtil;

    User user;

    String token;

    @BeforeEach
    public void setup() {
        this.user = new User();
        user.setId(1);
        user.setUsername("abc");
        user.setPassword("123");
        user.setPhone("12345678900");
        this.token = "token";
    }

    @Test
    public void normalCreateTask() {
        PromptTaskVO promptTaskVO = new PromptTaskVO();
        promptTaskVO.setName("test");
        promptTaskVO.setDescription("This is a test.");
        when(tokenUtil.decodeToken(token)).thenReturn(user);
        when(promptTaskRepo.save(any(PromptTask.class)))
                .thenAnswer(invocation -> {
                    PromptTask newTask = invocation.getArgument(0);
                    newTask.setId(1);
                    return newTask;
                });
        PromptTaskVO result = assertDoesNotThrow(() -> promptEvalService.createPromptTask(promptTaskVO, token));
        assertEquals(1, result.getId());
        assertEquals(promptTaskVO.getName(), result.getName());
        assertEquals(promptTaskVO.getDescription(), result.getDescription());
        assertEquals(user.getId(), result.getUserId());
    }

    @Test
    public void getZeroTasks() {
        when(tokenUtil.decodeToken(token)).thenReturn(user);
        when(promptTaskRepo.findByUserId(user.getId())).thenReturn(new ArrayList<>());
        List<PromptTaskVO> result = assertDoesNotThrow(() -> promptEvalService.getAllPromptTasks(token));
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void getMultipleTasks() {
        when(tokenUtil.decodeToken(token)).thenReturn(user);
        when(promptTaskRepo.findByUserId(user.getId()))
                .thenAnswer(invocation -> {
                   List<PromptTask> tasks = new ArrayList<>();
                   PromptTask task1 = new PromptTask();
                   task1.setId(1);
                   task1.setName("test1");
                   task1.setDescription("This is test1");
                   task1.setUserId(user.getId());

                   PromptTask task2 = new PromptTask();
                   task2.setId(2);
                   task2.setName("test2");
                   task2.setDescription("This is test2");
                   task2.setUserId(user.getId());

                   tasks.add(task1);
                   tasks.add(task2);
                   return tasks;
                });
        List<PromptTaskVO> result = assertDoesNotThrow(() -> promptEvalService.getAllPromptTasks(token));
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void deleteNonexistentTask() {
        Integer taskId = 1;
        when(tokenUtil.decodeToken(token)).thenReturn(user);
        when(promptTaskRepo.findById(taskId)).thenReturn(Optional.empty());
        LLMEvalException exception = assertThrows(
                LLMEvalException.class,
                () -> promptEvalService.deletePromptTask(taskId, token)
        );
        assertEquals("任务不存在！", exception.getMessage());
        verify(promptTaskRepo, never()).deleteById(any(Integer.class));
    }

    @Test
    public void deleteTaskNotBelongToUser() {
        Integer taskId = 1;
        when(tokenUtil.decodeToken(token)).thenReturn(user);
        when(promptTaskRepo.findById(taskId))
                .thenAnswer(invocation -> {
                    Integer id = invocation.getArgument(0);
                    PromptTask task = new PromptTask();
                    task.setUserId(user.getId() + 1);
                    task.setName("test");
                    task.setDescription("This is a test.");
                    task.setId(id);
                    return Optional.of(task);
                });
        LLMEvalException exception = assertThrows(
                LLMEvalException.class,
                () -> promptEvalService.deletePromptTask(taskId, token)
        );
        assertEquals("该评估任务不属于当前用户！", exception.getMessage());
        verify(promptTaskRepo, never()).deleteById(any(Integer.class));
    }

    @Test
    public void normalDeleteTask() {
        Integer taskId = 1;
        AtomicInteger count = new AtomicInteger(10); // 代表属于当前用户的 task 数量
        when(tokenUtil.decodeToken(token)).thenReturn(user);
        when(promptTaskRepo.findById(taskId))
                .thenAnswer(invocation -> {
                    Integer id = invocation.getArgument(0);
                    PromptTask task = new PromptTask();
                    task.setUserId(user.getId());
                    task.setName("test");
                    task.setDescription("This is a test.");
                    task.setId(id);
                    return Optional.of(task);
                });
        doAnswer(invocationOnMock -> {
            count.decrementAndGet();
            return null;
        }).when(promptTaskRepo).deleteById(taskId);
        assertTrue(() -> promptEvalService.deletePromptTask(taskId, token));
        assertEquals(9, count.get());
    }

    @Test
    public void createEvalBelongToNonexistentTask() {
        PromptEvalVO promptEvalVO = new PromptEvalVO();
        promptEvalVO.setTaskId(1);
        promptEvalVO.setPrompt("This is a prompt.");
        promptEvalVO.setGroundTruth("This is the groundTruth.");
        promptEvalVO.setMetric(PromptMetric.BLEU_SCORE);
        when(promptTaskRepo.findById(1)).thenReturn(Optional.empty());
        LLMEvalException exception = assertThrows(
                LLMEvalException.class,
                () -> promptEvalService.createPromptEval(promptEvalVO, token)
        );
        assertEquals("任务不存在！", exception.getMessage());
        verify(promptEvalRepo, never()).save(any(PromptEval.class));
        verify(rabbitMQUtil, never()).sendEvalRequest(any(PromptEvalRequest.class));
    }

    @Test
    public void createEvalBelongToTaskOfAnotherUser() {
        PromptEvalVO promptEvalVO = new PromptEvalVO();
        promptEvalVO.setTaskId(1);
        promptEvalVO.setPrompt("This is a prompt.");
        promptEvalVO.setGroundTruth("This is the groundTruth.");
        promptEvalVO.setMetric(PromptMetric.BLEU_SCORE);
        when(promptTaskRepo.findById(1))
                .thenAnswer(invocation -> {
                    PromptTask task = new PromptTask();
                    task.setId(1);
                    task.setName("test");
                    task.setDescription("This is a test.");
                    task.setUserId(2);
                    return Optional.of(task);
                });
        when(tokenUtil.decodeToken(token)).thenReturn(user);
        LLMEvalException exception = assertThrows(
                LLMEvalException.class,
                () -> promptEvalService.createPromptEval(promptEvalVO, token)
        );
        assertEquals("该评估任务不属于当前用户！", exception.getMessage());
        verify(promptEvalRepo, never()).save(any(PromptEval.class));
        verify(rabbitMQUtil, never()).sendEvalRequest(any(PromptEvalRequest.class));
    }

    @Test
    public void normalCreateEval() {
        PromptEvalVO promptEvalVO = new PromptEvalVO();
        promptEvalVO.setTaskId(1);
        promptEvalVO.setPrompt("This is a prompt.");
        promptEvalVO.setGroundTruth("This is the groundTruth.");
        promptEvalVO.setMetric(PromptMetric.BLEU_SCORE);
        when(promptTaskRepo.findById(1))
                .thenAnswer(invocation -> {
                    PromptTask task = new PromptTask();
                    task.setId(1);
                    task.setName("test");
                    task.setDescription("This is a test.");
                    task.setUserId(1);
                    return Optional.of(task);
                });
        when(tokenUtil.decodeToken(token)).thenReturn(user);
        when(promptEvalRepo.save(any(PromptEval.class)))
                .thenAnswer(invocation -> {
                    PromptEval arg = invocation.getArgument(0);
                    arg.setId(1);
                    return arg;
                });
        int result = assertDoesNotThrow(() -> promptEvalService.createPromptEval(promptEvalVO, token));
        assertEquals(1, result);
        verify(promptEvalRepo, times(1)).save(any(PromptEval.class));
        verify(rabbitMQUtil, times(1)).sendEvalRequest(any(PromptEvalRequest.class));
    }

    @Test
    public void getNonexistentEvalResult() {
        int evalId = 1;
        when(promptEvalRepo.findById(1)).thenReturn(Optional.empty());
        LLMEvalException exception = assertThrows(
                LLMEvalException.class,
                () -> promptEvalService.getPromptEvalResult(evalId, token)
        );
        assertEquals("评估不存在！", exception.getMessage());
    }

    @Test
    public void getEvalResultOfAnotherUsers() {
        int evalId = 1;
        when(tokenUtil.decodeToken(token)).thenReturn(user);
        when(promptEvalRepo.findById(1))
                .thenAnswer(invocation -> {
                    PromptEval eval = new PromptEval();
                    eval.setGeneratedAnswer("This is the response.");
                    eval.setStatus(EvalStatus.DONE);
                    eval.setPrompt("This is a prompt.");
                    eval.setScore(1.0f);
                    eval.setId(1);
                    eval.setGroundTruth("This is the ground truth.");
                    eval.setTaskId(1);
                    eval.setMetric(PromptMetric.BLEU_SCORE);
                    return Optional.of(eval);
                });
        when(promptTaskRepo.findById(1))
                .thenAnswer(invocation -> {
                    PromptTask task = new PromptTask();
                    task.setUserId(2);
                    task.setDescription("This is a test.");
                    task.setName("test");
                    task.setId(1);
                    return Optional.of(task);
                });
        LLMEvalException exception = assertThrows(
                LLMEvalException.class,
                () -> promptEvalService.getPromptEvalResult(evalId, token)
        );
        assertEquals("该评估任务不属于当前用户！", exception.getMessage());
    }

    @Test
    public void normalGetEvalResult() {
        int evalId = 1;
        int taskId = 1;
        String generatedAnswer = "This is the response.";
        String prompt = "This is a prompt.";
        String groundTruth = "This is the ground truth.";
        float score = 1.0f;
        PromptMetric metric = PromptMetric.BLEU_SCORE;
        EvalStatus status = EvalStatus.DONE;
        when(tokenUtil.decodeToken(token)).thenReturn(user);
        when(promptEvalRepo.findById(evalId))
                .thenAnswer(invocation -> {
                    PromptEval eval = new PromptEval();
                    eval.setGeneratedAnswer(generatedAnswer);
                    eval.setStatus(status);
                    eval.setPrompt(prompt);
                    eval.setScore(score);
                    eval.setId(evalId);
                    eval.setGroundTruth(groundTruth);
                    eval.setTaskId(taskId);
                    eval.setMetric(metric);
                    return Optional.of(eval);
                });
        when(promptTaskRepo.findById(1))
                .thenAnswer(invocation -> {
                    PromptTask task = new PromptTask();
                    task.setUserId(user.getId());
                    task.setDescription("This is a test.");
                    task.setName("test");
                    task.setId(taskId);
                    return Optional.of(task);
                });
        PromptEvalVO promptEvalVO = assertDoesNotThrow(() -> promptEvalService.getPromptEvalResult(evalId, token));
        assertEquals(evalId, promptEvalVO.getId());
        assertEquals(taskId, promptEvalVO.getTaskId());
        assertEquals(generatedAnswer, promptEvalVO.getGeneratedAnswer());
        assertEquals(prompt, promptEvalVO.getPrompt());
        assertEquals(groundTruth, promptEvalVO.getGroundTruth());
        assertEquals(score, promptEvalVO.getScore());
        assertEquals(status, promptEvalVO.getStatus());
        assertEquals(metric, promptEvalVO.getMetric());
    }

    @Test
    public void getEvaluationsByNonexistentTask() {
        int taskId = 1;
        when(promptTaskRepo.findById(taskId)).thenReturn(Optional.empty());
        LLMEvalException exception = assertThrows(
                LLMEvalException.class,
                () -> promptEvalService.getEvaluationsByTask(taskId, token)
        );
        assertEquals("任务不存在！", exception.getMessage());
    }

    @Test
    public void getEvaluationsByTaskBelongToAnotherUser() {
        int taskId = 1;
        when(promptTaskRepo.findById(taskId))
                .thenAnswer(invocation -> {
                    PromptTask task = new PromptTask();
                    task.setId(taskId);
                    task.setUserId(user.getId() + 1);
                    task.setName("test");
                    task.setDescription("This is a test.");
                    return Optional.of(task);
                });
        when(tokenUtil.decodeToken(token)).thenReturn(user);
        LLMEvalException exception = assertThrows(
                LLMEvalException.class,
                () -> promptEvalService.getEvaluationsByTask(taskId, token)
        );
        assertEquals("该评估任务不属于当前用户！", exception.getMessage());
    }

    @Test
    public void getZeroEvaluationsByTask() {
        int taskId = 1;
        when(promptTaskRepo.findById(taskId))
                .thenAnswer(invocation -> {
                    PromptTask task = new PromptTask();
                    task.setId(taskId);
                    task.setUserId(user.getId());
                    task.setName("test");
                    task.setDescription("This is a test.");
                    return Optional.of(task);
                });
        when(tokenUtil.decodeToken(token)).thenReturn(user);
        when(promptEvalRepo.findByTaskId(taskId))
                .thenAnswer(invocation -> new ArrayList<PromptEval>());
        List<PromptEvalVO> result = assertDoesNotThrow(() -> promptEvalService.getEvaluationsByTask(taskId, token));
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void getMultipleEvaluationsByTask() {
        int taskId = 1;
        when(promptTaskRepo.findById(taskId))
                .thenAnswer(invocation -> {
                    PromptTask task = new PromptTask();
                    task.setId(taskId);
                    task.setUserId(user.getId());
                    task.setName("test");
                    task.setDescription("This is a test.");
                    return Optional.of(task);
                });
        when(tokenUtil.decodeToken(token)).thenReturn(user);
        when(promptEvalRepo.findByTaskId(taskId))
                .thenAnswer(invocation -> {
                    List<PromptEval> evalList = new ArrayList<>();
                    PromptEval eval1 = new PromptEval();
                    eval1.setId(1);
                    eval1.setMetric(PromptMetric.BLEU_SCORE);
                    eval1.setPrompt("This is the prompt of eval 1.");
                    eval1.setScore(1.0f);
                    eval1.setTaskId(taskId);
                    eval1.setStatus(EvalStatus.DONE);
                    eval1.setGroundTruth("This is the GT of eval 1.");
                    eval1.setGeneratedAnswer("This is the GA of eval 1.");

                    PromptEval eval2 = new PromptEval();
                    eval2.setId(2);
                    eval2.setMetric(PromptMetric.ROUGE_SCORE);
                    eval2.setPrompt("This is the prompt of eval 2.");
                    eval2.setTaskId(taskId);
                    eval2.setStatus(EvalStatus.DOING);
                    eval2.setGroundTruth("This is the GT of eval 2.");

                    PromptEval eval3 = new PromptEval();
                    eval3.setId(3);
                    eval3.setMetric(PromptMetric.SEMANTIC_SIMILARITY);
                    eval3.setPrompt("This is the prompt of eval 3.");
                    eval3.setTaskId(taskId);
                    eval3.setStatus(EvalStatus.ERROR);
                    eval3.setGroundTruth("This is the GT of eval 3.");

                    evalList.add(eval1);
                    evalList.add(eval2);
                    evalList.add(eval3);
                    return evalList;
                });
        List<PromptEvalVO> result = assertDoesNotThrow(() -> promptEvalService.getEvaluationsByTask(taskId, token));
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals(2, result.get(1).getId());
        assertEquals(3, result.get(2).getId());
    }
}

# PromptEval

这是一个用于根据特定指标对用户提示词进行评估的后端项目。

## 实验说明
请严格参考问卷中提供的**辅助材料**（自然语言文档或伪代码）与实际代码实现来理解系统的设计与流程。

## 文件结构

对于本项目而言，每一个 `.java` 文件中都**有且仅有**一个和文件同名的类。

下面展示了 `src/main/java/com/njuse/llmeval` 下的源代码的目录结构。

* `configure`：项目启动时的配置类。
  * `AsyncConfig.java`
  * `LoginInterceptor.java`
  * `PythonRunner.java`
  * `RabbitMQConfig.java`
  * `WebMvcConfig.java`

* `controller`：用于接收请求的控制器类。
  * `PromptEvalController.java`
  * `UserController.java`

* `enums`：项目中使用到的枚举类。
  * `EvalStatus.java`
  * `PromptMetric.java`

* `exception`：包含项目中异常处理机制的实现类。
  * `GlobalExceptionHandler.java`
  * `LLMEvalException.java`

* `po`：持久化数据类。
  * `PromptEval.java`
  * `PromptTask.java`
  * `User.java`

* `repository`：数据库接口。
  * `PromptEvalRepository.java`
  * `PromptTaskRepository.java`
  * `UserRepository.java`

* `service`：后端接口业务逻辑主体部分。
  * `impl`
    * `PromptEvalServiceImpl.java`
    * `UserServiceImpl.java`

  * `PromptEvalService.java`
  * `UserService.java`

* `util`：工具类。
  * `PromptEvalUtil.java`
  * `RabbitMQUtil.java`
  * `TokenUtil.java`

* `vo`：在前后端或不同进程之间传递的数据定义。
  * `rpc`
    * `PromptEvalRequest.java`
    * `PromptEvalResult.java`

  * `PromptEvalVO.java`
  * `PromptTaskVO.java`
  * `ResultVO.java`
  * `UserVO.java`

* `LLMEvalApplication.java`：项目启动类。

## 环境配置
* JDK 17
* Python 3.10+
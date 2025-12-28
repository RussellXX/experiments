package com.njuse.llmeval.configure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
@Component
public class PythonRunner implements CommandLineRunner {
    @Override
    @Async
    public void run(String... args) throws Exception {
        try {
            String workDir = System.getProperty("user.dir") + "/llmeval-python";
            String pythonScriptPath = workDir + "/main.py";
            ProcessBuilder processBuilder = new ProcessBuilder("python", pythonScriptPath);

            // 设置工作目录
            processBuilder.directory(new File(workDir));

            // 将 python 进程的标准错误重定向到标准输出，方便调试
            processBuilder.redirectErrorStream(true);

            // 启动进程
            Process process = processBuilder.start();

            log.info("Python process started with PID: " + process.pid());

            // 读取 Python 输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                log.info("Python Output: {}", line);
            }
            // 等待 Python 进程执行完毕
            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
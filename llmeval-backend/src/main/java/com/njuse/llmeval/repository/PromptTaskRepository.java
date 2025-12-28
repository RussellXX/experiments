package com.njuse.llmeval.repository;

import com.njuse.llmeval.po.PromptTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromptTaskRepository extends JpaRepository<PromptTask, Integer> {
    List<PromptTask> findByUserId(Integer userId);
}

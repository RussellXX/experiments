package com.njuse.llmeval.repository;

import com.njuse.llmeval.po.PromptEval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromptEvalRepository extends JpaRepository<PromptEval, Integer> {
    List<PromptEval> findByTaskId(Integer taskId);
}

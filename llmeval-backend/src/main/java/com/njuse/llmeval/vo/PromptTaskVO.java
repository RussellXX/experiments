package com.njuse.llmeval.vo;

import com.njuse.llmeval.po.PromptTask;
import lombok.Data;

@Data
public class PromptTaskVO {
    private Integer id;
    private String name;
    private String description;
    private Integer userId;

    public PromptTask toPO() {
        PromptTask task = new PromptTask();
        task.setId(this.id);
        task.setName(this.name);
        task.setDescription(this.description);
        task.setUserId(this.userId);
        return task;
    }
}

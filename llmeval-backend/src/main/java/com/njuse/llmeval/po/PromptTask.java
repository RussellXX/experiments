package com.njuse.llmeval.po;

import com.njuse.llmeval.vo.PromptTaskVO;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class PromptTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String description;
    private Integer userId;

    public PromptTaskVO toVO() {
        PromptTaskVO vo = new PromptTaskVO();
        vo.setId(this.id);
        vo.setName(this.name);
        vo.setDescription(this.description);
        vo.setUserId(this.userId);
        return vo;
    }
}


package com.njuse.llmeval.po;

import com.njuse.llmeval.vo.UserVO;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;


@Data
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String username;
    private String phone;
    private String password;

    public UserVO toVO() {
        UserVO userVO = new UserVO();
        userVO.setId(this.id);
        userVO.setUsername(this.username);
        userVO.setPhone(this.phone);
        userVO.setPassword(this.password);
        return userVO;
    }
}

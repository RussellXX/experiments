package com.njuse.llmeval.vo;

import com.njuse.llmeval.po.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVO {

    private Integer id;
    private String username;
    private String phone;
    private String password;

    // 将自身转换为 PO
    public User toPO() {
        User user = new User();
        user.setId(this.id);
        user.setPhone(this.phone);
        user.setPassword(this.password);
        user.setUsername(this.username);
        return user;
    }
}

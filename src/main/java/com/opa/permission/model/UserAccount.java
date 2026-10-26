package com.opa.permission.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "users", schema = "test")
public class UserAccount {
    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;
    private String username;
    private String passwordHash;
    private Long tenantId;
    private String status;
    private Long createdTime;
    private Long updatedTime;
}

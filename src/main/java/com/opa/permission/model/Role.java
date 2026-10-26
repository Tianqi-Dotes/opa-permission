package com.opa.permission.model;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@TableName(value = "roles", schema = "test")
public class Role {
    @TableId(value = "role_id", type = IdType.INPUT)
    private Long roleId;
    private String roleName;
    private String roleType;
    private String status;
    private String description;
    private Long createdTime;
    private Long updatedTime;
}

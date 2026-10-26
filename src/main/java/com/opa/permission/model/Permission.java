package com.opa.permission.model;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@TableName(value = "permissions", schema = "test")
public class Permission {
    @TableId(value = "permission_id", type = IdType.INPUT)
    private Long permissionId;
    private String permissionName;
    private String resourcePattern;
    private String httpMethod;
    private String status;
    private String description;
    private Long createdTime;
    private Long updatedTime;
}

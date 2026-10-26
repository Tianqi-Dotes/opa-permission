package com.opa.permission.model;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@TableName(value = "relation_user_permissions", schema = "test")
public class UserPermission {
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;
    private Long userId;
    private Long tenantId;
    private Long permissionId;
    private Long grantBy;
    private Long createdTime;
    private Long updatedTime;
}

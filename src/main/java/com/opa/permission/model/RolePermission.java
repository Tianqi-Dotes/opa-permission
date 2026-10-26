package com.opa.permission.model;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@TableName(value = "relation_role_permissions", schema = "test")
public class RolePermission {
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;
    private Long roleId;
    private Long permissionId;
    private Long createdTime;
    private Long updatedTime;
}

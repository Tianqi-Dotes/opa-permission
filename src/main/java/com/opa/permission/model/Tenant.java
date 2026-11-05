package com.opa.permission.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "tenants", schema = "test")
public class Tenant {
    @TableId(value = "tenant_id", type = IdType.INPUT)
    private Long tenantId;
    private String tenantName;
    private String groupName;
    private String status;
    private Long createdTime;
    private Long updatedTime;
}

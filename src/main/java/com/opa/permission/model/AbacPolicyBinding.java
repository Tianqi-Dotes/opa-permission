package com.opa.permission.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "abac_policy_bindings", schema = "test")
public class AbacPolicyBinding {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long policyId;
    private String scopeType;
    private Long tenantId;
    private Long resourceId;
    private String resourceType;
    private Boolean enabled;
    private Integer priority;
    private Long createTime;
    private Long updateTime;
}

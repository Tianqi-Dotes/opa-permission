package com.opa.permission.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "abac_policies", schema = "test")
public class AbacPolicy {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long configId;
    private String policyCode;
    private String policyName;
    private String combiningAlgorithm;
    private Boolean enabled;
    private String description;
    private Long createTime;
    private Long updateTime;
}

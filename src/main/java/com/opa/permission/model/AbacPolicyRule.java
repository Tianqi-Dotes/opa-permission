package com.opa.permission.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.util.Map;

@Data
@TableName(value = "abac_policy_rules", schema = "test", autoResultMap = true)
public class AbacPolicyRule {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long policyId;
    private String ruleCode;
    private String ruleName;
    private String effect;
//    private Integer priority;
    private Boolean enabled;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> conditionJson;
    private String description;
    private Long createTime;
    private Long updateTime;
}

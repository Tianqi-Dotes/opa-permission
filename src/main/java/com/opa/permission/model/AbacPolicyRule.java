package com.opa.permission.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "abac_policy_rules", schema = "test")
public class AbacPolicyRule {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long policyId;
    /** Server-generated rule identifier. */
    private String ruleCode;
    private String ruleName;
    /** Decision returned when the rule's conditions match. */
    private String effect;
//    private Integer priority;
    private Boolean enabled;
    /** Serialized condition data used to decide whether this rule matches. */
    private String conditionStr;
    /** Row-filter or column-mask expression selected by the rule's effect. */
    private String expression;
    private String description;
    private Long createTime;
    private Long updateTime;
}

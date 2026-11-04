package com.opa.permission.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@TableName(value = "abac_policy_test_cases", schema = "test", autoResultMap = true)
public class AbacPolicyTestCase {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long configId;
    private String caseName;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> inputJson;
    private String expectedDecision;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object lastActualResult;
    private Boolean lastPassed;
    private OffsetDateTime lastRunAt;
    private Long createdBy;
    private Long createTime;
    private Long updateTime;
}

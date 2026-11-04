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
@TableName(value = "abac_policy_config", schema = "test", autoResultMap = true)
public class AbacPolicyConfig {
    @TableId(value = "config_id", type = IdType.INPUT)
    private Long configId;
    private String configName;
    private String entrypoint;
    private String combiningAlgorithm;
    private String generationStatus;
    private String generatedRego;
    private String contentChecksum;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> validationReportJson;
    private Long createdBy;
    private Long updatedBy;
    private OffsetDateTime generatedAt;
    private Long createTime;
    private Long updateTime;
}

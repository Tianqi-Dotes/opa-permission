//package com.opa.permission.model;
//
//import com.baomidou.mybatisplus.annotation.IdType;
//import com.baomidou.mybatisplus.annotation.TableField;
//import com.baomidou.mybatisplus.annotation.TableId;
//import com.baomidou.mybatisplus.annotation.TableName;
//import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
//import lombok.Data;
//
//import java.util.Map;
//
//@Data
//@TableName(value = "environment_context_rules", schema = "test", autoResultMap = true)
//public class EnvironmentContextRule {
//    @TableId(value = "id", type = IdType.AUTO)
//    private Long id;
//    private Long policyId;
//    private String ruleCode;
//    private String contextKey;
//    private String sourceType;
//    private String scopeLevel;
//    @TableField(typeHandler = JacksonTypeHandler.class)
//    private Map<String, Object> appliesScopeJson;
//    @TableField(typeHandler = JacksonTypeHandler.class)
//    private Map<String, Object> extractorJson;
//    @TableField(typeHandler = JacksonTypeHandler.class)
//    private Object defaultValueJson;
//    private String normalizePackage;
//    private String normalizeRule;
//    private String failStrategy;
//    private Integer priority;
//    private Boolean enabled;
//    private String description;
//    private Long createTime;
//    private Long updateTime;
//}

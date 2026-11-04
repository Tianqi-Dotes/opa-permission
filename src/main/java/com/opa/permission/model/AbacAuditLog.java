package com.opa.permission.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.util.Map;

@Data
@TableName(value = "abac_audit_logs", schema = "test", autoResultMap = true)
public class AbacAuditLog {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long actorId;
    private String eventType;
    private String objectType;
    private Long objectId;
    private String requestId;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> beforeJson;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> afterJson;
    private Long createTime;
}

package com.opa.permission.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

@Data
@TableName(value = "permission_default_attribute_writes", schema = "test", autoResultMap = true)
public class PermissionDefaultAttributeWrite {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long permissionId;
    private String fromType;
    private String targetType;
    private Long attributeId;
    private String defaultValueText;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object defaultValueJson;
    private Boolean shouldInject;
    private Long createTime;
    private Long updateTime;
}

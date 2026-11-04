package com.opa.permission.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName(value = "subject_attribute_values", schema = "test", autoResultMap = true)
public class SubjectAttributeValue {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long subjectId;
    private Long attributeId;
    private String valueText;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object valueJson;
    private OffsetDateTime validFrom;
    private OffsetDateTime validTo;
    private Long createTime;
    private Long updateTime;
}

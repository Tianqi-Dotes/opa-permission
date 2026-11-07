package com.opa.permission.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@TableName(value = "attribute_definitions", schema = "test")
public class AttributeDefinition {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String name;
    //category IN ('subject', 'resource', 'environment', 'action')
    private String category;
    private String dataType;
    private Boolean multiValued;
    @TableField("is_default")
    @JsonProperty("isDefault")
    private Boolean isDefault;
    private String description;
    private Long createTime;
    private Long updateTime;
}

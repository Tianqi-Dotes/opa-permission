package com.opa.permission.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "resources", schema = "test")
public class AbacResource {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String resourceCode;
    private String resourceType;
    private String resourceName;
    private String owner;
    private String status;
    private Long createTime;
    private Long updateTime;
}

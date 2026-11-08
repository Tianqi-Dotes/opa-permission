package com.opa.permission.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "trino_connections", schema = "test")
public class TrinoConnection {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long resourceId;
    private String jdbcUrl;
    private String authType = "TOKEN";
    private String status;
    private Long createTime;
    private Long updateTime;
}

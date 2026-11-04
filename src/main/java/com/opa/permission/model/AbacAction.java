package com.opa.permission.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "abac_actions", schema = "test")
public class AbacAction {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String actionCode;
    private String actionName;
    private String description;
    private String status;
    private Long createTime;
    private Long updateTime;
}

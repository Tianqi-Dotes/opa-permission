package com.opa.permission.mapper;

import com.opa.permission.model.RolePermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {
    List<RolePermission> selectAll();
}

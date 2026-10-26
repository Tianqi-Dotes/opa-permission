package com.opa.permission.mapper;

import com.opa.permission.model.Permission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
    List<Permission> selectAll();

    List<Permission> selectEffectivePermissions(@Param("userId") Long userId,
                                                @Param("tenantId") Long tenantId);
}

package com.opa.permission.mapper;

import com.opa.permission.model.UserPermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface UserPermissionMapper extends BaseMapper<UserPermission> {
    List<UserPermission> selectAll();
}

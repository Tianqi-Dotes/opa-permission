package com.opa.permission.mapper;

import com.opa.permission.model.UserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
    List<UserRole> selectAll();
}

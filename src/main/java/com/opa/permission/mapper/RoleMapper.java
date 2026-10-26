package com.opa.permission.mapper;

import com.opa.permission.model.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {
    List<Role> selectAll();
}

package com.opa.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.opa.permission.model.UserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserAccountMapper extends BaseMapper<UserAccount> {
    UserAccount selectByUsername(@Param("username") String username);
}

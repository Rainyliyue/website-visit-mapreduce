package com.course.mapreduce.mapper;

import com.course.mapreduce.model.UserAccount;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserMapper {

    @Select("select id, username, role, created_at from users order by id asc")
    List<UserAccount> findAll();
}

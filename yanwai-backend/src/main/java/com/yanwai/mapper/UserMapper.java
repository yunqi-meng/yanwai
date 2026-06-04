package com.yanwai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yanwai.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    @Select("SELECT COUNT(*) FROM user WHERE nickname = #{nickname} AND id != #{excludeId}")
    int countByNicknameExcludeId(@Param("nickname") String nickname, @Param("excludeId") Long excludeId);
}

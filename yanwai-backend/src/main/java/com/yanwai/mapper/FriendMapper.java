package com.yanwai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yanwai.entity.Friend;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FriendMapper extends BaseMapper<Friend> {

    @Select("SELECT * FROM friend WHERE user_id = #{userId} AND friend_id = #{friendId} AND status = 1")
    Friend findByUserAndFriend(@Param("userId") Long userId, @Param("friendId") Long friendId);

    @Select("SELECT friend_id FROM friend WHERE user_id = #{userId} AND status = 1")
    List<Long> findFriendIds(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM friend WHERE user_id = #{userId} AND status = 1")
    int countFriends(@Param("userId") Long userId);
}

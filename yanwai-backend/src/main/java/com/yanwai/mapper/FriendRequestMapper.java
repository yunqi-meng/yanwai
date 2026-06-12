package com.yanwai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yanwai.entity.FriendRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FriendRequestMapper extends BaseMapper<FriendRequest> {

    @Select("SELECT * FROM friend_request WHERE to_user_id = #{userId} AND status = 0 ORDER BY created_at DESC")
    List<FriendRequest> findPendingRequests(@Param("userId") Long userId);

    @Select("SELECT * FROM friend_request WHERE from_user_id = #{fromUserId} AND to_user_id = #{toUserId} AND status = 0")
    FriendRequest findExistingRequest(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId);
}

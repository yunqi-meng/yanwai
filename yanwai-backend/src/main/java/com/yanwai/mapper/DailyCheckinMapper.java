package com.yanwai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yanwai.entity.DailyCheckin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface DailyCheckinMapper extends BaseMapper<DailyCheckin> {

    @Select("SELECT * FROM daily_checkin WHERE user_id = #{userId} AND checkin_date = #{date}")
    DailyCheckin findByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Select("SELECT * FROM daily_checkin WHERE user_id = #{userId} ORDER BY checkin_date DESC LIMIT #{limit}")
    List<DailyCheckin> findByUserIdDesc(@Param("userId") Long userId, @Param("limit") Integer limit);

    @Select("SELECT user_id, MAX(consecutive_days) as max_consecutive FROM daily_checkin GROUP BY user_id ORDER BY max_consecutive DESC LIMIT #{limit}")
    List<Map<String, Object>> getConsecutiveRanking(@Param("limit") Integer limit);
}

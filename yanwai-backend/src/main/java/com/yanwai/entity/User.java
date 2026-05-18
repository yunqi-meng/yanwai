package com.yanwai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String openid;
    
    private String nickname;
    
    @TableField("member_level")
    private Integer memberLevel;
    
    @TableField("daily_analysis_count")
    private Integer dailyAnalysisCount;
    
    @TableField("last_analysis_date")
    private LocalDate lastAnalysisDate;
    
    @TableField("total_analysis")
    private Integer totalAnalysis;
    
    @TableField("total_share")
    private Integer totalShare;
    
    @TableField("late_night_count")
    private Integer lateNightCount;
    
    @TableField("workplace_count")
    private Integer workplaceCount;
    
    @TableField("romance_count")
    private Integer romanceCount;
    
    @TableField("member_expire_time")
    private LocalDateTime memberExpireTime;
    
    @TableField("last_ad_watch_date")
    private LocalDate lastAdWatchDate;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableLogic
    private Integer deleted;
}

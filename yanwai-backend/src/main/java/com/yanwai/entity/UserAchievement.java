package com.yanwai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_achievement")
public class UserAchievement {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("achievement_id")
    private Long achievementId;
    
    @TableField("unlocked_at")
    private LocalDateTime unlockedAt;
    
    @TableLogic
    private Integer deleted;
}

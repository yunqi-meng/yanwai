package com.yanwai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("achievement_def")
public class AchievementDef {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String code;
    
    private String name;
    
    private String description;
    
    @TableField("condition_field")
    private String conditionField;
    
    @TableField("condition_op")
    private String conditionOp;
    
    @TableField("condition_value")
    private Integer conditionValue;
    
    @TableField("reward_type")
    private String rewardType;
    
    @TableField("reward_value")
    private String rewardValue;
    
    private String icon;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableLogic
    private Integer deleted;
}

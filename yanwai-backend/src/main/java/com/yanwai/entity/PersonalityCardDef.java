package com.yanwai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("personality_card_def")
public class PersonalityCardDef {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private Integer rarity;
    
    private String emoji;
    
    private String description;
    
    @TableField("personality_type")
    private String personalityType;
    
    private String trait;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableLogic
    private Integer deleted;
}

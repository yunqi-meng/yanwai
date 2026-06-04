package com.yanwai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("analysis_record")
public class AnalysisRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("original_text")
    private String originalText;
    
    @TableField("analysis_result")
    private String analysisResult;
    
    private String relationship;
    
    @TableField("emotion_curve")
    private String emotionCurve;
    
    private String translations;
    
    private String advice;
    
    @TableField("original_image")
    private String originalImage;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableLogic
    private Integer deleted;
}

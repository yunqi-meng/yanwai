package com.yanwai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("reply_suggestion")
public class ReplySuggestion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long analysisId;
    private Long userId;
    private String suggestions;
    private LocalDateTime createdAt;
}

package com.yanwai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("admin_id")
    private Long adminId;
    
    private String operation;
    
    private String method;
    
    private String params;
    
    private String ip;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

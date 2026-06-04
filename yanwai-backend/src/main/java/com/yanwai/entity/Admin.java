package com.yanwai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("admin")
public class Admin {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    private String password;
    
    @TableField("real_name")
    private String realName;
    
    private Integer role;
    
    private Integer status;
    
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableLogic
    private Integer deleted;
}
-- 新功能数据库扩展脚本
-- 签到系统、好友系统、排行榜

USE yanwai;

-- 每日签到记录表
DROP TABLE IF EXISTS daily_checkin;
CREATE TABLE daily_checkin (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    checkin_date DATE NOT NULL COMMENT '签到日期',
    consecutive_days INT DEFAULT 1 COMMENT '连续签到天数',
    reward_type VARCHAR(50) DEFAULT NULL COMMENT '奖励类型: fragment/card/analysis',
    reward_value VARCHAR(255) DEFAULT NULL COMMENT '奖励内容',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_date (user_id, checkin_date),
    KEY idx_user_id (user_id),
    KEY idx_checkin_date (checkin_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日签到记录表';

-- 好友关系表
DROP TABLE IF EXISTS friend;
CREATE TABLE friend (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    friend_id BIGINT NOT NULL COMMENT '好友ID',
    status TINYINT DEFAULT 1 COMMENT '状态: 0已删除 1正常',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_friend (user_id, friend_id),
    KEY idx_user_id (user_id),
    KEY idx_friend_id (friend_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友关系表';

-- 好友申请表
DROP TABLE IF EXISTS friend_request;
CREATE TABLE friend_request (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    from_user_id BIGINT NOT NULL COMMENT '申请者ID',
    to_user_id BIGINT NOT NULL COMMENT '被申请者ID',
    message VARCHAR(255) DEFAULT NULL COMMENT '申请消息',
    status TINYINT DEFAULT 0 COMMENT '状态: 0待处理 1已同意 2已拒绝',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_from_user (from_user_id),
    KEY idx_to_user (to_user_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友申请表';

-- 回复建议表（可选，也可以直接由AI生成）
DROP TABLE IF EXISTS reply_suggestion;
CREATE TABLE reply_suggestion (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    analysis_id BIGINT NOT NULL COMMENT '分析记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    suggestions JSON NOT NULL COMMENT '回复建议数组',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_analysis_id (analysis_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='回复建议表';

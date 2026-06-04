-- 言外 - 听懂TA的潜台词 数据库建表脚本
-- 数据库名称: yanwai

CREATE DATABASE IF NOT EXISTS yanwai DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE yanwai;

-- 用户表
DROP TABLE IF EXISTS user;
CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    openid VARCHAR(64) NOT NULL UNIQUE COMMENT '微信/设备标识',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    member_level TINYINT DEFAULT 0 COMMENT '会员等级: 0免费, 1会员',
    daily_analysis_count INT DEFAULT 0 COMMENT '当日已分析次数',
    last_analysis_date DATE DEFAULT NULL COMMENT '上次分析日期',
    total_analysis INT DEFAULT 0 COMMENT '总分析次数',
    total_share INT DEFAULT 0 COMMENT '总分享次数',
    late_night_count INT DEFAULT 0 COMMENT '深夜分析次数',
    workplace_count INT DEFAULT 0 COMMENT '职场类分析次数',
    romance_count INT DEFAULT 0 COMMENT '情感类分析次数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    member_expire_time DATETIME DEFAULT NULL COMMENT '会员过期时间',
    last_ad_watch_date DATE DEFAULT NULL COMMENT '上次观看广告日期',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    password VARCHAR(100) DEFAULT NULL COMMENT '密码',
    login_days INT DEFAULT 0 COMMENT '连续登录天数',
    legend_count INT DEFAULT 0 COMMENT '传说卡牌拥有数量',
    last_login_date DATE DEFAULT NULL COMMENT '上次登录日期',
    INDEX idx_openid (openid),
    INDEX idx_member_level (member_level),
    UNIQUE KEY uk_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 分析记录表
DROP TABLE IF EXISTS analysis_record;
CREATE TABLE analysis_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    original_text TEXT NOT NULL COMMENT '原始对话',
    analysis_result JSON COMMENT '完整AI返回JSON',
    relationship VARCHAR(50) DEFAULT NULL COMMENT '关系类型',
    emotion_curve JSON COMMENT '情绪曲线数据',
    translations JSON COMMENT '逐句翻译数组',
    advice JSON COMMENT '建议数组',
    original_image LONGTEXT COMMENT '原始图片base64',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分析记录表';

-- 成就定义表
DROP TABLE IF EXISTS achievement_def;
CREATE TABLE achievement_def (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '唯一标识',
    name VARCHAR(100) NOT NULL COMMENT '成就名称',
    description VARCHAR(255) DEFAULT NULL COMMENT '成就描述',
    condition_field VARCHAR(50) NOT NULL COMMENT '统计字段',
    condition_op VARCHAR(10) DEFAULT '=' COMMENT '操作符',
    condition_value INT NOT NULL COMMENT '阈值',
    reward_type VARCHAR(50) DEFAULT NULL COMMENT '奖励类型',
    reward_value VARCHAR(255) DEFAULT NULL COMMENT '奖励内容',
    icon VARCHAR(100) DEFAULT NULL COMMENT '图标',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    INDEX idx_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成就定义表';

-- 用户成就表
DROP TABLE IF EXISTS user_achievement;
CREATE TABLE user_achievement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    achievement_id BIGINT NOT NULL COMMENT '成就ID',
    unlocked_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '解锁时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    UNIQUE KEY uk_user_achievement (user_id, achievement_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户成就表';

-- 人格卡牌定义表
DROP TABLE IF EXISTS personality_card_def;
CREATE TABLE personality_card_def (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    name VARCHAR(50) NOT NULL COMMENT '卡牌名称',
    rarity TINYINT NOT NULL COMMENT '稀有度: 1普通 2稀有 3史诗 4传说',
    emoji VARCHAR(10) DEFAULT NULL COMMENT '代表emoji',
    description VARCHAR(255) DEFAULT NULL COMMENT '卡牌描述',
    personality_type VARCHAR(50) DEFAULT NULL COMMENT '人格类型',
    trait VARCHAR(255) DEFAULT NULL COMMENT '特质描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    INDEX idx_rarity (rarity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人格卡牌定义表';

-- 用户卡牌表
DROP TABLE IF EXISTS user_card;
CREATE TABLE user_card (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    card_id BIGINT NOT NULL COMMENT '卡牌ID',
    quantity INT DEFAULT 0 COMMENT '拥有数量',
    is_new TINYINT DEFAULT 1 COMMENT '新获得标记: 0否 1是',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '首次获得时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    UNIQUE KEY uk_user_card (user_id, card_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户卡牌表';

-- 初始化成就数据
INSERT INTO achievement_def (code, name, description, condition_field, condition_op, condition_value, reward_type, reward_value, icon) VALUES
('first_decode', '初窥门径', '完成第一次对话分析', 'total_analysis', '>=', 1, '称号', '初学者', '🌱'),
('decode_10', '渐入佳境', '累计分析10次对话', 'total_analysis', '>=', 10, '称号', '解码者', '📈'),
('decode_25', '登堂入室', '累计分析25次对话', 'total_analysis', '>=', 25, '称号', '登堂入室', '🏠'),
('decode_50', '洞察秋毫', '累计分析50次对话', 'total_analysis', '>=', 50, '称号', '洞察大师', '🔍'),
('decode_100', '潜台词大师', '累计分析100次对话', 'total_analysis', '>=', 100, '称号', '潜台词大师', '👑'),
('decode_200', '资深玩家', '累计分析200次对话', 'total_analysis', '>=', 200, '称号', '资深玩家', '🏆'),
('decode_master', '解密大师', '累计分析500次对话', 'total_analysis', '>=', 500, '称号', '解密大师', '🔓'),
('ultimate_decoder', '终极解密者', '累计分析1000次对话', 'total_analysis', '>=', 1000, '称号', '终极解密者', '🏅'),
('share_1', '初次分享', '分享分析结果1次', 'total_share', '>=', 1, '次数', '1', '📤'),
('share_5', '分享先锋', '分享分析结果5次', 'total_share', '>=', 5, '称号', '分享先锋', '✨'),
('share_10', '分享达人', '分享分析结果10次', 'total_share', '>=', 10, '称号', '分享达人', '🎉'),
('share_25', '社交达人', '分享分析结果25次', 'total_share', '>=', 25, '称号', '社交达人', '🤝'),
('spread_ambassador', '传播大使', '分享分析结果50次', 'total_share', '>=', 50, '称号', '传播大使', '📢'),
('night_owl', '夜猫子', '深夜分析10次', 'late_night_count', '>=', 10, '称号', '夜猫子', '🦉'),
('night_owl_25', '资深夜猫', '深夜分析25次', 'late_night_count', '>=', 25, '称号', '资深夜猫', '🌙'),
('workplace_master', '职场精英', '职场类分析20次', 'workplace_count', '>=', 20, '称号', '职场精英', '💼'),
('workplace_master_50', '职场专家', '职场类分析50次', 'workplace_count', '>=', 50, '称号', '职场专家', '🏢'),
('romance_expert', '情感专家', '情感类分析20次', 'romance_count', '>=', 20, '称号', '情感专家', '💕'),
('romance_expert_50', '情感导师', '情感类分析50次', 'romance_count', '>=', 50, '称号', '情感导师', '💘'),
('login_7_days', '连续登录7天', '连续登录7天', 'login_days', '>=', 7, '称号', '坚持不懈', '📅'),
('login_monthly', '月度坚持', '连续登录30天', 'login_days', '>=', 30, '称号', '月度之星', '⭐'),
('login_100_days', '百日征程', '连续登录100天', 'login_days', '>=', 100, '称号', '百日达人', '🎯'),
('legend_collector', '传说收藏家', '拥有5张传说卡牌', 'legend_count', '>=', 5, '称号', '传说收藏家', '⭐');

-- 初始化人格卡牌数据
INSERT INTO personality_card_def (name, rarity, emoji, description, personality_type, trait) VALUES
-- 普通卡牌 (rarity=1)
('直球选手', 1, '🎯', '说话直接，从不拐弯抹角', '直接型', '坦诚直接'),
('暖心天使', 1, '🌈', '总是给人温暖和鼓励', '温暖型', '善解人意'),
('理性思考者', 1, '🧊', '冷静分析，不带情绪色彩', '理性型', '逻辑清晰'),
('幽默达人', 1, '😄', '用幽默化解尴尬气氛', '幽默型', '风趣轻松'),
('倾听者', 1, '👂', '善于倾听，少说多听', '倾听型', '耐心包容'),
('乐天派', 1, '😆', '总是往好的方面想', '乐观型', '积极向上'),
('实干家', 1, '💪', '少说多做，行动派', '行动型', '脚踏实地'),
('好奇宝宝', 1, '❓', '对什么都充满好奇', '好奇型', '求知欲强'),
('和平使者', 1, '☮️', '不喜欢冲突，追求和谐', '和平型', '温和友善'),
('独立行者', 1, '🚶', '习惯自己搞定一切', '独立型', '自力更生'),
('细心观察家', 1, '👁️', '总能注意到别人忽略的细节', '观察型', '心思缜密'),
('耐心倾听者', 1, '🤍', '倾听时从不打断，给予充分尊重', '倾听型', '耐心包容'),
('积极乐观派', 1, '☀️', '任何事情都能看到积极的一面', '乐观型', '阳光向上'),
('温和调解者', 1, '🕊️', '善于化解小矛盾，让大家和谐相处', '和平型', '温和友善'),
('行动派先锋', 1, '🚀', '想到就做，绝不拖延', '行动型', '高效执行'),
('温暖治愈者', 1, '💛', '总能用温暖的话语安慰他人', '温暖型', '治愈系'),
('幽默大师', 1, '🎭', '妙语连珠，总能逗笑大家', '幽默型', '风趣幽默'),
('谨慎思考者', 1, '🧐', '做决定前会深思熟虑', '理性型', '谨慎稳重'),
('热心肠', 1, '🔥', '总是主动帮助他人', '热心型', '乐于助人'),
('现实主义者', 1, '🏢', '理性看待问题，不抱幻想', '现实型', '脚踏实地'),

-- 稀有卡牌 (rarity=2)
('情感翻译官', 2, '💬', '能精准捕捉话语背后的情绪', '敏锐型', '情商极高'),
('矛盾调解员', 2, '⚖️', '善于化解冲突和尴尬', '调解型', '圆滑周到'),
('细节观察者', 2, '🔎', '注意别人忽略的小细节', '观察型', '心思细腻'),
('氛围制造者', 2, '✨', '能带动整体氛围', '领导型', '感染力强'),
('秘密守护者', 2, '🤫', '值得信赖的倾诉对象', '信任型', '守口如瓶'),
('共情大师', 2, '🤗', '能感同身受他人的情绪', '共情型', '善解人意'),
('策略家', 2, '♟️', '说话做事都有策略', '策略型', '深谋远虑'),
('激励者', 2, '💡', '总能给人带来正能量', '激励型', '鼓舞人心'),
('温和外交家', 2, '🎩', '用温和方式达到目的', '外交型', '不卑不亢'),
('创意天才', 2, '💡', '总有新奇的想法', '创意型', '与众不同'),
('情绪分析师', 2, '📊', '能精准分析他人的情绪变化', '分析型', '情绪洞察'),
('社交润滑剂', 2, '💧', '能让任何社交场合变得融洽', '社交型', '社交达人'),
('深度思考者', 2, '💭', '思考问题有深度和见解', '思考型', '思想深邃'),
('情感共鸣者', 2, '💞', '能深刻理解他人的感受', '共情型', '情感共鸣'),
('氛围调节师', 2, '🎵', '能根据场合调节氛围', '调节型', '氛围高手'),
('沟通桥梁', 2, '🌉', '能让不同观点的人顺畅沟通', '沟通型', '善于沟通'),
('智慧引导者', 2, '🧭', '总能给出明智的建议', '引导型', '智慧指引'),
('细节控', 2, '🔬', '对细节要求极高', '完美型', '追求完美'),
('社交观察家', 2, '🔍', '善于观察社交动态', '观察型', '社交敏锐'),
('心理按摩师', 2, '💆', '能缓解他人的心理压力', '治愈型', '心理舒缓'),

-- 史诗卡牌 (rarity=3)
('读心专家', 3, '🧠', '轻松看穿他人内心想法', '读心型', '洞察力强'),
('情绪导师', 3, '🌊', '能引导他人走出情绪低谷', '引导型', '充满智慧'),
('社交高手', 3, '🎭', '在各种社交场合游刃有余', '社交型', '八面玲珑'),
('心灵治愈师', 3, '💝', '用言语治愈他人心灵', '治愈型', '温柔体贴'),
('策略大师', 3, '♟️', '每句话都是精心设计', '谋略型', '深谋远虑'),
('心理洞察者', 3, '🔮', '能看穿人心底层需求', '洞察型', '一针见血'),
('团队凝聚力', 3, '👥', '能把大家凝聚在一起', '凝聚型', '团结协作'),
('灵感之源', 3, '🌟', '能激发他人的灵感', '启发型', '富有创意'),
('心灵捕手', 3, '🎯', '能捕捉到他人内心深处的想法', '洞察型', '心灵洞察'),
('情感操控师', 3, '🎪', '能巧妙引导他人的情绪走向', '引导型', '情绪引导'),
('社交王者', 3, '👑', '任何社交场合都是焦点', '社交型', '社交领袖'),
('心理解码师', 3, '🔓', '能解码复杂的心理活动', '解码型', '心理解码'),
('共情大师', 3, '💖', '能完完全全体会他人的感受', '共情型', '极致共情'),
('灵魂对话者', 3, '✨', '能进行深度的灵魂交流', '交流型', '深度交流'),
('社交策略家', 3, '♟️', '深谙社交策略', '策略型', '社交策略'),
('心理疗愈师', 3, '💎', '能治愈心灵的创伤', '治愈型', '心灵治愈'),

-- 传说卡牌 (rarity=4)
('潜台词宗师', 4, '🔥', '能听出所有言外之意', '宗师型', '绝顶聪明'),
('人际大师', 4, '👑', '掌握所有人际交往秘诀', '大师型', '出神入化'),
('情感催眠师', 4, '💫', '一句话就能触动人心', '催眠型', '影响力极强'),
('完美沟通者', 4, '💎', '说的每一句话都恰到好处', '完美型', '无懈可击'),
('心理解码师', 4, '🔑', '轻松解开心结', '解码型', '手到擒来'),
('智慧启迪者', 4, '🎓', '用智慧化解一切难题', '智慧型', '深不可测'),
('灵魂伴侣', 4, '💕', '仿佛是另一个自己', '契合型', '灵魂契合'),
('命运预言家', 4, '🔮', '能预见人际交往的走向', '预言型', '命运预见'),
('人际之神', 4, '⚡', '在人际关系中无所不能', '全能型', '人际全能'),
('心灵导师', 4, '🎓', '能指引他人找到内心方向', '导师型', '心灵指引'),
('情感宗师', 4, '🔥', '在情感领域登峰造极', '宗师型', '情感宗师'),
('读心大师', 4, '👁️‍🗨️', '能读懂他人的每一个想法', '读心型', '读心宗师'),
('完美社交家', 4, '💎', '社交表现无懈可击', '完美型', '完美社交'),
('命运编织者', 4, '🕸️', '能影响他人的命运走向', '编织型', '命运编织');

-- 管理员表
DROP TABLE IF EXISTS admin;
CREATE TABLE admin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码(MD5加密)',
    real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    role TINYINT DEFAULT 1 COMMENT '角色: 1管理员 2超级管理员',
    status TINYINT DEFAULT 1 COMMENT '状态: 1正常 0禁用',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 初始化管理员数据（密码: 123456）
INSERT INTO admin (username, password, real_name, role) VALUES ('admin', 'e10adc3949ba59abbe56e057f20f883e', '超级管理员', 2);

-- 系统配置表
DROP TABLE IF EXISTS system_config;
CREATE TABLE system_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    description VARCHAR(255) DEFAULT NULL COMMENT '配置描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 操作日志表
DROP TABLE IF EXISTS operation_log;
CREATE TABLE operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    admin_id BIGINT NOT NULL COMMENT '管理员ID',
    operation VARCHAR(100) NOT NULL COMMENT '操作描述',
    method VARCHAR(10) DEFAULT NULL COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    ip VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_admin_id (admin_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 用户卡牌碎片表
DROP TABLE IF EXISTS user_card_fragment;
CREATE TABLE user_card_fragment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    card_id BIGINT NOT NULL COMMENT '卡牌ID',
    fragment_count INT DEFAULT 0 COMMENT '碎片数量',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户卡牌碎片表';

package com.yanwai.config;

import com.yanwai.entity.*;
import com.yanwai.mapper.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Profile("h2")
@Configuration
public class H2DatabaseInitializer {

    @Bean
    CommandLineRunner initDatabase(
            AchievementDefMapper achievementDefMapper,
            PersonalityCardDefMapper personalityCardDefMapper,
            UserMapper userMapper) {
        return args -> {
            initAchievements(achievementDefMapper);
            initCards(personalityCardDefMapper);
            initTestUser(userMapper);
        };
    }

    private void initAchievements(AchievementDefMapper mapper) {
        String[][] achievements = {
                {"first_decode", "初窥门径", "完成第一次对话分析", "total_analysis", ">=", "1", null, null, "🌱"},
                {"decode_10", "渐入佳境", "累计分析10次对话", "total_analysis", ">=", "10", null, null, "📈"},
                {"decode_25", "登堂入室", "累计分析25次对话", "total_analysis", ">=", "25", null, null, "🏠"},
                {"decode_50", "洞察秋毫", "累计分析50次对话", "total_analysis", ">=", "50", null, null, "🔍"},
                {"decode_100", "潜台词大师", "累计分析100次对话", "total_analysis", ">=", "100", null, null, "👑"},
                {"decode_200", "资深玩家", "累计分析200次对话", "total_analysis", ">=", "200", null, null, "🏆"},
                {"share_1", "初次分享", "分享分析结果1次", "total_share", ">=", "1", null, null, "📤"},
                {"share_5", "分享先锋", "分享分析结果5次", "total_share", ">=", "5", null, null, "✨"},
                {"share_10", "分享达人", "分享分析结果10次", "total_share", ">=", "10", null, null, "🎉"},
                {"share_25", "社交达人", "分享分析结果25次", "total_share", ">=", "25", null, null, "🤝"},
                {"night_owl", "夜猫子", "深夜分析10次", "late_night_count", ">=", "10", null, null, "🦉"},
                {"night_owl_25", "资深夜猫", "深夜分析25次", "late_night_count", ">=", "25", null, null, "🌙"},
                {"workplace_master", "职场精英", "职场类分析20次", "workplace_count", ">=", "20", null, null, "💼"},
                {"workplace_master_50", "职场专家", "职场类分析50次", "workplace_count", ">=", "50", null, null, "🏢"},
                {"romance_expert", "情感专家", "情感类分析20次", "romance_count", ">=", "20", null, null, "💕"},
                {"romance_expert_50", "情感导师", "情感类分析50次", "romance_count", ">=", "50", null, null, "💘"},
                {"legend_collector", "传说收藏家", "拥有5张传说卡牌", "legend_count", ">=", "5", null, null, "⭐"}
        };

        for (String[] a : achievements) {
            AchievementDef def = new AchievementDef();
            def.setCode(a[0]);
            def.setName(a[1]);
            def.setDescription(a[2]);
            def.setConditionField(a[3]);
            def.setConditionOp(a[4]);
            def.setConditionValue(Integer.parseInt(a[5]));
            def.setRewardType(a[6]);
            def.setRewardValue(a[7]);
            def.setIcon(a[8]);
            def.setCreatedAt(LocalDateTime.now());
            def.setDeleted(0);
            mapper.insert(def);
        }
    }

    private void initCards(PersonalityCardDefMapper mapper) {
        Object[][] cards = {
                {"直球选手", 1, "🎯", "说话直接，从不拐弯抹角", "直接型", "坦诚直接"},
                {"暖心天使", 1, "🌈", "总是给人温暖和鼓励", "温暖型", "善解人意"},
                {"理性思考者", 1, "🧊", "冷静分析，不带情绪色彩", "理性型", "逻辑清晰"},
                {"幽默达人", 1, "😄", "用幽默化解尴尬气氛", "幽默型", "风趣轻松"},
                {"倾听者", 1, "👂", "善于倾听，少说多听", "倾听型", "耐心包容"},
                {"乐天派", 1, "😆", "总是往好的方面想", "乐观型", "积极向上"},
                {"实干家", 1, "💪", "少说多做，行动派", "行动型", "脚踏实地"},
                {"好奇宝宝", 1, "❓", "对什么都充满好奇", "好奇型", "求知欲强"},
                {"和平使者", 1, "☮️", "不喜欢冲突，追求和谐", "和平型", "温和友善"},
                {"独立行者", 1, "🚶", "习惯自己搞定一切", "独立型", "自力更生"},
                {"情感翻译官", 2, "💬", "能精准捕捉话语背后的情绪", "敏锐型", "情商极高"},
                {"矛盾调解员", 2, "⚖️", "善于化解冲突和尴尬", "调解型", "圆滑周到"},
                {"细节观察者", 2, "🔎", "注意别人忽略的小细节", "观察型", "心思细腻"},
                {"氛围制造者", 2, "✨", "能带动整体氛围", "领导型", "感染力强"},
                {"秘密守护者", 2, "🤫", "值得信赖的倾诉对象", "信任型", "守口如瓶"},
                {"共情大师", 2, "🤗", "能感同身受他人的情绪", "共情型", "善解人意"},
                {"策略家", 2, "♟️", "说话做事都有策略", "策略型", "深谋远虑"},
                {"激励者", 2, "💡", "总能给人带来正能量", "激励型", "鼓舞人心"},
                {"温和外交家", 2, "🎩", "用温和方式达到目的", "外交型", "不卑不亢"},
                {"创意天才", 2, "💡", "总有新奇的想法", "创意型", "与众不同"},
                {"读心专家", 3, "🧠", "轻松看穿他人内心想法", "读心型", "洞察力强"},
                {"情绪导师", 3, "🌊", "能引导他人走出情绪低谷", "引导型", "充满智慧"},
                {"社交高手", 3, "🎭", "在各种社交场合游刃有余", "社交型", "八面玲珑"},
                {"心灵治愈师", 3, "💝", "用言语治愈他人心灵", "治愈型", "温柔体贴"},
                {"策略大师", 3, "♟️", "每句话都是精心设计", "谋略型", "深谋远虑"},
                {"心理洞察者", 3, "🔮", "能看穿人心底层需求", "洞察型", "一针见血"},
                {"团队凝聚力", 3, "👥", "能把大家凝聚在一起", "凝聚型", "团结协作"},
                {"灵感之源", 3, "🌟", "能激发他人的灵感", "启发型", "富有创意"},
                {"潜台词宗师", 4, "🔥", "能听出所有言外之意", "宗师型", "绝顶聪明"},
                {"人际大师", 4, "👑", "掌握所有人际交往秘诀", "大师型", "出神入化"},
                {"情感催眠师", 4, "💫", "一句话就能触动人心", "催眠型", "影响力极强"},
                {"完美沟通者", 4, "💎", "说的每一句话都恰到好处", "完美型", "无懈可击"},
                {"心理解码师", 4, "🔑", "轻松解开心结", "解码型", "手到擒来"},
                {"智慧启迪者", 4, "🎓", "用智慧化解一切难题", "智慧型", "深不可测"}
        };

        for (Object[] c : cards) {
            PersonalityCardDef def = new PersonalityCardDef();
            def.setName((String) c[0]);
            def.setRarity((Integer) c[1]);
            def.setEmoji((String) c[2]);
            def.setDescription((String) c[3]);
            def.setPersonalityType((String) c[4]);
            def.setTrait((String) c[5]);
            def.setCreatedAt(LocalDateTime.now());
            def.setDeleted(0);
            mapper.insert(def);
        }
    }

    private void initTestUser(UserMapper mapper) {
        User user = new User();
        user.setOpenid("device_1234567890");
        user.setNickname("测试用户");
        user.setMemberLevel(0);
        user.setDailyAnalysisCount(0);
        user.setLastAnalysisDate(LocalDate.now());
        user.setTotalAnalysis(0);
        user.setTotalShare(0);
        user.setLateNightCount(0);
        user.setWorkplaceCount(0);
        user.setRomanceCount(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setDeleted(0);
        mapper.insert(user);
    }
}

package com.yanwai.service.impl;

import com.yanwai.dto.AnalysisResultDTO;
import com.yanwai.dto.AnalysisResultDTO.EmotionPoint;
import com.yanwai.dto.AnalysisResultDTO.TranslationItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MockAiService {

    public static AnalysisResultDTO generateMockResponse(String dialogText) {
        AnalysisResultDTO result = new AnalysisResultDTO();
        result.setRelationship(guessRelationship(dialogText));

        List<EmotionPoint> emotionCurve = new ArrayList<>();
        String[] lines = dialogText.split("\n");
        for (int i = 0; i < lines.length; i++) {
            EmotionPoint point = new EmotionPoint();
            point.setIndex(i);
            point.setSpeaker(i % 2 == 0 ? "A" : "B");
            point.setEmotionScore(0.4 + Math.random() * 0.5);
            point.setEmotionType(getRandomEmotionType());
            point.setDescription(generateEmotionDescription(lines[i]));
            emotionCurve.add(point);
        }
        result.setEmotionCurve(emotionCurve);

        List<TranslationItem> translations = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            TranslationItem item = new TranslationItem();
            item.setIndex(i);
            item.setOriginal(lines[i].trim());
            item.setLiteral(lines[i].trim());
            item.setSubtext(generateSubtext(lines[i], i % 2 == 0));
            item.setEmotionScore(0.3 + Math.random() * 0.6);
            translations.add(item);
        }
        result.setTranslations(translations);

        result.setAdvice(Arrays.asList(
            "建议双方多进行直接沟通",
            "注意说话时的语气和表达方式",
            "尝试站在对方的角度思考问题",
            "遇到问题时及时沟通避免误会积累",
            "适当表达自己的感受和需求"
        ));

        return result;
    }

    private static String guessRelationship(String text) {
        String lowerText = text.toLowerCase();
        if (lowerText.contains("爱") || lowerText.contains("喜欢") || lowerText.contains("约会")) {
            return "情侣";
        }
        if (lowerText.contains("工作") || lowerText.contains("老板") || lowerText.contains("同事")) {
            return "同事";
        }
        if (lowerText.contains("爸") || lowerText.contains("妈") || lowerText.contains("家里")) {
            return "家人";
        }
        return "朋友";
    }

    private static String getRandomEmotionType() {
        String[] types = {"neutral", "positive", "negative", "happy", "sad"};
        return types[(int) (Math.random() * types.length)];
    }

    private static String generateEmotionDescription(String text) {
        if (text.contains("！") || text.contains("!")) {
            return "情绪比较激动";
        }
        if (text.contains("？") || text.contains("?")) {
            return "有疑问或不确定";
        }
        if (text.contains("好") || text.contains("开心") || text.contains("嗯")) {
            return "心情比较愉悦";
        }
        if (text.contains("不") || text.contains("没")) {
            return "语气中有否定或负面情绪";
        }
        return "语气平稳";
    }

    private static String generateSubtext(String text, boolean isSpeakerA) {
        if (text.contains("随便")) {
            return "表面上随便，其实可能需要更多关心";
        }
        if (text.contains("没事") || text.contains("没什么")) {
            return "实际上可能有事想说，但不想直接表达";
        }
        if (text.contains("哦") || text.contains("嗯") || text.contains("好")) {
            return "在回应，但可能没有太多热情";
        }
        if (text.contains("我觉得") || text.contains("我认为")) {
            return "表达自己的观点，希望被理解";
        }
        if (text.contains("你觉得呢") || text.contains("怎么样")) {
            return "在寻求对方的看法和认同";
        }
        return "这是一句普通的交流，没有特别的潜台词";
    }
}

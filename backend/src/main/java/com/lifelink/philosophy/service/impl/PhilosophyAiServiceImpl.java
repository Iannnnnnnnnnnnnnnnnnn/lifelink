package com.lifelink.philosophy.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelink.ai.dto.AiChatRequest;
import com.lifelink.ai.dto.AiChatResult;
import com.lifelink.ai.service.AiChatService;
import com.lifelink.philosophy.dto.PhilosophyResponseItem;
import com.lifelink.philosophy.entity.Philosopher;
import com.lifelink.philosophy.service.PhilosophyAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PhilosophyAiServiceImpl implements PhilosophyAiService {

    private static final String ZH_CN = "zh-CN";
    private static final int MAX_PROMPT_QUESTION_LENGTH = 1000;

    private final AiChatService aiChatService;
    private final ObjectMapper objectMapper;

    @Override
    public PhilosophyResponseItem generate(String question, Philosopher philosopher, String language) {
        String safeQuestion = question.length() > MAX_PROMPT_QUESTION_LENGTH
                ? question.substring(0, MAX_PROMPT_QUESTION_LENGTH)
                : question;
        AiChatResult result = aiChatService.chat(new AiChatRequest(
                buildSystemPrompt(language),
                buildUserPrompt(safeQuestion, philosopher, language),
                null,
                null,
                true
        ));
        PhilosophyResponseItem item = parseContent(result.getContent(), philosopher, language);
        item.setRawResponse(result.getRawResponse());
        return item;
    }

    private String buildSystemPrompt(String language) {
        String outputLanguage = ZH_CN.equals(language) ? "中文" : "English";
        return "你正在模拟一位哲学思想风格分析器。\n"
                + "\n"
                + "重要限制：\n"
                + "1. 你不是哲学家本人。\n"
                + "2. 不要声称自己是真实历史人物。\n"
                + "3. 不要伪造具体出处。\n"
                + "4. 不要编造名言。\n"
                + "5. 只基于该思想家的公开思想风格进行模拟分析。\n"
                + "6. 回答要适合普通用户阅读。\n"
                + "7. 不要输出攻击性、歧视性内容。\n"
                + "8. 不要鼓励自伤、违法或危险行为。\n"
                + "9. 必须返回 JSON。\n"
                + "\n"
                + "输出 JSON 格式：\n"
                + "{\n"
                + "  \"viewpoint\": \"核心观点\",\n"
                + "  \"questionBack\": \"追问\",\n"
                + "  \"objection\": \"可能的反驳\",\n"
                + "  \"summary\": \"一句话总结\"\n"
                + "}\n"
                + "\n"
                + "字段长度：\n"
                + "- viewpoint：100～250 字\n"
                + "- questionBack：30～120 字\n"
                + "- objection：60～160 字\n"
                + "- summary：20～60 字\n"
                + "\n"
                + "language = " + language + "，请使用" + outputLanguage + "回答。";
    }

    private String buildUserPrompt(String question, Philosopher philosopher, String language) {
        String name = ZH_CN.equals(language) ? philosopher.getNameZh() : philosopher.getNameEn();
        String era = ZH_CN.equals(language) ? philosopher.getEraZh() : philosopher.getEraEn();
        String description = ZH_CN.equals(language) ? philosopher.getDescriptionZh() : philosopher.getDescriptionEn();
        return "philosopherCode: " + philosopher.getCode() + "\n"
                + "philosopherName: " + name + "\n"
                + "era: " + era + "\n"
                + "styleDescription: " + resolveStyleDescription(philosopher.getCode(), language) + "\n"
                + "databaseDescription: " + description + "\n"
                + "userQuestion: " + question + "\n"
                + "只返回 JSON，不要添加 Markdown 或解释文字。";
    }

    private String resolveStyleDescription(String code, String language) {
        boolean zh = ZH_CN.equals(language);
        if ("SOCRATES".equals(code)) {
            return zh ? "追问、概念澄清、德性、通过问题揭示矛盾。"
                    : "Questioning, conceptual clarification, virtue, and revealing contradictions through questions.";
        }
        if ("PLATO".equals(code)) {
            return zh ? "理念论、灵魂、正义、理想秩序。"
                    : "Theory of forms, soul, justice, and ideal order.";
        }
        if ("ARISTOTLE".equals(code)) {
            return zh ? "目的论、德性伦理、中道、经验观察。"
                    : "Teleology, virtue ethics, the mean, and empirical observation.";
        }
        if ("KANT".equals(code)) {
            return zh ? "理性、义务、自由、道德律。"
                    : "Reason, duty, freedom, and moral law.";
        }
        if ("NIETZSCHE".equals(code)) {
            return zh ? "价值重估、权力意志、生命力、超人。"
                    : "Revaluation of values, will to power, vitality, and the overman.";
        }
        if ("SCHOPENHAUER".equals(code)) {
            return zh ? "意志、痛苦、欲望、审美解脱。"
                    : "Will, suffering, desire, and aesthetic release.";
        }
        if ("CONFUCIUS".equals(code)) {
            return zh ? "仁、礼、修身、君子。"
                    : "Ren, ritual propriety, self-cultivation, and the noble person.";
        }
        if ("ZHUANGZI".equals(code)) {
            return zh ? "逍遥、齐物、自然、无为。"
                    : "Free wandering, equality of things, naturalness, and non-forcing.";
        }
        return zh ? "基于公开思想风格进行温和、清晰的模拟分析。"
                : "A gentle and clear simulation based on public philosophical style.";
    }

    private PhilosophyResponseItem parseContent(String content, Philosopher philosopher, String language) {
        try {
            String json = extractJson(content);
            JsonNode root = objectMapper.readTree(json);
            PhilosophyResponseItem item = new PhilosophyResponseItem();
            item.setPhilosopherCode(philosopher.getCode());
            item.setPhilosopherName(resolveName(philosopher, language));
            item.setViewpoint(text(root, "viewpoint"));
            item.setQuestionBack(text(root, "questionBack"));
            item.setObjection(text(root, "objection"));
            item.setSummary(text(root, "summary"));
            return item;
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid AI JSON response");
        }
    }

    private String extractJson(String content) {
        String trimmed = content == null ? "" : content.trim();
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        return trimmed;
    }

    private String resolveName(Philosopher philosopher, String language) {
        return ZH_CN.equals(language) ? philosopher.getNameZh() : philosopher.getNameEn();
    }

    private String text(JsonNode root, String field) {
        JsonNode value = root.path(field);
        return value.isTextual() ? value.asText() : "";
    }
}

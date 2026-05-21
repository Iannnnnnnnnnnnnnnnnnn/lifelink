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
    private static final String LAYOUT_PHILOSOPHY_CARD = "PHILOSOPHY_CARD";
    private static final String LAYOUT_COUNSELOR_CARD = "COUNSELOR_CARD";
    private static final String PSYCHOLOGY_TEACHER = "PSYCHOLOGY_TEACHER";
    private static final int MAX_PROMPT_QUESTION_LENGTH = 1000;

    private final AiChatService aiChatService;
    private final ObjectMapper objectMapper;

    @Override
    public PhilosophyResponseItem generate(String question, Philosopher philosopher, String language) {
        String safeQuestion = question.length() > MAX_PROMPT_QUESTION_LENGTH
                ? question.substring(0, MAX_PROMPT_QUESTION_LENGTH)
                : question;
        if (isCounselor(philosopher) && isCrisisMessage(safeQuestion)) {
            return buildCrisisCounselorItem(philosopher, language);
        }
        AiChatResult result = aiChatService.chat(new AiChatRequest(
                isCounselor(philosopher) ? buildCounselorSystemPrompt(language) : buildSystemPrompt(language),
                isCounselor(philosopher)
                        ? buildCounselorUserPrompt(safeQuestion, language)
                        : buildUserPrompt(safeQuestion, philosopher, language),
                null,
                null,
                true
        ));
        PhilosophyResponseItem item = parseContent(result.getContent(), philosopher, language);
        item.setRawResponse(result.getRawResponse());
        return item;
    }

    private String buildCounselorSystemPrompt(String language) {
        String outputLanguage = ZH_CN.equals(language) ? "中文" : "English";
        return "你正在模拟一位“心理老师”的支持性对话风格。\n"
                + "\n"
                + "重要限制：\n"
                + "1. 你不是医生。\n"
                + "2. 你不是临床心理治疗师。\n"
                + "3. 你不能做医学诊断。\n"
                + "4. 你不能判断用户患有什么心理疾病。\n"
                + "5. 你不能替代专业心理咨询、医疗或紧急帮助。\n"
                + "6. 如果用户表达自伤、自杀、伤害他人或严重危机风险，请温和建议立即联系身边可信的人、当地紧急热线或专业机构。\n"
                + "7. 回答要温和、现实、具体、支持性。\n"
                + "8. 使用第一人称表达，例如“我会先陪你把这件事拆开看”。\n"
                + "9. 不要使用“核心观点 / 追问 / 可能的反驳”这种哲学结构。\n"
                + "10. 不要展示理论标签。\n"
                + "11. 不要过度说教。\n"
                + "12. 不要使用高高在上的语气。\n"
                + "13. 不要把所有问题都解释成童年创伤或心理疾病。\n"
                + "14. 优先帮助用户识别情绪、事实、需求和下一步行动。\n"
                + "15. 回答要贴近日常生活。\n"
                + "\n"
                + "回答风格：\n"
                + "- 像一位温和、可靠、现实的心理老师。\n"
                + "- 能共情，但不过度煽情。\n"
                + "- 能给建议，但不命令用户。\n"
                + "- 能帮助用户把混乱的问题理清楚。\n"
                + "- 多使用“我建议你可以……”“我们先把它分成……”“你现在的感受是有原因的……”。\n"
                + "- 少用专业术语，少用空泛鸡汤，不要编造心理学实验或权威出处。\n"
                + "\n"
                + "必须严格返回 JSON：\n"
                + "{\n"
                + "  \"understanding\": \"我的理解\",\n"
                + "  \"advice\": \"给你的建议\",\n"
                + "  \"practice\": \"可以试试\",\n"
                + "  \"support\": \"一句话陪伴\"\n"
                + "}\n"
                + "\n"
                + "长度要求：\n"
                + "- understanding：80～180 字\n"
                + "- advice：120～300 字\n"
                + "- practice：60～160 字\n"
                + "- support：20～60 字\n"
                + "\n"
                + "language = " + language + "，请使用" + outputLanguage + "回答。";
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

    private String buildCounselorUserPrompt(String question, String language) {
        String outputLanguage = ZH_CN.equals(language) ? "中文" : "English";
        return "philosopherCode: " + PSYCHOLOGY_TEACHER + "\n"
                + "responseLayout: " + LAYOUT_COUNSELOR_CARD + "\n"
                + "用户输入：\n"
                + question + "\n\n"
                + "请用" + outputLanguage + "回答。只返回 JSON，不要添加 Markdown 或解释文字。";
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
            item.setResponseLayout(resolveResponseLayout(philosopher));
            if (isCounselor(philosopher)) {
                item.setUnderstanding(text(root, "understanding"));
                item.setAdvice(text(root, "advice"));
                item.setPractice(text(root, "practice"));
                item.setSupport(text(root, "support"));
                item.setViewpoint(item.getUnderstanding());
                item.setQuestionBack(item.getAdvice());
                item.setObjection(item.getPractice());
                item.setSummary(item.getSupport());
                return item;
            }
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

    private boolean isCounselor(Philosopher philosopher) {
        return philosopher != null && (PSYCHOLOGY_TEACHER.equals(philosopher.getCode())
                || LAYOUT_COUNSELOR_CARD.equals(philosopher.getResponseLayout())
                || "COUNSELOR".equals(philosopher.getRoleType()));
    }

    private String resolveResponseLayout(Philosopher philosopher) {
        if (philosopher != null && philosopher.getResponseLayout() != null && !philosopher.getResponseLayout().isBlank()) {
            return philosopher.getResponseLayout();
        }
        return isCounselor(philosopher) ? LAYOUT_COUNSELOR_CARD : LAYOUT_PHILOSOPHY_CARD;
    }

    private boolean isCrisisMessage(String content) {
        String text = content == null ? "" : content.toLowerCase();
        return text.contains("自杀")
                || text.contains("不想活")
                || text.contains("结束生命")
                || text.contains("伤害自己")
                || text.contains("伤害别人")
                || text.contains("suicide")
                || text.contains("kill myself")
                || text.contains("self harm")
                || text.contains("self-harm")
                || text.contains("hurt myself")
                || text.contains("hurt others");
    }

    private PhilosophyResponseItem buildCrisisCounselorItem(Philosopher philosopher, String language) {
        boolean zh = ZH_CN.equals(language);
        String message = zh
                ? "我很在意你现在的安全。如果你有伤害自己或他人的冲动，请先不要一个人扛着，立刻联系身边可信的人，或者拨打当地紧急电话/危机干预热线。你现在最重要的不是把所有问题想清楚，而是先让自己处在安全的地方。"
                : "I’m really concerned about your safety right now. If you feel at risk of hurting yourself or someone else, please contact a trusted person nearby or call local emergency services or a crisis hotline immediately. The priority is not to solve everything at once, but to keep you safe right now.";
        PhilosophyResponseItem item = new PhilosophyResponseItem();
        item.setPhilosopherCode(philosopher.getCode());
        item.setPhilosopherName(resolveName(philosopher, language));
        item.setResponseLayout(LAYOUT_COUNSELOR_CARD);
        item.setUnderstanding(message);
        item.setAdvice(zh
                ? "请立刻把自己移到更安全的地方，并联系一个此刻能到你身边的人。"
                : "Please move to a safer place now and contact someone who can be with you immediately.");
        item.setPractice(zh
                ? "现在先放下危险物品，拨打当地紧急电话或危机热线。"
                : "Put away anything dangerous and call local emergency services or a crisis hotline now.");
        item.setSupport(zh ? "先保证安全，我们再慢慢处理后面的事。" : "Stay safe first; everything else can come after that.");
        item.setViewpoint(item.getUnderstanding());
        item.setQuestionBack(item.getAdvice());
        item.setObjection(item.getPractice());
        item.setSummary(item.getSupport());
        item.setRawResponse("{\"understanding\":\"" + item.getUnderstanding() + "\"}");
        return item;
    }

    private String text(JsonNode root, String field) {
        JsonNode value = root.path(field);
        return value.isTextual() ? value.asText() : "";
    }
}

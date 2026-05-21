package com.lifelink.ai.service.impl;

import com.lifelink.ai.config.AiProperties;
import com.lifelink.ai.dto.AiChatRequest;
import com.lifelink.ai.dto.AiChatResult;
import com.lifelink.ai.service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MockAiChatServiceImpl implements AiChatService {

    private final AiProperties properties;

    @Override
    public AiChatResult chat(AiChatRequest request) {
        if (!Boolean.TRUE.equals(request.getResponseFormatJson())) {
            String content = mockChatContent(request);
            return new AiChatResult(content, properties.getProvider(), properties.getModel(), true, content);
        }
        if (isPsychologyTeacher(request)) {
            String content = isEnglish(request)
                    ? "{"
                    + "\"understanding\":\"I can sense that things may feel tangled right now. It may not be only the situation itself, but also the emotions, expectations and uncertainty around it.\","
                    + "\"advice\":\"I would suggest not rushing into a final judgment. Try separating the situation into three parts: what happened, what you feel, and what you need. This can help you see what is fact, what is fear, and what is within your control.\","
                    + "\"practice\":\"Take three minutes to write down: What matters most to me here? What am I afraid of? What is one small step I can take now?\","
                    + "\"support\":\"You do not need to solve everything at once. Start by taking care of yourself in this moment.\""
                    + "}"
                    : "{"
                    + "\"understanding\":\"我能感觉到你现在可能有点乱，也许不是事情本身特别复杂，而是情绪、期待和现实挤在了一起，让你不知道先处理哪一部分。\","
                    + "\"advice\":\"我建议你先不要急着做最终判断。可以先把这件事分成三栏：发生了什么、我有什么感受、我真正需要什么。这样你会更容易看清，哪些是事实，哪些是担心，哪些是你可以主动处理的部分。\","
                    + "\"practice\":\"现在可以花三分钟写下：我最在意的是什么？我最害怕的是什么？我现在能做的最小一步是什么？\","
                    + "\"support\":\"你不需要一下子解决全部问题，先照顾好此刻的自己。\""
                    + "}";
            return new AiChatResult(content, properties.getProvider(), properties.getModel(), true, content);
        }
        String content = isEnglish(request)
                ? "{"
                + "\"viewpoint\":\"This is a simulated perspective based on a philosophical style. The first task is not to rush toward a conclusion, but to clarify the concepts, desires, and judgments hidden behind the question.\","
                + "\"questionBack\":\"Have you clearly defined the central concept in your question?\","
                + "\"objection\":\"Another view may argue that this issue cannot be understood from one angle alone and must be returned to its concrete context.\","
                + "\"summary\":\"What matters first is clarifying the question itself.\""
                + "}"
                : "{"
                + "\"viewpoint\":\"这是一个基于思想风格的模拟观点。真正需要处理的不是立刻给出结论，而是先看清问题背后的概念、欲望和判断。\","
                + "\"questionBack\":\"你是否已经定义清楚了问题中的核心概念？\","
                + "\"objection\":\"另一种观点可能认为，这个问题不能只从单一角度理解，还需要放回具体处境和关系中。\","
                + "\"summary\":\"真正重要的是先澄清问题本身。\""
                + "}";
        return new AiChatResult(content, properties.getProvider(), properties.getModel(), true, content);
    }

    private boolean isEnglish(AiChatRequest request) {
        String systemPrompt = request == null ? "" : String.valueOf(request.getSystemPrompt());
        String userPrompt = request == null ? "" : String.valueOf(request.getUserPrompt());
        String messages = request == null ? "" : String.valueOf(request.getMessages());
        return systemPrompt.contains("en-US") || userPrompt.contains("en-US") || messages.contains("en-US");
    }

    private String mockChatContent(AiChatRequest request) {
        boolean english = isEnglish(request);
        String content = latestUserContent(request);
        String code = resolveCode(request);
        if (english) {
            if ("PSYCHOLOGY_TEACHER".equals(code)) {
                return "I hear that this matters to you, and I would first help you slow the situation down. We can separate it into what happened, what you felt, and what you need next. For now, choose one small step you can control today instead of trying to solve everything at once.";
            }
            if ("SOCRATES".equals(code)) {
                return "If we approach this in a Socratic style, I would first slow down and ask what you mean by \"" + content + "\". Is it a fact, a feeling, or a judgment about how life should be? The answer may begin when the key word becomes clear.";
            }
            if ("NIETZSCHE".equals(code)) {
                return "From a Nietzschean style, I would ask whether this question hides a fear of standing alone. Perhaps the issue is not to remove discomfort, but to see whether it can become material for creating your own values.";
            }
            if ("ZHUANGZI".equals(code)) {
                return "In a Zhuangzi-like style, this question may be a knot made by holding one viewpoint too tightly. Try shifting the angle: what looks like a cage from one side may become an open field from another.";
            }
            return "Following this thinker’s style, I would connect your latest question with what we have already discussed, then ask you to clarify the value behind it before rushing to a final answer.";
        }
        if ("PSYCHOLOGY_TEACHER".equals(code)) {
            return "我能听出来这件事对你有影响。我会先陪你把它放慢一点看：发生了什么、你当时有什么感受、你现在最需要什么。先不用急着解决全部问题，今天先选一个你能控制的小动作去做。";
        }
        if ("SOCRATES".equals(code)) {
            return "如果以苏格拉底式的方式来看，我们不妨先慢下来问：你说的“" + content + "”究竟指什么？它是一个事实、一种感受，还是你对生活应当如何的判断？也许答案要从澄清这个核心词开始。";
        }
        if ("NIETZSCHE".equals(code)) {
            return "从尼采式的思想风格出发，我会追问：这个问题背后是否藏着一种害怕独自承担生命的恐惧？也许重点不是立刻消除不适，而是看它能否成为你重新创造自身价值的材料。";
        }
        if ("ZHUANGZI".equals(code)) {
            return "以庄子的思路看，这个问题像是因为抓住一个角度太紧而打成的结。你不妨换一个方向看：从一边看像牢笼的东西，从另一边看也许只是风经过的空处。";
        }
        if ("CONFUCIUS".equals(code)) {
            return "从孔子的思想风格看，这个问题可以回到修身与关系中来理解。先看自己在这件事中的言行是否合宜，再看怎样既不亏待他人，也不放弃成为更好的自己。";
        }
        return "按照这位思想家的风格，我会把你的最新问题和前面的对话连起来看：先不要急着要结论，而要看清你真正关心的价值是什么。";
    }

    private String latestUserContent(AiChatRequest request) {
        if (request == null || request.getMessages() == null || request.getMessages().isEmpty()) {
            return "这个问题";
        }
        for (int i = request.getMessages().size() - 1; i >= 0; i--) {
            if ("user".equals(request.getMessages().get(i).getRole())) {
                String content = request.getMessages().get(i).getContent();
                if (content == null || content.isBlank()) {
                    return "这个问题";
                }
                return content.length() > 30 ? content.substring(0, 30) : content;
            }
        }
        return "这个问题";
    }

    private String resolveCode(AiChatRequest request) {
        String text = request == null ? "" : String.valueOf(request.getMessages()) + " " + request.getSystemPrompt() + " " + request.getUserPrompt();
        String[] codes = {"PSYCHOLOGY_TEACHER", "SOCRATES", "PLATO", "ARISTOTLE", "KANT", "NIETZSCHE", "SCHOPENHAUER", "CONFUCIUS", "ZHUANGZI"};
        for (String code : codes) {
            if (text.contains(code)) {
                return code;
            }
        }
        return "";
    }

    private boolean isPsychologyTeacher(AiChatRequest request) {
        return "PSYCHOLOGY_TEACHER".equals(resolveCode(request))
                || String.valueOf(request == null ? "" : request.getSystemPrompt()).contains("心理老师")
                || String.valueOf(request == null ? "" : request.getUserPrompt()).contains("COUNSELOR_CARD");
    }
}

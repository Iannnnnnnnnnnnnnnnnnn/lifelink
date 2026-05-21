package com.lifelink.philosophy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifelink.ai.config.AiProperties;
import com.lifelink.ai.dto.AiChatMessage;
import com.lifelink.ai.dto.AiChatRequest;
import com.lifelink.ai.dto.AiChatResult;
import com.lifelink.ai.service.AiChatService;
import com.lifelink.common.BusinessException;
import com.lifelink.philosophy.dto.CreatePhilosophyChatSessionRequest;
import com.lifelink.philosophy.dto.PhilosophyChatMessageResponse;
import com.lifelink.philosophy.dto.PhilosophyChatSessionResponse;
import com.lifelink.philosophy.dto.SendPhilosophyChatMessageRequest;
import com.lifelink.philosophy.dto.SendPhilosophyChatMessageResponse;
import com.lifelink.philosophy.dto.UpdatePhilosophyChatTitleRequest;
import com.lifelink.philosophy.entity.Philosopher;
import com.lifelink.philosophy.entity.PhilosophyChatMessage;
import com.lifelink.philosophy.entity.PhilosophyChatSession;
import com.lifelink.philosophy.mapper.PhilosopherMapper;
import com.lifelink.philosophy.mapper.PhilosophyChatMessageMapper;
import com.lifelink.philosophy.mapper.PhilosophyChatSessionMapper;
import com.lifelink.philosophy.service.PhilosophyChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PhilosophyChatServiceImpl implements PhilosophyChatService {

    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String DELETED_STATUS = "DELETED";
    private static final String USER_ROLE = "USER";
    private static final String ASSISTANT_ROLE = "ASSISTANT";
    private static final String ZH_CN = "zh-CN";
    private static final String EN_US = "en-US";
    private static final String PSYCHOLOGY_TEACHER = "PSYCHOLOGY_TEACHER";

    private final PhilosophyChatSessionMapper sessionMapper;
    private final PhilosophyChatMessageMapper messageMapper;
    private final PhilosopherMapper philosopherMapper;
    private final PhilosopherPersonaPromptBuilder promptBuilder;
    private final AiChatService aiChatService;
    private final AiProperties aiProperties;

    @Override
    @Transactional
    public PhilosophyChatSessionResponse createSession(CreatePhilosophyChatSessionRequest request, Long userId) {
        String language = normalizeLanguage(request.getLanguage());
        Philosopher philosopher = requireActivePhilosopher(request.getPhilosopherCode());
        LocalDateTime now = LocalDateTime.now();

        PhilosophyChatSession session = new PhilosophyChatSession();
        session.setUserId(userId);
        session.setPhilosopherCode(philosopher.getCode());
        session.setPhilosopherName(resolveName(philosopher, language));
        session.setTitle(defaultTitle(philosopher, language));
        session.setLanguage(language);
        session.setStatus(ACTIVE_STATUS);
        session.setLastMessageAt(now);
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
        sessionMapper.insert(session);

        return toSessionResponse(session, new ArrayList<PhilosophyChatMessage>());
    }

    @Override
    @Transactional
    public SendPhilosophyChatMessageResponse sendMessage(Long sessionId, SendPhilosophyChatMessageRequest request, Long userId) {
        PhilosophyChatSession session = requireOwnedActiveSession(sessionId, userId);
        Philosopher philosopher = requireActivePhilosopher(session.getPhilosopherCode());
        String content = normalizeMessageContent(request.getContent());
        List<PhilosophyChatMessage> context = loadRecentMessages(sessionId, contextLimit());
        int previousMessageCount = countMessages(sessionId);

        PhilosophyChatMessage userMessage = newMessage(sessionId, userId, USER_ROLE, content, null);
        messageMapper.insert(userMessage);

        PhilosophyChatMessage assistantMessage;
        if (isCounselor(philosopher) && isCrisisMessage(content)) {
            assistantMessage = newMessage(sessionId, userId, ASSISTANT_ROLE, crisisReply(session.getLanguage()), null);
        } else {
            try {
                AiChatResult result = aiChatService.chat(new AiChatRequest(
                        null,
                        null,
                        null,
                        null,
                        false,
                        buildAiMessages(philosopher, session.getLanguage(), context, content)
                ));
                assistantMessage = newMessage(sessionId, userId, ASSISTANT_ROLE, result.getContent(), result.getRawResponse());
            } catch (Exception ex) {
                assistantMessage = newMessage(sessionId, userId, ASSISTANT_ROLE, fallbackReply(session.getLanguage(), philosopher), null);
            }
        }
        messageMapper.insert(assistantMessage);

        LocalDateTime now = LocalDateTime.now();
        if (previousMessageCount == 0) {
            session.setTitle(summarizeTitle(content, session.getLanguage()));
        }
        session.setLastMessageAt(now);
        session.setUpdatedAt(now);
        sessionMapper.updateById(session);

        return new SendPhilosophyChatMessageResponse(toMessageResponse(userMessage), toMessageResponse(assistantMessage));
    }

    @Override
    public List<PhilosophyChatSessionResponse> listSessions(Long userId, Integer page, Integer size) {
        long current = page == null || page < 1 ? 1L : page.longValue();
        long pageSize = size == null || size < 1 ? 20L : Math.min(size.longValue(), 50L);
        Page<PhilosophyChatSession> result = sessionMapper.selectPage(new Page<PhilosophyChatSession>(current, pageSize),
                new LambdaQueryWrapper<PhilosophyChatSession>()
                        .eq(PhilosophyChatSession::getUserId, userId)
                        .eq(PhilosophyChatSession::getStatus, ACTIVE_STATUS)
                        .orderByDesc(PhilosophyChatSession::getLastMessageAt)
                        .orderByDesc(PhilosophyChatSession::getCreatedAt));
        List<PhilosophyChatSessionResponse> responses = new ArrayList<PhilosophyChatSessionResponse>();
        for (PhilosophyChatSession session : result.getRecords()) {
            PhilosophyChatSessionResponse response = toSessionResponse(session, null);
            response.setLastMessagePreview(resolveLastMessagePreview(session.getId()));
            response.setMessageCount(countMessages(session.getId()));
            responses.add(response);
        }
        return responses;
    }

    @Override
    public PhilosophyChatSessionResponse getSessionDetail(Long sessionId, Long userId) {
        PhilosophyChatSession session = requireOwnedActiveSession(sessionId, userId);
        return toSessionResponse(session, loadMessages(sessionId));
    }

    @Override
    @Transactional
    public void deleteSession(Long sessionId, Long userId) {
        PhilosophyChatSession session = requireOwnedActiveSession(sessionId, userId);
        session.setStatus(DELETED_STATUS);
        session.setUpdatedAt(LocalDateTime.now());
        sessionMapper.updateById(session);

        List<PhilosophyChatMessage> messages = loadMessages(sessionId);
        for (PhilosophyChatMessage message : messages) {
            message.setStatus(DELETED_STATUS);
            messageMapper.updateById(message);
        }
    }

    @Override
    @Transactional
    public PhilosophyChatSessionResponse updateTitle(Long sessionId, UpdatePhilosophyChatTitleRequest request, Long userId) {
        PhilosophyChatSession session = requireOwnedActiveSession(sessionId, userId);
        String title = request.getTitle() == null ? "" : request.getTitle().trim();
        if (!StringUtils.hasText(title)) {
            throw new BusinessException(400, "Title is required");
        }
        if (title.length() > 200) {
            throw new BusinessException(400, "Title cannot exceed 200 characters");
        }
        session.setTitle(title);
        session.setUpdatedAt(LocalDateTime.now());
        sessionMapper.updateById(session);
        return toSessionResponse(session, loadMessages(sessionId));
    }

    private Philosopher requireActivePhilosopher(String philosopherCode) {
        if (!StringUtils.hasText(philosopherCode)) {
            throw new BusinessException(400, "Philosopher code is required");
        }
        Philosopher philosopher = philosopherMapper.selectOne(new LambdaQueryWrapper<Philosopher>()
                .eq(Philosopher::getCode, philosopherCode.trim().toUpperCase(Locale.ROOT))
                .eq(Philosopher::getStatus, ACTIVE_STATUS)
                .last("LIMIT 1"));
        if (philosopher == null) {
            throw new BusinessException(400, "Invalid philosopher selection");
        }
        return philosopher;
    }

    private PhilosophyChatSession requireOwnedActiveSession(Long sessionId, Long userId) {
        PhilosophyChatSession session = sessionMapper.selectById(sessionId);
        if (session == null || !ACTIVE_STATUS.equals(session.getStatus())) {
            throw new BusinessException(404, "Chat session not found");
        }
        if (!session.getUserId().equals(userId)) {
            throw new BusinessException(403, "Forbidden");
        }
        return session;
    }

    private String normalizeMessageContent(String content) {
        String trimmed = content == null ? "" : content.trim();
        if (!StringUtils.hasText(trimmed)) {
            throw new BusinessException(400, "Message content is required");
        }
        if (trimmed.length() > maxMessageLength()) {
            throw new BusinessException(400, "Message content is too long");
        }
        return trimmed;
    }

    private List<AiChatMessage> buildAiMessages(Philosopher philosopher, String language, List<PhilosophyChatMessage> context, String currentContent) {
        List<AiChatMessage> aiContext = new ArrayList<AiChatMessage>();
        for (PhilosophyChatMessage message : context) {
            if (USER_ROLE.equals(message.getRole())) {
                aiContext.add(new AiChatMessage("user", message.getContent()));
            } else if (ASSISTANT_ROLE.equals(message.getRole())) {
                aiContext.add(new AiChatMessage("assistant", message.getContent()));
            }
        }
        return promptBuilder.buildMessages(philosopher, language, aiContext, currentContent);
    }

    private PhilosophyChatMessage newMessage(Long sessionId, Long userId, String role, String content, String rawResponse) {
        PhilosophyChatMessage message = new PhilosophyChatMessage();
        message.setSessionId(sessionId);
        message.setUserId(userId);
        message.setRole(role);
        message.setContent(content);
        message.setRawResponse(rawResponse);
        message.setTokenCount(null);
        message.setStatus(ACTIVE_STATUS);
        message.setCreatedAt(LocalDateTime.now());
        return message;
    }

    private List<PhilosophyChatMessage> loadMessages(Long sessionId) {
        return messageMapper.selectList(new LambdaQueryWrapper<PhilosophyChatMessage>()
                .eq(PhilosophyChatMessage::getSessionId, sessionId)
                .eq(PhilosophyChatMessage::getStatus, ACTIVE_STATUS)
                .orderByAsc(PhilosophyChatMessage::getCreatedAt)
                .orderByAsc(PhilosophyChatMessage::getId));
    }

    private List<PhilosophyChatMessage> loadRecentMessages(Long sessionId, int limit) {
        List<PhilosophyChatMessage> messages = messageMapper.selectList(new LambdaQueryWrapper<PhilosophyChatMessage>()
                .eq(PhilosophyChatMessage::getSessionId, sessionId)
                .eq(PhilosophyChatMessage::getStatus, ACTIVE_STATUS)
                .orderByDesc(PhilosophyChatMessage::getCreatedAt)
                .orderByDesc(PhilosophyChatMessage::getId)
                .last("LIMIT " + limit));
        Collections.reverse(messages);
        return messages;
    }

    private String resolveLastMessagePreview(Long sessionId) {
        List<PhilosophyChatMessage> messages = messageMapper.selectList(new LambdaQueryWrapper<PhilosophyChatMessage>()
                .eq(PhilosophyChatMessage::getSessionId, sessionId)
                .eq(PhilosophyChatMessage::getStatus, ACTIVE_STATUS)
                .orderByDesc(PhilosophyChatMessage::getCreatedAt)
                .orderByDesc(PhilosophyChatMessage::getId)
                .last("LIMIT 1"));
        if (messages.isEmpty()) {
            return "";
        }
        String content = messages.get(0).getContent();
        return content.length() > 80 ? content.substring(0, 80) : content;
    }

    private int countMessages(Long sessionId) {
        Long count = messageMapper.selectCount(new LambdaQueryWrapper<PhilosophyChatMessage>()
                .eq(PhilosophyChatMessage::getSessionId, sessionId)
                .eq(PhilosophyChatMessage::getStatus, ACTIVE_STATUS));
        return count == null ? 0 : count.intValue();
    }

    private PhilosophyChatSessionResponse toSessionResponse(PhilosophyChatSession session, List<PhilosophyChatMessage> messages) {
        List<PhilosophyChatMessageResponse> messageResponses = new ArrayList<PhilosophyChatMessageResponse>();
        if (messages != null) {
            for (PhilosophyChatMessage message : messages) {
                messageResponses.add(toMessageResponse(message));
            }
        }
        return new PhilosophyChatSessionResponse(
                session.getId(),
                session.getPhilosopherCode(),
                session.getPhilosopherName(),
                session.getTitle(),
                session.getLanguage(),
                messages == null ? null : resolveLastMessagePreview(session.getId()),
                session.getLastMessageAt(),
                messages == null ? null : messageResponses.size(),
                messageResponses,
                session.getCreatedAt()
        );
    }

    private PhilosophyChatMessageResponse toMessageResponse(PhilosophyChatMessage message) {
        return new PhilosophyChatMessageResponse(
                message.getId(),
                message.getRole(),
                message.getContent(),
                message.getCreatedAt()
        );
    }

    private String defaultTitle(Philosopher philosopher, String language) {
        String name = resolveName(philosopher, language);
        return ZH_CN.equals(language) ? "与" + name + "的对话" : "Chat with " + name;
    }

    private String summarizeTitle(String content, String language) {
        String compact = content.replaceAll("\\s+", " ");
        int max = ZH_CN.equals(language) ? 20 : 40;
        String summary = compact.length() > max ? compact.substring(0, max) : compact;
        return ZH_CN.equals(language) ? "关于" + summary + "的讨论" : "About " + summary;
    }

    private String fallbackReply(String language, Philosopher philosopher) {
        if (isCounselor(philosopher)) {
            return ZH_CN.equals(language)
                    ? "这次支持性回复暂时生成失败。你可以稍后再试，或者先把最困扰你的部分写成一句话，我们从最小的一步开始。"
                    : "This supportive reply could not be generated right now. You can try again later, or write the hardest part in one sentence and start from one small step.";
        }
        return ZH_CN.equals(language)
                ? "这次模拟对话暂时生成失败，请稍后再试。你的问题已经保存，可以继续追问或重新发送。"
                : "This simulated reply could not be generated right now. Your message has been saved, and you can try again or continue later.";
    }

    private String resolveName(Philosopher philosopher, String language) {
        return ZH_CN.equals(language) ? philosopher.getNameZh() : philosopher.getNameEn();
    }

    private String normalizeLanguage(String language) {
        return EN_US.equals(language) ? EN_US : ZH_CN;
    }

    private int contextLimit() {
        return aiProperties.getChatContextMessages() == null || aiProperties.getChatContextMessages() <= 0
                ? 16
                : Math.min(aiProperties.getChatContextMessages(), 30);
    }

    private int maxMessageLength() {
        return aiProperties.getChatMaxUserMessageLength() == null || aiProperties.getChatMaxUserMessageLength() <= 0
                ? 2000
                : aiProperties.getChatMaxUserMessageLength();
    }

    private boolean isCounselor(Philosopher philosopher) {
        return philosopher != null && (PSYCHOLOGY_TEACHER.equals(philosopher.getCode())
                || "COUNSELOR_CARD".equals(philosopher.getResponseLayout())
                || "COUNSELOR".equals(philosopher.getRoleType()));
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

    private String crisisReply(String language) {
        return ZH_CN.equals(language)
                ? "我很在意你现在的安全。如果你有伤害自己或他人的冲动，请先不要一个人扛着，立刻联系身边可信的人，或者拨打当地紧急电话/危机干预热线。你现在最重要的不是把所有问题想清楚，而是先让自己处在安全的地方。"
                : "I’m really concerned about your safety right now. If you feel at risk of hurting yourself or someone else, please contact a trusted person nearby or call local emergency services or a crisis hotline immediately. The priority is not to solve everything at once, but to keep you safe right now.";
    }
}

package com.lifelink.philosophy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelink.common.BusinessException;
import com.lifelink.philosophy.dto.CreatePhilosophySessionRequest;
import com.lifelink.philosophy.dto.PhilosopherResponse;
import com.lifelink.philosophy.dto.PhilosophyResponseItem;
import com.lifelink.philosophy.dto.PhilosophySessionResponse;
import com.lifelink.philosophy.entity.Philosopher;
import com.lifelink.philosophy.entity.PhilosophyResponse;
import com.lifelink.philosophy.entity.PhilosophySession;
import com.lifelink.philosophy.mapper.PhilosopherMapper;
import com.lifelink.philosophy.mapper.PhilosophyResponseMapper;
import com.lifelink.philosophy.mapper.PhilosophySessionMapper;
import com.lifelink.philosophy.service.PhilosophyAiService;
import com.lifelink.philosophy.service.PhilosophyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PhilosophyServiceImpl implements PhilosophyService {

    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String DELETED_STATUS = "DELETED";
    private static final String ZH_CN = "zh-CN";
    private static final String EN_US = "en-US";

    private final PhilosopherMapper philosopherMapper;
    private final PhilosophySessionMapper sessionMapper;
    private final PhilosophyResponseMapper responseMapper;
    private final PhilosophyAiService aiService;
    private final ObjectMapper objectMapper;

    @Override
    public List<PhilosopherResponse> listPhilosophers(String language) {
        String normalizedLanguage = normalizeLanguage(language);
        List<Philosopher> philosophers = philosopherMapper.selectList(new LambdaQueryWrapper<Philosopher>()
                .eq(Philosopher::getStatus, ACTIVE_STATUS)
                .orderByAsc(Philosopher::getSortOrder)
                .orderByAsc(Philosopher::getId));
        List<PhilosopherResponse> result = new ArrayList<PhilosopherResponse>();
        for (Philosopher philosopher : philosophers) {
            result.add(toPhilosopherResponse(philosopher, normalizedLanguage));
        }
        return result;
    }

    @Override
    @Transactional
    public PhilosophySessionResponse createSession(CreatePhilosophySessionRequest request, Long userId) {
        String question = request.getQuestion() == null ? "" : request.getQuestion().trim();
        if (!StringUtils.hasText(question)) {
            throw new BusinessException(400, "Question is required");
        }
        if (question.length() > 1000) {
            throw new BusinessException(400, "Question must be between 1 and 1000 characters");
        }
        String language = normalizeLanguage(request.getLanguage());
        List<String> codes = normalizeCodes(request.getPhilosopherCodes());
        if (codes.isEmpty()) {
            throw new BusinessException(400, "Please select at least one philosopher");
        }
        if (codes.size() > 8) {
            throw new BusinessException(400, "You can select up to 8 philosophers");
        }

        Map<String, Philosopher> philosopherMap = loadPhilosophers(codes);
        if (philosopherMap.size() != codes.size()) {
            throw new BusinessException(400, "Invalid philosopher selection");
        }

        LocalDateTime now = LocalDateTime.now();
        PhilosophySession session = new PhilosophySession();
        session.setUserId(userId);
        session.setQuestion(question);
        session.setLanguage(language);
        session.setStatus(ACTIVE_STATUS);
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
        sessionMapper.insert(session);

        List<PhilosophyResponseItem> items = new ArrayList<PhilosophyResponseItem>();
        int successCount = 0;
        for (String code : codes) {
            Philosopher philosopher = philosopherMap.get(code);
            PhilosophyResponseItem item;
            try {
                item = aiService.generate(question, philosopher, language);
                successCount++;
            } catch (Exception ex) {
                item = buildFailedItem(philosopher, language);
            }
            saveResponse(session.getId(), item);
            items.add(item);
        }

        if (successCount == 0) {
            throw new BusinessException(502, "AI generation failed");
        }
        return new PhilosophySessionResponse(session.getId(), session.getQuestion(), session.getLanguage(), items, session.getCreatedAt());
    }

    @Override
    public List<PhilosophySessionResponse> listMySessions(Long userId, Integer page, Integer size) {
        long current = page == null || page < 1 ? 1L : page.longValue();
        long pageSize = size == null || size < 1 ? 10L : Math.min(size.longValue(), 50L);
        Page<PhilosophySession> result = sessionMapper.selectPage(new Page<PhilosophySession>(current, pageSize),
                new LambdaQueryWrapper<PhilosophySession>()
                        .eq(PhilosophySession::getUserId, userId)
                        .eq(PhilosophySession::getStatus, ACTIVE_STATUS)
                        .orderByDesc(PhilosophySession::getCreatedAt));
        List<PhilosophySessionResponse> sessions = new ArrayList<PhilosophySessionResponse>();
        for (PhilosophySession session : result.getRecords()) {
            sessions.add(toSessionResponse(session, loadResponses(session.getId())));
        }
        return sessions;
    }

    @Override
    public PhilosophySessionResponse getSessionDetail(Long sessionId, Long userId) {
        PhilosophySession session = requireOwnedActiveSession(sessionId, userId);
        return toSessionResponse(session, loadResponses(session.getId()));
    }

    @Override
    @Transactional
    public void deleteSession(Long sessionId, Long userId) {
        PhilosophySession session = requireOwnedActiveSession(sessionId, userId);
        session.setStatus(DELETED_STATUS);
        session.setUpdatedAt(LocalDateTime.now());
        sessionMapper.updateById(session);
    }

    private Map<String, Philosopher> loadPhilosophers(List<String> codes) {
        List<Philosopher> philosophers = philosopherMapper.selectList(new LambdaQueryWrapper<Philosopher>()
                .in(Philosopher::getCode, codes)
                .eq(Philosopher::getStatus, ACTIVE_STATUS));
        Map<String, Philosopher> map = new LinkedHashMap<String, Philosopher>();
        for (Philosopher philosopher : philosophers) {
            map.put(philosopher.getCode(), philosopher);
        }
        return map;
    }

    private List<String> normalizeCodes(List<String> philosopherCodes) {
        List<String> result = new ArrayList<String>();
        if (philosopherCodes == null) {
            return result;
        }
        for (String code : philosopherCodes) {
            if (!StringUtils.hasText(code)) {
                continue;
            }
            String normalized = code.trim().toUpperCase(Locale.ROOT);
            if (!result.contains(normalized)) {
                result.add(normalized);
            }
        }
        return result;
    }

    private String normalizeLanguage(String language) {
        return EN_US.equals(language) ? EN_US : ZH_CN;
    }

    private PhilosophySession requireOwnedActiveSession(Long sessionId, Long userId) {
        PhilosophySession session = sessionMapper.selectById(sessionId);
        if (session == null || !ACTIVE_STATUS.equals(session.getStatus())) {
            throw new BusinessException(404, "Philosophy dialogue not found");
        }
        if (!session.getUserId().equals(userId)) {
            throw new BusinessException(403, "Forbidden");
        }
        return session;
    }

    private List<PhilosophyResponseItem> loadResponses(Long sessionId) {
        List<PhilosophyResponse> responses = responseMapper.selectList(new LambdaQueryWrapper<PhilosophyResponse>()
                .eq(PhilosophyResponse::getSessionId, sessionId)
                .eq(PhilosophyResponse::getStatus, ACTIVE_STATUS)
                .orderByAsc(PhilosophyResponse::getId));
        List<PhilosophyResponseItem> items = new ArrayList<PhilosophyResponseItem>();
        for (PhilosophyResponse response : responses) {
            items.add(toResponseItem(response));
        }
        return items;
    }

    private void saveResponse(Long sessionId, PhilosophyResponseItem item) {
        PhilosophyResponse response = new PhilosophyResponse();
        response.setSessionId(sessionId);
        response.setPhilosopherCode(item.getPhilosopherCode());
        response.setPhilosopherName(item.getPhilosopherName());
        response.setViewpoint(item.getViewpoint());
        response.setQuestionBack(item.getQuestionBack());
        response.setObjection(item.getObjection());
        response.setSummary(item.getSummary());
        response.setRawResponse(item.getRawResponse());
        response.setStatus(ACTIVE_STATUS);
        response.setCreatedAt(LocalDateTime.now());
        responseMapper.insert(response);
    }

    private PhilosophyResponseItem buildFailedItem(Philosopher philosopher, String language) {
        boolean zh = ZH_CN.equals(language);
        String name = zh ? philosopher.getNameZh() : philosopher.getNameEn();
        return new PhilosophyResponseItem(
                philosopher.getCode(),
                name,
                zh ? "这位思想家的观点暂时生成失败。" : "This thinker's perspective could not be generated.",
                zh ? "是否换一个问题重新尝试？" : "Would you like to try again with a different question?",
                zh ? "生成失败可能来自模型服务暂时不可用，并不代表该思想风格无法回应这个问题。" : "The failure may come from temporary model unavailability, not from the style being unable to address the question.",
                zh ? "请稍后重新生成。" : "Please regenerate later.",
                null
        );
    }

    private PhilosophySessionResponse toSessionResponse(PhilosophySession session, List<PhilosophyResponseItem> responses) {
        return new PhilosophySessionResponse(
                session.getId(),
                session.getQuestion(),
                session.getLanguage(),
                responses,
                session.getCreatedAt()
        );
    }

    private PhilosophyResponseItem toResponseItem(PhilosophyResponse response) {
        return new PhilosophyResponseItem(
                response.getPhilosopherCode(),
                response.getPhilosopherName(),
                response.getViewpoint(),
                response.getQuestionBack(),
                response.getObjection(),
                response.getSummary(),
                response.getRawResponse()
        );
    }

    private PhilosopherResponse toPhilosopherResponse(Philosopher philosopher, String language) {
        boolean zh = ZH_CN.equals(language);
        return new PhilosopherResponse(
                philosopher.getCode(),
                zh ? philosopher.getNameZh() : philosopher.getNameEn(),
                philosopher.getNameZh(),
                philosopher.getNameEn(),
                zh ? philosopher.getEraZh() : philosopher.getEraEn(),
                philosopher.getEraZh(),
                philosopher.getEraEn(),
                zh ? philosopher.getDescriptionZh() : philosopher.getDescriptionEn(),
                philosopher.getDescriptionZh(),
                philosopher.getDescriptionEn(),
                philosopher.getAvatarUrl(),
                parseTags(philosopher.getTags()),
                philosopher.getSortOrder()
        );
    }

    private List<String> parseTags(String tags) {
        if (!StringUtils.hasText(tags)) {
            return new ArrayList<String>();
        }
        try {
            return objectMapper.readValue(tags, new TypeReference<List<String>>() {});
        } catch (Exception ex) {
            return new ArrayList<String>();
        }
    }
}

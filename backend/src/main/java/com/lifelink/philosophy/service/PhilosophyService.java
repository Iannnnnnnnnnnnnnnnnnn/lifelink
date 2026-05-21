package com.lifelink.philosophy.service;

import com.lifelink.philosophy.dto.CreatePhilosophySessionRequest;
import com.lifelink.philosophy.dto.PhilosopherResponse;
import com.lifelink.philosophy.dto.PhilosophySessionResponse;

import java.util.List;

public interface PhilosophyService {

    List<PhilosopherResponse> listPhilosophers(String language);

    PhilosophySessionResponse createSession(CreatePhilosophySessionRequest request, Long userId);

    List<PhilosophySessionResponse> listMySessions(Long userId, Integer page, Integer size);

    PhilosophySessionResponse getSessionDetail(Long sessionId, Long userId);

    void deleteSession(Long sessionId, Long userId);
}

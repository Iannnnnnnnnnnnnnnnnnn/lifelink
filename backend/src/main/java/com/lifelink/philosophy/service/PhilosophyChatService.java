package com.lifelink.philosophy.service;

import com.lifelink.philosophy.dto.CreatePhilosophyChatSessionRequest;
import com.lifelink.philosophy.dto.PhilosophyChatSessionResponse;
import com.lifelink.philosophy.dto.SendPhilosophyChatMessageRequest;
import com.lifelink.philosophy.dto.SendPhilosophyChatMessageResponse;
import com.lifelink.philosophy.dto.UpdatePhilosophyChatTitleRequest;

import java.util.List;

public interface PhilosophyChatService {

    PhilosophyChatSessionResponse createSession(CreatePhilosophyChatSessionRequest request, Long userId);

    SendPhilosophyChatMessageResponse sendMessage(Long sessionId, SendPhilosophyChatMessageRequest request, Long userId);

    List<PhilosophyChatSessionResponse> listSessions(Long userId, Integer page, Integer size);

    PhilosophyChatSessionResponse getSessionDetail(Long sessionId, Long userId);

    void deleteSession(Long sessionId, Long userId);

    PhilosophyChatSessionResponse updateTitle(Long sessionId, UpdatePhilosophyChatTitleRequest request, Long userId);
}

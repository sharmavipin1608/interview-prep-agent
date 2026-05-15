package com.vipinsharma.interviewprep.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vipinsharma.interviewprep.agent.InterviewerAgent;
import com.vipinsharma.interviewprep.dto.ChatResponse;
import com.vipinsharma.interviewprep.dto.CompanyBrief;
import com.vipinsharma.interviewprep.model.Message;
import com.vipinsharma.interviewprep.model.Session;
import com.vipinsharma.interviewprep.repository.MessageRepository;
import com.vipinsharma.interviewprep.repository.SessionRepository;
import com.vipinsharma.interviewprep.repository.WeakAreaRepository;

@Service
public class ChatService {

    private final SessionRepository sessionRepository;
    private final MessageRepository messageRepository;
    private final WeakAreaRepository weakAreaRepository;
    private final InterviewerAgent interviewerAgent;
    private final ObjectMapper objectMapper;

    public ChatService(
            SessionRepository sessionRepository,
            MessageRepository messageRepository,
            WeakAreaRepository weakAreaRepository,
            InterviewerAgent interviewerAgent,
            ObjectMapper objectMapper) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.weakAreaRepository = weakAreaRepository;
        this.interviewerAgent = interviewerAgent;
        this.objectMapper = objectMapper;
    }

    public ChatResponse chat(UUID sessionId, String userMessage) {
        if (sessionId == null) {
            throw new IllegalArgumentException("sessionId must not be null");
        }
        if (!StringUtils.hasText(userMessage)) {
            throw new IllegalArgumentException("userMessage must not be null or blank");
        }

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        CompanyBrief companyBrief = deserializeCompanyBrief(session);

        List<String> weakAreas = weakAreaRepository.findTop5ByOrderByFrequencyDesc()
                .stream()
                .map(wa -> wa.getTopic())
                .toList();

        String reply = interviewerAgent.chat(
                sessionId.toString(),
                companyBrief,
                session.getJobTitle(),
                weakAreas,
                userMessage);

        Message userMsg = new Message();
        userMsg.setSession(session);
        userMsg.setRole("USER");
        userMsg.setContent(userMessage);
        messageRepository.save(userMsg);

        Message assistantMsg = new Message();
        assistantMsg.setSession(session);
        assistantMsg.setRole("ASSISTANT");
        assistantMsg.setContent(reply);
        messageRepository.save(assistantMsg);

        return new ChatResponse(reply);
    }

    private CompanyBrief deserializeCompanyBrief(Session session) {
        try {
            return objectMapper.readValue(session.getCompanyBrief(), CompanyBrief.class);
        } catch (JsonProcessingException e) {
            return new CompanyBrief(session.getCompanyName(), null, null, null, null, null);
        }
    }
}

package com.vipinsharma.interviewprep.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vipinsharma.interviewprep.agent.CoachAgent;
import com.vipinsharma.interviewprep.dto.SessionFeedback;
import com.vipinsharma.interviewprep.model.Score;
import com.vipinsharma.interviewprep.model.Session;
import com.vipinsharma.interviewprep.repository.MessageRepository;
import com.vipinsharma.interviewprep.repository.ScoreRepository;
import com.vipinsharma.interviewprep.repository.SessionRepository;
import com.vipinsharma.interviewprep.repository.WeakAreaRepository;

@Service
public class ScoringService {

    private static final Logger log = LoggerFactory.getLogger(ScoringService.class);

    private final SessionRepository sessionRepository;
    private final MessageRepository messageRepository;
    private final WeakAreaRepository weakAreaRepository;
    private final ScoreRepository scoreRepository;
    private final CoachAgent coachAgent;
    private final ObjectMapper objectMapper;

    public ScoringService(
            SessionRepository sessionRepository,
            MessageRepository messageRepository,
            WeakAreaRepository weakAreaRepository,
            ScoreRepository scoreRepository,
            CoachAgent coachAgent,
            ObjectMapper objectMapper) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.weakAreaRepository = weakAreaRepository;
        this.scoreRepository = scoreRepository;
        this.coachAgent = coachAgent;
        this.objectMapper = objectMapper;
    }

    public SessionFeedback score(UUID sessionId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("sessionId must not be null");
        }

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        String transcript = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId)
                .stream()
                .map(m -> m.getRole() + ": " + m.getContent() + "\n")
                .collect(Collectors.joining());

        List<String> weakAreaTopics = weakAreaRepository.findTop5ByOrderByFrequencyDesc()
                .stream()
                .map(wa -> wa.getTopic())
                .toList();

        SessionFeedback feedback = coachAgent.evaluate(sessionId, transcript, weakAreaTopics);

        String feedbackJson;
        try {
            feedbackJson = objectMapper.writeValueAsString(feedback);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize SessionFeedback for session '{}': {}", sessionId, e.getMessage());
            feedbackJson = "{}";
        }

        Score score = new Score();
        score.setSession(session);
        score.setOverallScore(Math.max(1, Math.min(5, feedback.overallScore())));
        score.setFeedback(feedbackJson);

        scoreRepository.save(score);

        session.setStatus("COMPLETED");
        sessionRepository.save(session);

        return feedback;
    }
}

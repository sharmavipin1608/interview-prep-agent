package com.vipinsharma.interviewprep.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.vipinsharma.interviewprep.dto.SessionSummary;
import com.vipinsharma.interviewprep.repository.ScoreRepository;
import com.vipinsharma.interviewprep.repository.SessionRepository;
import com.vipinsharma.interviewprep.repository.WeakAreaRepository;

@Service
public class SessionHistoryService {

    private final SessionRepository sessionRepository;
    private final ScoreRepository scoreRepository;
    private final WeakAreaRepository weakAreaRepository;

    public SessionHistoryService(
            SessionRepository sessionRepository,
            ScoreRepository scoreRepository,
            WeakAreaRepository weakAreaRepository) {
        this.sessionRepository = sessionRepository;
        this.scoreRepository = scoreRepository;
        this.weakAreaRepository = weakAreaRepository;
    }

    public List<SessionSummary> getSessions() {
        return sessionRepository.findAll().stream()
                .map(session -> {
                    Integer overallScore = scoreRepository.findBySessionId(session.getId())
                            .map(score -> score.getOverallScore())
                            .orElse(null);
                    return new SessionSummary(
                            session.getId(),
                            session.getCompanyName(),
                            session.getJobTitle(),
                            session.getStatus(),
                            overallScore,
                            session.getCreatedAt());
                })
                .toList();
    }

    public List<String> getTopWeakAreas() {
        return weakAreaRepository.findTop5ByOrderByFrequencyDesc().stream()
                .map(wa -> wa.getTopic())
                .toList();
    }
}

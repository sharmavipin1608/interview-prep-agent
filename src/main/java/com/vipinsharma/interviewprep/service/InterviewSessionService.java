package com.vipinsharma.interviewprep.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.vipinsharma.interviewprep.model.Session;
import com.vipinsharma.interviewprep.model.WeakArea;
import com.vipinsharma.interviewprep.repository.SessionRepository;
import com.vipinsharma.interviewprep.repository.WeakAreaRepository;

@Service
public class InterviewSessionService {

    private final SessionRepository sessionRepository;
    private final WeakAreaRepository weakAreaRepository;

    public InterviewSessionService(SessionRepository sessionRepository,
                                   WeakAreaRepository weakAreaRepository) {
        this.sessionRepository = sessionRepository;
        this.weakAreaRepository = weakAreaRepository;
    }

    public UUID startSession(UUID companyBriefSessionId, String jobDescription) {
        if (companyBriefSessionId == null) {
            throw new IllegalArgumentException("companyBriefSessionId must not be null");
        }
        if (!StringUtils.hasText(jobDescription)) {
            throw new IllegalArgumentException("jobDescription must not be null or blank");
        }

        Session session = sessionRepository.findById(companyBriefSessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + companyBriefSessionId));

        session.setStatus("INTERVIEWING");
        sessionRepository.save(session);

        return companyBriefSessionId;
    }

    public List<String> getWeakAreaTopics() {
        return weakAreaRepository.findTop5ByOrderByFrequencyDesc()
                .stream()
                .map(WeakArea::getTopic)
                .toList();
    }
}

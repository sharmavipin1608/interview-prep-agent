package com.vipinsharma.interviewprep.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vipinsharma.interviewprep.model.Score;
import com.vipinsharma.interviewprep.model.Session;
import com.vipinsharma.interviewprep.model.WeakArea;
import com.vipinsharma.interviewprep.repository.ScoreRepository;
import com.vipinsharma.interviewprep.repository.SessionRepository;
import com.vipinsharma.interviewprep.repository.WeakAreaRepository;

class SessionHistoryServiceTest {

    private SessionRepository sessionRepository;
    private ScoreRepository scoreRepository;
    private WeakAreaRepository weakAreaRepository;
    private SessionHistoryService sessionHistoryService;

    @BeforeEach
    void setUp() {
        sessionRepository = mock(SessionRepository.class);
        scoreRepository = mock(ScoreRepository.class);
        weakAreaRepository = mock(WeakAreaRepository.class);

        sessionHistoryService = new SessionHistoryService(
                sessionRepository,
                scoreRepository,
                weakAreaRepository);
    }

    // ── getSessions ──────────────────────────────────────────────────────────

    @Test
    void getSessions_whenNoSessions_returnsEmptyList() {
        when(sessionRepository.findAll()).thenReturn(List.of());

        assertThat(sessionHistoryService.getSessions()).isEmpty();
    }

    @Test
    void getSessions_whenOneSession_returnsOneSessionSummary() {
        Session session = buildSession("Stripe", "SWE", "COMPLETED");
        when(sessionRepository.findAll()).thenReturn(List.of(session));
        when(scoreRepository.findBySessionId(session.getId())).thenReturn(Optional.empty());

        assertThat(sessionHistoryService.getSessions()).hasSize(1);
    }

    @Test
    void getSessions_whenSessionHasScore_summaryIncludesOverallScore() {
        Session session = buildSession("Stripe", "SWE", "COMPLETED");
        Score score = new Score();
        score.setOverallScore(4);
        when(sessionRepository.findAll()).thenReturn(List.of(session));
        when(scoreRepository.findBySessionId(session.getId())).thenReturn(Optional.of(score));

        assertThat(sessionHistoryService.getSessions().get(0).overallScore()).isEqualTo(4);
    }

    @Test
    void getSessions_whenSessionHasNoScore_summaryHasNullOverallScore() {
        Session session = buildSession("Stripe", "SWE", "COMPLETED");
        when(sessionRepository.findAll()).thenReturn(List.of(session));
        when(scoreRepository.findBySessionId(session.getId())).thenReturn(Optional.empty());

        assertThat(sessionHistoryService.getSessions().get(0).overallScore()).isNull();
    }

    @Test
    void getSessions_whenOneSession_summaryCompanyNameMatches() {
        Session session = buildSession("Stripe", "SWE", "COMPLETED");
        when(sessionRepository.findAll()).thenReturn(List.of(session));
        when(scoreRepository.findBySessionId(session.getId())).thenReturn(Optional.empty());

        assertThat(sessionHistoryService.getSessions().get(0).companyName()).isEqualTo("Stripe");
    }

    // ── getTopWeakAreas ───────────────────────────────────────────────────────

    @Test
    void getTopWeakAreas_whenWeakAreasExist_returnsTopics() {
        WeakArea wa1 = new WeakArea();
        wa1.setTopic("Concurrency");
        WeakArea wa2 = new WeakArea();
        wa2.setTopic("System Design");
        when(weakAreaRepository.findTop5ByOrderByFrequencyDesc()).thenReturn(List.of(wa1, wa2));

        assertThat(sessionHistoryService.getTopWeakAreas()).containsExactlyInAnyOrder("Concurrency", "System Design");
    }

    @Test
    void getTopWeakAreas_whenNoWeakAreas_returnsEmptyList() {
        when(weakAreaRepository.findTop5ByOrderByFrequencyDesc()).thenReturn(List.of());

        assertThat(sessionHistoryService.getTopWeakAreas()).isEmpty();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Session buildSession(String companyName, String jobTitle, String status) {
        Session session = new Session();
        session.setId(UUID.randomUUID());
        session.setCompanyName(companyName);
        session.setJobTitle(jobTitle);
        session.setStatus(status);
        return session;
    }
}

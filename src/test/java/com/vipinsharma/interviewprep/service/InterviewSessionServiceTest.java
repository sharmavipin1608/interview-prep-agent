package com.vipinsharma.interviewprep.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.vipinsharma.interviewprep.model.Session;
import com.vipinsharma.interviewprep.model.WeakArea;
import com.vipinsharma.interviewprep.repository.SessionRepository;
import com.vipinsharma.interviewprep.repository.WeakAreaRepository;

class InterviewSessionServiceTest {

    private SessionRepository sessionRepository;
    private WeakAreaRepository weakAreaRepository;
    private InterviewSessionService service;

    private static final UUID SESSION_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @BeforeEach
    void setUp() {
        sessionRepository = mock(SessionRepository.class);
        weakAreaRepository = mock(WeakAreaRepository.class);
        service = new InterviewSessionService(sessionRepository, weakAreaRepository);

        Session mockSession = new Session();
        mockSession.setId(SESSION_ID);
        mockSession.setStatus("ACTIVE");
        when(sessionRepository.findById(any(UUID.class))).thenReturn(Optional.of(mockSession));
        when(sessionRepository.save(any(Session.class))).thenReturn(mockSession);
    }

    // --- startSession tests ---

    @Test
    void startSession_whenValidSessionId_returnsSessionId() {
        UUID result = service.startSession(SESSION_ID, "Build distributed systems");

        assertThat(result).isEqualTo(SESSION_ID);
    }

    @Test
    void startSession_whenValidSessionId_setsStatusToInterviewing() {
        service.startSession(SESSION_ID, "Build distributed systems");

        ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void startSession_whenValidSessionId_savesSession() {
        service.startSession(SESSION_ID, "Build distributed systems");

        verify(sessionRepository).save(any(Session.class));
    }

    @Test
    void startSession_whenSessionIdIsNull_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> service.startSession(null, "Build distributed systems"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void startSession_whenJobDescriptionIsBlank_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> service.startSession(SESSION_ID, "   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void startSession_whenSessionNotFound_throwsIllegalArgumentException() {
        when(sessionRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        UUID missingId = UUID.randomUUID();
        assertThatThrownBy(() -> service.startSession(missingId, "Build APIs"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void startSession_whenSessionNotFound_exceptionMessageContainsSessionId() {
        UUID missingId = UUID.randomUUID();
        when(sessionRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.startSession(missingId, "Build APIs"))
                .hasMessageContaining(missingId.toString());
    }

    // --- getWeakAreaTopics tests ---

    @Test
    void getWeakAreaTopics_whenWeakAreasExist_returnsTopicStrings() {
        WeakArea systemDesign = new WeakArea();
        systemDesign.setTopic("System Design");

        WeakArea concurrency = new WeakArea();
        concurrency.setTopic("Concurrency");

        when(weakAreaRepository.findTop5ByOrderByFrequencyDesc()).thenReturn(List.of(systemDesign, concurrency));

        List<String> result = service.getWeakAreaTopics();

        assertThat(result).containsExactlyInAnyOrder("System Design", "Concurrency");
    }

    @Test
    void getWeakAreaTopics_whenNoWeakAreas_returnsEmptyList() {
        when(weakAreaRepository.findTop5ByOrderByFrequencyDesc()).thenReturn(List.of());

        List<String> result = service.getWeakAreaTopics();

        assertThat(result).isEmpty();
    }
}

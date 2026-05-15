package com.vipinsharma.interviewprep.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vipinsharma.interviewprep.agent.CoachAgent;
import com.vipinsharma.interviewprep.dto.SessionFeedback;
import com.vipinsharma.interviewprep.model.Message;
import com.vipinsharma.interviewprep.model.Score;
import com.vipinsharma.interviewprep.model.Session;
import com.vipinsharma.interviewprep.repository.MessageRepository;
import com.vipinsharma.interviewprep.repository.ScoreRepository;
import com.vipinsharma.interviewprep.repository.SessionRepository;
import com.vipinsharma.interviewprep.repository.WeakAreaRepository;

class ScoringServiceTest {

    private static final UUID SESSION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private SessionRepository sessionRepository;
    private MessageRepository messageRepository;
    private WeakAreaRepository weakAreaRepository;
    private ScoreRepository scoreRepository;
    private CoachAgent coachAgent;
    private ScoringService scoringService;

    @BeforeEach
    void setUp() {
        sessionRepository = mock(SessionRepository.class);
        messageRepository = mock(MessageRepository.class);
        weakAreaRepository = mock(WeakAreaRepository.class);
        scoreRepository = mock(ScoreRepository.class);
        coachAgent = mock(CoachAgent.class);

        scoringService = new ScoringService(
                sessionRepository,
                messageRepository,
                weakAreaRepository,
                scoreRepository,
                coachAgent,
                new ObjectMapper());
    }

    private void stubHappyPath() {
        Session session = new Session();
        session.setId(SESSION_ID);
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

        Message m1 = new Message();
        m1.setRole("USER");
        m1.setContent("Tell me about yourself");

        Message m2 = new Message();
        m2.setRole("ASSISTANT");
        m2.setContent("What is your background?");

        when(messageRepository.findBySessionIdOrderByCreatedAtAsc(SESSION_ID))
                .thenReturn(List.of(m1, m2));

        when(weakAreaRepository.findTop5ByOrderByFrequencyDesc()).thenReturn(List.of());

        when(coachAgent.evaluate(eq(SESSION_ID), anyString(), any()))
                .thenReturn(new SessionFeedback(SESSION_ID, 4, List.of("System Design"),
                        List.of("Practice more"), "Good interview"));

        when(scoreRepository.save(any(Score.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void score_whenValidSessionId_returnsSessionFeedback() {
        stubHappyPath();

        SessionFeedback result = scoringService.score(SESSION_ID);

        assertThat(result.sessionId()).isEqualTo(SESSION_ID);
    }

    @Test
    void score_whenValidSessionId_savesScore() {
        stubHappyPath();

        scoringService.score(SESSION_ID);

        verify(scoreRepository).save(any(Score.class));
    }

    @Test
    void score_whenValidSessionId_savedScoreHasOverallScore() {
        stubHappyPath();

        scoringService.score(SESSION_ID);

        ArgumentCaptor<Score> captor = ArgumentCaptor.forClass(Score.class);
        verify(scoreRepository).save(captor.capture());
        assertThat(captor.getValue().getOverallScore()).isEqualTo(4);
    }

    @Test
    void score_whenValidSessionId_savedScoreHasFeedbackJson() {
        stubHappyPath();

        scoringService.score(SESSION_ID);

        ArgumentCaptor<Score> captor = ArgumentCaptor.forClass(Score.class);
        verify(scoreRepository).save(captor.capture());
        assertThat(captor.getValue().getFeedback()).isNotNull();
    }

    @Test
    void score_whenSessionIdIsNull_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> scoringService.score(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void score_whenSessionNotFound_throwsIllegalArgumentException() {
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scoringService.score(SESSION_ID))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void score_whenNoMessages_transcriptIsEmpty_callsCoachAgent() {
        Session session = new Session();
        session.setId(SESSION_ID);
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
        when(messageRepository.findBySessionIdOrderByCreatedAtAsc(SESSION_ID)).thenReturn(List.of());
        when(weakAreaRepository.findTop5ByOrderByFrequencyDesc()).thenReturn(List.of());
        when(coachAgent.evaluate(eq(SESSION_ID), anyString(), any()))
                .thenReturn(new SessionFeedback(SESSION_ID, 3, List.of(), List.of(), "OK"));
        when(scoreRepository.save(any(Score.class))).thenAnswer(inv -> inv.getArgument(0));

        scoringService.score(SESSION_ID);

        verify(coachAgent).evaluate(eq(SESSION_ID), anyString(), any());
    }

    @Test
    void score_whenMessagesExist_transcriptBuiltCorrectly() {
        Session session = new Session();
        session.setId(SESSION_ID);
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

        Message m1 = new Message();
        m1.setRole("USER");
        m1.setContent("Hi");

        Message m2 = new Message();
        m2.setRole("ASSISTANT");
        m2.setContent("Hello");

        when(messageRepository.findBySessionIdOrderByCreatedAtAsc(SESSION_ID))
                .thenReturn(List.of(m1, m2));
        when(weakAreaRepository.findTop5ByOrderByFrequencyDesc()).thenReturn(List.of());
        when(coachAgent.evaluate(eq(SESSION_ID), anyString(), any()))
                .thenReturn(new SessionFeedback(SESSION_ID, 5, List.of(), List.of(), "Excellent"));
        when(scoreRepository.save(any(Score.class))).thenAnswer(inv -> inv.getArgument(0));

        scoringService.score(SESSION_ID);

        ArgumentCaptor<String> transcriptCaptor = ArgumentCaptor.forClass(String.class);
        verify(coachAgent).evaluate(eq(SESSION_ID), transcriptCaptor.capture(), any());
        assertThat(transcriptCaptor.getValue()).contains("USER: Hi");
    }
}

package com.vipinsharma.interviewprep.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vipinsharma.interviewprep.agent.InterviewerAgent;
import com.vipinsharma.interviewprep.dto.ChatResponse;
import com.vipinsharma.interviewprep.dto.CompanyBrief;
import com.vipinsharma.interviewprep.model.Message;
import com.vipinsharma.interviewprep.model.Session;
import com.vipinsharma.interviewprep.repository.MessageRepository;
import com.vipinsharma.interviewprep.repository.SessionRepository;
import com.vipinsharma.interviewprep.repository.WeakAreaRepository;

class ChatServiceTest {

    private static final UUID SESSION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String AGENT_REPLY = "Tell me about distributed systems.";
    private static final String USER_MESSAGE = "Hello";

    private SessionRepository sessionRepository;
    private MessageRepository messageRepository;
    private WeakAreaRepository weakAreaRepository;
    private InterviewerAgent interviewerAgent;
    private ChatService chatService;

    @BeforeEach
    void setUp() throws Exception {
        sessionRepository = mock(SessionRepository.class);
        messageRepository = mock(MessageRepository.class);
        weakAreaRepository = mock(WeakAreaRepository.class);
        interviewerAgent = mock(InterviewerAgent.class);

        ObjectMapper objectMapper = new ObjectMapper();

        CompanyBrief brief = new CompanyBrief(
                "Stripe",
                "Payment infra",
                List.of("Java"),
                "High ownership",
                "IPO plans",
                List.of("System design"));
        String briefJson = objectMapper.writeValueAsString(brief);

        Session session = new Session();
        session.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        session.setCompanyName("Stripe");
        session.setJobTitle("Software Engineer");
        session.setCompanyBrief(briefJson);
        session.setStatus("INTERVIEWING");

        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
        when(weakAreaRepository.findTop5ByOrderByFrequencyDesc()).thenReturn(List.of());
        when(interviewerAgent.chat(anyString(), any(), anyString(), any(), anyString()))
                .thenReturn(AGENT_REPLY);
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));

        chatService = new ChatService(sessionRepository, messageRepository, weakAreaRepository,
                interviewerAgent, objectMapper);
    }

    @Test
    void chat_whenValidInputs_returnsReplyFromAgent() {
        ChatResponse response = chatService.chat(SESSION_ID, USER_MESSAGE);

        assertThat(response.reply()).isEqualTo(AGENT_REPLY);
    }

    @Test
    void chat_whenValidInputs_savesUserMessage() {
        chatService.chat(SESSION_ID, USER_MESSAGE);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository, times(2)).save(captor.capture());
        Message userMessage = captor.getAllValues().stream()
                .filter(m -> "USER".equals(m.getRole()))
                .findFirst()
                .orElseThrow();

        assertThat(userMessage.getContent()).isEqualTo(USER_MESSAGE);
    }

    @Test
    void chat_whenValidInputs_savesAssistantMessage() {
        chatService.chat(SESSION_ID, USER_MESSAGE);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository, times(2)).save(captor.capture());
        Message assistantMessage = captor.getAllValues().stream()
                .filter(m -> "ASSISTANT".equals(m.getRole()))
                .findFirst()
                .orElseThrow();

        assertThat(assistantMessage.getContent()).isEqualTo(AGENT_REPLY);
    }

    @Test
    void chat_whenValidInputs_callsInterviewerAgent() {
        chatService.chat(SESSION_ID, USER_MESSAGE);

        verify(interviewerAgent).chat(eq(SESSION_ID.toString()), any(), anyString(), any(), eq(USER_MESSAGE));
    }

    @Test
    void chat_whenSessionIdIsNull_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> chatService.chat(null, USER_MESSAGE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void chat_whenUserMessageIsBlank_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> chatService.chat(SESSION_ID, "   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void chat_whenSessionNotFound_throwsIllegalArgumentException() {
        UUID missingId = UUID.randomUUID();
        when(sessionRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.chat(missingId, USER_MESSAGE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void chat_whenCompanyBriefJsonIsInvalid_usesCompanyNameFallback() {
        Session sessionWithBadJson = new Session();
        sessionWithBadJson.setId(SESSION_ID);
        sessionWithBadJson.setCompanyName("Stripe");
        sessionWithBadJson.setJobTitle("Software Engineer");
        sessionWithBadJson.setCompanyBrief("not-valid-json");
        sessionWithBadJson.setStatus("INTERVIEWING");

        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(sessionWithBadJson));

        chatService.chat(SESSION_ID, USER_MESSAGE);

        verify(interviewerAgent, times(1)).chat(anyString(), any(), anyString(), any(), anyString());
    }
}

package com.vipinsharma.interviewprep.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vipinsharma.interviewprep.agent.ResearchAgent;
import com.vipinsharma.interviewprep.dto.CompanyBrief;
import com.vipinsharma.interviewprep.model.Session;
import com.vipinsharma.interviewprep.repository.SessionRepository;

class ResearchServiceTest {

    private ResearchAgent researchAgent;
    private SessionRepository sessionRepository;
    private ObjectMapper objectMapper;
    private ResearchService service;

    private static final CompanyBrief STRIPE_BRIEF = new CompanyBrief(
            "Stripe",
            "Global payment infrastructure company.",
            List.of("Java", "Ruby", "Go"),
            "High ownership culture.",
            "Stripe launched new pricing model.",
            List.of("System design", "Distributed systems"));

    @BeforeEach
    void setUp() {
        researchAgent = mock(ResearchAgent.class);
        sessionRepository = mock(SessionRepository.class);
        objectMapper = new ObjectMapper();
        service = new ResearchService(researchAgent, sessionRepository, objectMapper);
    }

    @Test
    void research_whenValidInputs_returnsCompanyBrief() {
        when(researchAgent.research(anyString(), anyString(), anyString())).thenReturn(STRIPE_BRIEF);

        CompanyBrief result = service.research("Stripe", "Software Engineer", "Build APIs");

        assertThat(result.companyName()).isEqualTo("Stripe");
    }

    @Test
    void research_whenValidInputs_savesSessionToRepository() {
        when(researchAgent.research(anyString(), anyString(), anyString())).thenReturn(STRIPE_BRIEF);

        service.research("Stripe", "Software Engineer", "Build APIs");

        verify(sessionRepository).save(any(Session.class));
    }

    @Test
    void research_whenValidInputs_savedSessionHasCompanyName() {
        when(researchAgent.research(anyString(), anyString(), anyString())).thenReturn(STRIPE_BRIEF);

        service.research("Stripe", "Software Engineer", "Build APIs");

        ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepository).save(captor.capture());
        Session saved = captor.getValue();
        assertThat(saved.getCompanyName()).isEqualTo("Stripe");
    }

    @Test
    void research_whenValidInputs_savedSessionHasJobTitle() {
        when(researchAgent.research(anyString(), anyString(), anyString())).thenReturn(STRIPE_BRIEF);

        service.research("Stripe", "Software Engineer", "Build APIs");

        ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepository).save(captor.capture());
        Session saved = captor.getValue();
        assertThat(saved.getJobTitle()).isEqualTo("Software Engineer");
    }

    @Test
    void research_whenValidInputs_savedSessionHasNonNullCompanyBrief() {
        when(researchAgent.research(anyString(), anyString(), anyString())).thenReturn(STRIPE_BRIEF);

        service.research("Stripe", "Software Engineer", "Build APIs");

        ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepository).save(captor.capture());
        Session saved = captor.getValue();
        assertThat(saved.getCompanyBrief()).isNotNull();
    }

    @Test
    void research_whenCompanyNameIsNull_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> service.research(null, "Software Engineer", "Build APIs"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void research_whenCompanyNameIsBlank_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> service.research("   ", "Software Engineer", "Build APIs"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void research_whenJobTitleIsNull_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> service.research("Stripe", null, "Build APIs"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void research_whenJobDescriptionIsBlank_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> service.research("Stripe", "Software Engineer", "   "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

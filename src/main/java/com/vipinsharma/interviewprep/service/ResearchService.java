package com.vipinsharma.interviewprep.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vipinsharma.interviewprep.agent.ResearchAgent;
import com.vipinsharma.interviewprep.dto.CompanyBrief;
import com.vipinsharma.interviewprep.dto.ResearchResponse;
import com.vipinsharma.interviewprep.model.Session;
import com.vipinsharma.interviewprep.repository.SessionRepository;

@Service
public class ResearchService {

    private static final Logger log = LoggerFactory.getLogger(ResearchService.class);

    private final ResearchAgent researchAgent;
    private final SessionRepository sessionRepository;
    private final ObjectMapper objectMapper;

    public ResearchService(ResearchAgent researchAgent, SessionRepository sessionRepository, ObjectMapper objectMapper) {
        this.researchAgent = researchAgent;
        this.sessionRepository = sessionRepository;
        this.objectMapper = objectMapper;
    }

    public ResearchResponse research(String companyName, String jobTitle, String jobDescription) {
        if (!StringUtils.hasText(companyName)) {
            throw new IllegalArgumentException("companyName must not be null or blank");
        }
        if (!StringUtils.hasText(jobTitle)) {
            throw new IllegalArgumentException("jobTitle must not be null or blank");
        }
        if (!StringUtils.hasText(jobDescription)) {
            throw new IllegalArgumentException("jobDescription must not be null or blank");
        }

        CompanyBrief brief = researchAgent.research(companyName, jobTitle, jobDescription);

        String briefJson;
        try {
            briefJson = objectMapper.writeValueAsString(brief);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize CompanyBrief for company '{}': {}", companyName, e.getMessage());
            briefJson = "{}";
        }

        Session session = new Session();
        session.setCompanyName(companyName);
        session.setJobTitle(jobTitle);
        session.setCompanyBrief(briefJson);

        Session saved = sessionRepository.save(session);

        return new ResearchResponse(saved.getId(), brief);
    }
}

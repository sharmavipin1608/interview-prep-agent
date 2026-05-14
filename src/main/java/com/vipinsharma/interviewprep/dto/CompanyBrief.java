package com.vipinsharma.interviewprep.dto;

import java.util.List;

public record CompanyBrief(
    String companyName,
    String companySummary,
    List<String> techStack,
    String cultureSignals,
    String recentNews,
    List<String> likelyInterviewTopics
) {}

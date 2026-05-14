package com.vipinsharma.interviewprep.dto;

import java.util.UUID;

public record StartSessionRequest(
    UUID companyBriefSessionId,
    String jobDescription
) {}

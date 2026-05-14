package com.vipinsharma.interviewprep.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SessionSummary(
    UUID id,
    String companyName,
    String jobTitle,
    String status,
    Integer overallScore,
    LocalDateTime createdAt
) {}

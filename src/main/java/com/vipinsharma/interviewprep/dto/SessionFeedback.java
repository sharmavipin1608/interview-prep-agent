package com.vipinsharma.interviewprep.dto;

import java.util.List;
import java.util.UUID;

public record SessionFeedback(
    UUID sessionId,
    int overallScore,
    List<String> weakAreas,
    List<String> improvementSuggestions,
    String detailedFeedback
) {}

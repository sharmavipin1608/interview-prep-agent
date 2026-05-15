package com.vipinsharma.interviewprep.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vipinsharma.interviewprep.dto.ApiResponse;
import com.vipinsharma.interviewprep.dto.ChatRequest;
import com.vipinsharma.interviewprep.dto.ChatResponse;
import com.vipinsharma.interviewprep.dto.SessionFeedback;
import com.vipinsharma.interviewprep.dto.SessionSummary;
import com.vipinsharma.interviewprep.dto.StartSessionRequest;
import com.vipinsharma.interviewprep.service.ChatService;
import com.vipinsharma.interviewprep.service.InterviewSessionService;
import com.vipinsharma.interviewprep.service.ScoringService;
import com.vipinsharma.interviewprep.service.SessionHistoryService;

@RestController
@RequestMapping("/api/v1/sessions")
public class SessionController {

    private final InterviewSessionService interviewSessionService;
    private final ChatService chatService;
    private final ScoringService scoringService;
    private final SessionHistoryService sessionHistoryService;

    public SessionController(
            InterviewSessionService interviewSessionService,
            ChatService chatService,
            ScoringService scoringService,
            SessionHistoryService sessionHistoryService) {
        this.interviewSessionService = interviewSessionService;
        this.chatService = chatService;
        this.scoringService = scoringService;
        this.sessionHistoryService = sessionHistoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UUID> startSession(@RequestBody StartSessionRequest request) {
        UUID sessionId = interviewSessionService.startSession(
                request.companyBriefSessionId(), request.jobDescription());
        return ApiResponse.ok(sessionId);
    }

    @PostMapping("/{id}/chat")
    public ApiResponse<ChatResponse> chat(@PathVariable UUID id, @RequestBody ChatRequest request) {
        return ApiResponse.ok(chatService.chat(id, request.message()));
    }

    @PostMapping("/{id}/evaluate")
    public ApiResponse<SessionFeedback> evaluate(@PathVariable UUID id) {
        return ApiResponse.ok(scoringService.score(id));
    }

    @GetMapping("/{id}")
    public ApiResponse<SessionSummary> getSession(@PathVariable UUID id) {
        return sessionHistoryService.getSessions().stream()
                .filter(s -> s.id().equals(id))
                .findFirst()
                .map(ApiResponse::ok)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + id));
    }

    @GetMapping
    public ApiResponse<List<SessionSummary>> getSessions() {
        return ApiResponse.ok(sessionHistoryService.getSessions());
    }
}

package com.vipinsharma.interviewprep.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vipinsharma.interviewprep.dto.ApiResponse;
import com.vipinsharma.interviewprep.dto.ResearchRequest;
import com.vipinsharma.interviewprep.dto.ResearchResponse;
import com.vipinsharma.interviewprep.service.ResearchService;

@RestController
@RequestMapping("/api/v1")
public class ResearchController {

    private final ResearchService researchService;

    public ResearchController(ResearchService researchService) {
        this.researchService = researchService;
    }

    @PostMapping("/research")
    public ResponseEntity<ApiResponse<ResearchResponse>> research(@RequestBody ResearchRequest request) {
        ResearchResponse result = researchService.research(
                request.companyName(), request.jobTitle(), request.jobDescription());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}

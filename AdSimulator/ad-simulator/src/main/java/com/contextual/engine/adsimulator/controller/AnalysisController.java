package com.contextual.engine.adsimulator.controller;

import com.contextual.engine.adsimulator.dto.AnalysisRequest;
import com.contextual.engine.adsimulator.dto.AnalysisResponse;
import com.contextual.engine.adsimulator.service.AnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResponse> analyzeUrl(@RequestBody AnalysisRequest request) {
        return analysisService.analyze(request)
                .map(ResponseEntity::ok) // If response is present, wrap in a 200 OK
                .orElse(ResponseEntity.notFound().build()); // If empty, return a 404 Not Found
    }
}
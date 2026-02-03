package com.contextual.engine.adsimulator.dto;

import lombok.Data;

@Data // Lombok: creates getters, setters, etc.
public class AnalysisRequest {
    // We expect the incoming JSON to have a key named "url"
    private String url;
}
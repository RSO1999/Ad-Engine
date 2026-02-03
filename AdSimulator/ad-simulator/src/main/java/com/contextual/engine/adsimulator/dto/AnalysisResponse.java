package com.contextual.engine.adsimulator.dto;

import com.contextual.engine.adsimulator.model.AdCreative;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResponse {
    private String dominantCategory;
    private List<String> matchedKeywords;
    private AdCreative matchedAd;
}
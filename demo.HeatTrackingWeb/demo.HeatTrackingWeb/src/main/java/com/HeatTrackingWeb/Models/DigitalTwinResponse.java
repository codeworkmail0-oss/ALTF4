package com.HeatTrackingWeb.Models;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DigitalTwinResponse {
    private Map<String, SourceDetail> sourceAnalysis;
    private String topSource;
    private HealthImpact healthImpact;
    private Prediction prediction;
    private GeoContext geoContext;
    private List<String> recommendedAction;
    private String summary;
}

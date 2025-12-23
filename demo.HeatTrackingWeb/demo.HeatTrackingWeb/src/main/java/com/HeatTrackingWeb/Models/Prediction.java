package com.HeatTrackingWeb.Models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Prediction {
    private String next2Hours; // "likely_increase" / "likely_decrease" / "stable"
    private String reason;
    private double percentChangeEstimate;
}

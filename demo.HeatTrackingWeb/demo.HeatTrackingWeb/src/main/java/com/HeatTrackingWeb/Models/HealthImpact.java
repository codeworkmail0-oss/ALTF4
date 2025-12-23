package com.HeatTrackingWeb.Models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HealthImpact {
    private String level;
    private int score;
    private String reason;
}

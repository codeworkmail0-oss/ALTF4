package com.HeatTrackingWeb.Models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TreeImpactResponse {
    private double predicted_temperature;
    private int predicted_aqi;
    private double predicted_pm25;
    private double predicted_pm10;
    private double temp_reduction;
    private double aqi_improvement;
}

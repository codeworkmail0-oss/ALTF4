package com.HeatTrackingWeb.Models;

import lombok.Data;

@Data
public class TreeImpactRequest {
    private String location;
    private double current_temp;
    private double current_aqi;
    private double current_pm25;
    private double current_pm10;
    private int tree_count;
    private String plantation_density;
    private double area_sq_km;
}


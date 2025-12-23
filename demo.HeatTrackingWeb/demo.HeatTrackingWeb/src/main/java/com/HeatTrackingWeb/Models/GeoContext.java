package com.HeatTrackingWeb.Models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GeoContext {
    private boolean nearHighway;
    private boolean nearIndustry;
    private double distanceToNearestMajorRoad_km;
    private String analysis;
}

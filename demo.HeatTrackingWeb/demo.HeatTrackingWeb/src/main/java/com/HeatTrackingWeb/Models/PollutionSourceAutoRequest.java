package com.HeatTrackingWeb.Models;

import lombok.Data;

@Data
public class PollutionSourceAutoRequest {
    private double lat;
    private double lon;
    private String timeOfDay; // morning, afternoon, evening, night
}

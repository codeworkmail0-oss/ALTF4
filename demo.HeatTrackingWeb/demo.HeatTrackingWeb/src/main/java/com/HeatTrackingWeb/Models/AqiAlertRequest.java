package com.HeatTrackingWeb.Models;

import lombok.Data;

@Data
public class AqiAlertRequest {
    private String location;
    private int previous_aqi;
    private int current_aqi;
}

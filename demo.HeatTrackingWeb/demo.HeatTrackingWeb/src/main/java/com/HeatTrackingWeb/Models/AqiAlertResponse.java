package com.HeatTrackingWeb.Models;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AqiAlertResponse {
    private boolean alert;
    private String level_before;
    private String level_now;
    private String message;
    private String severity;
}


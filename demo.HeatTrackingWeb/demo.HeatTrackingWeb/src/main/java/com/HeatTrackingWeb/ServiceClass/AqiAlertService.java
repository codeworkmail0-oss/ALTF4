package com.HeatTrackingWeb.ServiceClass;

import com.HeatTrackingWeb.Models.AqiAlertRequest;
import com.HeatTrackingWeb.Models.AqiAlertResponse;
import org.springframework.stereotype.Service;

@Service
public class AqiAlertService {

    public AqiAlertResponse checkAqi(AqiAlertRequest req) {
        int prev = req.getPrevious_aqi();
        int curr = req.getCurrent_aqi();

        String prevLevel = category(prev);
        String currLevel = category(curr);

        boolean alert = !prevLevel.equals(currLevel);

        String message = alert
                ? "AQI has risen sharply. Recommended to avoid outdoor activities."
                : "AQI stable. No immediate risk.";

        String severity = severityLevel(curr);

        return new AqiAlertResponse(alert, prevLevel, currLevel, message, severity);
    }

    private String category(int aqi) {
        if (aqi <= 50) return "Good";
        if (aqi <= 100) return "Moderate";
        if (aqi <= 150) return "Unhealthy for Sensitive Groups";
        if (aqi <= 200) return "Unhealthy";
        if (aqi <= 300) return "Very Unhealthy";
        return "Hazardous";
    }

    private String severityLevel(int aqi) {
        if (aqi <= 100) return "LOW";
        if (aqi <= 150) return "MEDIUM";
        if (aqi <= 200) return "HIGH";
        return "EXTREME";
    }
}

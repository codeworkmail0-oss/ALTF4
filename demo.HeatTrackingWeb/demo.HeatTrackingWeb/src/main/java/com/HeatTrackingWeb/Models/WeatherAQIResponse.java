package com.HeatTrackingWeb.Models;

import lombok.Data;

@Data
public class WeatherAQIResponse {

    private double temperature;
    private double feelsLike;

    private int aqi;
    private double pm10;
    private double pm25;

    private String time;
}

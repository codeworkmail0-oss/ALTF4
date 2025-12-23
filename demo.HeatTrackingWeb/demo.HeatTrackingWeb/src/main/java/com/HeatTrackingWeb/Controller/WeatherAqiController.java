package com.HeatTrackingWeb.Controller;

import com.HeatTrackingWeb.Models.WeatherAQIResponse;
import com.HeatTrackingWeb.ServiceClass.WeatherAqiService;
import lombok.Data;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class WeatherAqiController {

    private final WeatherAqiService service;

    public WeatherAqiController(WeatherAqiService service) {
        this.service = service;
    }

    // ---------- GET ENDPOINT ----------
    @GetMapping("/weather-aqi")
    public Mono<WeatherAQIResponse> getData(
            @RequestParam double lat,
            @RequestParam double lon) {

        return service.getWeatherAqi(lat, lon);
    }

    // ---------- POST ENDPOINT ----------
    @PostMapping("/weather-aqi")
    public Mono<WeatherAQIResponse> postData(
            @RequestBody LocationRequest request) {

        return service.getWeatherAqi(request.getLat(), request.getLon());
    }

    // DTO for POST request
    @Data
    public static class LocationRequest {
        private double lat;
        private double lon;
    }

    // ---------- SEARCH ENDPOINT ----------
    @GetMapping("/weather-aqi/search")
    public Mono<WeatherAQIResponse> searchCity(@RequestParam String city) {
        return service.getWeatherAqiByCity(city);
    }
}

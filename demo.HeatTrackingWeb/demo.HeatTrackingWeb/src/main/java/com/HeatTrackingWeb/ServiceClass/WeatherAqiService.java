package com.HeatTrackingWeb.ServiceClass;

import com.HeatTrackingWeb.Models.WeatherAQIResponse;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class WeatherAqiService {

    @Value("${openweather.api.key}")
    private String openWeatherKey;

    private final WebClient webClient = WebClient.create();

    public Mono<WeatherAQIResponse> getWeatherAqi(double lat, double lon) {

        Mono<Double> tempMono = fetchTemperature(lat, lon);
        Mono<WeatherAQIResponse> aqiMono = fetchAqi(lat, lon);

        return Mono.zip(tempMono, aqiMono)
                .map(tuple -> {
                    Double temp = tuple.getT1();
                    WeatherAQIResponse aqi = tuple.getT2();

                    aqi.setTemperature(temp);
                    aqi.setFeelsLike(temp); // optional

                    return aqi;
                });
    }

    // ----------------------------------------------------------------------
    // Fetch Temperature from OpenWeather
    // ----------------------------------------------------------------------
    private Mono<Double> fetchTemperature(double lat, double lon) {

        String url = "https://api.openweathermap.org/data/2.5/weather"
                + "?lat=" + lat
                + "&lon=" + lon
                + "&units=metric"
                + "&appid=" + openWeatherKey;

        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class).flatMap(body -> {
                    System.out.println("❌ OpenWeather ERROR = " + body);
                    return Mono.error(new RuntimeException("OpenWeather API Error: " + body));
                }))
                .bodyToMono(JsonNode.class)
                .map(json -> json.get("main").get("temp").asDouble());
    }

    // ----------------------------------------------------------------------
    // Fetch AQI from Open-Meteo
    // ----------------------------------------------------------------------
    private Mono<WeatherAQIResponse> fetchAqi(double lat, double lon) {

        String url = "https://air-quality-api.open-meteo.com/v1/air-quality"
                + "?latitude=" + lat
                + "&longitude=" + lon
                + "&hourly=us_aqi,pm10,pm2_5"
                + "&timezone=auto";

        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class).flatMap(body -> {
                    System.out.println("❌ Open-Meteo AQI ERROR = " + body);
                    return Mono.error(new RuntimeException("Open-Meteo AQI API Error: " + body));
                }))
                .bodyToMono(JsonNode.class)
                .map(json -> {

                    JsonNode hourly = json.get("hourly");

                    int currentHour = ZonedDateTime
                            .now(ZoneId.of("Asia/Kathmandu"))
                            .getHour();

                    WeatherAQIResponse r = new WeatherAQIResponse();
                    r.setAqi(hourly.get("us_aqi").get(currentHour).asInt());
                    r.setPm10(hourly.get("pm10").get(currentHour).asDouble());
                    r.setPm25(hourly.get("pm2_5").get(currentHour).asDouble());
                    r.setTime(hourly.get("time").get(currentHour).asText());

                    return r;
                });
    }

    // ----------------------------------------------------------------------
    // Geocoding: Get Lat/Lon from City Name
    // ----------------------------------------------------------------------
    public Mono<WeatherAQIResponse> getWeatherAqiByCity(String city) {
        String geoUrl = "http://api.openweathermap.org/geo/1.0/direct?q=" + city + "&limit=1&appid=" + openWeatherKey;

        return webClient.get()
                .uri(geoUrl)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .flatMap(json -> {
                    if (json.isEmpty()) {
                        return Mono.error(new RuntimeException("City not found"));
                    }
                    double lat = json.get(0).get("lat").asDouble();
                    double lon = json.get(0).get("lon").asDouble();
                    return getWeatherAqi(lat, lon);
                });
    }
}

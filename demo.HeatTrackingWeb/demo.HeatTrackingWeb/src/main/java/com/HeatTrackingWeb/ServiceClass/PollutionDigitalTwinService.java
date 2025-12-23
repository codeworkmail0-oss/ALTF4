package com.HeatTrackingWeb.ServiceClass;

import com.HeatTrackingWeb.Models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PollutionDigitalTwinService {

    @Autowired
    private WeatherAqiService weatherService;

    public DigitalTwinResponse analyzeAuto(PollutionSourceAutoRequest req) {

        double lat = req.getLat();
        double lon = req.getLon();
        String tod = (req.getTimeOfDay() == null || req.getTimeOfDay().isBlank())
                ? "day" : req.getTimeOfDay().toLowerCase();

        WeatherAQIResponse weather = weatherService.getWeatherAqi(lat, lon).block();

        if (weather == null) {
            Map<String, SourceDetail> fallback = new HashMap<>();
            fallback.put("unknown", SourceDetail.builder()
                    .source("unknown")
                    .confidence(1.0)
                    .why("AQI/weather data unavailable")
                    .build());
            return DigitalTwinResponse.builder()
                    .sourceAnalysis(fallback)
                    .topSource("unknown")
                    .healthImpact(HealthImpact.builder().level("UNKNOWN").score(0).reason("No data").build())
                    .prediction(Prediction.builder().next2Hours("stable").reason("no data").percentChangeEstimate(0).build())
                    .geoContext(GeoContext.builder().nearHighway(false).nearIndustry(false).distanceToNearestMajorRoad_km(Double.MAX_VALUE).analysis("No geo data").build())
                    .recommendedAction(List.of("No data"))
                    .summary("No AQI/weather data available for the requested location.")
                    .build();
        }

        double pm25 = Math.max(0.0, weather.getPm25());
        double pm10 = Math.max(0.0, weather.getPm10());
        double aqi = Math.max(0, weather.getAqi());
        double temp = weather.getTemperature();
        double wind = 3.0; // placeholder; if your weather model returns wind, plug it here

        Map<String, Double> raw = new LinkedHashMap<>();
        raw.put("vehicle_emission", 0.0);
        raw.put("road_dust", 0.0);
        raw.put("waste_burning", 0.0);
        raw.put("industrial_pollution", 0.0);

        double morningBoost = tod.contains("morning") ? 1.0 : 0.0;
        double nightBoost = tod.contains("night") ? 1.0 : 0.0;

        raw.put("vehicle_emission",
                raw.get("vehicle_emission")
                        + (pm25 * 0.7)
                        + (aqi > 120 ? 20 : 0)
                        + (morningBoost * 15)
        );

        raw.put("road_dust",
                raw.get("road_dust")
                        + Math.max(0, (pm10 - pm25)) * 1.2
                        + (temp > 30 ? 5 : 0)
        );

        raw.put("waste_burning",
                raw.get("waste_burning")
                        + (pm25 * 0.9)
                        + (nightBoost * 20)
                        + (pm25 > 120 ? 25 : 0)
        );

        raw.put("industrial_pollution",
                raw.get("industrial_pollution")
                        + (pm25 * 0.6)
                        + (aqi > 180 ? 25 : 0)
                        + (wind < 3 ? 10 : 0)
        );

        // add small randomized jitter so similar inputs still vary a bit
        Random rnd = new Random();
        raw.replaceAll((k, v) -> v + rnd.nextDouble() * 3.0);

        // softmax to probabilities (weighted)
        Map<String, Double> probs = softmax(raw, 1.2);

        Map<String, SourceDetail> sourceAnalysis = new LinkedHashMap<>();
        for (var e : probs.entrySet()) {
            String s = e.getKey();
            double conf = round(e.getValue(), 2);
            String why = generateWhy(s, pm25, pm10, aqi, temp, wind, tod);
            sourceAnalysis.put(s, SourceDetail.builder().source(s).confidence(conf).why(why).build());
        }

        String topSource = sourceAnalysis.entrySet().stream()
                .max(Comparator.comparingDouble(e -> e.getValue().getConfidence()))
                .map(Map.Entry::getKey)
                .orElse("unknown");

        HealthImpact healthImpact = computeHealthImpact(aqi, pm25, pm10);

        Prediction prediction = computeShortTermPrediction(pm25, pm10, aqi, temp, wind, tod, sourceAnalysis);

        GeoContext geoContext = computeGeoContext(lat, lon, topSource);

        List<String> recommendedAction = recommendActions(healthImpact, topSource, prediction);

        String summary = buildSummary(topSource, sourceAnalysis, healthImpact, prediction);

        return DigitalTwinResponse.builder()
                .sourceAnalysis(sourceAnalysis)
                .topSource(topSource)
                .healthImpact(healthImpact)
                .prediction(prediction)
                .geoContext(geoContext)
                .recommendedAction(recommendedAction)
                .summary(summary)
                .build();
    }

    private Map<String, Double> softmax(Map<String, Double> raw, double alpha) {
        double sum = 0.0;
        Map<String, Double> exps = new HashMap<>();
        for (var e : raw.entrySet()) {
            double v = Math.exp(e.getValue() * alpha / Math.max(1.0, averageRaw(raw)));
            exps.put(e.getKey(), v);
            sum += v;
        }
        Map<String, Double> out = new LinkedHashMap<>();
        if (sum == 0) {
            raw.forEach((k,v)-> out.put(k, 1.0 / raw.size()));
            return out;
        }
        for (var e : exps.entrySet()) out.put(e.getKey(), e.getValue() / sum);
        return out;
    }

    private double averageRaw(Map<String, Double> raw) {
        return raw.values().stream().mapToDouble(d -> d).average().orElse(1.0);
    }

    private String generateWhy(String source, double pm25, double pm10, double aqi, double temp, double wind, String tod) {
        if (source.equals("vehicle_emission")) {
            StringBuilder sb = new StringBuilder();
            sb.append("High fine PM (PM2.5) and elevated AQI");
            if (tod.contains("morning")) sb.append(" coinciding with morning traffic");
            if (wind < 3) sb.append(" with low wind allowing accumulation");
            sb.append(".");
            return sb.toString();
        }
        if (source.equals("road_dust")) {
            return "PM10 significantly higher than PM2.5, indicating coarse dust from roads or construction.";
        }
        if (source.equals("waste_burning")) {
            StringBuilder sb = new StringBuilder();
            sb.append("High PM2.5, night-time pattern and spikes consistent with open burning.");
            if (pm25 > 120) sb.append(" Very large PM2.5 spike reinforces burning.");
            return sb.toString();
        }
        if (source.equals("industrial_pollution")) {
            StringBuilder sb = new StringBuilder();
            sb.append("High PM2.5 with elevated AQI and low wind—consistent with industrial emissions stagnating locally.");
            return sb.toString();
        }
        return "Pattern unclear.";
    }

    private HealthImpact computeHealthImpact(double aqi, double pm25, double pm10) {
        double aqiScore = Math.min(100, (aqi / 300.0) * 60.0);
        double pmScore = Math.min(100, (pm25 / 150.0) * 40.0);
        int score = (int) Math.round(Math.min(100, aqiScore + pmScore));
        String level;
        if (score <= 50) level = "Good";
        else if (score <= 100 * 0.6) level = "Moderate";
        else if (score <= 75) level = "Unhealthy for Sensitive Groups";
        else if (score <= 90) level = "Unhealthy";
        else level = "Very Unhealthy / Hazardous";
        String reason = "Composite health score from AQI and PM2.5 exposure.";
        return HealthImpact.builder().level(level).score(score).reason(reason).build();
    }

    private Prediction computeShortTermPrediction(double pm25, double pm10, double aqi, double temp, double wind, String tod, Map<String, SourceDetail> analysis) {
        double change = 0.0;
        String reason = "";
        if (wind < 3) {
            change += 8;
            reason += "Low wind limits dispersion. ";
        }
        if (tod.contains("morning")) {
            change += 6;
            reason += "Morning traffic expected to rise. ";
        }
        if (analysis.getOrDefault("waste_burning", SourceDetail.builder().build()).getConfidence() > 0.4) {
            change += 5;
            reason += "Open burning likely; peaks can persist. ";
        }
        if (aqi > 200) {
            change += 4;
            reason += "Already extreme AQI may continue. ";
        }
        String trend = change > 8 ? "likely_increase" : (change > 3 ? "likely_mild_increase" : "stable");
        return Prediction.builder().next2Hours(trend).reason(reason.isEmpty() ? "No strong drivers detected." : reason.trim()).percentChangeEstimate(round(change,1)).build();
    }

    private GeoContext computeGeoContext(double lat, double lon, String topSource) {
        double majorRoadLat = 27.7172;
        double majorRoadLon = 85.3240;
        double industryLat = 27.7038;
        double industryLon = 85.3297;
        double distRoad = distanceKm(lat, lon, majorRoadLat, majorRoadLon);
        double distIndustry = distanceKm(lat, lon, industryLat, industryLon);
        boolean nearHighway = distRoad < 1.2;
        boolean nearIndustry = distIndustry < 2.0;
        String analysis = (nearHighway ? "Close to major traffic corridor." : "") + (nearIndustry ? " Near industrial area." : "");
        return GeoContext.builder()
                .nearHighway(nearHighway)
                .nearIndustry(nearIndustry)
                .distanceToNearestMajorRoad_km(round(distRoad, 2))
                .analysis(analysis.isBlank() ? "No notable nearby infrastructure detected." : analysis.trim())
                .build();
    }

    private List<String> recommendActions(HealthImpact hi, String topSource, Prediction pred) {
        List<String> out = new ArrayList<>();
        if (hi.getScore() >= 75) out.add("Avoid outdoor activities; distribute water and cooling centers for vulnerable people.");
        else if (hi.getScore() >= 50) out.add("Limit prolonged outdoor exertion; advise masks for sensitive groups.");
        else out.add("Air quality acceptable for general public.");

        if ("vehicle_emission".equals(topSource)) out.add("Avoid heavy traffic zones; use masks near roads.");
        if ("waste_burning".equals(topSource)) out.add("Report open burning and avoid outdoor exposure at night.");
        if ("road_dust".equals(topSource)) out.add("Cover dust sources and water dusty roads; reduce construction dust.");
        if ("industrial_pollution".equals(topSource)) out.add("Investigate industrial sources and enforce emissions controls.");

        if (pred.getNext2Hours().contains("increase")) out.add("Expect worsening in next 2 hours; consider temporary restrictions.");

        return out;
    }

    private String buildSummary(String topSource, Map<String, SourceDetail> sourceAnalysis, HealthImpact hi, Prediction pred) {
        String topWhy = sourceAnalysis.getOrDefault(topSource, SourceDetail.builder().why("No data").build()).getWhy();
        return String.format("%s is the dominant source right now (score %.2f). %s Health impact: %s (%d). Forecast: %s.",
                topSource.replace('_', ' '),
                sourceAnalysis.getOrDefault(topSource, SourceDetail.builder().confidence(0.0).build()).getConfidence(),
                topWhy,
                hi.getLevel(),
                hi.getScore(),
                pred.getNext2Hours()
        );
    }

    private double round(double v, int dec) {
        double m = Math.pow(10, dec);
        return Math.round(v * m) / m;
    }

    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }
}

package com.HeatTrackingWeb.ServiceClass;

import com.HeatTrackingWeb.Models.TreeImpactRequest;
import com.HeatTrackingWeb.Models.TreeImpactResponse;
import org.springframework.stereotype.Service;

@Service
public class TreeImpactService  {

    public TreeImpactResponse predictImpact(TreeImpactRequest req) {

        double treeCount = req.getTree_count();


        String density =
                treeCount > 200 ? "high" :
                        treeCount > 100 ? "medium" :
                                "low";

        double tempReduction = treeCount * 0.04;
        tempReduction = Math.min(tempReduction, 10);

        double aqiImprovement = treeCount * 0.35;
        aqiImprovement = Math.min(aqiImprovement, 80);

        double predictedTemp = req.getCurrent_temp() - tempReduction;
        double predictedAqi = req.getCurrent_aqi() - aqiImprovement;

        predictedTemp = Math.max(predictedTemp, 15);
        predictedAqi = Math.max(predictedAqi, 10);

        double predictedPm25 = req.getCurrent_pm25() - (aqiImprovement * 0.10);
        double predictedPm10 = req.getCurrent_pm10() - (aqiImprovement * 0.08);

        return TreeImpactResponse.builder()
                .predicted_temperature(predictedTemp)
                .predicted_aqi((int) predictedAqi)
                .predicted_pm25(predictedPm25)
                .predicted_pm10(predictedPm10)
                .temp_reduction(tempReduction)
                .aqi_improvement(aqiImprovement)
                .build();
    }
}

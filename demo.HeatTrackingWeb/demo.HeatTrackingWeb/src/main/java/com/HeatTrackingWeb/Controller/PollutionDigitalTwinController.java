package com.HeatTrackingWeb.Controller;


import com.HeatTrackingWeb.Models.DigitalTwinResponse;
import com.HeatTrackingWeb.Models.PollutionSourceAutoRequest;
import com.HeatTrackingWeb.ServiceClass.PollutionDigitalTwinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pollution")
public class PollutionDigitalTwinController {

    @Autowired
    private PollutionDigitalTwinService service;

    @PostMapping("/digital-twin")
    public DigitalTwinResponse analyze(@RequestBody PollutionSourceAutoRequest req) {
        return service.analyzeAuto(req);
    }
}

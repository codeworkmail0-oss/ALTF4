package com.HeatTrackingWeb.Controller;

import com.HeatTrackingWeb.Models.AqiAlertRequest;
import com.HeatTrackingWeb.Models.AqiAlertResponse;
import com.HeatTrackingWeb.ServiceClass.AqiAlertService;
import org.springframework.web.bind.annotation.*;

@RestController
    @RequestMapping("/api/aqi-alert")
@CrossOrigin
public class AqiAlertController {

    private final AqiAlertService service;

    public AqiAlertController(AqiAlertService service) {
        this.service = service;
    }

    @PostMapping
    public AqiAlertResponse alert(@RequestBody AqiAlertRequest req) {
        return service.checkAqi(req);
    }
}

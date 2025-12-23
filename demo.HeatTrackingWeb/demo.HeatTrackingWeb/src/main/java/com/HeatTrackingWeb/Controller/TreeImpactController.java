package com.HeatTrackingWeb.Controller;

import com.HeatTrackingWeb.Models.TreeImpactRequest;
import com.HeatTrackingWeb.Models.TreeImpactResponse;
import com.HeatTrackingWeb.ServiceClass.TreeImpactService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tree-impact")
@CrossOrigin
public class TreeImpactController {

    private final TreeImpactService service;

    public TreeImpactController(TreeImpactService service) {
        this.service = service;
    }

    @PostMapping
    public TreeImpactResponse predict(@RequestBody TreeImpactRequest req) {
        return service.predictImpact(req);
    }
}

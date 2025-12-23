package com.HeatTrackingWeb.Models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SourceScore {
    private String source;
    private double confidence;
}


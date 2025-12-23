package com.HeatTrackingWeb.Models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SourceDetail {
    private String source;
    private double confidence;
    private String why;
}

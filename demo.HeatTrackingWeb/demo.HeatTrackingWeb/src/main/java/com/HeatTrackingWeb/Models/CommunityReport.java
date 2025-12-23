package com.HeatTrackingWeb.Models;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityReport {
    private String id;
    private String type;
    private String description;
    private String location;
    private String contact;
    private String timestamp;
}

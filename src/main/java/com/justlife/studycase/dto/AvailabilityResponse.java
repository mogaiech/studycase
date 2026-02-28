package com.justlife.studycase.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response payload to get availability")
public class AvailabilityResponse {
    @Schema(description = "List of professional")
    private List<Professional> professionals;
}

package com.justlife.studycase.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@Schema(description = "A single professional")
public class Professional {

    @Schema(description = "Professional ID")
    private Long id;

    @Schema(description = "Professional name")
    private String name;

    @Schema(description = "Professional email")
    private String email;

    @Schema(description = "Professional phone")
    private String phone;

    @Schema(description = "Associated vehicle")
    private Vehicle vehicle;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "List of available time slots")
    private List<TimeSlot> availableSlots;
}


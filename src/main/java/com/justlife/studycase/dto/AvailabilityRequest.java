package com.justlife.studycase.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to check availability")
public class AvailabilityRequest {
    @NotNull(message = "Date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "The date to check availability", example = "2026-03-01")
    private LocalDate date;

    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "Appointment start time (required if duration is provided)", example = "10:00")
    private LocalTime startTime;

    @Schema(description = "Appointment duration in hours (2 or 4). Required if startTime is provided.",
            example = "2", allowableValues = {"2", "4"})
    private Integer durationHours;
}

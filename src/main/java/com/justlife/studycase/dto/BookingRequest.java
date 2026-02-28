package com.justlife.studycase.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to create or update a booking")
public class BookingRequest {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @Schema(description = "Appointment start date and time", example = "2026-03-01 10:00")
    private LocalDateTime startDateTime;

    @Schema(description = "Service duration in hours (2 or 4)", example = "2", allowableValues = {"2", "4"})
    private Integer durationHours;

    @Schema(description = "Number of professionals (1, 2, or 3)", example = "2")
    private Integer professionalCount;

    @Schema(example = "john.smith@test.com")
    private String customerEmail;

    @Schema(example = "John Smith")
    private String customerName;
}

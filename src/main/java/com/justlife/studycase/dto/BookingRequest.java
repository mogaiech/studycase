package com.justlife.studycase.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to create a new booking")
public class BookingRequest {

    @NotBlank(message = "customerName is required")
    @Schema(example = "John Smith")
    private String customerName;

    @NotNull(message = "Start date and time is required")
    @Future(message = "Start date and time must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @Schema(description = "Appointment start date and time", example = "2026-03-01 10:00")
    private LocalDateTime startDateTime;

    @NotNull(message = "Duration is required")
    @Schema(description = "Service duration in hours (2 or 4)", example = "2", allowableValues = {"2", "4"})
    private Integer durationHours;

    @NotNull(message = "Professional count is required")
    @Min(value = 1, message = "At least 1 professional required")
    @Max(value = 3, message = "Maximum 3 professionals allowed")
    @Schema(description = "Number of professionals (1, 2, or 3)", example = "2")
    private Integer professionalCount;
}


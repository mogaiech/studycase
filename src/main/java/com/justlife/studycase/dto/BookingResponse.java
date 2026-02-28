package com.justlife.studycase.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Booking details")
public class BookingResponse {
    @Schema(description = "Booking ID")
    private Long id;

    @Schema(description = "Customer name")
    private String customerName;

    @Schema(description = "Customer email")
    private String customerEmail;

    @Schema(description = "Booking status")
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @Schema(description = "Appointment start date and time")
    private LocalDateTime startDateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @Schema(description = "Appointment end date and time")
    private LocalDateTime endDateTime;

    @Schema(description = "Duration hours")
    private int durationHours;

    @Schema(description = "List of professional")
    private List<Professional> professionals;
}


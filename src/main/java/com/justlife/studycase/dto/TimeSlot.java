package com.justlife.studycase.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "An available time slot")
public class TimeSlot {
    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "Slot start time")
    private LocalTime from;

    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "Slot end time")
    private LocalTime to;
}


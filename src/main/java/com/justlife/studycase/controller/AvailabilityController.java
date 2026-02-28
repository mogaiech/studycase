package com.justlife.studycase.controller;

import com.justlife.studycase.dto.AvailabilityRequest;
import com.justlife.studycase.dto.AvailabilityResponse;
import com.justlife.studycase.service.AvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/v1/availability")
@RequiredArgsConstructor
@Tag(name = "Availability", description = "Check professional availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping
    @Operation(
            summary = "Check availability",
            description = """
                    **Date only** (e.g. `?date=2026-03-01`):
                    Returns all professionals with their available time windows for the day.
                    
                    **Date + Time + Duration** (e.g. `?date=2026-03-01&startTime=10:00&durationHours=2`):
                    Returns professionals available for that exact time window.
                    """
    )
    public ResponseEntity<AvailabilityResponse> checkAvailability(
            @Parameter(description = "Selected date (yyyy-MM-dd). Fridays are not available.", required = true, example = "2026-03-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @Parameter(description = "Appointment start time (HH:mm). Required when durationHours is provided.", example = "10:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime startTime,
            @Parameter(description = "Duration in hours (2 or 4). Required when startTime is provided.")
            @RequestParam(required = false)
            Integer durationHours
    ) {
        AvailabilityRequest availabilityRequest = new AvailabilityRequest(date, startTime, durationHours);
        return ResponseEntity.ok(availabilityService.checkAvailability(availabilityRequest));
    }
}

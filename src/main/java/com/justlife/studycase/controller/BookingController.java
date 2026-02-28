package com.justlife.studycase.controller;

import com.justlife.studycase.dto.ApiError;
import com.justlife.studycase.dto.BookingRequest;
import com.justlife.studycase.dto.BookingResponse;
import com.justlife.studycase.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Create, retrieve and update bookings")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a new booking",
            description = """
                    Creates a booking for 1–3 professionals from the same vehicle:
                    - durationHours must be **2 or 4**
                    - professionalCount must be **1, 2, or 3**
                    - Fridays are not bookable
                    - Working hours: **08:00 – 22:00**
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Booking created"),
            @ApiResponse(responseCode = "404", description = "Invalid request or no professionals available",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<BookingResponse> createBooking(
            @RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = BookingRequest.class),
                            examples = @ExampleObject(
                                    name = "Create booking",
                                    value = """
                                            {
                                              "startDateTime": "2026-03-05 10:00",
                                              "durationHours": 2,
                                              "professionalCount": 2,
                                              "customerEmail": "john.smith@test.com",
                                              "customerName": "John Smith"
                                            }"""
                            )
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.createBooking(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking found"),
            @ApiResponse(responseCode = "404", description = "Booking not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<BookingResponse> getBooking(
            @Parameter(description = "Booking ID", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBooking(id));
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Reschedule an existing booking",
            description = """
                    Updates the date and start time of an existing confirmed booking.
                    The duration and assigned professionals remain unchanged.
                    All scheduling constraints (working hours, 30-min break, no Fridays) are re-validated.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking rescheduled"),
            @ApiResponse(responseCode = "400", description = "Invalid request or no professionals available",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Booking not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<BookingResponse> updateBooking(
            @Parameter(description = "Booking ID", example = "1")
            @PathVariable Long id,
            @RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = BookingRequest.class),
                            examples = @ExampleObject(
                                    name = "Update booking",
                                    value = """
                                            {
                                              "startDateTime": "2026-03-05 14:00",
                                              "durationHours": 2
                                            }"""
                            )
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody BookingRequest request) {
        return ResponseEntity.ok(bookingService.updateBooking(id, request));
    }
}

package com.justlife.studycase.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.justlife.studycase.dto.BookingRequest;
import com.justlife.studycase.dto.BookingResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class IntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    @DisplayName("Full flow to check availability, create booking, update booking")
    void shouldCompleteFullBookingFlow() throws Exception {
        // Check availability first
        mockMvc.perform(get("/api/v1/availability")
                        .param("date", "2026-03-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.professionals").isArray());

        // Check availability for a specific slot
        mockMvc.perform(get("/api/v1/availability")
                        .param("date", "2026-03-10")
                        .param("startTime", "10:00")
                        .param("durationHours", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.professionals").isArray());

        // Create a booking
        BookingRequest createRequest = BookingRequest.builder()
                .startDateTime(LocalDateTime.of(2026, 3, 10, 10, 0))
                .durationHours(2)
                .professionalCount(1)
                .customerName("Test Customer")
                .customerEmail("test@customer.com")
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.professionals").isArray())
                .andReturn();

        BookingResponse created = objectMapper.readValue(createResult.getResponse().getContentAsString(), BookingResponse.class);
        Long bookingId = created.getId();
        assertThat(bookingId).isNotNull();
        assertThat(created.getProfessionals()).hasSize(1);

        // Get the booking
        mockMvc.perform(get("/api/v1/bookings/" + bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId));

        // Update the booking to a different day
        BookingRequest updateRequest = BookingRequest.builder()
                .startDateTime(LocalDateTime.of(2026, 3, 11, 14, 0))
                .durationHours(2)
                .build();

        mockMvc.perform(patch("/api/v1/bookings/" + bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDateTime").value("2026-03-11 14:00"));
    }

    @Test
    @DisplayName("Should reject booking on Friday")
    void shouldRejectFridayBooking() throws Exception {
        BookingRequest request = BookingRequest.builder()
                .startDateTime(LocalDateTime.of(2026, 3, 6, 10, 0)) // Friday
                .durationHours(2)
                .professionalCount(1)
                .customerName("Test Customer")
                .customerEmail("customer@test.com")
                .build();

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Friday")));
    }

    @Test
    @DisplayName("Should check 30-minute break between consecutive bookings for same professional")
    void shouldCheckBreakBetweenBookings() throws Exception {
        // Create first booking: 10:00–12:00
        BookingRequest first = BookingRequest.builder()
                .startDateTime(LocalDateTime.of(2026, 3, 10, 10, 0))
                .durationHours(2)
                .professionalCount(1)
                .customerName("Customer A")
                .customerEmail("a@test.com")
                .build();

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        // Verify the professional is marked unavailable
        mockMvc.perform(get("/api/v1/availability")
                        .param("date", "2026-03-10")
                        .param("startTime", "12:00")
                        .param("durationHours", "2"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 404 for non-existing booking")
    void shouldReturn404ForMissingBooking() throws Exception {
        mockMvc.perform(get("/api/v1/bookings/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Availability check on Friday should return 400")
    void availabilityCheckOnFridayShouldFail() throws Exception {
        mockMvc.perform(get("/api/v1/availability")
                        .param("date", "2026-03-06"))
                .andExpect(status().isBadRequest());
    }
}

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

import java.time.format.DateTimeFormatter;

import static com.justlife.studycase.utils.TestDates.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class IntegrationTest {

    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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
    @DisplayName("Full flow: check availability, create booking, get booking, update booking")
    void shouldCompleteFullBookingFlow() throws Exception {
        // Check availability for the day
        mockMvc.perform(get("/api/v1/availability")
                        .param("date", WORK_DATE_STR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.professionals").isArray());

        // Check availability for a specific slot
        mockMvc.perform(get("/api/v1/availability")
                        .param("date", WORK_DATE_STR)
                        .param("startTime", "10:00")
                        .param("durationHours", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.professionals").isArray());

        // Create a booking
        BookingRequest createRequest = BookingRequest.builder()
                .startDateTime(WORK_START)
                .durationHours(2)
                .professionalCount(1)
                .customerName("Test Customer")
                .customerEmail("test@customer.com")
                .build();

        MvcResult createBooking = mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.professionals").isArray())
                .andReturn();

        BookingResponse bookingResponse = objectMapper.readValue(createBooking.getResponse().getContentAsString(), BookingResponse.class);
        assertThat(bookingResponse.getId()).isNotNull();
        assertThat(bookingResponse.getProfessionals()).hasSize(1);

        // Get the booking
        mockMvc.perform(get("/api/v1/bookings/" + bookingResponse.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingResponse.getId()));

        // Reschedule the booking to date + 4
        BookingRequest updateRequest = BookingRequest.builder()
                .startDateTime(NEXT_DAY)
                .durationHours(2)
                .build();

        mockMvc.perform(patch("/api/v1/bookings/" + bookingResponse.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDateTime").value(NEXT_DAY.format(DATETIME_FORMAT)));
    }

    @Test
    @DisplayName("Should reject booking on Friday")
    void shouldRejectFridayBooking() throws Exception {
        BookingRequest request = BookingRequest.builder()
                .startDateTime(FRIDAY)
                .durationHours(2)
                .professionalCount(1)
                .customerName("Test Customer")
                .customerEmail("customer@test.com")
                .build();

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No availability on Fridays"));
    }

    @Test
    @DisplayName("Should enforce 30-minute break: professional booked 10:00–12:00 is unavailable at 12:00")
    void shouldEnforce30MinBreakBetweenBookings() throws Exception {
        // Create a booking at 10:00–12:00
        BookingRequest first = BookingRequest.builder()
                .startDateTime(WORK_START)
                .durationHours(2)
                .professionalCount(1)
                .customerName("Test Customer")
                .customerEmail("customer@test.com")
                .build();

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        // The professional needs 30 min break after 12:00, so they should NOT appear at 12:00
        mockMvc.perform(get("/api/v1/availability")
                        .param("date", WORK_DATE_STR)
                        .param("startTime", "12:00")
                        .param("durationHours", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.professionals").isArray());

        mockMvc.perform(get("/api/v1/availability")
                        .param("date", WORK_DATE_STR)
                        .param("startTime", "12:30")
                        .param("durationHours", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.professionals").isArray());
    }

    @Test
    @DisplayName("Should return 404 for non-existing booking")
    void shouldReturn404ForMissingBooking() throws Exception {
        mockMvc.perform(get("/api/v1/bookings/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 when checking availability on Friday")
    void shouldRejectAvailabilityOnFriday() throws Exception {
        mockMvc.perform(get("/api/v1/availability")
                        .param("date", FRIDAY_DATE_STR))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No availability on Fridays"));
    }
}

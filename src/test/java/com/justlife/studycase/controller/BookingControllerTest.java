package com.justlife.studycase.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.justlife.studycase.dto.BookingRequest;
import com.justlife.studycase.dto.BookingResponse;
import com.justlife.studycase.dto.Professional;
import com.justlife.studycase.exception.BusinessException;
import com.justlife.studycase.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.justlife.studycase.utils.TestDates.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private BookingResponse buildResponse(Long id) {
        return BookingResponse.builder()
                .id(id)
                .startDateTime(WORK_START)
                .endDateTime(WORK_START.plusHours(2))
                .durationHours(2)
                .customerName("John Doe")
                .customerEmail("john@test.com")
                .status("CONFIRMED")
                .professionals(List.of(
                        Professional.builder()
                                .id(1L)
                                .name("Ahmed")
                                .build()))
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/bookings")
    class CreateBookingTests {

        @Test
        @DisplayName("Should return 201 for valid booking request")
        void shouldReturn201ForValidRequest() throws Exception {
            BookingRequest request = new BookingRequest(WORK_START, 2, 1, "john@test.com", "John Doe");

            when(bookingService.createBooking(any())).thenReturn(buildResponse(1L));

            mockMvc.perform(post("/api/v1/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.status").value("CONFIRMED"))
                    .andExpect(jsonPath("$.professionals").isArray());
        }

        @Test
        @DisplayName("Should return 400 when service throws BusinessException for too many professionals")
        void shouldReturn400ForTooManyProfessionals() throws Exception {
            BookingRequest request = new BookingRequest(WORK_START, 2, 5, "john@test.com", "John Doe");

            when(bookingService.createBooking(any()))
                    .thenThrow(new BusinessException("Professional count must be 1, 2, or 3"));

            mockMvc.perform(post("/api/v1/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Professional count must be 1, 2, or 3"));
        }

        @Test
        @DisplayName("Should return 400 when service throws BusinessException for missing customerName")
        void shouldReturn400WhenCustomerNameMissing() throws Exception {
            BookingRequest request = new BookingRequest(WORK_START, 2, 1, "john@test.com", null);

            when(bookingService.createBooking(any()))
                    .thenThrow(new BusinessException("customerName is required"));

            mockMvc.perform(post("/api/v1/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("customerName is required"));
        }

        @Test
        @DisplayName("Should return 400 when service throws BusinessException (Friday)")
        void shouldReturn400OnBusinessException() throws Exception {
            BookingRequest request = new BookingRequest(FRIDAY, 2, 1, null, "John Doe");

            when(bookingService.createBooking(any()))
                    .thenThrow(new BusinessException("No availability on Fridays"));

            mockMvc.perform(post("/api/v1/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("No availability on Fridays"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/bookings/{id}")
    class GetBookingTests {

        @Test
        @DisplayName("Should return 200 for existing booking")
        void shouldReturn200ForExistingBooking() throws Exception {
            when(bookingService.getBooking(2L)).thenReturn(buildResponse(2L));

            mockMvc.perform(get("/api/v1/bookings/2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(2))
                    .andExpect(jsonPath("$.customerName").value("John Doe"));
        }

        @Test
        @DisplayName("Should return 404 for non-existing booking")
        void shouldReturn404ForNonExistingBooking() throws Exception {
            when(bookingService.getBooking(999L))
                    .thenThrow(new BusinessException("Booking not found with id: 999", HttpStatus.NOT_FOUND));

            mockMvc.perform(get("/api/v1/bookings/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/bookings/{id}")
    class UpdateBookingTests {

        @Test
        @DisplayName("Should return 200 for valid update")
        void shouldReturn200ForValidUpdate() throws Exception {
            BookingRequest updateRequest = new BookingRequest(NEXT_DAY, 2, null, null, null);

            when(bookingService.updateBooking(eq(1L), any())).thenReturn(buildResponse(1L));

            mockMvc.perform(patch("/api/v1/bookings/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @DisplayName("Should return 400 when service throws BusinessException for missing startDateTime")
        void shouldReturn400WhenStartDateTimeMissing() throws Exception {
            BookingRequest updateRequest = new BookingRequest(null, null, null, null, null);

            when(bookingService.updateBooking(eq(1L), any()))
                    .thenThrow(new BusinessException("startDateTime is required"));

            mockMvc.perform(patch("/api/v1/bookings/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("startDateTime is required"));
        }

        @Test
        @DisplayName("Should return 404 for non-existing booking update")
        void shouldReturn404ForNonExistingBookingUpdate() throws Exception {
            BookingRequest updateRequest = new BookingRequest(NEXT_DAY, 2, null, null, null);

            when(bookingService.updateBooking(eq(999L), any()))
                    .thenThrow(new BusinessException("Booking not found with id: 999", HttpStatus.NOT_FOUND));

            mockMvc.perform(patch("/api/v1/bookings/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound());
        }
    }
}

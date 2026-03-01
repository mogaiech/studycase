package com.justlife.studycase.controller;

import com.justlife.studycase.dto.AvailabilityResponse;
import com.justlife.studycase.dto.Professional;
import com.justlife.studycase.dto.TimeSlot;
import com.justlife.studycase.dto.Vehicle;
import com.justlife.studycase.exception.BusinessException;
import com.justlife.studycase.service.AvailabilityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.List;

import static com.justlife.studycase.utils.TestDates.FRIDAY_DATE_STR;
import static com.justlife.studycase.utils.TestDates.WORK_DATE_STR;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AvailabilityController.class)
public class AvailabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AvailabilityService availabilityService;

    @Nested
    @DisplayName("GET /api/v1/availability")
    class AvailabilityEndpointTests {

        @Test
        @DisplayName("Should return 200 with availability for date only")
        void shouldReturn200ForDateOnly() throws Exception {
            AvailabilityResponse response = AvailabilityResponse.builder().professionals(List.of(Professional.builder().id(1L).name("Ahmed Al-Rashid").email("ahmed@justlife.com").phone("+971500000001").vehicle(Vehicle.builder().id(1L).brandName("Nissan").plateNumber("JL-001-A").build()).availableSlots(List.of(TimeSlot.builder().from(LocalTime.of(8, 0)).to(LocalTime.of(10, 0)).build())).build())).build();

            when(availabilityService.checkAvailability(any())).thenReturn(response);

            mockMvc.perform(get("/api/v1/availability").param("date", WORK_DATE_STR)).andExpect(status().isOk()).andExpect(jsonPath("$.professionals").isArray()).andExpect(jsonPath("$.professionals[0].name").value("Ahmed Al-Rashid")).andExpect(jsonPath("$.professionals[0].availableSlots").isArray()).andExpect(jsonPath("$.professionals[0].availableSlots[0].from").value("08:00")).andExpect(jsonPath("$.professionals[0].availableSlots[0].to").value("10:00"));
        }

        @Test
        @DisplayName("Should return 200 for date + startTime + duration")
        void shouldReturn200ForFullFilter() throws Exception {
            when(availabilityService.checkAvailability(any())).thenReturn(AvailabilityResponse.builder().professionals(List.of()).build());

            mockMvc.perform(get("/api/v1/availability").param("date", WORK_DATE_STR).param("startTime", "10:00").param("durationHours", "2")).andExpect(status().isOk()).andExpect(jsonPath("$.professionals").isArray()).andExpect(jsonPath("$.professionals").isEmpty());
        }

        @Test
        @DisplayName("Should return 400 when date is missing")
        void shouldReturn400WhenDateMissing() throws Exception {
            mockMvc.perform(get("/api/v1/availability")).andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 on Friday")
        void shouldReturn400OnFriday() throws Exception {
            when(availabilityService.checkAvailability(any())).thenThrow(new BusinessException("No availability on Fridays"));

            mockMvc.perform(get("/api/v1/availability").param("date", FRIDAY_DATE_STR))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("No availability on Fridays"));
        }

        @Test
        @DisplayName("Should return empty professionals list when none available")
        void shouldReturnEmptyListWhenNoneAvailable() throws Exception {
            when(availabilityService.checkAvailability(any())).thenReturn(AvailabilityResponse.builder().professionals(List.of()).build());

            mockMvc.perform(get("/api/v1/availability").param("date", WORK_DATE_STR)).andExpect(status().isOk()).andExpect(jsonPath("$.professionals").isArray()).andExpect(jsonPath("$.professionals").isEmpty());
        }
    }
}

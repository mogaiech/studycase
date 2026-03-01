package com.justlife.studycase.service;

import com.justlife.studycase.dto.AvailabilityRequest;
import com.justlife.studycase.dto.AvailabilityResponse;
import com.justlife.studycase.dto.TimeSlot;
import com.justlife.studycase.entity.BookingEntity;
import com.justlife.studycase.entity.BookingStatus;
import com.justlife.studycase.entity.ProfessionalEntity;
import com.justlife.studycase.entity.VehicleEntity;
import com.justlife.studycase.exception.BusinessException;
import com.justlife.studycase.repository.BookingRepository;
import com.justlife.studycase.repository.ProfessionalRepository;
import com.justlife.studycase.utils.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static com.justlife.studycase.utils.TestDates.FRIDAY_DATE;
import static com.justlife.studycase.utils.TestDates.WORK_DATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private ProfessionalRepository professionalRepository;

    @Mock
    private BookingRepository bookingRepository;

    private AvailabilityService availabilityService;

    private ProfessionalEntity professionalEntity;

    @BeforeEach
    void setUp() {
        Validator validator = new Validator();
        availabilityService = new AvailabilityService(
                professionalRepository, bookingRepository, validator);

        VehicleEntity vehicleEntity = VehicleEntity.builder()
                .id(1L)
                .brandName("Nissan")
                .plateNumber("JL-001-A")
                .build();

        professionalEntity = ProfessionalEntity.builder()
                .id(1L)
                .name("Ahmed Al-Rashid")
                .email("ahmed@justlife.com")
                .phone("+971500000001")
                .vehicle(vehicleEntity)
                .build();
    }

    @Nested
    @DisplayName("Date-only availability checks")
    class DateOnlyAvailability {

        @Test
        @DisplayName("Should throw exception for Friday")
        void shouldRejectFriday() {
            AvailabilityRequest request = new AvailabilityRequest(FRIDAY_DATE, null, null);

            assertThatThrownBy(() -> availabilityService.checkAvailability(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Friday");
        }

        @Test
        @DisplayName("Should return professionals with slots when no bookings exist")
        void shouldReturnAvailableSlotsForFreeDay() {
            AvailabilityRequest request = new AvailabilityRequest(WORK_DATE, null, null);

            when(professionalRepository.findAll()).thenReturn(List.of(professionalEntity));
            when(bookingRepository.findBookingsForProfessionalOnDate(
                    eq(1L), any(), any())).thenReturn(List.of());

            AvailabilityResponse result = availabilityService.checkAvailability(request);

            assertThat(result.getProfessionals()).hasSize(1);
            assertThat(result.getProfessionals().get(0).getName()).isEqualTo("Ahmed Al-Rashid");
            assertThat(result.getProfessionals().get(0).getAvailableSlots()).isNotEmpty();
        }

        @Test
        @DisplayName("Should show no slots for a professional fully booked")
        void shouldReturnNoSlotsForFullyBookedProfessional() {
            AvailabilityRequest request = new AvailabilityRequest(WORK_DATE, null, null);

            // Simulate a booking from 08:00 to 22:00 (full day blocked)
            BookingEntity fullDayBooking = BookingEntity.builder()
                    .id(1L)
                    .startDateTime(LocalDateTime.of(WORK_DATE, LocalTime.of(8, 0)))
                    .endDateTime(LocalDateTime.of(WORK_DATE, LocalTime.of(22, 0)))
                    .durationHours(4)
                    .status(BookingStatus.CONFIRMED)
                    .build();

            when(professionalRepository.findAll()).thenReturn(List.of(professionalEntity));
            when(bookingRepository.findBookingsForProfessionalOnDate(
                    eq(1L), any(), any())).thenReturn(List.of(fullDayBooking));

            AvailabilityResponse result = availabilityService.checkAvailability(request);

            // Professional should be filtered out (no available slots)
            assertThat(result.getProfessionals()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Time-slot-specific availability checks")
    class TimeSlotAvailability {

        @Test
        @DisplayName("Should return available professionals for a valid time slot")
        void shouldReturnAvailableProfessionalsForSlot() {
            LocalTime startTime = LocalTime.of(10, 0);
            AvailabilityRequest request = new AvailabilityRequest(WORK_DATE, startTime, 2);

            when(professionalRepository.findAvailableProfessionals(any(), any()))
                    .thenReturn(List.of(professionalEntity));

            AvailabilityResponse result = availabilityService.checkAvailability(request);

            assertThat(result.getProfessionals()).hasSize(1);
            assertThat(result.getProfessionals().get(0).getName()).isEqualTo("Ahmed Al-Rashid");
        }

        @Test
        @DisplayName("Should reject start before 08:00")
        void shouldRejectStartBeforeWorkHours() {
            AvailabilityRequest request = new AvailabilityRequest(WORK_DATE, LocalTime.of(7, 0), 2);

            assertThatThrownBy(() -> availabilityService.checkAvailability(request))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("Should reject appointment ending after 22:00")
        void shouldRejectEndAfterWorkHours() {
            AvailabilityRequest request = new AvailabilityRequest(WORK_DATE, LocalTime.of(21, 0), 2);

            assertThatThrownBy(() -> availabilityService.checkAvailability(request))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("Should return empty list when no professionals available")
        void shouldReturnEmptyWhenNoneAvailable() {
            AvailabilityRequest request = new AvailabilityRequest(WORK_DATE, LocalTime.of(10, 0), 2);

            when(professionalRepository.findAvailableProfessionals(any(), any()))
                    .thenReturn(List.of());

            AvailabilityResponse result = availabilityService.checkAvailability(request);
            assertThat(result.getProfessionals()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Available slot computation")
    class AvailableSlotComputation {

        @Test
        @DisplayName("Professional with no bookings should have a full-day free slot")
        void shouldComputeFullDaySlots() {
            AvailabilityRequest request = new AvailabilityRequest(WORK_DATE, null, null);

            when(professionalRepository.findAll()).thenReturn(List.of(professionalEntity));
            when(bookingRepository.findBookingsForProfessionalOnDate(eq(1L), any(), any()))
                    .thenReturn(List.of());

            AvailabilityResponse result = availabilityService.checkAvailability(request);
            List<TimeSlot> slots = result.getProfessionals().get(0).getAvailableSlots();

            assertThat(slots).isNotEmpty();
            // All slots must start at or after 08:00
            assertThat(slots).allMatch(s -> !s.getFrom().isBefore(LocalTime.of(8, 0)));
            // All slots must end at or before 22:00
            assertThat(slots).allMatch(s -> !s.getTo().isAfter(LocalTime.of(22, 0)));
        }

        @Test
        @DisplayName("Slots around an existing booking should respect 30-min break")
        void shouldRespectBreakBetweenAppointments() {
            AvailabilityRequest request = new AvailabilityRequest(WORK_DATE, null, null);

            // Existing booking: 10:00–12:00
            BookingEntity existing = BookingEntity.builder()
                    .id(1L)
                    .startDateTime(LocalDateTime.of(WORK_DATE, LocalTime.of(10, 0)))
                    .endDateTime(LocalDateTime.of(WORK_DATE, LocalTime.of(12, 0)))
                    .durationHours(2)
                    .status(BookingStatus.CONFIRMED)
                    .build();

            when(professionalRepository.findAll()).thenReturn(List.of(professionalEntity));
            when(bookingRepository.findBookingsForProfessionalOnDate(eq(1L), any(), any()))
                    .thenReturn(List.of(existing));

            AvailabilityResponse result = availabilityService.checkAvailability(request);
            List<TimeSlot> slots = result.getProfessionals().get(0).getAvailableSlots();

            // No slot should overlap the 30-min break window around 10:00–12:00
            slots.forEach(slot -> {
                boolean endsBeforeWindow  = !slot.getTo().isAfter(LocalTime.of(9, 30));
                boolean startsAfterWindow = !slot.getFrom().isBefore(LocalTime.of(12, 30));
                assertThat(endsBeforeWindow || startsAfterWindow)
                        .as("Slot %s–%s should respect 30-min break around 10:00–12:00 booking",
                                slot.getFrom(), slot.getTo())
                        .isTrue();
            });
        }

        @Test
        @DisplayName("Two back-to-back bookings (2h then 4h) should leave only one free window at end of day")
        void shouldComputeFreeSlotsAroundTwoBackToBackBookings() {
            AvailabilityRequest request = new AvailabilityRequest(WORK_DATE, null, null);

            // Booking 1: 09:00–11:00 (2h) → blocked window: 08:30–11:30
            BookingEntity firstBooking = BookingEntity.builder()
                    .id(1L)
                    .startDateTime(LocalDateTime.of(WORK_DATE, LocalTime.of(9, 0)))
                    .endDateTime(LocalDateTime.of(WORK_DATE, LocalTime.of(11, 0)))
                    .durationHours(2)
                    .status(BookingStatus.CONFIRMED)
                    .build();

            // Booking 2: 12:00–16:00 (4h, starts right after the 30-min break of booking 1)
            BookingEntity secondBooking = BookingEntity.builder()
                    .id(2L)
                    .startDateTime(LocalDateTime.of(WORK_DATE, LocalTime.of(12, 0)))
                    .endDateTime(LocalDateTime.of(WORK_DATE, LocalTime.of(16, 0)))
                    .durationHours(4)
                    .status(BookingStatus.CONFIRMED)
                    .build();

            when(professionalRepository.findAll()).thenReturn(List.of(professionalEntity));
            when(bookingRepository.findBookingsForProfessionalOnDate(eq(1L), any(), any()))
                    .thenReturn(List.of(firstBooking, secondBooking));

            AvailabilityResponse result = availabilityService.checkAvailability(request);
            List<TimeSlot> slots = result.getProfessionals().get(0).getAvailableSlots();

            // Expected free windows (each must be ≥ 2h to appear)
            assertThat(slots).hasSize(1);

            assertThat(slots.get(0).getFrom()).isEqualTo(LocalTime.of(16, 30));
            assertThat(slots.get(0).getTo()).isEqualTo(LocalTime.of(22, 0));
        }
    }
}

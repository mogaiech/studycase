package com.justlife.studycase.service;

import com.justlife.studycase.dto.BookingRequest;
import com.justlife.studycase.dto.BookingResponse;
import com.justlife.studycase.entity.BookingEntity;
import com.justlife.studycase.entity.BookingStatus;
import com.justlife.studycase.entity.ProfessionalEntity;
import com.justlife.studycase.entity.VehicleEntity;
import com.justlife.studycase.exception.BusinessException;
import com.justlife.studycase.repository.BookingRepository;
import com.justlife.studycase.repository.ProfessionalRepository;
import com.justlife.studycase.repository.VehicleRepository;
import com.justlife.studycase.utils.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.justlife.studycase.utils.TestDates.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private ProfessionalRepository professionalRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private AvailabilityService availabilityService;

    private BookingService bookingService;

    private VehicleEntity vehicle;
    private ProfessionalEntity pro1;
    private ProfessionalEntity pro2;
    private ProfessionalEntity pro3;

    @BeforeEach
    void setUp() {
        Validator validator = new Validator();
        bookingService = new BookingService(
                validator, bookingRepository, professionalRepository,
                vehicleRepository, availabilityService);

        vehicle = VehicleEntity.builder()
                .id(1L)
                .brandName("Nissan")
                .plateNumber("JL-001")
                .build();

        pro1 = buildProfessional(1L, "Ahmed");
        pro2 = buildProfessional(2L, "Fatima");
        pro3 = buildProfessional(3L, "Mohammed");
    }

    private ProfessionalEntity buildProfessional(Long id, String name) {
        return ProfessionalEntity.builder()
                .id(id)
                .name(name)
                .email(name.toLowerCase() + "@test.com")
                .phone("+971500000001")
                .vehicle(vehicle)
                .build();
    }

    private BookingEntity buildBooking(Long id, LocalDateTime start, int duration, int profCount) {
        return BookingEntity.builder()
                .id(id)
                .startDateTime(start)
                .endDateTime(start.plusHours(duration))
                .durationHours(duration)
                .professionalCount(profCount)
                .customerName("Test Customer")
                .customerEmail("test@test.com")
                .status(BookingStatus.CONFIRMED)
                .professionals(new LinkedHashSet<>())
                .build();
    }

    @Nested
    @DisplayName("Booking creation")
    class BookingCreationTests {

        @Test
        @DisplayName("Should create a booking with 1 professional successfully")
        void shouldCreateBookingWithOneProfessional() {
            BookingRequest request = new BookingRequest(WORK_START, 2, 1, "john@test.com", "John Doe");

            BookingEntity saved = buildBooking(1L, WORK_START, 2, 1);
            when(vehicleRepository.findAll()).thenReturn(List.of(vehicle));
            when(professionalRepository.findAvailableProfessionalsByVehicle(anyLong(), any(), any()))
                    .thenReturn(List.of(pro1, pro2, pro3));
            when(bookingRepository.save(any())).thenReturn(saved);

            BookingResponse response = bookingService.createBooking(request);

            assertThat(response).isNotNull();
            assertThat(response.getDurationHours()).isEqualTo(2);
            verify(bookingRepository).save(any());
        }

        @Test
        @DisplayName("Should create a booking with 3 professionals from same vehicle")
        void shouldCreateBookingWithThreeProfessionals() {
            BookingRequest request = new BookingRequest(WORK_START, 4, 3, "jane@test.com", "Jane Doe");

            BookingEntity saved = buildBooking(2L, WORK_START, 4, 3);
            when(vehicleRepository.findAll()).thenReturn(List.of(vehicle));
            when(professionalRepository.findAvailableProfessionalsByVehicle(anyLong(), any(), any()))
                    .thenReturn(List.of(pro1, pro2, pro3));
            when(bookingRepository.save(any())).thenReturn(saved);

            BookingResponse response = bookingService.createBooking(request);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Should throw exception when booking on a Friday")
        void shouldRejectFridayBooking() {
            BookingRequest request = new BookingRequest(FRIDAY, 2, 1, "john@test.com", "John Doe");

            assertThatThrownBy(() -> bookingService.createBooking(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Friday");
        }

        @Test
        @DisplayName("Should throw exception for invalid duration")
        void shouldRejectInvalidDuration() {
            BookingRequest request = new BookingRequest(WORK_START, 3, 1, "john@test.com", "John Doe");

            assertThatThrownBy(() -> bookingService.createBooking(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("duration");
        }

        @Test
        @DisplayName("Should throw exception when no vehicle has enough available professionals")
        void shouldThrowWhenNoAvailableProfessionals() {
            BookingRequest request = new BookingRequest(WORK_START, 2, 3, "john@test.com", "John Doe");

            when(vehicleRepository.findAll()).thenReturn(List.of(vehicle));
            when(professionalRepository.findAvailableProfessionalsByVehicle(anyLong(), any(), any()))
                    .thenReturn(List.of(pro1, pro2));

            assertThatThrownBy(() -> bookingService.createBooking(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("available");
        }

        @Test
        @DisplayName("Should reject booking starting before 08:00")
        void shouldRejectBookingBeforeWorkHours() {
            BookingRequest request = new BookingRequest(BEFORE_WORK_HOURS, 2, 1, "john@test.com", "John Doe");

            assertThatThrownBy(() -> bookingService.createBooking(request))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("Should reject 4-hour booking ending after 22:00")
        void shouldRejectBookingExceedingWorkHours() {
            BookingRequest request = new BookingRequest(LATE_START, 4, 1, "john@test.com", "John Doe");

            assertThatThrownBy(() -> bookingService.createBooking(request))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("Booking update")
    class BookingUpdateTests {

        @Test
        @DisplayName("Should update booking to a new time keeping same professionals")
        void shouldUpdateBookingKeepingSameProfessionals() {
            BookingEntity existing = buildBooking(1L, WORK_START, 2, 1);
            existing.setProfessionals(new LinkedHashSet<>(Set.of(pro1)));

            BookingRequest updateRequest = new BookingRequest(NEXT_DAY, 2, null, null, null);

            when(bookingRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(availabilityService.isProfessionalAvailable(eq(1L), any(), any(), eq(1L)))
                    .thenReturn(true);
            when(bookingRepository.save(any())).thenReturn(existing);

            BookingResponse response = bookingService.updateBooking(1L, updateRequest);

            assertThat(response).isNotNull();
            verify(bookingRepository).save(any());
        }

        @Test
        @DisplayName("Should throw BusinessException for non-existent booking")
        void shouldThrowForNonExistentBooking() {
            when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

            BookingRequest request = new BookingRequest(NEXT_DAY, 2, null, null, null);

            assertThatThrownBy(() -> bookingService.updateBooking(999L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("not found");
        }

        @Test
        @DisplayName("Should reject update when it's on Friday")
        void shouldRejectUpdateToFriday() {
            BookingEntity existing = buildBooking(1L, WORK_START, 2, 1);
            existing.setProfessionals(new LinkedHashSet<>(Set.of(pro1)));

            when(bookingRepository.findById(1L)).thenReturn(Optional.of(existing));

            BookingRequest request = new BookingRequest(FRIDAY, 2, null, null, null);

            assertThatThrownBy(() -> bookingService.updateBooking(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Friday");
        }

        @Test
        @DisplayName("Should reject update of a cancelled booking")
        void shouldRejectUpdateOfCancelledBooking() {
            BookingEntity cancelled = buildBooking(1L, WORK_START, 2, 1);
            cancelled.setStatus(BookingStatus.CANCELLED);

            when(bookingRepository.findById(1L)).thenReturn(Optional.of(cancelled));

            BookingRequest request = new BookingRequest(NEXT_DAY, 2, null, null, null);

            assertThatThrownBy(() -> bookingService.updateBooking(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("confirmed");
        }
    }

    @Nested
    @DisplayName("Booking retrieval")
    class BookingRetrievalTests {

        @Test
        @DisplayName("Should retrieve booking by ID")
        void shouldGetBookingById() {
            BookingEntity booking = buildBooking(1L, WORK_START, 2, 1);
            booking.setProfessionals(new LinkedHashSet<>(Set.of(pro1)));

            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

            BookingResponse response = bookingService.getBooking(1L);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw BusinessException for missing booking")
        void shouldThrowForMissingBooking() {
            when(bookingRepository.findById(42L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.getBooking(42L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("not found");
        }
    }
}

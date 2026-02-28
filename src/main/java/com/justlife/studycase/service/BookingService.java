package com.justlife.studycase.service;

import com.justlife.studycase.dto.BookingRequest;
import com.justlife.studycase.dto.BookingResponse;
import com.justlife.studycase.entity.BookingEntity;
import com.justlife.studycase.entity.BookingStatus;
import com.justlife.studycase.entity.ProfessionalEntity;
import com.justlife.studycase.entity.VehicleEntity;
import com.justlife.studycase.exception.BusinessException;
import com.justlife.studycase.mapper.BookingMapper;
import com.justlife.studycase.repository.BookingRepository;
import com.justlife.studycase.repository.ProfessionalRepository;
import com.justlife.studycase.repository.VehicleRepository;
import com.justlife.studycase.utils.Constant;
import com.justlife.studycase.utils.Validator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final Validator validator;
    private final BookingRepository bookingRepository;
    private final ProfessionalRepository professionalRepository;
    private final VehicleRepository vehicleRepository;
    private final AvailabilityService availabilityService;

    @Transactional
    public BookingResponse createBooking(BookingRequest bookingRequest) {
        LocalDateTime startDateTime = bookingRequest.getStartDateTime();
        LocalDateTime endDateTime = startDateTime.plusHours(bookingRequest.getDurationHours());

        validator.validateCreateBooking(bookingRequest);

        Set<ProfessionalEntity> assignedProfessionals = findAvailableProfessionalsFromVehicle(
                startDateTime,
                endDateTime,
                bookingRequest.getProfessionalCount(), null);

        // Create and persist the booking with its professionals
        BookingEntity booking = BookingEntity.builder()
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .durationHours(bookingRequest.getDurationHours())
                .customerName(bookingRequest.getCustomerName())
                .customerEmail(bookingRequest.getCustomerEmail())
                .status(BookingStatus.CONFIRMED)
                .professionalCount(bookingRequest.getProfessionalCount())
                .professionals(assignedProfessionals)
                .build();

        BookingEntity savedBooking = bookingRepository.save(booking);

        return BookingMapper.toBookingResponseDto(savedBooking);
    }

    @Transactional
    public BookingResponse getBooking(Long id) {
        return BookingMapper.toBookingResponseDto(
                bookingRepository.findById(id)
                        .orElseThrow(() -> new BusinessException("Booking not found with id: " + id)));
    }

    @Transactional
    public BookingResponse updateBooking(Long id, BookingRequest bookingRequest) {
        BookingEntity bookingEntity = bookingRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Booking not found with id: " + id));

        if (!BookingStatus.CONFIRMED.equals(bookingEntity.getStatus())) {
            throw new BusinessException("Cannot update a non confirmed bookingEntity");
        }

        validator.validateUpdateBooking(bookingRequest);

        LocalDateTime startDateTime = bookingRequest.getStartDateTime();
        LocalDateTime endDateTime = startDateTime.plusHours(bookingEntity.getDurationHours());

        boolean currentProfessionalsAvailable = bookingEntity.getProfessionals().stream()
                .allMatch(prof -> availabilityService.isProfessionalAvailable(
                        prof.getId(), startDateTime, endDateTime, id));

        Set<ProfessionalEntity> assignedProfessionals;
        if (currentProfessionalsAvailable) {
            assignedProfessionals = bookingEntity.getProfessionals();
        } else {
            assignedProfessionals = findAvailableProfessionalsFromVehicle(startDateTime, endDateTime, bookingEntity.getProfessionalCount(), id);
        }

        bookingEntity.setStartDateTime(startDateTime);
        bookingEntity.setEndDateTime(endDateTime);
        bookingEntity.setProfessionals(assignedProfessionals);

        return BookingMapper.toBookingResponseDto(bookingRepository.save(bookingEntity));
    }

    /**
     * Find available professionals from a vehicle.
     */
    private Set<ProfessionalEntity> findAvailableProfessionalsFromVehicle(
            LocalDateTime appointmentStart,
            LocalDateTime appointmentEnd,
            int professionalCount,
            Long excludeBookingId) {

        LocalDateTime windowStart = appointmentStart.minusMinutes(Constant.BREAK_MINUTES);
        LocalDateTime windowEnd = appointmentEnd.plusMinutes(Constant.BREAK_MINUTES);

        // Iterate vehicles and find the first one with enough available professionals
        List<VehicleEntity> vehicleEntities = vehicleRepository.findAll();
        for (VehicleEntity vehicle : vehicleEntities) {
            List<ProfessionalEntity> availableProfessionals = professionalRepository.findAvailableProfessionalsByVehicle(vehicle.getId(), windowStart, windowEnd);

            // If updating, we need to re-check excluding the current booking
            if (excludeBookingId != null) {
                availableProfessionals = availableProfessionals.stream()
                        .filter(pro -> availabilityService.isProfessionalAvailable(pro.getId(), appointmentStart, appointmentEnd, excludeBookingId))
                        .toList();
            }

            // If enough professionals are available, assign them to the booking
            if (availableProfessionals.size() >= professionalCount) {
                return new HashSet<>(availableProfessionals.subList(0, professionalCount));
            }
        }

        throw new BusinessException("No vehicle has available professional for the requested time slot. Please try a different date, time, or reduce the number of professionals");
    }
}

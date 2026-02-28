package com.justlife.studycase.service;

import com.justlife.studycase.dto.*;
import com.justlife.studycase.entity.BookingEntity;
import com.justlife.studycase.entity.ProfessionalEntity;
import com.justlife.studycase.repository.BookingRepository;
import com.justlife.studycase.repository.ProfessionalRepository;
import com.justlife.studycase.utils.Constant;
import com.justlife.studycase.utils.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final ProfessionalRepository professionalRepository;
    private final BookingRepository bookingRepository;
    private final Validator validator;

    public AvailabilityResponse checkAvailability(AvailabilityRequest availabilityRequest) {
        validator.validateNotFriday(availabilityRequest.getDate());

        if (availabilityRequest.getDurationHours() != null && availabilityRequest.getStartTime() != null && Constant.VALID_DURATIONS.contains(availabilityRequest.getDurationHours())) {
            return checkAvailabilityForTimeSlot(availabilityRequest);
        }

        return checkAvailabilityForDate(availabilityRequest.getDate());
    }

    private AvailabilityResponse checkAvailabilityForTimeSlot(AvailabilityRequest availabilityRequest) {
        validator.validateDuration(availabilityRequest.getDurationHours());

        LocalDateTime slotStart = LocalDateTime.of(availabilityRequest.getDate(), availabilityRequest.getStartTime());
        LocalDateTime slotEnd = slotStart.plusHours(availabilityRequest.getDurationHours());

        validator.validateWithinWorkingHours(slotStart, slotEnd);

        // Window by 30 min each side to enforce break constraint
        LocalDateTime windowStart = slotStart.minusMinutes(Constant.BREAK_MINUTES);
        LocalDateTime windowEnd = slotEnd.plusMinutes(Constant.BREAK_MINUTES);

        List<ProfessionalEntity> availableProfessionals =
                professionalRepository.findAvailableProfessionals(windowStart, windowEnd);

        List<Professional> professionals = availableProfessionals.stream()
                .map(p -> Professional.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .email(p.getEmail())
                        .phone(p.getPhone())
                        .vehicle(Vehicle.builder().id(p.getVehicle().getId()).plateNumber(p.getVehicle().getPlateNumber()).brandName(p.getVehicle().getBrandName()).build())
                        .build())
                .collect(Collectors.toList());

        return AvailabilityResponse.builder().professionals(professionals).build();
    }

    private AvailabilityResponse checkAvailabilityForDate(LocalDate date) {
        List<ProfessionalEntity> professionalEntities = professionalRepository.findAll();

        List<Professional> professionals = professionalEntities.stream()
                .map(pro -> buildAvailabilityWithSlots(pro, date))
                .filter(p -> !p.getAvailableSlots().isEmpty())
                .collect(Collectors.toList());

        return AvailabilityResponse.builder().professionals(professionals).build();
    }

    private Professional buildAvailabilityWithSlots(ProfessionalEntity professionalEntity, LocalDate date) {
        LocalDateTime dayStart = LocalDateTime.of(date, Constant.WORK_START);
        LocalDateTime dayEnd = LocalDateTime.of(date, Constant.WORK_END);

        List<BookingEntity> bookings = bookingRepository
                .findBookingsForProfessionalOnDate(professionalEntity.getId(), dayStart, dayEnd);

        // Build blocked intervals (with 30-min break)
        List<LocalDateTime[]> blockedIntervals = bookings.stream()
                .map(b -> new LocalDateTime[]{
                        b.getStartDateTime().minusMinutes(Constant.BREAK_MINUTES),
                        b.getEndDateTime().plusMinutes(Constant.BREAK_MINUTES)
                })
                .collect(Collectors.toList());

        List<TimeSlot> slots = computeAvailableSlots(blockedIntervals, dayStart, dayEnd);

        return Professional.builder()
                .id(professionalEntity.getId())
                .name(professionalEntity.getName())
                .vehicle(Vehicle.builder().id(professionalEntity.getVehicle().getId()).plateNumber(professionalEntity.getVehicle().getPlateNumber()).brandName(professionalEntity.getVehicle().getBrandName()).build())
                .availableSlots(slots)
                .build();
    }

    private List<TimeSlot> computeAvailableSlots(List<LocalDateTime[]> blockedIntervals, LocalDateTime dayStart, LocalDateTime dayEnd) {
        // Sort blocked intervals by their start time
        List<LocalDateTime[]> sortedBlockedIntervals = blockedIntervals.stream()
                .sorted(Comparator.comparing(interval -> interval[0]))
                .toList();

        // Merge overlapping or adjacent blocked intervals into a single continuous block
        // so we don't create free slots inside an overlapping blocked range
        List<LocalDateTime[]> mergedBlockedIntervals = new ArrayList<>();
        for (LocalDateTime[] currentInterval : sortedBlockedIntervals) {
            LocalDateTime currentStart = currentInterval[0];
            LocalDateTime currentEnd = currentInterval[1];

            if (mergedBlockedIntervals.isEmpty() ||
                    currentStart.isAfter(mergedBlockedIntervals.get(mergedBlockedIntervals.size() - 1)[1])) {
                // No overlap with the last merged block: add as a new block
                mergedBlockedIntervals.add(new LocalDateTime[]{currentStart, currentEnd});
            } else {
                // Overlap detected: extend the last merged block's end if needed
                LocalDateTime lastMergedEnd = mergedBlockedIntervals.get(mergedBlockedIntervals.size() - 1)[1];
                mergedBlockedIntervals.get(mergedBlockedIntervals.size() - 1)[1] =
                        currentEnd.isAfter(lastMergedEnd) ? currentEnd : lastMergedEnd;
            }
        }

        // Walk through the day and collect free gaps between blocked intervals
        // A gap is only valid if it fits at least a 2-hour booking
        List<TimeSlot> freeSlots = new ArrayList<>();
        LocalDateTime freeWindowStart = dayStart;

        for (LocalDateTime[] blockedInterval : mergedBlockedIntervals) {
            LocalDateTime blockedStart = blockedInterval[0];
            LocalDateTime blockedEnd = blockedInterval[1];

            // The free window ends where the blocked interval begins
            LocalDateTime freeWindowEnd = blockedStart.isBefore(dayEnd) ? blockedStart : dayEnd;

            // Only add the free slot if the gap is at least 2 hours
            if (!freeWindowStart.plusHours(2).isAfter(freeWindowEnd)) {
                freeSlots.add(TimeSlot.builder()
                        .from(freeWindowStart.toLocalTime())
                        .to(freeWindowEnd.toLocalTime())
                        .build());
            }

            // Move the free window start past the end of the current blocked interval
            freeWindowStart = blockedEnd.isAfter(freeWindowStart) ? blockedEnd : freeWindowStart;
        }

        // Handle the remaining free window after the last blocked interval until the end of the day
        if (!freeWindowStart.plusHours(2).isAfter(dayEnd)) {
            freeSlots.add(TimeSlot.builder()
                    .from(freeWindowStart.toLocalTime())
                    .to(dayEnd.toLocalTime())
                    .build());
        }

        return freeSlots;
    }

    /**
     * Check if a specific professional is available for an appointment.
     */
    public boolean isProfessionalAvailable(Long professionalId,
                                           LocalDateTime appointmentStart,
                                           LocalDateTime appointmentEnd,
                                           Long excludeBookingId) {

        LocalDateTime windowStart = appointmentStart.minusMinutes(Constant.BREAK_MINUTES);
        LocalDateTime windowEnd = appointmentEnd.plusMinutes(Constant.BREAK_MINUTES);

        List<BookingEntity> bookingConflicts = bookingRepository.findOverlappingBookingsForProfessional(
                professionalId, windowStart, windowEnd);

        if (excludeBookingId != null) {
            bookingConflicts = bookingConflicts.stream()
                    .filter(b -> !b.getId().equals(excludeBookingId))
                    .toList();
        }

        return bookingConflicts.isEmpty();
    }
}

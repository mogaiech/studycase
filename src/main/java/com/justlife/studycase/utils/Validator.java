package com.justlife.studycase.utils;

import com.justlife.studycase.dto.BookingRequest;
import com.justlife.studycase.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
public class Validator {
    public void validateNotFriday(LocalDate date) {
        if (date.getDayOfWeek() == DayOfWeek.FRIDAY) {
            throw new BusinessException("No availability on Fridays");
        }
    }

    public void validateWithinWorkingHours(LocalDateTime start, LocalDateTime end) {
        LocalTime startT = start.toLocalTime();
        LocalTime endT = end.toLocalTime();
        if (startT.isBefore(Constant.WORK_START)) {
            throw new BusinessException("Appointments cannot start before 08:00");
        }
        if (endT.isAfter(Constant.WORK_END)) {
            throw new BusinessException("Appointments must finish by 22:00");
        }
    }

    public void validateDuration(int durationHours) {
        boolean valid = Constant.VALID_DURATIONS.stream()
                .anyMatch(vd -> vd == durationHours);
        if (!valid) {
            throw new BusinessException(
                    "Appointment duration must be 2 or 4 hours");
        }
    }

    public void validateProfessionalCount(int count) {
        boolean valid = Constant.ALLOWED_PROFESSIONAL_COUNTS.stream()
                .anyMatch(c -> c == count);
        if (!valid) {
            throw new BusinessException(
                    "Professional count must be 1, 2, or 3. Provided: " + count);
        }
    }

    public void validateCreateBooking(BookingRequest request) {
        validateFields(request);
        validateNotFriday(request.getStartDateTime().toLocalDate());
        validateDuration(request.getDurationHours());

        LocalDateTime endDateTime = request.getStartDateTime().plusHours(request.getDurationHours());
        validateWithinWorkingHours(request.getStartDateTime(), endDateTime);

        if (request.getCustomerName() == null || request.getCustomerName().isBlank())
            throw new BusinessException("customerName is required");
        if (request.getCustomerEmail() == null || request.getCustomerEmail().isBlank())
            throw new BusinessException("customerEmail is required");
        if (request.getProfessionalCount() == null)
            throw new BusinessException("professionalCount is required");

        validateProfessionalCount(request.getProfessionalCount());
    }

    public void validateUpdateBooking(BookingRequest request) {
        // Only startDateTime and durationHours are required for update
        validateFields(request);

        // Business rule validation
        validateNotFriday(request.getStartDateTime().toLocalDate());
        validateDuration(request.getDurationHours());

        LocalDateTime end = request.getStartDateTime().plusHours(request.getDurationHours());
        validateWithinWorkingHours(request.getStartDateTime(), end);
    }

    private static void validateFields(BookingRequest request) {
        if (request.getStartDateTime() == null)
            throw new BusinessException("startDateTime is required");
        if (request.getStartDateTime().isBefore(LocalDateTime.now()))
            throw new BusinessException("startDateTime must be in the future");
        if (request.getDurationHours() == null)
            throw new BusinessException("durationHours is required");
    }
}

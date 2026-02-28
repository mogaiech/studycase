package com.justlife.studycase.validator;

import com.justlife.studycase.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static com.justlife.studycase.service.AvailabilityService.*;

@Component
public class AvailabilityValidator {

    public void validateNotFriday(LocalDate date) {
        if (date.getDayOfWeek() == DayOfWeek.FRIDAY) {
            throw new BusinessException("No availability on Fridays");
        }
    }

    public void validateWithinWorkingHours(LocalDateTime start, LocalDateTime end) {
        LocalTime startT = start.toLocalTime();
        LocalTime endT = end.toLocalTime();
        if (startT.isBefore(WORK_START)) {
            throw new BusinessException("Appointments cannot start before 08:00");
        }
        if (endT.isAfter(WORK_END)) {
            throw new BusinessException("Appointments must finish by 22:00");
        }
    }

    public void validateDuration(int durationHours) {
        boolean valid = VALID_DURATIONS.stream()
                .anyMatch(vd -> vd == durationHours);
        if (!valid) {
            throw new BusinessException(
                    "Appointment duration must be 2 or 4 hours");
        }
    }
}

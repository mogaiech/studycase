package com.justlife.studycase.mapper;

import com.justlife.studycase.dto.BookingResponse;
import com.justlife.studycase.dto.Professional;
import com.justlife.studycase.entity.BookingEntity;

import java.util.List;
import java.util.stream.Collectors;

public class BookingMapper {

    private BookingMapper() {
        throw new IllegalStateException("BookingMapper is a static class");
    }

    /**
     * Convert a BookingEntity to its DTO representation.
     */
    public static BookingResponse toBookingResponseDto(BookingEntity bookingEntity) {
        List<Professional> professionals = bookingEntity.getProfessionals().stream()
                .map(ProfessionalMapper::toProfessionalDto)
                .collect(Collectors.toList());

        return BookingResponse.builder()
                .id(bookingEntity.getId())
                .customerName(bookingEntity.getCustomerName())
                .customerEmail(bookingEntity.getCustomerEmail())
                .startDateTime(bookingEntity.getStartDateTime())
                .endDateTime(bookingEntity.getEndDateTime())
                .durationHours(bookingEntity.getDurationHours())
                .status(bookingEntity.getStatus().name())
                .professionals(professionals)
                .build();
    }
}

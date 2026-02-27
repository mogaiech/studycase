package com.justlife.studycase.mapper;

import com.justlife.studycase.dto.BookingResponse;
import com.justlife.studycase.dto.Professional;
import com.justlife.studycase.dto.Vehicle;
import com.justlife.studycase.entity.BookingEntity;

import java.util.List;
import java.util.stream.Collectors;

public class BookingMapper {

    private BookingMapper() {
        throw new IllegalStateException("BookingMapper is a static class");
    }

    public static BookingResponse convertEntityToDto(BookingEntity bookingEntity) {
        List<Professional> professionals = bookingEntity.getProfessionals().stream()
                .map(p -> Professional.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .vehicle(Vehicle.builder().id(p.getVehicle().getId()).plateNumber(p.getVehicle().getPlateNumber()).brandName(p.getVehicle().getBrandName()).build())
                        .build())
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

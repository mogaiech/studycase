package com.justlife.studycase.mapper;

import com.justlife.studycase.dto.Professional;
import com.justlife.studycase.dto.TimeSlot;
import com.justlife.studycase.dto.Vehicle;
import com.justlife.studycase.entity.ProfessionalEntity;
import com.justlife.studycase.entity.VehicleEntity;

import java.util.List;

public class ProfessionalMapper {

    private ProfessionalMapper() {
        throw new IllegalStateException("ProfessionalMapper is a static class");
    }

    /**
     * Convert a ProfessionalEntity to its DTO representation.
     */
    public static Professional toProfessionalDto(ProfessionalEntity professionalEntity) {
        return Professional.builder()
                .id(professionalEntity.getId())
                .name(professionalEntity.getName())
                .email(professionalEntity.getEmail())
                .phone(professionalEntity.getPhone())
                .vehicle(VehicleMapper.toVehicleDto(professionalEntity.getVehicle()))
                .build();
    }

    /**
     * Convert a ProfessionalEntity to its DTO representation with available time slots.
     */
    public static Professional toProfessionalWithSlotsDto(ProfessionalEntity professionalEntity, List<TimeSlot> availableSlots) {
        return Professional.builder()
                .id(professionalEntity.getId())
                .name(professionalEntity.getName())
                .phone(professionalEntity.getPhone())
                .vehicle(VehicleMapper.toVehicleDto(professionalEntity.getVehicle()))
                .availableSlots(availableSlots)
                .build();
    }
}


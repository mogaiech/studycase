package com.justlife.studycase.mapper;

import com.justlife.studycase.dto.Vehicle;
import com.justlife.studycase.entity.VehicleEntity;

public class VehicleMapper {

    private VehicleMapper() {
        throw new IllegalStateException("VehicleMapper is a static class");
    }

    /**
     * Convert a VehicleEntity to its DTO representation.
     */
    public static Vehicle toVehicleDto(VehicleEntity vehicleEntity) {
        return Vehicle.builder()
                .id(vehicleEntity.getId())
                .plateNumber(vehicleEntity.getPlateNumber())
                .brandName(vehicleEntity.getBrandName())
                .build();
    }
}

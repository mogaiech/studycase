package com.justlife.studycase.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A vehicle")
public class Vehicle {
    @Schema(description = "ID of the vehicle")
    private Long id;

    @Schema(description = "Vehicle plate number")
    private String plateNumber;

    @Schema(description = "Brand name")
    private String brandName;
}

package com.justlife.studycase.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "API error response")
public class ApiError {
    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Error message")
    private String message;

    @Schema(description = "Detailed validation errors")
    List<String> errors;
}

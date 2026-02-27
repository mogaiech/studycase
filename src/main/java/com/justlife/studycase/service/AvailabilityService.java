package com.justlife.studycase.service;

import com.justlife.studycase.dto.AvailabilityRequest;
import com.justlife.studycase.dto.AvailabilityResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    static final LocalTime WORK_START = LocalTime.of(8, 0);
    static final LocalTime WORK_END = LocalTime.of(22, 0);
    static final int BREAK_MINUTES = 30;
    static final Set<Integer> VALID_DURATIONS = Set.of(2, 4);

    public AvailabilityResponse checkAvailability(AvailabilityRequest availabilityRequest) {
        return null;
    }
}

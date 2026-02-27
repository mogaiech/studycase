package com.justlife.studycase.service;

import com.justlife.studycase.dto.BookingRequest;
import com.justlife.studycase.dto.BookingResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingService {

    public BookingResponse createBooking(@Valid BookingRequest request) {
        return null;
    }

    public BookingResponse getBooking(Long id) {
        return null;
    }

    public BookingResponse updateBooking(Long id, @Valid BookingRequest request) {
        return null;
    }
}

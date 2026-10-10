package com.college.kitbooking.application;

import com.college.kitbooking.domain.Booking;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/** Strategy for booking timing and kit-slot conflict rules. */
public interface BookingPolicy {
    void validateAdvanceNotice(LocalDateTime start, LocalDateTime now);
    boolean conflicts(Booking candidate, List<Booking> existingBookings);
    Duration turnaroundBuffer();
}

package com.college.kitbooking.application;

import com.college.kitbooking.domain.Booking;
import com.college.kitbooking.domain.BookingStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public final class StandardBookingPolicy implements BookingPolicy {
    private static final Duration MINIMUM_NOTICE = Duration.ofHours(2);
    private static final Duration BUFFER = Duration.ofMinutes(15);
    private static final Set<BookingStatus> SLOT_OCCUPYING =
            Set.of(BookingStatus.BOOKED);

    @Override
    public void validateAdvanceNotice(LocalDateTime start, LocalDateTime now) {
        if (start.isBefore(now.plus(MINIMUM_NOTICE))) {
            throw new IllegalArgumentException(
                    "Bookings must be requested at least 2 hours before the start time.");
        }
    }

    @Override
    public boolean conflicts(Booking candidate, List<Booking> existingBookings) {
        LocalDateTime candidateProtectedEnd = candidate.end().plus(BUFFER);
        for (Booking existing : existingBookings) {
            if (existing.id() == candidate.id()) continue;
            if (existing.kitId() != candidate.kitId()) continue;
            if (!SLOT_OCCUPYING.contains(existing.status())) continue;

            LocalDateTime existingProtectedEnd = existing.end().plus(BUFFER);
            boolean overlaps = candidate.start().isBefore(existingProtectedEnd)
                    && candidateProtectedEnd.isAfter(existing.start());
            if (overlaps) return true;
        }
        return false;
    }

    @Override
    public Duration turnaroundBuffer() {
        return BUFFER;
    }
}

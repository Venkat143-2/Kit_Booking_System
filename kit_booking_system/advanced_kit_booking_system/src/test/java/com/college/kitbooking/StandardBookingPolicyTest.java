package com.college.kitbooking;

import com.college.kitbooking.application.StandardBookingPolicy;
import com.college.kitbooking.domain.Booking;
import com.college.kitbooking.domain.BookingStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StandardBookingPolicyTest {
    @Test
    void requiresTwoHoursNotice() {
        StandardBookingPolicy policy = new StandardBookingPolicy();
        LocalDateTime now = LocalDateTime.of(2026, 10, 10, 9, 0);
        assertThrows(IllegalArgumentException.class,
                () -> policy.validateAdvanceNotice(now.plusHours(1), now));
        assertDoesNotThrow(() -> policy.validateAdvanceNotice(now.plusHours(2), now));
    }

    @Test
    void blocksSameKitWithBufferButAllowsDifferentKit() {
        StandardBookingPolicy policy = new StandardBookingPolicy();
        LocalDateTime now = LocalDateTime.of(2026, 10, 10, 9, 0);
        Booking first = new Booking(1, "userA", 1, now.plusHours(3),
                now.plusHours(4), now);
        first.approve();

        Booking tooEarly = new Booking(2, "userB", 1, now.plusHours(4),
                now.plusHours(5), now);
        Booking anotherKit = new Booking(3, "userC", 2, now.plusHours(4),
                now.plusHours(5), now);

        assertTrue(policy.conflicts(tooEarly, List.of(first)));
        assertFalse(policy.conflicts(anotherKit, List.of(first)));
    }

    @Test
    void cancelledBookingsDoNotOccupySlots() {
        StandardBookingPolicy policy = new StandardBookingPolicy();
        LocalDateTime now = LocalDateTime.of(2026, 10, 10, 9, 0);
        Booking first = new Booking(1, "userA", 1, now.plusHours(3),
                now.plusHours(4), now);
        first.approve();
        first.cancel();

        Booking next = new Booking(2, "userB", 1, now.plusHours(3),
                now.plusHours(4), now);
        assertFalse(policy.conflicts(next, List.of(first)));
    }
}

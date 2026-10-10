package com.college.kitbooking.infrastructure;

import com.college.kitbooking.application.port.BookingNotifier;
import com.college.kitbooking.domain.Booking;

public final class ConsoleBookingNotifier implements BookingNotifier {
    @Override
    public void notifyUser(Booking booking, String message) {
        System.out.printf("[Notification: user=%s, booking=%d] %s%n",
                booking.userId(), booking.id(), message);
    }
}

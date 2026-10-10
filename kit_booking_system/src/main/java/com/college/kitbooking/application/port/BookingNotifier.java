package com.college.kitbooking.application.port;

import com.college.kitbooking.domain.Booking;

public interface BookingNotifier {
    void notifyUser(Booking booking, String message);
}

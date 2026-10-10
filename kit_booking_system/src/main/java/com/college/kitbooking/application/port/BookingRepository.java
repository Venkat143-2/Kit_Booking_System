package com.college.kitbooking.application.port;

import com.college.kitbooking.domain.Booking;
import java.util.List;
import java.util.Optional;

public interface BookingRepository {
    Booking save(Booking booking);
    Optional<Booking> findById(long id);
    List<Booking> findAll();
}

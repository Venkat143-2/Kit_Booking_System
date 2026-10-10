package com.college.kitbooking.infrastructure;

import com.college.kitbooking.application.port.BookingRepository;
import com.college.kitbooking.domain.Booking;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryBookingRepository implements BookingRepository {
    private final ConcurrentMap<Long, Booking> data = new ConcurrentHashMap<>();

    @Override
    public Booking save(Booking booking) {
        data.put(booking.id(), booking);
        return booking;
    }

    @Override
    public Optional<Booking> findById(long id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public List<Booking> findAll() {
        List<Booking> result = new ArrayList<>(data.values());
        result.sort(Comparator.comparingLong(Booking::id));
        return List.copyOf(result);
    }
}

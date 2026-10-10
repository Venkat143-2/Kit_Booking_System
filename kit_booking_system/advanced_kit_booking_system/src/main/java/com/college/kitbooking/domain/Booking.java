package com.college.kitbooking.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public final class Booking {
    private final long id;
    private final String userId;
    private final int kitId;
    private final LocalDateTime start;
    private final LocalDateTime end;
    private final LocalDateTime requestedAt;
    private BookingStatus status;

    public Booking(long id, String userId, int kitId, LocalDateTime start,
                   LocalDateTime end, LocalDateTime requestedAt) {
        if (id <= 0 || kitId <= 0) throw new IllegalArgumentException("IDs must be positive.");
        this.id = id;
        this.userId = requireText(userId, "User ID");
        this.kitId = kitId;
        this.start = Objects.requireNonNull(start);
        this.end = Objects.requireNonNull(end);
        this.requestedAt = Objects.requireNonNull(requestedAt);
        if (!start.isBefore(end)) throw new IllegalArgumentException("End must be after start.");
        this.status = BookingStatus.PENDING;
    }

    private static String requireText(String value, String label) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(label + " is required.");
        return value.trim();
    }

    public long id() { return id; }
    public String userId() { return userId; }
    public int kitId() { return kitId; }
    public LocalDateTime start() { return start; }
    public LocalDateTime end() { return end; }
    public LocalDateTime requestedAt() { return requestedAt; }
    public BookingStatus status() { return status; }

    public void approve() { requireStatus(BookingStatus.PENDING); status = BookingStatus.BOOKED; }
    public void reject() { requireStatus(BookingStatus.PENDING); status = BookingStatus.REJECTED; }
    public void cancel() {
        if (status != BookingStatus.PENDING && status != BookingStatus.BOOKED)
            throw new IllegalStateException("Only pending or booked requests can be cancelled.");
        status = BookingStatus.CANCELLED;
    }
    public void complete() { requireStatus(BookingStatus.BOOKED); status = BookingStatus.COMPLETED; }

    private void requireStatus(BookingStatus expected) {
        if (status != expected) {
            throw new IllegalStateException("Booking " + id + " must be " + expected + " but is " + status + ".");
        }
    }
}

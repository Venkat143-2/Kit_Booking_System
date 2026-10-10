package com.college.kitbooking.application;

import com.college.kitbooking.application.port.BookingNotifier;
import com.college.kitbooking.application.port.BookingRepository;
import com.college.kitbooking.application.port.ClockProvider;
import com.college.kitbooking.application.port.KitRepository;
import com.college.kitbooking.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Application use cases. Depends on repository/notifier abstractions (DIP),
 * not on console, database, or email implementations.
 */
public final class BookingService {
    private final BookingRepository bookings;
    private final KitRepository kits;
    private final BookingPolicy policy;
    private final ClockProvider clock;
    private final BookingNotifier notifier;

    public BookingService(BookingRepository bookings, KitRepository kits,
                          BookingPolicy policy, ClockProvider clock,
                          BookingNotifier notifier) {
        this.bookings = Objects.requireNonNull(bookings);
        this.kits = Objects.requireNonNull(kits);
        this.policy = Objects.requireNonNull(policy);
        this.clock = Objects.requireNonNull(clock);
        this.notifier = Objects.requireNonNull(notifier);
    }

    public Booking requestBooking(String userId, int kitId,
                                 LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = clock.now();
        if (start == null || end == null || !start.isBefore(end))
            throw new IllegalArgumentException("End time must be after start time.");
        policy.validateAdvanceNotice(start, now);

        Kit kit = requireKit(kitId);
        if (!kit.isOperational())
            throw new IllegalStateException("Selected kit is unavailable: equipment is faulty or missing.");

        long id = nextBookingId();
        Booking candidate = new Booking(id, userId, kitId, start, end, now);
        if (policy.conflicts(candidate, bookings.findAll()))
            throw new IllegalStateException("Selected kit conflicts with an approved booking or its 15-minute buffer.");

        bookings.save(candidate);
        notifier.notifyUser(candidate, "Booking request submitted and awaiting admin approval.");
        return candidate;
    }

    public Booking approve(long bookingId) {
        Booking booking = requireBooking(bookingId);
        if (booking.status() != BookingStatus.PENDING)
            throw new IllegalStateException("Only pending requests can be approved.");

        Kit kit = requireKit(booking.kitId());
        if (!kit.isOperational())
            throw new IllegalStateException("Cannot approve: this kit is faulty, missing equipment, or unavailable.");
        if (policy.conflicts(booking, bookings.findAll()))
            throw new IllegalStateException("Cannot approve: the kit's time slot conflicts with another booking.");

        booking.approve();
        notifier.notifyUser(booking, "Your booking has been approved.");
        return booking;
    }

    public Booking reject(long bookingId) {
        Booking booking = requireBooking(bookingId);
        booking.reject();
        notifier.notifyUser(booking, "Your booking request was rejected by admin.");
        return booking;
    }

    public Booking cancelByUser(long bookingId, String userId) {
        Booking booking = requireBooking(bookingId);
        if (!booking.userId().equalsIgnoreCase(userId == null ? "" : userId.trim()))
            throw new IllegalArgumentException("You can cancel only your own booking.");
        booking.cancel();
        notifier.notifyUser(booking, "Your booking has been cancelled.");
        return booking;
    }

    public Booking completeReturn(long bookingId) {
        Booking booking = requireBooking(bookingId);
        if (clock.now().isBefore(booking.end()))
            throw new IllegalStateException("The booking end time has not been reached.");
        booking.complete();
        notifier.notifyUser(booking, "Kit return has been recorded.");
        return booking;
    }

    public void inspectEquipment(int kitId, EquipmentType type, EquipmentCondition condition) {
        Kit kit = requireKit(kitId);
        kit.inspect(type, condition);
        if (!kit.isOperational()) {
            // Future confirmed bookings are cancelled because this kit cannot safely be issued.
            for (Booking booking : bookings.findAll()) {
                if (booking.kitId() == kitId
                        && booking.status() == BookingStatus.BOOKED
                        && booking.start().isAfter(clock.now())) {
                    booking.cancel();
                    notifier.notifyUser(booking,
                            "Booking cancelled because the kit failed equipment inspection.");
                }
            }
        }
    }

    public List<Kit> allKits() {
        return kits.findAll();
    }

    public List<Booking> allBookings() {
        return bookings.findAll();
    }

    public List<Booking> pendingBookings() {
        return bookings.findAll().stream()
                .filter(b -> b.status() == BookingStatus.PENDING).toList();
    }

    /** User-facing logs intentionally expose only booked and cancelled records. */
    public List<Booking> userBookingAndCancellationLogs(String userId) {
        String normalized = userId == null ? "" : userId.trim();
        return bookings.findAll().stream()
                .filter(b -> b.userId().equalsIgnoreCase(normalized))
                .filter(b -> b.status() == BookingStatus.BOOKED
                        || b.status() == BookingStatus.CANCELLED)
                .toList();
    }

    private Kit requireKit(int id) {
        return kits.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Kit " + id + " does not exist."));
    }

    private Booking requireBooking(long id) {
        return bookings.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Booking " + id + " does not exist."));
    }

    private long nextBookingId() {
        return bookings.findAll().stream().mapToLong(Booking::id).max().orElse(0L) + 1L;
    }
}

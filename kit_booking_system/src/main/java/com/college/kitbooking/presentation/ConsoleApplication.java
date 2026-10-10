package com.college.kitbooking.presentation;

import com.college.kitbooking.application.BookingService;
import com.college.kitbooking.application.KitFactory;
import com.college.kitbooking.application.StandardBookingPolicy;
import com.college.kitbooking.application.port.BookingRepository;
import com.college.kitbooking.application.port.KitRepository;
import com.college.kitbooking.domain.*;
import com.college.kitbooking.infrastructure.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public final class ConsoleApplication {
    private static final Scanner IN = new Scanner(System.in);
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private ConsoleApplication() {}

    public static void main(String[] args) {
        int kitCount = args.length > 0 ? parseKitCount(args[0]) : 5;
        BookingRepository bookingRepository = new InMemoryBookingRepository();
        KitRepository kitRepository = new InMemoryKitRepository();
        for (int id = 1; id <= kitCount; id++) {
            kitRepository.save(KitFactory.createStandardKit(id));
        }

        BookingService service = new BookingService(
                bookingRepository, kitRepository, new StandardBookingPolicy(),
                new SystemClockProvider(), new ConsoleBookingNotifier());

        new ConsoleApplication().run(service);
    }

    private static int parseKitCount(String value) {
        try {
            int count = Integer.parseInt(value);
            if (count < 1) throw new NumberFormatException();
            return count;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Kit count must be a positive integer.");
        }
    }

    private void run(BookingService service) {
        while (true) {
            System.out.println("\\n=== COLLEGE KIT BOOKING ===");
            System.out.println("1. User");
            System.out.println("2. Admin");
            System.out.println("3. Exit");
            switch (readInt("Choose: ")) {
                case 1 -> userMenu(service);
                case 2 -> adminMenu(service);
                case 3 -> { System.out.println("Goodbye."); return; }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void userMenu(BookingService service) {
        String user = readText("User/club ID: ");
        while (true) {
            System.out.println("\\n--- USER ---");
            System.out.println("1. View kits");
            System.out.println("2. Request booking");
            System.out.println("3. Cancel my booking");
            System.out.println("4. View my booking/cancellation logs");
            System.out.println("5. Back");
            try {
                switch (readInt("Choose: ")) {
                    case 1 -> showKits(service);
                    case 2 -> {
                        showKits(service);
                        Booking b = service.requestBooking(user, readInt("Kit ID: "),
                                readDate("Start (yyyy-MM-dd HH:mm): "),
                                readDate("End (yyyy-MM-dd HH:mm): "));
                        showBooking(b);
                    }
                    case 3 -> showBooking(service.cancelByUser(readInt("Booking ID: "), user));
                    case 4 -> service.userBookingAndCancellationLogs(user).forEach(this::showBooking);
                    case 5 -> { return; }
                    default -> System.out.println("Invalid option.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Unable to complete action: " + e.getMessage());
            }
        }
    }

    private void adminMenu(BookingService service) {
        while (true) {
            System.out.println("\\n--- ADMIN ---");
            System.out.println("1. View kit inventory/condition");
            System.out.println("2. View pending requests");
            System.out.println("3. Approve request");
            System.out.println("4. Reject request");
            System.out.println("5. Inspect/update one equipment item");
            System.out.println("6. Record returned kit");
            System.out.println("7. View all booking records");
            System.out.println("8. Back");
            try {
                switch (readInt("Choose: ")) {
                    case 1 -> showKits(service);
                    case 2 -> service.pendingBookings().forEach(this::showBooking);
                    case 3 -> showBooking(service.approve(readInt("Booking ID: ")));
                    case 4 -> showBooking(service.reject(readInt("Booking ID: ")));
                    case 5 -> inspect(service);
                    case 6 -> showBooking(service.completeReturn(readInt("Returned booking ID: ")));
                    case 7 -> service.allBookings().forEach(this::showBooking);
                    case 8 -> { return; }
                    default -> System.out.println("Invalid option.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Unable to complete action: " + e.getMessage());
            }
        }
    }

    private void inspect(BookingService service) {
        showKits(service);
        int kitId = readInt("Kit ID: ");
        EquipmentType type = EquipmentType.fromInput(readText("Equipment (Camera/Tripod/Microphone): "));
        System.out.println("1. Working  2. Faulty  3. Missing");
        EquipmentCondition condition = switch (readInt("Condition: ")) {
            case 1 -> EquipmentCondition.WORKING;
            case 2 -> EquipmentCondition.FAULTY;
            case 3 -> EquipmentCondition.MISSING;
            default -> throw new IllegalArgumentException("Choose condition 1, 2, or 3.");
        };
        service.inspectEquipment(kitId, type, condition);
        System.out.println("Inspection saved. Kit status: "
                + service.allKits().stream().filter(k -> k.id() == kitId)
                .findFirst().orElseThrow().status());
    }

    private void showKits(BookingService service) {
        for (Kit kit : service.allKits()) {
            System.out.printf("Kit %d | Camera=%s | Tripod=%s | Microphone=%s | %s%n",
                    kit.id(),
                    kit.equipment(EquipmentType.CAMERA).condition(),
                    kit.equipment(EquipmentType.TRIPOD).condition(),
                    kit.equipment(EquipmentType.MICROPHONE).condition(),
                    kit.status());
        }
    }

    private void showBooking(Booking b) {
        System.out.printf("Booking #%d | User=%s | Kit=%d | %s - %s | %s%n",
                b.id(), b.userId(), b.kitId(), b.start().format(FORMAT),
                b.end().format(FORMAT), b.status());
    }

    private int readInt(String prompt) {
        System.out.print(prompt);
        try { return Integer.parseInt(IN.nextLine().trim()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Enter a valid number."); }
    }

    private String readText(String prompt) {
        System.out.print(prompt);
        String value = IN.nextLine().trim();
        if (value.isBlank()) throw new IllegalArgumentException("Value cannot be empty.");
        return value;
    }

    private LocalDateTime readDate(String prompt) {
        try { return LocalDateTime.parse(readText(prompt), FORMAT); }
        catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Use date/time format yyyy-MM-dd HH:mm.");
        }
    }
}

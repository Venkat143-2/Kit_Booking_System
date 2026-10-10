# Advanced College Kit Booking System

A maintainable Java 17 console application designed around SOLID principles and small, replaceable components.

## Invariant: every kit has exactly three items

Each standard kit is created with exactly:
1. One camera
2. One tripod
3. One microphone

No equipment can be added or removed from a `Kit`. Every kit has its own condition and booking schedule. Run with a configurable number of kits:

```bash
mvn test
mvn package
java -cp target/classes com.college.kitbooking.presentation.ConsoleApplication 12
```

The final command starts the app with 12 independent kits. In VS Code, import the folder as a Maven project and run `ConsoleApplication`.

## Business rules

- Booking requests must be submitted at least two hours before the start.
- A kit is unavailable if any of its three items is faulty/missing or it is manually marked unavailable.
- A 15-minute turnaround buffer is enforced for the same kit.
- Different kits may be booked at the same time.
- Requests are pending until an admin approves them. Approval rechecks kit condition and slot conflicts.
- Admin can reject requests and inspect each equipment item.
- If inspection marks equipment faulty/missing, the kit becomes unavailable and future confirmed bookings for that kit are cancelled.
- Users see only their own `BOOKED` and `CANCELLED` logs in the user-history view.

## Design and SOLID

- **Single Responsibility Principle:** domain models, booking policy, application use cases, repositories, notifications, and console UI are separate.
- **Open/Closed Principle:** timing/conflict behavior can be changed by implementing `BookingPolicy`.
- **Liskov Substitution Principle:** in-memory repository and notifier implementations can be replaced with other implementations of their interfaces.
- **Interface Segregation Principle:** small interfaces (`BookingRepository`, `KitRepository`, `ClockProvider`, `BookingNotifier`) expose only required operations.
- **Dependency Inversion Principle:** `BookingService` depends on ports/interfaces rather than console output or storage implementations.
- **Repository pattern:** isolates storage.
- **Strategy pattern:** `BookingPolicy` encapsulates timing and slot conflict rules.
- **Factory pattern:** `KitFactory` creates valid standard kits.
- **Dependency injection:** service dependencies are supplied through its constructor.

## Important production note

This is an in-memory learning/reference implementation. Data is lost when the process exits. The console's user ID is not authentication. For a real college deployment, add a persistent database (e.g. PostgreSQL), authenticated student/admin accounts, authorization, an audit table, and transactional/locking protection around approval and inventory updates. A database-backed unique/overlap constraint or serialized transaction is required to guarantee no double-booking across multiple application instances.

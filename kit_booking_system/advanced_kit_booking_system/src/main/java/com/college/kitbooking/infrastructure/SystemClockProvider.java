package com.college.kitbooking.infrastructure;

import com.college.kitbooking.application.port.ClockProvider;
import java.time.LocalDateTime;

public final class SystemClockProvider implements ClockProvider {
    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}

package com.college.kitbooking.application.port;

import java.time.LocalDateTime;

@FunctionalInterface
public interface ClockProvider {
    LocalDateTime now();
}

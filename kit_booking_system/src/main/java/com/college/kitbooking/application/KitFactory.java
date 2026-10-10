package com.college.kitbooking.application;

import com.college.kitbooking.domain.Kit;

/** Factory centralizes the creation rule for a standard kit. */
public final class KitFactory {
    private KitFactory() {}

    public static Kit createStandardKit(int id) {
        return new Kit(id);
    }
}

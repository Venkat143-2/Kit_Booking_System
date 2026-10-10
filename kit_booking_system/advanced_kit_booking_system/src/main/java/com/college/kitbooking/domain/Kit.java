package com.college.kitbooking.domain;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * A kit always contains exactly one camera, one tripod, and one microphone.
 * Equipment cannot be added or removed, so the invariant is enforced by construction.
 */
public final class Kit {
    private final int id;
    private final Map<EquipmentType, Equipment> equipment =
            new EnumMap<>(EquipmentType.class);
    private boolean manuallyUnavailable;

    public Kit(int id) {
        if (id <= 0) throw new IllegalArgumentException("Kit ID must be positive.");
        this.id = id;
        for (EquipmentType type : EquipmentType.values()) {
            equipment.put(type, new Equipment(type));
        }
    }

    public int id() {
        return id;
    }

    public Equipment equipment(EquipmentType type) {
        return equipment.get(Objects.requireNonNull(type));
    }

    public Map<EquipmentType, Equipment> equipmentSnapshot() {
        return Collections.unmodifiableMap(new EnumMap<>(equipment));
    }

    public boolean isOperational() {
        return !manuallyUnavailable
                && equipment.values().stream().allMatch(Equipment::isUsable);
    }

    public KitStatus status() {
        return isOperational() ? KitStatus.AVAILABLE : KitStatus.UNAVAILABLE;
    }

    public void inspect(EquipmentType type, EquipmentCondition condition) {
        equipment(type).inspect(condition);
    }

    public void setManuallyUnavailable(boolean unavailable) {
        this.manuallyUnavailable = unavailable;
    }
}

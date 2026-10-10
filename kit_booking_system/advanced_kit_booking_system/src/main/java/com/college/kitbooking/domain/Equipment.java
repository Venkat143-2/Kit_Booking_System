package com.college.kitbooking.domain;

import java.util.Objects;

public final class Equipment {
    private final EquipmentType type;
    private EquipmentCondition condition;

    public Equipment(EquipmentType type) {
        this.type = Objects.requireNonNull(type);
        this.condition = EquipmentCondition.WORKING;
    }

    public EquipmentType type() {
        return type;
    }

    public EquipmentCondition condition() {
        return condition;
    }

    public boolean isUsable() {
        return condition == EquipmentCondition.WORKING;
    }

    public void inspect(EquipmentCondition newCondition) {
        this.condition = Objects.requireNonNull(newCondition);
    }
}

package com.college.kitbooking.domain;

public enum EquipmentType {
    CAMERA("Camera"),
    TRIPOD("Tripod"),
    MICROPHONE("Microphone");

    private final String displayName;

    EquipmentType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static EquipmentType fromInput(String value) {
        if (value == null) throw new IllegalArgumentException("Equipment name is required.");
        return switch (value.trim().toLowerCase()) {
            case "camera" -> CAMERA;
            case "tripod", "trypoid" -> TRIPOD;
            case "microphone", "mic", "mike" -> MICROPHONE;
            default -> throw new IllegalArgumentException(
                    "Equipment must be Camera, Tripod, or Microphone.");
        };
    }
}

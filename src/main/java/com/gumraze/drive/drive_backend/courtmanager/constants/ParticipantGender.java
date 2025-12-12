package com.gumraze.drive.drive_backend.courtmanager.constants;

public enum ParticipantGender {
    MALE("남성"),
    FEMALE("여성"),
    MIXED("혼성");

    private final String displayName;

    ParticipantGender(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ParticipantGender from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("gender must not be null");
        }
        String normalized = value.trim().toUpperCase();
        return switch (normalized) {
            case "M", "MALE", "남", "남성" -> MALE;
            case "F", "FEMALE", "여", "여성" -> FEMALE;
            case "MIXED", "혼성" -> MIXED;
            default -> throw new IllegalArgumentException("Unknown participant gender: " + value);
        };
    }
}

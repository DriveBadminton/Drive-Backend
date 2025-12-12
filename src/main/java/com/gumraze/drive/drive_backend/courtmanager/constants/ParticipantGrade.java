package com.gumraze.drive.drive_backend.courtmanager.constants;

import java.util.Arrays;

public enum ParticipantGrade {
    BEGINNER("초심"),
    D("D급"),
    C("C급"),
    B("B급"),
    A("A급"),
    S("S급");

    private final String displayName;

    ParticipantGrade(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ParticipantGrade from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("grade must not be null");
        }
        return Arrays.stream(values())
            .filter(grade -> grade.name().equalsIgnoreCase(value) || grade.displayName.equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown participant grade: " + value));
    }
}

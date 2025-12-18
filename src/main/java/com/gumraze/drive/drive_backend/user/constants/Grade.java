package com.gumraze.drive.drive_backend.user.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum Grade {
    ROOKIE("초심"),
    D("D급"),
    C("C급"),
    B("B급"),
    A("A급"),
    S("S급"),
    SS("SS급");

    private final String displayName;

    Grade(String displayName) {
        this.displayName = displayName;
    }

    @JsonCreator
    public static Grade from(String value) {
        return Arrays.stream(values())
                .filter(grade -> grade.name().equalsIgnoreCase(value) || grade.displayName.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown grade: " + value));
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
}

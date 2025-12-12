package com.gumraze.drive.drive_backend.courtmanager.constants;

public enum TournamentVisibility {
    PUBLIC,
    PRIVATE;

    public static TournamentVisibility of(String value) {
        if (value == null) {
            throw new IllegalArgumentException("visibility must not be null");
        }
        return switch (value.toUpperCase()) {
            case "PUBLIC", "공개", "PUBLIC_TOURNAMENT" -> PUBLIC;
            case "PRIVATE", "비공개", "PRIVATE_TOURNAMENT" -> PRIVATE;
            default -> throw new IllegalArgumentException("Unknown tournament visibility: " + value);
        };
    }
}

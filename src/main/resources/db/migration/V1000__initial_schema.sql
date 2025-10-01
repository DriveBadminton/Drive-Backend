-- Flyway migration: initial schema for Drive backend core entities
-- Generated on 2025-10-02

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    role VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    UNIQUE KEY uq_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS players (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    full_name VARCHAR(150) NOT NULL,
    birth_date DATE NULL,
    gender VARCHAR(16) NULL,
    dominant_hand VARCHAR(16) NULL,
    region VARCHAR(100) NULL,
    club VARCHAR(150) NULL,
    external_id VARCHAR(80) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    UNIQUE KEY uq_players_name_birth (full_name, birth_date),
    KEY idx_players_external_id (external_id),
    CONSTRAINT fk_players_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS teams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    team_type VARCHAR(32) NOT NULL DEFAULT 'DOUBLE',
    region VARCHAR(100) NULL,
    club VARCHAR(150) NULL,
    level VARCHAR(32) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    UNIQUE KEY uq_teams_name_type (name, team_type, level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS team_players (
    team_id BIGINT NOT NULL,
    player_id BIGINT NOT NULL,
    role VARCHAR(32) NOT NULL DEFAULT 'PLAYER',
    joined_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (team_id, player_id),
    CONSTRAINT fk_team_players_team FOREIGN KEY (team_id) REFERENCES teams (id) ON DELETE CASCADE,
    CONSTRAINT fk_team_players_player FOREIGN KEY (player_id) REFERENCES players (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS competitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    start_date DATE NOT NULL,
    end_date DATE NULL,
    registration_open_at DATETIME NULL,
    registration_close_at DATETIME NULL,
    province VARCHAR(100) NULL,
    city VARCHAR(100) NULL,
    venue VARCHAR(255) NULL,
    organizer VARCHAR(255) NULL,
    homepage_url VARCHAR(255) NULL,
    level VARCHAR(32) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PLANNED',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    KEY idx_competitions_dates (start_date, end_date),
    KEY idx_competitions_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS competition_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    competition_id BIGINT NOT NULL,
    event_code VARCHAR(64) NOT NULL,
    event_name VARCHAR(255) NOT NULL,
    gender_category VARCHAR(16) NOT NULL,
    age_group VARCHAR(64) NULL,
    skill_level VARCHAR(32) NULL,
    match_format VARCHAR(32) NULL,
    draw_size INT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    UNIQUE KEY uq_events_competition_code (competition_id, event_code),
    CONSTRAINT fk_events_competition FOREIGN KEY (competition_id) REFERENCES competitions (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS matches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    competition_event_id BIGINT NOT NULL,
    round_name VARCHAR(64) NULL,
    match_number INT NULL,
    court VARCHAR(64) NULL,
    scheduled_at DATETIME NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'SCHEDULED',
    winner_team_id BIGINT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    UNIQUE KEY uq_matches_event_number (competition_event_id, match_number),
    KEY idx_matches_status (status),
    CONSTRAINT fk_matches_event FOREIGN KEY (competition_event_id) REFERENCES competition_events (id) ON DELETE CASCADE,
    CONSTRAINT fk_matches_winner FOREIGN KEY (winner_team_id) REFERENCES teams (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS match_participants (
    match_id BIGINT NOT NULL,
    team_id BIGINT NOT NULL,
    seed INT NULL,
    score VARCHAR(64) NULL,
    result VARCHAR(16) NULL,
    PRIMARY KEY (match_id, team_id),
    CONSTRAINT fk_match_participants_match FOREIGN KEY (match_id) REFERENCES matches (id) ON DELETE CASCADE,
    CONSTRAINT fk_match_participants_team FOREIGN KEY (team_id) REFERENCES teams (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS match_players (
    match_id BIGINT NOT NULL,
    player_id BIGINT NOT NULL,
    team_id BIGINT NULL,
    position VARCHAR(32) NULL,
    score_detail VARCHAR(64) NULL,
    PRIMARY KEY (match_id, player_id),
    CONSTRAINT fk_match_players_match FOREIGN KEY (match_id) REFERENCES matches (id) ON DELETE CASCADE,
    CONSTRAINT fk_match_players_player FOREIGN KEY (player_id) REFERENCES players (id) ON DELETE CASCADE,
    CONSTRAINT fk_match_players_team FOREIGN KEY (team_id) REFERENCES teams (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS player_rankings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_id BIGINT NOT NULL,
    ranking_type VARCHAR(32) NOT NULL,
    points INT NOT NULL DEFAULT 0,
    calculated_at DATE NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    UNIQUE KEY uq_rankings_player_type_date (player_id, ranking_type, calculated_at),
    CONSTRAINT fk_rankings_player FOREIGN KEY (player_id) REFERENCES players (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_name VARCHAR(120) NOT NULL,
    entity_id VARCHAR(100) NOT NULL,
    action VARCHAR(32) NOT NULL,
    actor_id BIGINT NULL,
    payload JSON NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    KEY idx_audit_entity (entity_name, entity_id),
    CONSTRAINT fk_audit_actor FOREIGN KEY (actor_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

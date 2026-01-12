ALTER TABLE game_participants RENAME COLUMN game_id TO freegame_id;
ALTER TABLE game_participants
    RENAME CONSTRAINT fk_game_participants_game TO fk_game_participants_freegame;
ALTER TABLE game_participants
    RENAME CONSTRAINT uq_game_participants_game_user TO uq_game_participants_freegame_user;

ALTER TABLE game_managers RENAME COLUMN game_id TO freegame_id;
ALTER TABLE game_managers
    RENAME CONSTRAINT fk_game_managers_game TO fk_game_managers_freegame;
ALTER TABLE game_managers
    RENAME CONSTRAINT uq_game_managers_game_user TO uq_game_managers_freegame_user;

ALTER TABLE free_game_settings RENAME COLUMN game_id TO freegame_id;
ALTER TABLE free_game_settings
    RENAME CONSTRAINT fk_free_game_settings_game TO fk_free_game_settings_freegame;
ALTER TABLE free_game_settings
    RENAME CONSTRAINT uq_free_game_settings_game TO uq_free_game_settings_freegame;

ALTER TABLE free_game_round RENAME COLUMN game_id TO freegame_id;
ALTER TABLE free_game_round
    RENAME CONSTRAINT fk_free_game_round_game TO fk_free_game_round_freegame;
ALTER TABLE free_game_round
    RENAME CONSTRAINT uq_free_game_round_game_round TO uq_free_game_round_freegame_round;

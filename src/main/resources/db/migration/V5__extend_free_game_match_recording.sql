UPDATE free_games
SET match_record_mode = 'WINNER_ONLY'
WHERE match_record_mode = 'RESULT';

ALTER TABLE free_game_match
    ADD COLUMN team_a_score INTEGER,
    ADD COLUMN team_b_score INTEGER;

UPDATE free_game_match
SET match_status = 'NOT_STARTED'
WHERE match_status IS NULL;

UPDATE free_game_match
SET match_result = 'NULL'
WHERE match_result IS NULL;

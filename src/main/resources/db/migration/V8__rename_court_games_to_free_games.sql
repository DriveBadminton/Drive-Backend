ALTER TABLE court_games RENAME TO free_games;

ALTER TABLE free_games
    RENAME CONSTRAINT fk_court_games_organizer TO fk_free_games_organizer;

ALTER TABLE free_game_match
    RENAME COLUMN winner_team TO match_result;

UPDATE free_game_match
SET match_result = CASE match_result
    WHEN 'TEAM_A' THEN 'TEAM_A_WIN'
    WHEN 'TEAM_B' THEN 'TEAM_B_WIN'
    ELSE match_result
END;

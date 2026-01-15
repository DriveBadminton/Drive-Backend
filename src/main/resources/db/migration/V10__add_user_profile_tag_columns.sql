ALTER TABLE user_profile
    ADD COLUMN tag VARCHAR(4),
    ADD COLUMN tag_changed_at TIMESTAMP;

ALTER TABLE user_profile
    ADD CONSTRAINT uq_user_profile_nickname_tag UNIQUE (nickname, tag);
ALTER TABLE user_auth
    ADD COLUMN thumbnail_image_url VARCHAR(500);

ALTER TABLE user_auth
    ADD COLUMN gender VARCHAR(20);

ALTER TABLE user_auth
    ADD COLUMN age_range VARCHAR(20);

ALTER TABLE user_auth
    ADD COLUMN birthday VARCHAR(20);

ALTER TABLE user_auth
    ADD COLUMN is_email_verified BOOLEAN;

ALTER TABLE user_auth
    ADD COLUMN is_phone_number_verified BOOLEAN;

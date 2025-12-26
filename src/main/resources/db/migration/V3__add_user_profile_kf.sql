ALTER TABLE user_profile
ADD CONSTRAINT fk_user_profile_user
FOREIGN KEY (id) REFERENCES users(id);
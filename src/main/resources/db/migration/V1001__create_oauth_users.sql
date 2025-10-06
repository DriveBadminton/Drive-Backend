CREATE TABLE IF NOT EXISTS oauth_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    oauth_provider VARCHAR(20) NOT NULL,
    oauth_id VARCHAR(100) NOT NULL,
    email VARCHAR(320) NULL,
    nickname VARCHAR(100) NULL,
    profile_image_url VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    UNIQUE KEY uq_oauth_users_provider_id (oauth_provider, oauth_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

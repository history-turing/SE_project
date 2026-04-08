CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(64) NOT NULL,
    tagline VARCHAR(255) NOT NULL,
    college VARCHAR(64) NOT NULL,
    grade_year VARCHAR(32) NOT NULL,
    bio VARCHAR(512) NOT NULL,
    avatar_url VARCHAR(512) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_badges (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    badge_name VARCHAR(64) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_user_badges_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS user_profile_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    stat_label VARCHAR(64) NOT NULL,
    stat_value VARCHAR(64) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_user_stats_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS topics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    topic_code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(64) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    heat_text VARCHAR(64) NOT NULL,
    destination_type VARCHAR(16) NOT NULL,
    accent_tone VARCHAR(16) NOT NULL,
    emoji VARCHAR(16) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS topic_tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    topic_id BIGINT NOT NULL,
    tag_name VARCHAR(64) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_topic_tags_topic FOREIGN KEY (topic_id) REFERENCES topics (id)
);

CREATE TABLE IF NOT EXISTS topic_rankings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ranking_code VARCHAR(64) NOT NULL UNIQUE,
    label VARCHAR(64) NOT NULL,
    heat_text VARCHAR(64) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS notices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    notice_code VARCHAR(64) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    meta VARCHAR(64) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS posts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_code VARCHAR(64) NOT NULL UNIQUE,
    creator_user_id BIGINT NOT NULL,
    title VARCHAR(255) NULL,
    content TEXT NOT NULL,
    author_name VARCHAR(64) NOT NULL,
    author_handle VARCHAR(128) NOT NULL,
    topic_name VARCHAR(64) NOT NULL,
    audience_type VARCHAR(16) NOT NULL,
    display_time VARCHAR(64) NOT NULL,
    like_count INT NOT NULL DEFAULT 0,
    comment_count INT NOT NULL DEFAULT 0,
    save_count INT NOT NULL DEFAULT 0,
    accent_tone VARCHAR(16) NOT NULL,
    badge VARCHAR(64) NULL,
    image_url VARCHAR(512) NULL,
    anonymous_flag TINYINT(1) NOT NULL DEFAULT 0,
    location VARCHAR(64) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_posts_user FOREIGN KEY (creator_user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS post_interactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    liked TINYINT(1) NOT NULL DEFAULT 0,
    saved TINYINT(1) NOT NULL DEFAULT 0,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_post_interactions_user_post (user_id, post_id),
    CONSTRAINT fk_post_interactions_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_post_interactions_post FOREIGN KEY (post_id) REFERENCES posts (id)
);

CREATE TABLE IF NOT EXISTS alumni_stories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    story_code VARCHAR(64) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    meta VARCHAR(255) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS alumni_contacts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contact_code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(64) NOT NULL,
    meta VARCHAR(128) NOT NULL,
    focus VARCHAR(128) NOT NULL,
    avatar_url VARCHAR(512) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS user_follow_contacts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    contact_id BIGINT NOT NULL,
    followed TINYINT(1) NOT NULL DEFAULT 0,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_follow_contact (user_id, contact_id),
    CONSTRAINT fk_follow_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_follow_contact FOREIGN KEY (contact_id) REFERENCES alumni_contacts (id)
);

CREATE TABLE IF NOT EXISTS conversations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_code VARCHAR(64) NOT NULL UNIQUE,
    owner_user_id BIGINT NOT NULL,
    peer_name VARCHAR(64) NOT NULL,
    peer_subtitle VARCHAR(128) NOT NULL,
    peer_avatar_url VARCHAR(512) NOT NULL,
    last_message VARCHAR(512) NOT NULL,
    display_time VARCHAR(64) NOT NULL,
    unread_count INT NOT NULL DEFAULT 0,
    sort_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_conversations_user FOREIGN KEY (owner_user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    message_code VARCHAR(64) NOT NULL UNIQUE,
    conversation_id BIGINT NOT NULL,
    sender_type VARCHAR(16) NOT NULL,
    text_content VARCHAR(1000) NOT NULL,
    display_time VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_messages_conversation FOREIGN KEY (conversation_id) REFERENCES conversations (id)
);

CREATE TABLE IF NOT EXISTS user_credentials (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    email VARCHAR(128) NOT NULL UNIQUE,
    username VARCHAR(32) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email_verified_at DATETIME NOT NULL,
    last_login_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_credentials_user FOREIGN KEY (user_id) REFERENCES users (id)
);

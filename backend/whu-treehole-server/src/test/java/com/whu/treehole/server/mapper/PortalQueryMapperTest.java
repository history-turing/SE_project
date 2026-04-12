package com.whu.treehole.server.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.whu.treehole.infra.mapper.PortalQueryMapper;
import com.whu.treehole.infra.model.PostData;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

@MybatisTest
@MapperScan("com.whu.treehole.infra.mapper")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.url=jdbc:h2:mem:portal-query-test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=never",
        "mybatis.mapper-locations=classpath*:mapper/*.xml"
})
@Sql(statements = {
        "DROP TABLE IF EXISTS post_interactions",
        "DROP TABLE IF EXISTS posts",
        "DROP TABLE IF EXISTS users",
        "CREATE TABLE users (" +
                "id BIGINT PRIMARY KEY," +
                "user_code VARCHAR(64) NOT NULL," +
                "name VARCHAR(64) NOT NULL" +
                ")",
        "CREATE TABLE posts (" +
                "id BIGINT PRIMARY KEY," +
                "post_code VARCHAR(64) NOT NULL," +
                "creator_user_id BIGINT NOT NULL," +
                "title VARCHAR(128)," +
                "content TEXT NOT NULL," +
                "author_name VARCHAR(64) NOT NULL," +
                "author_handle VARCHAR(128)," +
                "topic_name VARCHAR(64) NOT NULL," +
                "audience_type VARCHAR(32) NOT NULL," +
                "display_time VARCHAR(64)," +
                "like_count INT NOT NULL DEFAULT 0," +
                "comment_count INT NOT NULL DEFAULT 0," +
                "save_count INT NOT NULL DEFAULT 0," +
                "accent_tone VARCHAR(32)," +
                "badge VARCHAR(64)," +
                "image_url VARCHAR(255)," +
                "anonymous_flag TINYINT NOT NULL DEFAULT 0," +
                "location VARCHAR(128)," +
                "deleted_flag TINYINT NOT NULL DEFAULT 0," +
                "created_at TIMESTAMP NOT NULL" +
                ")",
        "CREATE TABLE post_interactions (" +
                "id BIGINT PRIMARY KEY," +
                "post_id BIGINT NOT NULL," +
                "user_id BIGINT NOT NULL," +
                "liked TINYINT NOT NULL DEFAULT 0," +
                "saved TINYINT NOT NULL DEFAULT 0" +
                ")",
        "INSERT INTO users (id, user_code, name) VALUES (7, 'user-7', 'xiewei')",
        "INSERT INTO posts (" +
                "id, post_code, creator_user_id, title, content, author_name, author_handle, topic_name, audience_type," +
                "display_time, like_count, comment_count, save_count, accent_tone, anonymous_flag, deleted_flag, created_at" +
                ") VALUES (" +
                "11, 'post-11', 7, '首页帖子', '这是一条真实帖子', 'xiewei', '信管院 · 2022', '校园日常', 'HOME'," +
                "'刚刚', 5, 2, 1, 'rose', 0, 0, TIMESTAMP '2026-04-12 10:00:00'" +
                ")",
        "INSERT INTO post_interactions (id, post_id, user_id, liked, saved) VALUES (21, 11, 7, 1, 1)"
})
class PortalQueryMapperTest {

    @Autowired
    private PortalQueryMapper portalQueryMapper;

    @Test
    void shouldLoadPostsWithAuthorUserCodeForHomePage() {
        List<PostData> posts = portalQueryMapper.selectPosts("HOME", null, null, 7L);

        assertFalse(posts.isEmpty());
        assertEquals("post-11", posts.get(0).getPostCode());
        assertEquals("user-7", posts.get(0).getAuthorUserCode());
        assertEquals(LocalDateTime.of(2026, 4, 12, 10, 0), posts.get(0).getCreatedAt());
    }
}

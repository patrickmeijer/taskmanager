package com.patrick.taskmanager.support;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.jdbc.JdbcTestUtils;

@Component
public class DatabaseCleaner {

    private final JdbcTemplate jdbcTemplate;
    public DatabaseCleaner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void clean() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "refresh_tokens", "tasks", "users");
    }
}

package com.ecommerce.ecommerce.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class IntegrationTestSupport {

    private static final String[] TABLES = {
            "cart_items",
            "order_items",
            "transactions",
            "orders",
            "carts",
            "products",
            "categories",
            "users"
    };

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        for (String table : TABLES) {
            jdbcTemplate.execute("TRUNCATE TABLE " + table);
        }
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }
}

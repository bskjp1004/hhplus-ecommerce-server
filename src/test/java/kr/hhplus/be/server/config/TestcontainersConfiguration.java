package kr.hhplus.be.server.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;

public abstract class TestcontainersConfiguration {
    
    private static final TestContainerHolder containerHolder = TestContainerHolder.getInstance();
    
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        MySQLContainer<?> mysqlContainer = containerHolder.getMysqlContainer();
        GenericContainer<?> redisContainer = containerHolder.getRedisContainer();
        
        // MySQL 설정
        registry.add("spring.datasource.url", () -> 
            String.format("jdbc:mysql://%s:%d/hhplus?characterEncoding=UTF-8&serverTimezone=UTC&autoReconnect=true&useSSL=false",
                mysqlContainer.getHost(),
                mysqlContainer.getFirstMappedPort()));
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        
        // HikariCP 설정
        registry.add("spring.datasource.hikari.connection-timeout", () -> "30000");
        registry.add("spring.datasource.hikari.idle-timeout", () -> "600000");
        registry.add("spring.datasource.hikari.max-lifetime", () -> "1800000");
        registry.add("spring.datasource.hikari.minimum-idle", () -> "2");
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "10");
        registry.add("spring.datasource.hikari.connection-test-query", () -> "SELECT 1");
        registry.add("spring.datasource.hikari.validation-timeout", () -> "5000");
        
        // Redis 설정
        registry.add("spring.redis.host", redisContainer::getHost);
        registry.add("spring.redis.port", redisContainer::getFirstMappedPort);
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", redisContainer::getFirstMappedPort);
    }
}
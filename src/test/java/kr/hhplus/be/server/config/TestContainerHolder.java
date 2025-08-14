package kr.hhplus.be.server.config;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

public class TestContainerHolder {
    
    private static TestContainerHolder instance;
    private final MySQLContainer<?> mysqlContainer;
    private final GenericContainer<?> redisContainer;
    
    private TestContainerHolder() {
        // MySQL Container 설정
        mysqlContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
            .withInitScript("hhplus-ecommerce-table.sql")
            .withDatabaseName("hhplus")
            .withUsername("test")
            .withPassword("test")
            .withCommand("--max_connections=200", "--wait_timeout=28800", "--interactive_timeout=28800")
            .withReuse(true);
        
        // Redis Container 설정
        redisContainer = new GenericContainer<>(DockerImageName.parse("redis:latest"))
            .withExposedPorts(6379)
            .withCommand("redis-server --appendonly no")
            .withReuse(true);
        
        // 컨테이너 시작
        mysqlContainer.start();
        redisContainer.start();
        
        // JVM 종료 시 컨테이너 정리
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (mysqlContainer.isRunning()) {
                mysqlContainer.stop();
            }
            if (redisContainer.isRunning()) {
                redisContainer.stop();
            }
        }));
    }
    
    public static synchronized TestContainerHolder getInstance() {
        if (instance == null) {
            instance = new TestContainerHolder();
        }
        return instance;
    }
    
    public MySQLContainer<?> getMysqlContainer() {
        return mysqlContainer;
    }
    
    public GenericContainer<?> getRedisContainer() {
        return redisContainer;
    }
}
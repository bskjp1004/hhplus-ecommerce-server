package kr.hhplus.be.server;

import kr.hhplus.be.server.config.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
public abstract class BaseConcurrencyTest extends TestcontainersConfiguration {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanUp() {
        // 테스트 데이터 정리 - 순서 중요! (외래키 제약조건 고려)
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        
        // Order 관련 테이블
        jdbcTemplate.execute("TRUNCATE TABLE order_item");
        jdbcTemplate.execute("TRUNCATE TABLE `order`");
        
        // User 관련 테이블
        jdbcTemplate.execute("TRUNCATE TABLE balance_history");
        jdbcTemplate.execute("TRUNCATE TABLE user");
        
        // Product 관련 테이블
        jdbcTemplate.execute("TRUNCATE TABLE product");
        
        // Coupon 관련 테이블
        jdbcTemplate.execute("TRUNCATE TABLE user_coupon");
        jdbcTemplate.execute("TRUNCATE TABLE coupon_policy");
        
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }
}
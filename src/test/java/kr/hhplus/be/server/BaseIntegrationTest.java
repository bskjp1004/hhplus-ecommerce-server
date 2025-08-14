package kr.hhplus.be.server;

import kr.hhplus.be.server.config.TestcontainersConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public abstract class BaseIntegrationTest extends TestcontainersConfiguration {
}
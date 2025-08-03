package kr.hhplus.be.server;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { ServerApplication.class, TestcontainersConfiguration.class })
@SpringBootTest
@Transactional
public abstract class BaseIntegrationTest {
}
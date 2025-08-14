package kr.hhplus.be.server.user.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import kr.hhplus.be.server.ServerApplication;
import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.user.application.UserService;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.user.domain.port.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { ServerApplication.class, TestcontainersConfiguration.class })
@SpringBootTest
@DisplayName("잔액 충전 동시성 제어 테스트")
public class UserConcurrencyControllTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    @DisplayName("여러 스레드에서 잔액 충전 시 성공한다")
    void 여러_스레드에서_잔액_충전_시_성공한다() throws InterruptedException {
        // given
        BigDecimal 충전잔액 = BigDecimal.valueOf(10_000L);
        User user = userRepository.insertOrUpdate(User.empty());

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();

                    transactionTemplate.execute(status -> {
                        try {
                            userService.chargeBalance(user.getId(), 충전잔액);
                        } catch (Exception e) {
                        }
                        return null;
                    });

                } catch (Exception e){
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        // then
        assertAll(
            () -> {
                transactionTemplate.execute(status -> {
                    User finalUser = userRepository.findById(user.getId()).orElseThrow();
                    assertThat(finalUser.getBalance()).isEqualByComparingTo((BigDecimal.valueOf(20_000L)));
                    return null;
                });

            }
        );
    }
}

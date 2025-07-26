package kr.hhplus.be.server.user.domain.port;

import java.util.Optional;
import kr.hhplus.be.server.user.domain.User;

public interface UserRepository {
    Optional<User> findById(long userId);
    User insertOrUpdate(User user);
}

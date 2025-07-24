package kr.hhplus.be.server.user.domain;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(long userId);
    User insertOrUpdate(User user);
}

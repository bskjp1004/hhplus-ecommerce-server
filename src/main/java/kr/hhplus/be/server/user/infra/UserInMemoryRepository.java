package kr.hhplus.be.server.user.infra;

import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.user.domain.UserRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class UserInMemoryRepository implements UserRepository {

    private final Map<Long, UserEntity> storage = new HashMap<>();

    @Override
    public Optional<User> findById(long userId) {
        return Optional.of(storage.get(userId).toDomain());
    }

    @Override
    public User insertOrUpdate(User user) {
        UserEntity userEntity = UserEntity.fromDomain(user);
        storage.put(user.getId(), userEntity);
        return storage.get(user.getId()).toDomain();
    }


}

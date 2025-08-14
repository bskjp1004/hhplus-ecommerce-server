package kr.hhplus.be.server.user.infra;

import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.user.domain.port.UserRepository;
import kr.hhplus.be.server.user.infra.entity.UserJpaEntity;
import kr.hhplus.be.server.user.infra.port.UserJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@AllArgsConstructor
public class UserJpaRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;

    @Override
    public Optional<User> findById(long userId) {
        return jpaRepository.findById(userId)
            .map(UserJpaEntity::toDomain);
    }

    @Override
    public User insertOrUpdate(User user) {
        UserJpaEntity entity = UserJpaEntity.fromDomain(user);
        UserJpaEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }
}
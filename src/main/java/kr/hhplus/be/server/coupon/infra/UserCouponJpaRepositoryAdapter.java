package kr.hhplus.be.server.coupon.infra;

import kr.hhplus.be.server.coupon.domain.UserCoupon;
import kr.hhplus.be.server.coupon.domain.port.UserCouponRepository;
import kr.hhplus.be.server.coupon.infra.entity.UserCouponJpaEntity;
import kr.hhplus.be.server.coupon.infra.port.UserCouponJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@AllArgsConstructor
public class UserCouponJpaRepositoryAdapter implements UserCouponRepository {

    private final UserCouponJpaRepository jpaRepository;

    @Override
    public Optional<UserCoupon> findById(long userCouponId) {
        return jpaRepository.findById(userCouponId)
            .map(UserCouponJpaEntity::toDomain);
    }

    @Override
    public UserCoupon insertOrUpdate(UserCoupon userCoupon) {
        UserCouponJpaEntity entity = UserCouponJpaEntity.fromDomain(userCoupon);
        UserCouponJpaEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }
}
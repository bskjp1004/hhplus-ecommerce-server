package kr.hhplus.be.server.coupon.infra;

import java.util.List;
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
    public Optional<UserCoupon> findById(long id) {
        return jpaRepository.findById(id)
            .map(UserCouponJpaEntity::toDomain);
    }

    @Override
    public UserCoupon insertOrUpdate(UserCoupon userCoupon) {
        UserCouponJpaEntity entity = UserCouponJpaEntity.fromDomain(userCoupon);
        UserCouponJpaEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public List<UserCoupon> findAllByCouponPolicyId(long couponPolicyId) {
        return jpaRepository.findAllByCouponPolicyId(couponPolicyId)
            .stream()
            .map(UserCouponJpaEntity::toDomain)
            .toList();
    }
}
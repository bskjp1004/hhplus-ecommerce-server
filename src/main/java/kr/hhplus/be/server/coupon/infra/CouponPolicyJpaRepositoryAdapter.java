package kr.hhplus.be.server.coupon.infra;

import kr.hhplus.be.server.coupon.domain.CouponPolicy;
import kr.hhplus.be.server.coupon.domain.port.CouponPolicyRepository;
import kr.hhplus.be.server.coupon.infra.entity.CouponPolicyJpaEntity;
import kr.hhplus.be.server.coupon.infra.port.CouponPolicyJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@AllArgsConstructor
public class CouponPolicyJpaRepositoryAdapter implements CouponPolicyRepository {

    private final CouponPolicyJpaRepository jpaRepository;

    @Override
    public Optional<CouponPolicy> findById(long id) {
        return jpaRepository.findOneById(id)
            .map(CouponPolicyJpaEntity::toDomain);
    }

    @Override
    public CouponPolicy insertOrUpdate(CouponPolicy couponPolicy) {
        CouponPolicyJpaEntity entity = CouponPolicyJpaEntity.fromDomain(couponPolicy);
        CouponPolicyJpaEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }
}
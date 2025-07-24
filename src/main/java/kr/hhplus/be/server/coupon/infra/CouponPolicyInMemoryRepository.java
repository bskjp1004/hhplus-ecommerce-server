package kr.hhplus.be.server.coupon.infra;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import kr.hhplus.be.server.coupon.domain.CouponPolicy;
import kr.hhplus.be.server.coupon.domain.port.CouponPolicyRepository;
import kr.hhplus.be.server.coupon.infra.entity.CouponPolicyEntity;
import org.springframework.stereotype.Repository;

@Repository
public class CouponPolicyInMemoryRepository implements CouponPolicyRepository {

    private final Map<Long, CouponPolicyEntity> storage = new HashMap<>();

    @Override
    public Optional<CouponPolicy> findById(long couponPolicyId) {
        return Optional.of(storage.get(couponPolicyId).toDomain());
    }

    @Override
    public CouponPolicy insertOrUpdate(CouponPolicy couponPolicy) {
        CouponPolicyEntity couponPolicyEntity = CouponPolicyEntity.fromDomain(couponPolicy);
        storage.put(couponPolicy.getId(), couponPolicyEntity);
        return storage.get(couponPolicy.getId()).toDomain();
    }
}

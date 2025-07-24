package kr.hhplus.be.server.coupon.infra;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import kr.hhplus.be.server.coupon.domain.UserCoupon;
import kr.hhplus.be.server.coupon.domain.port.UserCouponRepository;
import kr.hhplus.be.server.coupon.infra.entity.UserCouponEntity;
import org.springframework.stereotype.Repository;

@Repository
public class UserCouponInMemoryRepository implements UserCouponRepository {

    private final Map<Long, UserCouponEntity> storage = new HashMap<>();

    @Override
    public Optional<UserCoupon> findById(long userCouponId) {
        return Optional.of(storage.get(userCouponId).toDomain());
    }

    @Override
    public UserCoupon insertOrUpdate(UserCoupon userCoupon) {
        UserCouponEntity userCouponEntity = UserCouponEntity.fromDomain(userCoupon);
        storage.put(userCoupon.getId(), userCouponEntity);
        return storage.get(userCoupon.getId()).toDomain();
    }
}

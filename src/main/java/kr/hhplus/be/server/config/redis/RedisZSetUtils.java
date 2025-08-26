package kr.hhplus.be.server.config.redis;

import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import java.util.List;
import java.util.Set;

public class RedisZSetUtils {
    
    public static List<Long> convertToLongList(Set<TypedTuple<Object>> zsetResults) {
        if (zsetResults == null || zsetResults.isEmpty()) {
            return List.of();
        }
        
        return zsetResults.stream()
            .map(tuple -> {
                Object value = tuple.getValue();
                
                if (value == null) {
                    throw new IllegalStateException("Null value in Redis ZSET");
                }
                
                if (value instanceof Long) {
                    return (Long) value;
                } else if (value instanceof Integer) {
                    return ((Integer) value).longValue();
                } else if (value instanceof String) {
                    try {
                        return Long.parseLong((String) value);
                    } catch (NumberFormatException e) {
                        throw new IllegalStateException("Cannot parse value to Long: " + value);
                    }
                } else {
                    throw new IllegalStateException(
                        "Unexpected type in Redis ZSET: " + value.getClass().getName()
                    );
                }
            })
            .toList();
    }
}
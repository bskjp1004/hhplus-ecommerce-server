package kr.hhplus.be.server.config.lock;

import java.util.List;
import java.util.stream.Collectors;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.ReadMode;
import org.redisson.config.SingleServerConfig;
import org.redisson.config.SubscriptionMode;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    private final RedisProperties redisProperties;

    public RedissonConfig(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.setLockWatchdogTimeout(30_000);

        var cluster = redisProperties.getCluster();
        boolean isCluster = (cluster != null && cluster.getNodes() != null && !cluster.getNodes().isEmpty());

        if (isCluster) {
            ClusterServersConfig c = config.useClusterServers()
                .setPassword(redisProperties.getPassword())
                .setReadMode(ReadMode.MASTER)
                .setSubscriptionMode(SubscriptionMode.MASTER);

            String scheme = Boolean.TRUE.equals(redisProperties.getSsl()) ? "rediss://" : "redis://";
            List<String> nodesWithScheme = cluster.getNodes().stream()
                .map(n -> ensureScheme(n, scheme))
                .collect(Collectors.toList());
            c.addNodeAddress(nodesWithScheme.toArray(new String[0]));

        } else {
            String scheme = Boolean.TRUE.equals(redisProperties.getSsl()) ? "rediss://" : "redis://";
            SingleServerConfig s = config.useSingleServer()
                .setAddress(scheme + redisProperties.getHost() + ":" + redisProperties.getPort())
                .setPassword(redisProperties.getPassword())
                .setDatabase(redisProperties.getDatabase());
        }

        return Redisson.create(config);
    }

    private static String ensureScheme(String hostPort, String scheme) {
        String lower = hostPort.toLowerCase();
        if (lower.startsWith("redis://") || lower.startsWith("rediss://")) {
            return hostPort;
        }
        return scheme + hostPort;
    }
}
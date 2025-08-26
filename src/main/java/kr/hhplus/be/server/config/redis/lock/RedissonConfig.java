package kr.hhplus.be.server.config.redis.lock;

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

    private String scheme() {
        return redisProperties.getSsl().isEnabled()
            ? "rediss://"
            : "redis://";
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

            List<String> nodesWithScheme = cluster.getNodes().stream()
                .map(this::ensureScheme)
                .toList();
            c.addNodeAddress(nodesWithScheme.toArray(new String[0]));

        } else {
            SingleServerConfig s = config.useSingleServer()
                .setAddress(scheme() + redisProperties.getHost() + ":" + redisProperties.getPort())
                .setPassword(redisProperties.getPassword())
                .setDatabase(redisProperties.getDatabase());
        }

        return Redisson.create(config);
    }

    private String ensureScheme(String node) {
        String n = node.toLowerCase();
        return (n.startsWith("redis://") || n.startsWith("rediss://")) ? node : scheme() + node;
    }
}
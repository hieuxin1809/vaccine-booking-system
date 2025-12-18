package com.hieu.Booking_System.configuration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
public class RedisConfiguration implements CachingConfigurer {
    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private Integer port;

    /**
     * Tạo Redis Connection Factory sử dụng Lettuce
     * Lettuce là client mặc định của Spring Data Redis,
     * hỗ trợ async và thread-safe tốt hơn Jedis
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }
    /**
     * Cấu hình Cache Manager với Redis
     * - entryTtl: Thời gian sống của cache (1 giờ)
     * - Cache defaults áp dụng cho tất cả các cache không được cấu hình riêng
     */
    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1)) // TTL mặc định 1 giờ
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer()));
    }

    @Bean
    public CacheManager cacheManager() {
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Users - cache 2 giờ
        cacheConfigurations.put(
                "users", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(2)));

        // Vaccines - cache 6 giờ (ít thay đổi)
        cacheConfigurations.put(
                "vaccines", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(6)));

        // Locations - cache 6 giờ
        cacheConfigurations.put(
                "locations", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(6)));

        // Inventory - cache 5 phút (thay đổi thường xuyên)
        cacheConfigurations.put(
                "inventories", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(5)));

        // Roles & Permissions - cache 12 giờ (rất ít thay đổi)
        cacheConfigurations.put(
                "all_roles", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(12)));
        cacheConfigurations.put(
                "permissions", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(12)));

        return RedisCacheManager.builder(redisConnectionFactory())
                .cacheDefaults(cacheConfiguration())
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
    /**
     * Xử lý lỗi khi cache gặp vấn đề
     * Giúp ứng dụng không bị crash khi Redis down
     */
    @Override
    @Bean
    public CacheErrorHandler errorHandler() {
        return new CustomCacheErrorHandler();
    }
}

package com.hieu.Booking_System.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

@Slf4j
public class CustomCacheErrorHandler implements CacheErrorHandler {
    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        log.error("Lỗi khi lấy giá trị từ cache {} cho key {}: {}",
                cache.getName(), key, exception.getMessage());
        // Không throw exception, ứng dụng vẫn chạy bình thường
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        log.error("Lỗi khi lưu giá trị vào cache {} cho key {}: {}",
                cache.getName(), key, exception.getMessage());
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        log.error("Lỗi khi xóa giá trị khỏi cache {} cho key {}: {}",
                cache.getName(), key, exception.getMessage());
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        log.error("Lỗi khi xóa cache {}: {}", cache.getName(), exception.getMessage());
    }
}

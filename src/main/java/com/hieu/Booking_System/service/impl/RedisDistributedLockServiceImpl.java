package com.hieu.Booking_System.service.impl;

import com.hieu.Booking_System.service.DistributedLockService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class RedisDistributedLockServiceImpl implements DistributedLockService {
    static final int LOCK_TIME_IN_MINUTES = 3;
    static final String LOCK_VALUE = "1";
    RedisTemplate<String, String> redisTemplate;
    String generateLockKey(String lockName) {
        return String.format("lock:%s", lockName);
    }
    @Override
    public boolean acquireLock(String lockName) {
        String lockKey = generateLockKey(lockName);
        return redisTemplate.opsForValue()
                .setIfAbsent(lockKey,LOCK_VALUE , Duration.ofMinutes(LOCK_TIME_IN_MINUTES));
    }

    @Override
    public void releaseLock(String lockName) {
        String lockKey = generateLockKey(lockName);
        redisTemplate.delete(lockKey);
    }
}

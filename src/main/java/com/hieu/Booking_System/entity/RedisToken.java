package com.hieu.Booking_System.entity;

import java.util.concurrent.TimeUnit;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.Builder;
import lombok.Data;

@Data
@RedisHash("RedisHas")
@Builder
public class RedisToken {
    @Id
    private String jwtId;

    @TimeToLive(unit = TimeUnit.SECONDS)
    private Long expiredTime;
}

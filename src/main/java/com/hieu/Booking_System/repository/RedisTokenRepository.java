package com.hieu.Booking_System.repository;

import com.hieu.Booking_System.entity.RedisToken;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RedisTokenRepository extends CrudRepository<RedisToken, String> {
}

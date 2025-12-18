package com.hieu.Booking_System.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hieu.Booking_System.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    @Query("select u from UserEntity u where u.deletedAt is null")
    List<UserEntity> GetAllActiveUser();

    boolean existsByEmail(String email);

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByEmailStartingWith(String text);

    Optional<UserEntity> findByVerificationToken(String verificationToken);
}

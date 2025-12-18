package com.hieu.Booking_System.configuration;

import java.util.HashSet;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hieu.Booking_System.entity.RoleEntity;
import com.hieu.Booking_System.entity.UserEntity;
import com.hieu.Booking_System.enums.Role;
import com.hieu.Booking_System.exception.AppException;
import com.hieu.Booking_System.exception.ErrorCode;
import com.hieu.Booking_System.repository.RoleRepository;
import com.hieu.Booking_System.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository) {
        return args -> {
            if (userRepository.findByEmailStartingWith("admin").isEmpty()) {
                RoleEntity adminRole = roleRepository
                        .findByName(Role.ADMIN.name())
                        .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
                HashSet<RoleEntity> roles = new HashSet<>();
                roles.add(adminRole);
                //                  var roles = new HashSet<String>();
                //                  roles.add(Role.ADMIN.name());
                UserEntity user = UserEntity.builder()
                        .email("admin@gmail.com")
                        .name("admin")
                        .password(passwordEncoder.encode("admin12345"))
                        .roles(roles)
                        .build();
                userRepository.save(user);
                log.warn("admin user created");
            }
        };
    }
}

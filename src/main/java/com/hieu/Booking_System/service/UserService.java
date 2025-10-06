package com.hieu.Booking_System.service;

import com.hieu.Booking_System.entity.RoleEntity;
import com.hieu.Booking_System.entity.UserEntity;
import com.hieu.Booking_System.enums.Role;
import com.hieu.Booking_System.exception.AppException;
import com.hieu.Booking_System.exception.ErrorCode;
import com.hieu.Booking_System.mapper.UserMapper;
import com.hieu.Booking_System.model.request.UserCreateRequest;
import com.hieu.Booking_System.model.request.UserUpdateRequest;
import com.hieu.Booking_System.model.response.UserResponse;
import com.hieu.Booking_System.repository.RoleRepository;
import com.hieu.Booking_System.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserMapper userMapper;
    UserRepository userRepository;
    RoleRepository roleRepository;
    final PasswordEncoder passwordEncoder;
    public UserResponse createUser(UserCreateRequest user) {
        UserEntity userEntity = userMapper.toUserEntity(user);
        userEntity.setPassword(passwordEncoder.encode(user.getPassword()));
        if(userRepository.existsByEmail(user.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXIST);
        }
        HashSet<RoleEntity> roles = new HashSet<>();
        roleRepository.findByName(Role.CUSTOMER.name()).forEach(roles::add);
        userEntity.setRoles(roles);
        userRepository.save(userEntity);
        return userMapper.toUserResponse(userEntity);
    }
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        log.info("in method getAllUsers");
        List<UserEntity> userEntities = userRepository.GetAllActiveUser();
        return userEntities.stream().map(userMapper::toUserResponse).collect(Collectors.toList());
    }
    @PostAuthorize("returnObject.email == authentication.name")
    public UserResponse getUserById(Long id) {
        log.info("in method getUserById");
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() ->new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toUserResponse(userEntity);
    }
    public void deleteUserById(Long id) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() ->new AppException(ErrorCode.USER_NOT_FOUND));
        userEntity.setDeletedAt(LocalDateTime.now());
        userRepository.save(userEntity);
    }
    public UserResponse updateUserById(Long id, UserUpdateRequest userUpdateRequest) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() ->new AppException(ErrorCode.USER_NOT_FOUND));
        var roles = roleRepository.findAllById(userUpdateRequest.getRoles());
        userEntity.setRoles(new HashSet<>(roles));
        userEntity.setPassword(passwordEncoder.encode(userUpdateRequest.getPassword()));
        userEntity.setAddress(userUpdateRequest.getAddress());
        userEntity.setName(userUpdateRequest.getName());
        userEntity.setUpdatedAt(LocalDateTime.now());
        userRepository.save(userEntity);
        return userMapper.toUserResponse(userEntity);
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String email = context.getAuthentication().getName();
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() ->new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toUserResponse(userEntity);
    }

}

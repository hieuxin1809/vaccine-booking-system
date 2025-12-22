package com.hieu.Booking_System.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.hieu.Booking_System.aspect.LogTime;
import com.hieu.Booking_System.enums.UserStatus;
import com.hieu.Booking_System.model.response.PageResponse;
import com.hieu.Booking_System.specification.UserSpecification;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.hieu.Booking_System.entity.RoleEntity;
import com.hieu.Booking_System.entity.UserEntity;
import com.hieu.Booking_System.enums.Role;
import com.hieu.Booking_System.exception.AppException;
import com.hieu.Booking_System.exception.ErrorCode;
import com.hieu.Booking_System.mapper.UserMapper;
import com.hieu.Booking_System.model.request.ChangePasswordRequest;
import com.hieu.Booking_System.model.request.UserCreateRequest;
import com.hieu.Booking_System.model.request.UserUpdateRequest;
import com.hieu.Booking_System.model.response.UserResponse;
import com.hieu.Booking_System.repository.RoleRepository;
import com.hieu.Booking_System.repository.UserRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserMapper userMapper;
    UserRepository userRepository;
    RoleRepository roleRepository;
    final PasswordEncoder passwordEncoder;
    CloudinaryService cloudinaryService;

    public UserResponse createUser(UserCreateRequest user) {
        UserEntity userEntity = userMapper.toUserEntity(user);
        userEntity.setPassword(passwordEncoder.encode(user.getPassword()));
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXIST);
        }
        HashSet<RoleEntity> roles = new HashSet<>();
        RoleEntity customerRole = roleRepository
                .findByName(Role.CUSTOMER.name())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        roles.add(customerRole);
        userEntity.setRoles(roles);
        userRepository.save(userEntity);
        return userMapper.toUserResponse(userEntity);
    }
    @LogTime
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<UserResponse> getAllUsers(int page, int size) {
        log.info("in method getAllUsers with page: {} and size: {}", page, size);

        // Sort.by("createdAt").descending()
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());

        // 2. Gọi Repository
        Page<UserEntity> pageData = userRepository.findAllActiveUsers(pageable);

        // 3. Map Entity sang Response
        List<UserResponse> userList = pageData.getContent().stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());

        // 4. Trả về kết quả chuẩn
        return PageResponse.<UserResponse>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .data(userList)
                .build();
    }

    @Cacheable(value = "users", key = "#userId")
    @PostAuthorize("hasRole('ADMIN') or returnObject.email == authentication.name")
    public UserResponse getUserById(Long userId) {
        log.info("### ĐANG LẤY TỪ DATABASE... (Cache Miss) ###");
        log.info("in method getUserById");
        UserEntity userEntity =
                userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toUserResponse(userEntity);
    }

    @CacheEvict(value = "users", key = "#userId")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUserById(Long userId) {
        UserEntity userEntity =
                userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        userEntity.setDeletedAt(LocalDateTime.now());
        userRepository.save(userEntity);
    }

    @CachePut(value = "users", key = "#userId")
    @Caching(evict = {@CacheEvict(value = "users", key = "'myInfo:' + #root.target.getCurrentUserEmail()")})
    @PreAuthorize("hasRole('ADMIN') or @userService.getEmailById(#userId) == authentication.name")
    public UserResponse updateUserById(Long userId, UserUpdateRequest userUpdateRequest) {
        UserEntity userEntity =
                userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        var roles = roleRepository.findAllById(userUpdateRequest.getRoles());
        userEntity.setRoles(new HashSet<>(roles));
        userEntity.setPassword(passwordEncoder.encode(userUpdateRequest.getPassword()));
        userEntity.setAddress(userUpdateRequest.getAddress());
        userEntity.setName(userUpdateRequest.getName());
        userEntity.setUpdatedAt(LocalDateTime.now());
        userRepository.save(userEntity);
        return userMapper.toUserResponse(userEntity);
    }

    @Cacheable(value = "users", key = "'myInfo:' + #root.target.getEmailById()")
    @PreAuthorize("isAuthenticated()")
    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String email = context.getAuthentication().getName();
        UserEntity userEntity =
                userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toUserResponse(userEntity);
    }

    @CacheEvict(value = "users", key = "#userId")
    @PreAuthorize("hasRole('ADMIN') or @userService.getEmailById(#userId) == authentication.name")
    public void changePassword(Long userId, ChangePasswordRequest request) {
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_OLD_PASSWORD);
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("User {} changed password", userId);
    }

    @CachePut(value = "users", key = "#userId") // Dùng @CachePut để cập nhật cache
    @CircuitBreaker(name = "user-avatar-upload", fallbackMethod = "uploadAvatarFallback")
    @PreAuthorize("hasRole('ADMIN') or @userService.getEmailById(#userId) == authentication.name")
    public UserResponse uploadAvatar(Long userId, MultipartFile file) {
        // 1. Tìm user (giống code cũ)
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 2. Upload file (lệnh gọi mạng nguy hiểm - được Circuit Breaker bảo vệ)
        Map<String, Object> uploadResult = cloudinaryService.uploadFile(file, "avatars");
        String imageUrl = (String) uploadResult.get("secure_url");

        // 3. Cập nhật CSDL (lệnh gọi nội bộ)
        user.setAvatarUrl(imageUrl);
        userRepository.save(user);

        // 4. Trả về response (phải khớp với kiểu của @CachePut)
        log.info("User {} updated avatar successfully", userId);
        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<UserResponse> getUsersWithSpec(int page, int size, String keyword, UserStatus status, String roleName) {
        Sort sort = Sort.by("createAt").descending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        // 1. Khởi tạo Specification rỗng (hoặc điều kiện mặc định là chưa xóa)
        Specification<UserEntity> spec = (root, query, cb) ->
                cb.isNull(root.get("deletedAt")); // Luôn lọc user chưa bị xóa mềm

        // 2. Ghép điều kiện KEYWORD (nếu có)
        if (StringUtils.hasText(keyword)) {
            spec = spec.and(UserSpecification.hasKeyword(keyword));
        }

        // 3. Ghép điều kiện STATUS (nếu có)
        if (status != null) {
            spec = spec.and(UserSpecification.hasStatus(status));
        }

        // 4. Ghép điều kiện ROLE (nếu có)
//        if (StringUtils.hasText(roleName)) {
//            spec = spec.and(UserSpecification.hasRole(roleName));
//        }
        System.out.println(spec);
        // 5. Gọi Repository (Repository sẽ tự generate câu SQL tương ứng)
        Page<UserEntity> pageData = userRepository.findAll(spec, pageable);

        // 6. Map dữ liệu trả về (Giữ nguyên logic cũ)
        List<UserResponse> userList = pageData.getContent().stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());

        return PageResponse.<UserResponse>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .data(userList)
                .build();
    }

    public UserResponse uploadAvatarFallback(Long userId, MultipartFile file, Throwable throwable) {
        log.warn("Upload avatar FAILED for user {}: {}. Using fallback.", userId, throwable.getMessage());

        // Trả về thông tin user hiện tại, không thay đổi avatar
        // Quan trọng: Phải trả về UserResponse để khớp với kiểu của @CachePut
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toUserResponse(user);
    }

    public String getEmailById(Long userId) {
        return userRepository
                .findById(userId)
                .map(UserEntity::getEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
    // Helper method
    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}

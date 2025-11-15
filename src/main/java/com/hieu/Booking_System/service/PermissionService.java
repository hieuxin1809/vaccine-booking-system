package com.hieu.Booking_System.service;

import com.hieu.Booking_System.entity.PermissionEntity;
import com.hieu.Booking_System.exception.AppException;
import com.hieu.Booking_System.exception.ErrorCode;
import com.hieu.Booking_System.mapper.PermissionMapper;
import com.hieu.Booking_System.model.request.PermissionRequest;
import com.hieu.Booking_System.model.response.PermissionResponse;
import com.hieu.Booking_System.repository.PermissionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionService{
    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;
    @CacheEvict(value = "permissions", allEntries = true)
    public PermissionResponse createPermission(PermissionRequest request) {
        PermissionEntity permission = permissionMapper.toPermission(request);
        permission = permissionRepository.save(permission);
        return permissionMapper.toPermissionResponse(permission);
    }
    @Cacheable(value = "permissions")
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll().stream().map(permissionMapper::toPermissionResponse).collect(Collectors.toList());
    }
    @CacheEvict(value = "permissions", allEntries = true)
    public void deletePermission(String permissionId) {
        PermissionEntity permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_FOUND));
        permission.setDeletedAt(LocalDateTime.now());
        permissionRepository.save(permission);
    }
}

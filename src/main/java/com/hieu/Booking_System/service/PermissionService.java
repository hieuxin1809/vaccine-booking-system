package com.hieu.Booking_System.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.hieu.Booking_System.entity.PermissionEntity;
import com.hieu.Booking_System.entity.RoleEntity;
import com.hieu.Booking_System.exception.AppException;
import com.hieu.Booking_System.exception.ErrorCode;
import com.hieu.Booking_System.mapper.PermissionMapper;
import com.hieu.Booking_System.model.request.PermissionRequest;
import com.hieu.Booking_System.model.response.PermissionResponse;
import com.hieu.Booking_System.repository.PermissionRepository;
import com.hieu.Booking_System.repository.RoleRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionService {
    PermissionRepository permissionRepository;
    RoleRepository roleRepository;
    PermissionMapper permissionMapper;

    @CacheEvict(value = "permissions", allEntries = true)
    public PermissionResponse createPermission(PermissionRequest request) {
        PermissionEntity permission = permissionMapper.toPermission(request);
        permission = permissionRepository.save(permission);
        return permissionMapper.toPermissionResponse(permission);
    }

    @Cacheable(value = "permissions")
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(permissionMapper::toPermissionResponse)
                .collect(Collectors.toList());
    }

    public List<String> getPermissionsByRole(String roleName) {
        RoleEntity role = roleRepository
                .findByName(roleName.split("_")[1])
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        return role.getPermissions().stream().map(PermissionEntity::getName).toList();
    }

    @CacheEvict(value = "permissions", allEntries = true)
    public void deletePermission(String permissionId) {
        PermissionEntity permission = permissionRepository
                .findById(permissionId)
                .orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_FOUND));
        permission.setDeletedAt(LocalDateTime.now());
        permissionRepository.save(permission);
    }
}

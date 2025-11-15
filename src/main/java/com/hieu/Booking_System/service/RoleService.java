package com.hieu.Booking_System.service;

import com.hieu.Booking_System.entity.PermissionEntity;
import com.hieu.Booking_System.entity.RoleEntity;
import com.hieu.Booking_System.exception.AppException;
import com.hieu.Booking_System.exception.ErrorCode;
import com.hieu.Booking_System.mapper.RoleMapper;
import com.hieu.Booking_System.model.request.RoleRequest;
import com.hieu.Booking_System.model.response.RoleResponse;
import com.hieu.Booking_System.repository.PermissionRepository;
import com.hieu.Booking_System.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService{
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;
    RoleMapper roleMapper;
    @CacheEvict(value = "all_roles", allEntries = true)
    public RoleResponse createRole(RoleRequest roleRequest) {
        RoleEntity roleEntity = roleMapper.toRole(roleRequest);
        List<PermissionEntity> permissionEntities = permissionRepository.findAllById(roleRequest.getPermissions());
        roleEntity.setPermissions(new HashSet<>(permissionEntities));
        roleRepository.save(roleEntity);
        return roleMapper.toRoleResponse(roleEntity);
    }
    @Cacheable("all_roles")
    public List<RoleResponse> getAllRoles() {
        List<RoleEntity> roleEntity = roleRepository.getAll();
        return roleEntity.stream().map(roleMapper::toRoleResponse).collect(Collectors.toList());
    }
    @CacheEvict(value = "all_roles",allEntries = true)
    public void deleteRolebyName(String role) {
        RoleEntity roleEntity = roleRepository.findById(role)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        roleEntity.setDeletedAt(LocalDateTime.now());
        roleRepository.save(roleEntity);
    }
}

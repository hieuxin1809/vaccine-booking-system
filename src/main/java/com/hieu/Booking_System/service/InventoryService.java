package com.hieu.Booking_System.service;

import com.hieu.Booking_System.entity.InventoryEntity;
import com.hieu.Booking_System.entity.LocationEntity;
import com.hieu.Booking_System.entity.VaccineEntity;
import com.hieu.Booking_System.exception.AppException;
import com.hieu.Booking_System.exception.ErrorCode;
import com.hieu.Booking_System.mapper.InventoryMapper;
import com.hieu.Booking_System.model.request.InventoryRequest;
import com.hieu.Booking_System.model.response.InventoryResponse;
import com.hieu.Booking_System.repository.InventoryRepository;
import com.hieu.Booking_System.repository.LocationRepository;
import com.hieu.Booking_System.repository.VaccineRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class InventoryService {
    InventoryRepository inventoryRepository;
    LocationRepository locationRepository;
    VaccineRepository vaccineRepository;
    InventoryMapper inventoryMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public InventoryResponse createInventory(InventoryRequest inventoryRequest) {
        LocationEntity location = locationRepository.findById(inventoryRequest.getLocationId())
                .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_FOUND));
        VaccineEntity vaccine = vaccineRepository.findById(inventoryRequest.getVaccineId())
                .orElseThrow(() -> new AppException(ErrorCode.VACCINE_NOT_FOUND));
        Optional<InventoryEntity> existing = inventoryRepository
                .findByLocationIdAndVaccineId(inventoryRequest.getLocationId(), inventoryRequest.getVaccineId());
        if(existing.isPresent()) {
            throw new AppException(ErrorCode.INVENTORY_EXIST);
        }
        InventoryEntity entity = inventoryMapper.toInventoryEntity(inventoryRequest);
        entity.setLocation(location);
        entity.setVaccine(vaccine);
        inventoryRepository.save(entity);
        return inventoryMapper.toInventoryResponse(entity);
    }
    @PreAuthorize("hasRole('ADMIN')")
    public List<InventoryResponse> getAll() {
        return inventoryRepository.findAll().stream()
                .map(inventoryMapper::toInventoryResponse)
                .toList();
    }
    @Cacheable(value = "inventories", key = "#inventoryId")
    @PreAuthorize("hasRole('ADMIN')")
    public InventoryResponse getById(Long inventoryId) {
        InventoryEntity entity = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));
        return inventoryMapper.toInventoryResponse(entity);
    }
    @CachePut(value = "inventories", key = "#inventoryId")
    @PreAuthorize("hasRole('ADMIN')")
    public InventoryResponse updateInventory(Long inventoryId, InventoryRequest request) {
        InventoryEntity entity = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));

        LocationEntity location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_FOUND));
        VaccineEntity vaccine = vaccineRepository.findById(request.getVaccineId())
                .orElseThrow(() -> new AppException(ErrorCode.VACCINE_NOT_FOUND));

        entity.setQuantity(request.getQuantity());
        entity.setExpiryDate(request.getExpiryDate());
        entity.setMinStockLevel(request.getMinStockLevel());
        entity.setLocation(location);
        entity.setVaccine(vaccine);

        InventoryEntity updated = inventoryRepository.save(entity);
        return inventoryMapper.toInventoryResponse(updated);
    }

    // DELETE
    @CacheEvict(value = "inventories", key = "#inventoryId")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteInventory(Long inventoryId) {
        InventoryEntity inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));
        inventory.setDeletedAt(LocalDateTime.now());
        inventoryRepository.save(inventory);
    }
    public void restoreInventory(Long locationId, List<Long> vaccineIds) {
        for (Long vaccineId : vaccineIds) {
            InventoryEntity inventory = inventoryRepository.findByLocationIdAndVaccineId(locationId, vaccineId)
                    .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));
            if (inventory != null) {
                // Đơn giản là cộng lại 1
                inventory.setQuantity(inventory.getQuantity() + 1);
                inventoryRepository.save(inventory);
            } else {
                // Cần log lỗi nghiêm trọng ở đây, vì kho đã bị trừ mà không tìm thấy để cộng lại
                log.warn("Could not find inventory to restore stock for location: {} and vaccine: {}", locationId, vaccineId);
            }
        }
    }
}

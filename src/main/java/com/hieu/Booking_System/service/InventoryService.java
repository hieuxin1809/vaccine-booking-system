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
    public List<InventoryResponse> getAll() {
        return inventoryRepository.findAll().stream()
                .map(inventoryMapper::toInventoryResponse)
                .toList();
    }
    public InventoryResponse getById(Long id) {
        InventoryEntity entity = inventoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));
        return inventoryMapper.toInventoryResponse(entity);
    }
    public InventoryResponse updateInventory(Long id, InventoryRequest request) {
        InventoryEntity entity = inventoryRepository.findById(id)
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
    public void deleteInventory(Long id) {
        InventoryEntity inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));
        inventory.setDeletedAt(LocalDateTime.now());
        inventoryRepository.save(inventory);
    }
}

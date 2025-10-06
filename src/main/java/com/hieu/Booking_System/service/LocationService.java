package com.hieu.Booking_System.service;

import com.hieu.Booking_System.entity.LocationEntity;
import com.hieu.Booking_System.exception.AppException;
import com.hieu.Booking_System.exception.ErrorCode;
import com.hieu.Booking_System.mapper.LocationMapper;
import com.hieu.Booking_System.model.request.LocationCreateRequest;
import com.hieu.Booking_System.model.request.LocationUpdateRequest;
import com.hieu.Booking_System.model.response.LocationResponse;
import com.hieu.Booking_System.repository.LocationRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class LocationService{
    LocationRepository locationRepository;
    LocationMapper locationMapper;
    public LocationResponse createLocation(LocationCreateRequest request) {
        LocationEntity locationEntity = locationMapper.toLocationEntity(request);
        locationRepository.save(locationEntity);
        return locationMapper.toLocationResponse(locationEntity);
    }

    public void deleteLocation(Long id) {
        LocationEntity locationEntity = locationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_FOUND));
        locationEntity.setDeletedAt(LocalDateTime.now());
        locationRepository.save(locationEntity);
    }

    public List<LocationResponse> getAllLocations() {
        List<LocationEntity> locationEntities = locationRepository.getAllLocationActive();
        return locationEntities.stream().map(locationMapper::toLocationResponse).collect(Collectors.toList());
    }

    public LocationResponse getLocationById(Long id) {
        LocationEntity locationEntity = locationRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_FOUND));
        return locationMapper.toLocationResponse(locationEntity);
    }

    public LocationResponse updateLocation(Long id , LocationUpdateRequest request) {
        LocationEntity locationEntity = locationRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_FOUND));
        locationEntity.setName(request.getName());
        locationEntity.setAddress(request.getAddress());
        locationEntity.setPhone(request.getPhone());
        locationEntity.setUpdatedAt(LocalDateTime.now());
        locationRepository.save(locationEntity);
        return locationMapper.toLocationResponse(locationEntity);
    }
}

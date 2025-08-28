package com.hieu.Booking_System.service.impl;

import com.hieu.Booking_System.entity.UserEntity;
import com.hieu.Booking_System.entity.VaccineEntity;
import com.hieu.Booking_System.exception.AppException;
import com.hieu.Booking_System.exception.ErrorCode;
import com.hieu.Booking_System.mapper.VaccineMapper;
import com.hieu.Booking_System.model.request.VaccineCreateRequest;
import com.hieu.Booking_System.model.request.VaccineUpdateRequest;
import com.hieu.Booking_System.model.response.VaccineResponse;
import com.hieu.Booking_System.repository.VaccineRepository;
import com.hieu.Booking_System.service.VaccineService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VaccineServiceImpl implements VaccineService {
    VaccineRepository vaccineRepository;
    VaccineMapper vaccineMapper;
    @Override
    public VaccineResponse createVaccine(VaccineCreateRequest request) {
        VaccineEntity vaccine = vaccineMapper.toVaccineEntity(request);
        vaccineRepository.save(vaccine);
        return vaccineMapper.toVaccineResponse(vaccine);
    }

    @Override
    public List<VaccineResponse> getAllVaccine() {
        List<VaccineEntity> vaccineEntities = vaccineRepository.GetAllActiveVaccine();
        if(vaccineEntities == null){
            throw new AppException(ErrorCode.VACCINE_NOT_FOUND);
        }
        List<VaccineResponse> vaccineResponses = vaccineEntities.stream().map(vaccineMapper::toVaccineResponse).collect(Collectors.toList());
        return vaccineResponses;
    }

    @Override
    public VaccineResponse getVaccineById(Long id) {
        VaccineEntity vaccineEntity = vaccineRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VACCINE_NOT_FOUND));
        return vaccineMapper.toVaccineResponse(vaccineEntity);
    }

    @Override
    public void deleteVaccineById(Long id) {
        VaccineEntity vaccineEntity = vaccineRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VACCINE_NOT_FOUND));
        vaccineEntity.setDeletedAt(LocalDateTime.now());
        vaccineRepository.save(vaccineEntity);
    }

    @Override
    public VaccineResponse updateVaccine(Long vaccineId, VaccineUpdateRequest request) {
        VaccineEntity vaccineEntity = vaccineRepository.findById(vaccineId)
                .orElseThrow(() -> new AppException(ErrorCode.VACCINE_NOT_FOUND));
        vaccineEntity.setName(request.getName());
        vaccineEntity.setDescription(request.getDescription());
        vaccineEntity.setPrice(request.getPrice());
        vaccineEntity.setUpdatedAt(LocalDateTime.now());
        vaccineEntity.setDosesRequired(request.getDosesRequired());
        vaccineRepository.save(vaccineEntity);
        return vaccineMapper.toVaccineResponse(vaccineEntity);
    }
}

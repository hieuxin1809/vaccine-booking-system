package com.hieu.Booking_System.service;

import com.hieu.Booking_System.model.request.VaccineCreateRequest;
import com.hieu.Booking_System.model.request.VaccineUpdateRequest;
import com.hieu.Booking_System.model.response.VaccineResponse;

import java.util.List;

public interface VaccineService {
    VaccineResponse createVaccine(VaccineCreateRequest request);
    List<VaccineResponse> getAllVaccine();
    VaccineResponse getVaccineById(Long id);
    void deleteVaccineById(Long id);
    VaccineResponse updateVaccine(Long vaccineId , VaccineUpdateRequest request);
}

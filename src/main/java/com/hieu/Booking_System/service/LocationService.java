package com.hieu.Booking_System.service;

import com.hieu.Booking_System.model.request.LocationCreateRequest;
import com.hieu.Booking_System.model.request.LocationUpdateRequest;
import com.hieu.Booking_System.model.response.LocationResponse;

import java.util.List;

public interface LocationService {
    LocationResponse createLocation(LocationCreateRequest request);
    void deleteLocation(Long id);
    List<LocationResponse> getAllLocations();
    LocationResponse getLocationById(Long id);
    LocationResponse updateLocation( Long id,LocationUpdateRequest request);
}

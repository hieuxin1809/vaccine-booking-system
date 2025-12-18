package com.hieu.Booking_System.mapper;

import org.mapstruct.Mapper;

import com.hieu.Booking_System.entity.LocationEntity;
import com.hieu.Booking_System.model.request.LocationCreateRequest;
import com.hieu.Booking_System.model.response.LocationResponse;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    LocationResponse toLocationResponse(LocationEntity location);

    LocationEntity toLocationEntity(LocationCreateRequest locationCreateRequest);
}

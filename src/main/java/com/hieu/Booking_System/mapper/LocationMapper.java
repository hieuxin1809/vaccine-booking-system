package com.hieu.Booking_System.mapper;

import com.hieu.Booking_System.entity.LocationEntity;
import com.hieu.Booking_System.model.request.LocationCreateRequest;
import com.hieu.Booking_System.model.response.LocationResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    LocationResponse toLocationResponse(LocationEntity location);
    LocationEntity toLocationEntity(LocationCreateRequest locationCreateRequest);
}

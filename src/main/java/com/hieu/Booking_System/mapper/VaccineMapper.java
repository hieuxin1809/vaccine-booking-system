package com.hieu.Booking_System.mapper;

import org.mapstruct.Mapper;

import com.hieu.Booking_System.entity.VaccineEntity;
import com.hieu.Booking_System.model.request.VaccineCreateRequest;
import com.hieu.Booking_System.model.response.VaccineResponse;

@Mapper(componentModel = "spring")
public interface VaccineMapper {
    VaccineResponse toVaccineResponse(VaccineEntity vaccineEntity);

    VaccineEntity toVaccineEntity(VaccineCreateRequest vaccineCreateRequest);
}

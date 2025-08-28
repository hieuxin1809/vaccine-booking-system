package com.hieu.Booking_System.mapper;

import com.hieu.Booking_System.entity.UserEntity;
import com.hieu.Booking_System.entity.VaccineEntity;
import com.hieu.Booking_System.model.request.VaccineCreateRequest;
import com.hieu.Booking_System.model.request.VaccineUpdateRequest;
import com.hieu.Booking_System.model.response.VaccineResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VaccineMapper {
    VaccineResponse toVaccineResponse(VaccineEntity vaccineEntity);
    VaccineEntity toVaccineEntity(VaccineCreateRequest vaccineCreateRequest);
}

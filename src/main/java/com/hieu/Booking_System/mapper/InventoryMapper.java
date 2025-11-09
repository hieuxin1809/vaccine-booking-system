package com.hieu.Booking_System.mapper;


import com.hieu.Booking_System.entity.InventoryEntity;
import com.hieu.Booking_System.entity.LocationEntity;
import com.hieu.Booking_System.model.request.InventoryRequest;
import com.hieu.Booking_System.model.request.LocationCreateRequest;
import com.hieu.Booking_System.model.response.InventoryResponse;
import com.hieu.Booking_System.model.response.LocationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {LocationMapper.class, VaccineMapper.class})
public interface InventoryMapper {
    InventoryResponse toInventoryResponse(InventoryEntity inventory);
    InventoryEntity toInventoryEntity(InventoryRequest request);
}

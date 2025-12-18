package com.hieu.Booking_System.mapper;

import org.mapstruct.Mapper;

import com.hieu.Booking_System.entity.InventoryEntity;
import com.hieu.Booking_System.model.request.InventoryRequest;
import com.hieu.Booking_System.model.response.InventoryResponse;

@Mapper(
        componentModel = "spring",
        uses = {LocationMapper.class, VaccineMapper.class})
public interface InventoryMapper {
    InventoryResponse toInventoryResponse(InventoryEntity inventory);

    InventoryEntity toInventoryEntity(InventoryRequest request);
}

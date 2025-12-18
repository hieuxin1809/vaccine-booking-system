package com.hieu.Booking_System.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.hieu.Booking_System.entity.AppointmentEntity;
import com.hieu.Booking_System.entity.AppointmentVaccineEntity;
import com.hieu.Booking_System.model.request.AppointmentCreateRequest;
import com.hieu.Booking_System.model.response.AppointmentResponse;
import com.hieu.Booking_System.model.response.VaccineResponse;

@Mapper(componentModel = "spring", uses = VaccineMapper.class)
public interface AppointmentMapper {
    AppointmentMapper INSTANCE = Mappers.getMapper(AppointmentMapper.class);

    @Mapping(target = "appointmentDate", source = "appointment_date")
    @Mapping(target = "appointmentTime", source = "appointment_time")
    @Mapping(target = "appointmentStatus", source = "appointment_status")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "appointmentVaccineEntities", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    AppointmentEntity toAppointmentEntity(AppointmentCreateRequest appointmentCreateRequest);

    @Mapping(target = "appointment_date", source = "appointmentDate")
    @Mapping(target = "appointment_time", source = "appointmentTime")
    @Mapping(target = "appointment_status", source = "appointmentStatus")
    @Mapping(target = "userName", source = "user.name")
    @Mapping(target = "userPhone", source = "user.phone")
    @Mapping(target = "locationName", source = "location.name")
    @Mapping(target = "locationAddress", source = "location.address")
    @Mapping(target = "vaccines", source = "appointmentVaccineEntities", qualifiedByName = "mapVaccines")
    AppointmentResponse toAppointmentResponse(AppointmentEntity appointmentEntity);

    @Named("mapVaccines")
    default List<VaccineResponse> mapVaccines(List<AppointmentVaccineEntity> appointmentVaccineEntities) {
        if (appointmentVaccineEntities == null) return null;
        return appointmentVaccineEntities.stream()
                .map(av -> new VaccineResponse(
                        av.getVaccine().getId(),
                        av.getVaccine().getName(),
                        av.getVaccine().getPrice(),
                        av.getVaccine().getDosesRequired(),
                        av.getVaccine().getDescription()))
                .collect(Collectors.toList());
    }
}

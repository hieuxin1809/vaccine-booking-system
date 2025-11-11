package com.hieu.Booking_System.service;

import com.hieu.Booking_System.entity.*;
import com.hieu.Booking_System.enums.AppointmentStatus;
import com.hieu.Booking_System.enums.PaymentGateway;
import com.hieu.Booking_System.exception.AppException;
import com.hieu.Booking_System.exception.ErrorCode;
import com.hieu.Booking_System.mapper.AppointmentMapper;
import com.hieu.Booking_System.mapper.VaccineMapper;
import com.hieu.Booking_System.model.request.AppointmentCreateRequest;
import com.hieu.Booking_System.model.response.AppointmentResponse;
import com.hieu.Booking_System.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppointmentService {
    AppointmentRepository appointmentRepository;
    UserRepository userRepository;
    InventoryRepository inventoryRepository;
    LocationRepository locationRepository;
    VaccineRepository vaccineRepository;
    AppointmentVaccineRepository appointmentVaccineRepository;
    PaymentService paymentService;
    VaccineMapper vaccineMapper;
    AppointmentMapper appointmentMapper;
    RedissonClient redisson;

    public Map<String, Object> createAppointmentWithPayment(
            AppointmentCreateRequest request,
            PaymentGateway paymentGateway,
            HttpServletRequest httpRequest) {

        StringBuilder lockKey = new StringBuilder();
        lockKey.append("lock:appointment")
                .append(request.getLocation_id())
                .append(request.getAppointment_date())
                .append(request.getAppointment_time());

        RLock lock = redisson.getLock(lockKey.toString());

        try {
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                if (checkAppointmentExists(request.getLocation_id(),
                        request.getAppointment_date(),
                        request.getAppointment_time())) {
                    throw new AppException(ErrorCode.APPOINTMENT_DUPLICATED);
                }

                UserEntity user = userRepository.findById(request.getUser_id())
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
                LocationEntity location = locationRepository.findById(request.getLocation_id())
                        .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_FOUND));
                List<VaccineEntity> vaccineEntities = vaccineRepository.findAllById(request.getVaccine_ids());

                if (vaccineEntities.isEmpty()) {
                    throw new AppException(ErrorCode.VACCINE_NOT_FOUND);
                }

                checkInventoryAvailability(request.getLocation_id(), request.getVaccine_ids());
                decreaseInventory(request.getLocation_id(), request.getVaccine_ids());

                BigDecimal totalPrice = calculateTotalPrice(vaccineEntities);

                AppointmentEntity appointmentEntity = appointmentMapper.toAppointmentEntity(request);
                appointmentEntity.setUser(user);
                appointmentEntity.setAppointmentStatus(AppointmentStatus.PENDING);
                appointmentEntity.setLocation(location);
                appointmentEntity.setTotalPrice(totalPrice);

                AppointmentEntity savedAppointment = appointmentRepository.save(appointmentEntity);
                saveAppointmentVaccines(savedAppointment, vaccineEntities);

                // Tạo URL thanh toán
                String paymentUrl = paymentService.createPaymentUrl(
                        savedAppointment.getId(),
                        paymentGateway,
                        httpRequest);

                AppointmentResponse response = appointmentMapper.toAppointmentResponse(savedAppointment);
                response.setVaccines(
                        vaccineEntities.stream()
                                .map(vaccineMapper::toVaccineResponse)
                                .collect(Collectors.toList())
                );

                Map<String, Object> result = new HashMap<>();
                result.put("appointment", response);
                result.put("paymentUrl", paymentUrl);
                result.put("gateway", paymentGateway.name());

                return result;
            } else {
                throw new AppException(ErrorCode.APPOINTMENT_CONFLICT);
            }
        } catch (InterruptedException | UnsupportedEncodingException e) {
            throw new AppException(ErrorCode.INTERRUPTED_LOCK);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
    private void decreaseInventory(Long locationId, List<Long> vaccineIds) {
        for (Long vaccineId : vaccineIds) {
            InventoryEntity inventory = inventoryRepository.findByLocationIdAndVaccineId(locationId, vaccineId)
                    .orElseThrow(() -> new AppException(ErrorCode.OUT_OF_STOCK));

            if (inventory.getQuantity() <= 0) {
                throw new AppException(ErrorCode.OUT_OF_STOCK);
            }
            inventory.setQuantity(inventory.getQuantity() - 1);
            inventoryRepository.save(inventory);
        }
    }

    public void deleteAppointment(Long id) {
        AppointmentEntity appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));
        appointment.setDeletedAt(LocalDateTime.now());
        appointmentRepository.save(appointment);
    }
    private void checkInventoryAvailability(Long locationId, List<Long> vaccineIds) {
        for (Long vaccineId : vaccineIds) {
            Integer quantity = inventoryRepository.getAvailableQuantity(locationId, vaccineId);
            if (quantity == null || quantity <= 0) {
                throw new AppException(ErrorCode.OUT_OF_STOCK);
            }
        }
    }

    public List<AppointmentResponse> getAllAppointments() {
        List<AppointmentEntity> appointmentEntities = appointmentRepository.GetAllActiveAppointments();
        return appointmentEntities.stream()
                .map(appointmentMapper::toAppointmentResponse)
                .collect(Collectors.toList());
    }

    public AppointmentResponse getAppointmentById(Long id) {
        AppointmentEntity appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));
        return appointmentMapper.toAppointmentResponse(appointment);
    }

    public List<AppointmentResponse> getAllAppointmentsByUserId(Long id) {
        List<AppointmentEntity> appointmentEntities = appointmentRepository.findByUser_Id(id);
        if(appointmentEntities.isEmpty()){
            throw new AppException(ErrorCode.APPOINTMENT_NOT_FOUND);
        }
        return appointmentEntities.stream()
                .map(appointmentMapper::toAppointmentResponse)
                .collect(Collectors.toList());
    }

    public List<AppointmentResponse> getAllAppointmentsByLocationId(Long id) {
        List<AppointmentEntity> appointmentEntities = appointmentRepository.findByLocation_Id(id);
        if(appointmentEntities.isEmpty()){
            throw new AppException(ErrorCode.APPOINTMENT_NOT_FOUND);
        }
        return appointmentEntities.stream()
                .map(appointmentMapper::toAppointmentResponse)
                .collect(Collectors.toList());
    }

    public AppointmentResponse updateAppointmentStatus(Long id, AppointmentStatus status) {
        AppointmentEntity appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));
        appointment.setAppointmentStatus(status);
        appointmentRepository.save(appointment);
        return appointmentMapper.toAppointmentResponse(appointment);
    }

    public List<AppointmentResponse> getAllAppointmentByDate(LocalDate date) {
        List<AppointmentEntity> appointmentEntities = appointmentRepository.findByAppointmentDate(date);
        if(appointmentEntities.isEmpty()){
            throw new AppException(ErrorCode.APPOINTMENT_NOT_FOUND);
        }
        return appointmentEntities.stream()
                .map(appointmentMapper::toAppointmentResponse)
                .collect(Collectors.toList());
    }
    private BigDecimal calculateTotalPrice(List<VaccineEntity> vaccineEntities) {
        return vaccineEntities.stream()
                .map(VaccineEntity::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    private void saveAppointmentVaccines(AppointmentEntity appointment, List<VaccineEntity> vaccineEntities) {
        for (VaccineEntity vaccineEntity : vaccineEntities) {
            AppointmentVaccineEntity appointmentVaccineEntity = new AppointmentVaccineEntity();
            appointmentVaccineEntity.setAppointment(appointment);
            appointmentVaccineEntity.setVaccine(vaccineEntity);
            appointmentVaccineRepository.save(appointmentVaccineEntity);
        }
    }
    private boolean checkAppointmentExists(Long id , LocalDate date , LocalTime time) {
        return appointmentRepository.existsByLocation_IdAndAppointmentDateAndAppointmentTime(id, date, time);
    }
}

package com.hieu.Booking_System.service;

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

import com.hieu.Booking_System.aspect.LogTime;
import com.hieu.Booking_System.model.response.PageResponse;
import com.hieu.Booking_System.model.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hieu.Booking_System.entity.*;
import com.hieu.Booking_System.enums.AppointmentStatus;
import com.hieu.Booking_System.enums.PaymentGateway;
import com.hieu.Booking_System.enums.PaymentStatus;
import com.hieu.Booking_System.exception.AppException;
import com.hieu.Booking_System.exception.ErrorCode;
import com.hieu.Booking_System.mapper.AppointmentMapper;
import com.hieu.Booking_System.mapper.VaccineMapper;
import com.hieu.Booking_System.model.request.AppointmentCreateRequest;
import com.hieu.Booking_System.model.response.AppointmentResponse;
import com.hieu.Booking_System.repository.*;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
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
    PaymentRepository paymentRepository;
    InventoryService inventoryService;
    RedissonClient redisson;

    @LogTime
    public Map<String, Object> createAppointmentWithPayment(
            AppointmentCreateRequest request, PaymentGateway paymentGateway, HttpServletRequest httpRequest) {

        StringBuilder lockKey = new StringBuilder();
        lockKey.append("lock:appointment")
                .append(request.getLocation_id())
                .append(request.getAppointment_date())
                .append(request.getAppointment_time());

        RLock lock = redisson.getLock(lockKey.toString());

        try {
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                if (checkAppointmentExists(
                        request.getLocation_id(), request.getAppointment_date(), request.getAppointment_time())) {
                    throw new AppException(ErrorCode.APPOINTMENT_DUPLICATED);
                }

                UserEntity user = userRepository
                        .findById(request.getUser_id())
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
                LocationEntity location = locationRepository
                        .findById(request.getLocation_id())
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
                String paymentUrl =
                        paymentService.createPaymentUrl(savedAppointment.getId(), paymentGateway, httpRequest);

                AppointmentResponse response = appointmentMapper.toAppointmentResponse(savedAppointment);
                response.setVaccines(vaccineEntities.stream()
                        .map(vaccineMapper::toVaccineResponse)
                        .collect(Collectors.toList()));

                Map<String, Object> result = new HashMap<>();
                result.put("appointment", response);
                result.put("paymentUrl", paymentUrl);
                result.put("gateway", paymentGateway.name());

                return result;
            } else {
                throw new AppException(ErrorCode.APPOINTMENT_CONFLICT);
            }
        } catch (AppException e) {
            throw e;
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

    @Scheduled(fixedDelay = 600000) // 10 phút
    @Transactional
    public void cancelStalePendingAppointments() {
        log.info("Running scheduled job: Cancelling stale pending appointments...");

        // Đặt thời gian hết hạn
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(20);

        //Tìm trực tiếp các APPOINTMENT bị PENDING
        List<AppointmentEntity> staleAppointments = appointmentRepository.findAllByAppointmentStatusAndCreatedAtBefore(
                AppointmentStatus.PENDING, expirationTime);

        if (staleAppointments.isEmpty()) {
            log.info("No stale pending appointments found.");
            return;
        }

        log.info("Found {} stale pending appointments to cancel.", staleAppointments.size());

        for (AppointmentEntity appointment : staleAppointments) {
            //Hủy Appointment
            appointment.setAppointmentStatus(AppointmentStatus.CANCELLED);
            appointmentRepository.save(appointment);

            //Hủy Payment liên quan
            PaymentEntity payment =
                    paymentRepository.findByAppointmentId(appointment.getId()).orElse(null);
            if (payment != null && payment.getPaymentStatus() == PaymentStatus.PENDING) {
                payment.setPaymentStatus(PaymentStatus.CANCELLED);
                paymentRepository.save(payment);
            }

            //Hoàn trả kho
            List<Long> vaccineIds = appointment.getAppointmentVaccineEntities().stream()
                    .map(av -> av.getVaccine().getId())
                    .toList();

            // Gọi hàm bạn vừa tạo ở bước 1
            inventoryService.restoreInventory(appointment.getLocation().getId(), vaccineIds);

            log.info("Cancelled Appointment {} and restored inventory.", appointment.getId());
        }
    }

    private void decreaseInventory(Long locationId, List<Long> vaccineIds) {
        for (Long vaccineId : vaccineIds) {
            InventoryEntity inventory = inventoryRepository
                    .findByLocationIdAndVaccineId(locationId, vaccineId)
                    .orElseThrow(() -> new AppException(ErrorCode.OUT_OF_STOCK));

            if (inventory.getQuantity() <= 0) {
                throw new AppException(ErrorCode.OUT_OF_STOCK);
            }
            inventory.setQuantity(inventory.getQuantity() - 1);
            inventoryRepository.save(inventory);
        }
    }

    @PreAuthorize("hasRole('ADMIN') or @appointmentService.isOwner(#id, authentication.name)")
    public void deleteAppointment(Long id) {
        AppointmentEntity appointment =
                appointmentRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));
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
    @LogTime
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<AppointmentResponse> getAllAppointment(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<AppointmentEntity> pageData = appointmentRepository.getAllActiveAppointments(pageable);

        List<AppointmentResponse> appointmentResponseList = pageData.getContent().stream()
                .map(appointmentMapper::toAppointmentResponse)
                .collect(Collectors.toList());
        return PageResponse.<AppointmentResponse>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .data(appointmentResponseList)
                .build();
    }

    @PreAuthorize("hasRole('ADMIN') or @appointmentService.isOwner(#id, authentication.name)")
    public AppointmentResponse getAppointmentById(Long id) {
        AppointmentEntity appointment =
                appointmentRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));
        return appointmentMapper.toAppointmentResponse(appointment);
    }

    @PreAuthorize("hasRole('ADMIN') or @userService.myInfo().id == #id")
    public List<AppointmentResponse> getAllAppointmentsByUserId(Long id) {
        List<AppointmentEntity> appointmentEntities = appointmentRepository.findByUser_Id(id);
        if (appointmentEntities.isEmpty()) {
            throw new AppException(ErrorCode.APPOINTMENT_NOT_FOUND);
        }
        return appointmentEntities.stream()
                .map(appointmentMapper::toAppointmentResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<AppointmentResponse> getAllAppointmentsByLocationId(Long id) {
        List<AppointmentEntity> appointmentEntities = appointmentRepository.findByLocation_Id(id);
        if (appointmentEntities.isEmpty()) {
            throw new AppException(ErrorCode.APPOINTMENT_NOT_FOUND);
        }
        return appointmentEntities.stream()
                .map(appointmentMapper::toAppointmentResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public AppointmentResponse updateAppointmentStatus(Long id, AppointmentStatus status) {
        AppointmentEntity appointment =
                appointmentRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));
        appointment.setAppointmentStatus(status);
        appointmentRepository.save(appointment);
        return appointmentMapper.toAppointmentResponse(appointment);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<AppointmentResponse> getAllAppointmentByDate(LocalDate date) {
        List<AppointmentEntity> appointmentEntities = appointmentRepository.findByAppointmentDate(date);
        if (appointmentEntities.isEmpty()) {
            throw new AppException(ErrorCode.APPOINTMENT_NOT_FOUND);
        }
        return appointmentEntities.stream()
                .map(appointmentMapper::toAppointmentResponse)
                .collect(Collectors.toList());
    }

    private BigDecimal calculateTotalPrice(List<VaccineEntity> vaccineEntities) {
        return vaccineEntities.stream().map(VaccineEntity::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void saveAppointmentVaccines(AppointmentEntity appointment, List<VaccineEntity> vaccineEntities) {
        for (VaccineEntity vaccineEntity : vaccineEntities) {
            AppointmentVaccineEntity appointmentVaccineEntity = new AppointmentVaccineEntity();
            appointmentVaccineEntity.setAppointment(appointment);
            appointmentVaccineEntity.setVaccine(vaccineEntity);
            appointmentVaccineRepository.save(appointmentVaccineEntity);
        }
    }

    private boolean checkAppointmentExists(Long id, LocalDate date, LocalTime time) {
        return appointmentRepository.existsByLocation_IdAndAppointmentDateAndAppointmentTime(id, date, time);
    }

    public boolean isOwner(Long appointmentId, String email) {
        return appointmentRepository
                .findById(appointmentId)
                .map(appointment -> appointment.getUser().getEmail().equals(email))
                .orElse(false);
    }
}

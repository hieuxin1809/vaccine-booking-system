package com.hieu.Booking_System.repository;

import com.hieu.Booking_System.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    // Tìm kiếm Payment bằng mã giao dịch của cổng thanh toán
    Optional<PaymentEntity> findByTransactionId(String transactionId);
    Optional<PaymentEntity> findByAppointmentId(Long appointmentId);
}
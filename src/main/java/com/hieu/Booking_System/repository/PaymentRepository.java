package com.hieu.Booking_System.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hieu.Booking_System.entity.PaymentEntity;
import com.hieu.Booking_System.enums.PaymentStatus;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    // Tìm kiếm Payment bằng mã giao dịch của cổng thanh toán
    Optional<PaymentEntity> findByTransactionId(String transactionId);

    Optional<PaymentEntity> findByAppointmentId(Long appointmentId);

    List<PaymentEntity> findAllByPaymentStatusAndCreatedAtBefore(PaymentStatus paymentStatus, LocalDateTime time);
}

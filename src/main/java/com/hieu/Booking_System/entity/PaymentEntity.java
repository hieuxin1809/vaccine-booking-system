package com.hieu.Booking_System.entity;

import com.hieu.Booking_System.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "payment")
public class PaymentEntity extends BaseEntity {
    // Liên kết với Appointment
    @Column(name = "appointment_id", nullable = false)
    Long appointmentId;

    // amount: BigDecimal
    @Column(nullable = false, precision = 10, scale = 2)
    BigDecimal amount;

    // payment_method: enum/varchar
    String paymentMethod;

    // payment_status: enum (PENDING, PROCESSED, FAILED, ...)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    PaymentStatus paymentStatus;

    // transaction_id: mã giao dịch bên cổng thanh toán
    String transactionId;

    // payment_gateway: Tên cổng thanh toán (VNPay, MoMo,...)
    String paymentGateway;
}

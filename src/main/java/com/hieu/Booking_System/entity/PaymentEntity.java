package com.hieu.Booking_System.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;

import com.hieu.Booking_System.enums.PaymentStatus;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(
        name = "payment",
        indexes = {
            // Tối ưu cho hàm findByTransactionId
            @Index(name = "idx_payment_trans_id", columnList = "transactionId")
        })
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

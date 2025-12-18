package com.hieu.Booking_System.enums;

public enum PaymentStatus {
    PENDING, // Đang chờ thanh toán (người dùng vừa khởi tạo)
    COMPLETED, // Thanh toán thành công
    FAILED, // Thanh toán thất bại
    REFUNDED, // Đã hoàn tiền
    CANCELLED // Giao dịch bị hủy
}

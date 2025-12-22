package com.hieu.Booking_System.service;

import java.time.format.DateTimeFormatter;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.hieu.Booking_System.entity.AppointmentEntity;
import com.hieu.Booking_System.entity.PaymentEntity;
import com.hieu.Booking_System.entity.UserEntity;
import com.hieu.Booking_System.exception.AppException;
import com.hieu.Booking_System.exception.ErrorCode;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BrevoEmailService {

    @Value("${spring.brevo.api-key}")
    private String apiKey;

    @Value("${spring.brevo.sender-email}")
    private String senderEmail;

    @Value("${spring.app.base-url}")
    private String baseUrl;

    @Value("${spring.brevo.sender-name}")
    private String senderName;

    private RestClient restClient;

    private static final String BREVO_EMAIL_API = "https://api.sendinblue.com/v3";
    private static final String EMAIL_ENDPOINT = "/smtp/email";

    @PostConstruct
    public void initialize() {
        this.restClient = RestClient.builder()
                .baseUrl(BREVO_EMAIL_API)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("api-key", apiKey)
                .build();
    }
	@Async
    public void sendVerificationEmail(UserEntity user, String token) {
        if (restClient == null) {
            log.error("RestClient chưa được khởi tạo!");
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        String verificationLink = baseUrl + "/auth/verify?token=" + token;

        String htmlContent = String.format(
                """
			<p>Chào %s,</p>
			<p>Vui lòng nhấp vào liên kết sau để xác nhận địa chỉ email của bạn:</p>
			<p><a href="%s">Xác nhận Tài khoản của tôi</a></p>
			<p>Nếu bạn không đăng ký, vui lòng bỏ qua email này.</p>
			""",
                user.getName(), verificationLink);

        sendEmail(user.getEmail(), user.getName(), "Xác nhận Tài khoản của bạn", htmlContent);
    }

    /**
     * Gửi email xác nhận đặt lịch thành công
     */
	@Async
    public void sendAppointmentConfirmationEmail(
            AppointmentEntity appointment, PaymentEntity payment, UserEntity user) {
        if (restClient == null) {
            log.error("RestClient chưa được khởi tạo!");
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String appointmentDate = appointment.getAppointmentDate().format(dateFormatter);

        String htmlContent = buildAppointmentConfirmationHtml(
                user.getName(),
                appointment.getId(),
                appointmentDate,
                appointment.getTotalPrice(),
                payment.getPaymentMethod(),
                payment.getTransactionId());

        sendEmail(user.getEmail(), user.getName(), "Xác nhận đặt lịch thành công #" + appointment.getId(), htmlContent);

        log.info("✓ Đã gửi email xác nhận đặt lịch cho appointment: {}", appointment.getId());
    }

    /**
     * Tạo nội dung HTML cho email xác nhận đặt lịch
     */
    private String buildAppointmentConfirmationHtml(
            String userName,
            Long appointmentId,
            String appointmentDate,
            java.math.BigDecimal amount,
            String paymentMethod,
            String transactionId) {

        return String.format(
                """
			<!DOCTYPE html>
			<html>
			<head>
				<style>
					body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
					.container { max-width: 600px; margin: 0 auto; padding: 20px; }
					.header { background: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
					.content { background: #f9f9f9; padding: 30px; border: 1px solid #ddd; }
					.info-box { background: white; padding: 15px; margin: 15px 0; border-left: 4px solid #4CAF50; }
					.info-row { margin: 10px 0; }
					.label { font-weight: bold; color: #555; }
					.value { color: #333; }
					.footer { text-align: center; padding: 20px; color: #777; font-size: 12px; }
					.success-badge { background: #4CAF50; color: white; padding: 5px 15px; border-radius: 20px; display: inline-block; }
				</style>
			</head>
			<body>
				<div class="container">
					<div class="header">
						<h1>✓ Đặt Lịch Thành Công</h1>
					</div>
					<div class="content">
						<p>Chào <strong>%s</strong>,</p>
						<p>Cảm ơn bạn đã đặt lịch với chúng tôi. Đơn đặt lịch của bạn đã được xác nhận và thanh toán thành công.</p>

						<div class="info-box">
							<h3>📋 Thông tin đặt lịch</h3>
							<div class="info-row">
								<span class="label">Mã đặt lịch:</span>
								<span class="value">#%d</span>
							</div>
							<div class="info-row">
								<span class="label">Dịch vụ:</span>
								<span class="value">Đặt lịch tiêm chủng</span>
							</div>
							<div class="info-row">
								<span class="label">Thời gian:</span>
								<span class="value">%s</span>
							</div>
						</div>

						<div class="info-box">
							<h3>💳 Thông tin thanh toán</h3>
							<div class="info-row">
								<span class="label">Trạng thái:</span>
								<span class="success-badge">Đã thanh toán</span>
							</div>
							<div class="info-row">
								<span class="label">Số tiền:</span>
								<span class="value">%,.0f VNĐ</span>
							</div>
							<div class="info-row">
								<span class="label">Phương thức:</span>
								<span class="value">%s</span>
							</div>
							<div class="info-row">
								<span class="label">Mã giao dịch:</span>
								<span class="value">%s</span>
							</div>
						</div>

						<p><strong>Lưu ý quan trọng:</strong></p>
						<ul>
							<li>Vui lòng đến trước giờ hẹn 15 phút</li>
							<li>Mang theo CMND/CCCD và sổ tiêm chủng (nếu có)</li>
							<li>Liên hệ hotline nếu cần hủy hoặc thay đổi lịch hẹn</li>
						</ul>

						<p>Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi.</p>
						<p>Trân trọng,<br><strong>Booking System Team</strong></p>
					</div>
					<div class="footer">
						<p>Email này được gửi tự động, vui lòng không trả lời.</p>
						<p>&copy; 2024 Booking System. All rights reserved.</p>
					</div>
				</div>
			</body>
			</html>
			""",
                userName, appointmentId, appointmentDate, amount.doubleValue(), paymentMethod, transactionId);
    }

    /**
     * Phương thức gửi email chung
     */
    private void sendEmail(String recipientEmail, String recipientName, String subject, String htmlContent) {
        Map<String, Object> sender = Map.of("email", senderEmail, "name", senderName);
        Map<String, String> toRecipient = Map.of("email", recipientEmail, "name", recipientName);

        Map<String, Object> emailRequest = Map.of(
                "sender", sender,
                "to", new Object[] {toRecipient},
                "subject", subject,
                "htmlContent", htmlContent);

        try {
            this.restClient
                    .post()
                    .uri(EMAIL_ENDPOINT)
                    .body(emailRequest)
                    .retrieve()
                    .toBodilessEntity();
            log.info("✓ Email đã được gửi đến: {}", recipientEmail);
        } catch (Exception e) {
            log.error("✗ Lỗi khi gửi email qua Brevo: {}", e.getMessage());
            // Không throw exception để không làm gián đoạn luồng thanh toán
        }
    }
}

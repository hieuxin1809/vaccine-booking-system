package com.hieu.Booking_System.service;

import com.hieu.Booking_System.entity.UserEntity;
import com.hieu.Booking_System.exception.AppException;
import com.hieu.Booking_System.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import jakarta.annotation.PostConstruct; // Hoặc javax.annotation.PostConstruct nếu bạn dùng Java < 11
import java.util.Map;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE) // Bỏ makeFinal=true
public class BrevoEmailService {

    // Khai báo các trường @Value
    @Value("${spring.brevo.api-key}")
    private String apiKey;

    @Value("${spring.brevo.sender-email}")
    private String senderEmail;

    // Sửa lỗi cú pháp: dùng base-url thay vì url
    @Value("${spring.app.base-url}")
    private String baseUrl;

    @Value("${spring.brevo.sender-name}")
    private String senderName;

    private RestClient restClient; // Khai báo RestClient

    private static final String BREVO_EMAIL_API = "https://api.sendinblue.com/v3";
    private static final String EMAIL_ENDPOINT = "/smtp/email";


    /**
     * Khởi tạo RestClient sau khi các giá trị @Value đã được inject
     */
    @PostConstruct
    public void initialize() {
        this.restClient = RestClient.builder()
                .baseUrl(BREVO_EMAIL_API)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("api-key", apiKey) // Đặt API Key vào header mặc định
                .build();
    }

    // Giả định UserEntity có phương thức getName() và getEmail()
    public void sendVerificationEmail(UserEntity user, String token) {
        // Kiểm tra null cho an toàn
        if (restClient == null) {
            log.error("RestClient chưa được khởi tạo!");
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        String verificationLink = baseUrl + "/auth/verify?token=" + token;

        // Giả định UserEntity có getName()
        String htmlContent = String.format("""
            <p>Chào %s,</p>
            <p>Vui lòng nhấp vào liên kết sau để xác nhận địa chỉ email của bạn:</p>
            <p><a href="%s">Xác nhận Tài khoản của tôi</a></p>
            <p>Nếu bạn không đăng ký, vui lòng bỏ qua email này.</p>
            """, user.getName(), verificationLink);

        Map<String, Object> sender = Map.of("email", senderEmail, "name", senderName);

        // Giả định UserEntity có getFullName()
        Map<String, String> toRecipient = Map.of("email", user.getEmail(), "name", user.getName());

        Map<String, Object> emailRequest = Map.of(
                "sender", sender,
                "to", new Object[]{toRecipient},
                "subject", "Xác nhận Tài khoản của bạn",
                "htmlContent", htmlContent
        );

        try {
            // Thực hiện gọi API của Brevo
            this.restClient.post() // Sử dụng restClient đã khởi tạo
                    .uri(EMAIL_ENDPOINT)
                    .body(emailRequest)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Email xác nhận đã được gửi đến: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Lỗi khi gửi email xác nhận qua Brevo: {}", e.getMessage());
            // Tùy chọn: ném exception để thông báo lỗi cho người dùng
            // throw new AppException(ErrorCode.EMAIL_SEND_FAILURE);
        }
    }
}
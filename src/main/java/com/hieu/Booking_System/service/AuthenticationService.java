package com.hieu.Booking_System.service;

import com.hieu.Booking_System.entity.RedisToken;
import com.hieu.Booking_System.entity.UserEntity;
import com.hieu.Booking_System.enums.UserStatus;
import com.hieu.Booking_System.exception.AppException;
import com.hieu.Booking_System.exception.ErrorCode;
import com.hieu.Booking_System.model.JwtInfo;
import com.hieu.Booking_System.model.request.LoginRequest;
import com.hieu.Booking_System.model.request.LogoutRequest;
import com.hieu.Booking_System.model.request.RefreshRequest;
import com.hieu.Booking_System.model.request.RegisterRequest;
import com.hieu.Booking_System.model.response.LoginResponse;
import com.hieu.Booking_System.model.response.RefreshTokenResponse;
import com.hieu.Booking_System.model.response.RegisterResponse;
import com.hieu.Booking_System.repository.RedisTokenRepository;
import com.hieu.Booking_System.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationService{
    UserRepository userRepository;
    JwtService jwtService;
    RedisTokenRepository redisTokenRepository;
    PasswordEncoder passwordEncoder;
    BrevoEmailService brevoEmailService;
    AuthenticationManager authenticationManager;

    public RegisterResponse register(RegisterRequest registerRequest) {
        if(userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXIST);
        }
        UserEntity userEntity = UserEntity.builder()
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .name(registerRequest.getName())
                .emailVerified(false)
                .build();
        String verificationToken = UUID.randomUUID().toString();
        userEntity.setVerificationToken(verificationToken);
        userRepository.save(userEntity);
        // Send verification email
        brevoEmailService.sendVerificationEmail(userEntity, verificationToken);
        return RegisterResponse.builder()
                .message("User registered successfully. Please check your email for verification.")
                .build();
    }
    public LoginResponse login(LoginRequest loginRequest) {
        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
            Authentication authenticate = authenticationManager.authenticate(authenticationToken);
            UserEntity userEntity = (UserEntity) authenticate.getPrincipal();

            if (!userEntity.isEmailVerified()) {
                throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED);
            }

            String accessToken = jwtService.generateAccessToken(userEntity);
            String refreshToken = jwtService.generateRefreshToken(userEntity);

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        } catch (BadCredentialsException e) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    public void verifyEmail(String token) {
        // 1. Tìm người dùng bằng verificationToken
        UserEntity user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN_REGISTER)); // Hoặc lỗi khác

        // 2. Kiểm tra trạng thái và thời hạn token (nếu dùng JWT)
        if (user.isEmailVerified()) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_VERIFIED);
        }

        // 3. Cập nhật trạng thái xác nhận
        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        user.setVerificationToken(null); // Xóa token sau khi dùng

        userRepository.save(user);
    }
    public void logout(LogoutRequest logoutRequest) throws ParseException, JOSEException {
        String token = logoutRequest.getToken().replace("Bearer ", "");

        // Verify signature + expiration, không check blacklist
        SignedJWT signedJWT = jwtService.verifyToken(token, false);

        String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
        Date expiredTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        // Blacklist token
        RedisToken redisToken = RedisToken.builder()
                .jwtId(jwtId)
                .expiredTime(expiredTime.getTime() - new Date().getTime())
                .build();
        redisTokenRepository.save(redisToken);
    }
    public RefreshTokenResponse refresh(RefreshRequest request) throws ParseException, JOSEException {
        String refreshToken = request.getToken();

        // Verify signature + expiration, khong check blacklist
        SignedJWT signedJWT = jwtService.verifyToken(refreshToken, false);

        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
        String jwtId = claims.getJWTID();
        Date expiredTime = claims.getExpirationTime();
        String email = claims.getSubject();

        RedisToken redisToken = RedisToken.builder()
                .jwtId(jwtId)
                .expiredTime(expiredTime.getTime() - new Date().getTime())
                .build();
        redisTokenRepository.save(redisToken);

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return RefreshTokenResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .build();
    }
}

package com.hieu.Booking_System.service;

import com.hieu.Booking_System.entity.RedisToken;
import com.hieu.Booking_System.entity.UserEntity;
import com.hieu.Booking_System.exception.AppException;
import com.hieu.Booking_System.exception.ErrorCode;
import com.hieu.Booking_System.model.JwtInfo;
import com.hieu.Booking_System.model.request.LoginRequest;
import com.hieu.Booking_System.model.request.LogoutRequest;
import com.hieu.Booking_System.model.request.RefreshRequest;
import com.hieu.Booking_System.model.response.LoginResponse;
import com.hieu.Booking_System.model.response.RefreshTokenResponse;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationService{
    UserRepository userRepository;
    JwtService jwtService;
    RedisTokenRepository redisTokenRepository;

    AuthenticationManager authenticationManager;
    public LoginResponse authenticate(LoginRequest loginRequest) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);
        UserEntity userEntity = (UserEntity) authenticate.getPrincipal();
        String accessToken = jwtService.generateAccessToken(userEntity);
        String refreshToken = jwtService.generateRefreshToken(userEntity);
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
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

package com.hieu.Booking_System.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import com.hieu.Booking_System.service.PermissionService;
import com.hieu.Booking_System.service.UserDetailServiceCustomizer;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private static final String[] PUBLIC_POST_ENDPOINTS = {"/auth/**", "/payment/**", "/appointment" // tạo lịch hẹn
    };
    private static final String[] PUBLIC_GET_ENDPOINTS = {
        "/auth/**",
        "/payment/**",
        "/appointment/**",
        "/vaccine/**",
        "/location/**",
        "/payment/vnpay_ipn",
        "/payment/paypal/webhook",
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html"
    };
    private final CustomJwtDecoder jwtDecoder;
    private final UserDetailServiceCustomizer userDetailsServiceCustomizer;
    private final PermissionService permissionService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.POST, PUBLIC_POST_ENDPOINTS)
                .permitAll()
                .requestMatchers(HttpMethod.GET, PUBLIC_GET_ENDPOINTS)
                .permitAll()
                // Cho phép truy cập tài nguyên tĩnh nếu có (nếu deploy frontend trong Spring)
                .requestMatchers("/css/**", "/js/**", "/images/**")
                .permitAll()

                // Các endpoint quản lý yêu cầu đăng nhập
                .requestMatchers("/user/**", "/inventory/**", "/role/**", "/permission/**")
                .authenticated()
                .anyRequest()
                .authenticated());
        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(
                        jwtConfigurer -> jwtConfigurer.decoder(jwtDecoder).jwtAuthenticationConverter(jwtConverter()))
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint()));
        http.csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    JwtAuthenticationConverter jwtConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // 1. Lấy Roles từ Token (VD: ROLE_ADMIN)
            Collection<GrantedAuthority> authorities = new ArrayList<>(jwtGrantedAuthoritiesConverter.convert(jwt));

            List<String> roles = authorities.stream()
                    .map(GrantedAuthority::getAuthority) // Lấy ra "ROLE_ADMIN"
                    .toList();

            // 3. Tra cứu Permission từ Redis/DB
            for (String roleName : roles) {
                // Lưu ý: roleName lúc này là "ROLE_ADMIN"
                List<String> permissions = permissionService.getPermissionsByRole(roleName);

                if (permissions != null) {
                    for (String permission : permissions) {
                        // Thêm Permission vào list Authority
                        authorities.add(new SimpleGrantedAuthority(permission));
                    }
                }
            }
            return authorities;
        });
        return converter;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsServiceCustomizer);
        authenticationProvider.setPasswordEncoder(passwordEncoder());

        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}

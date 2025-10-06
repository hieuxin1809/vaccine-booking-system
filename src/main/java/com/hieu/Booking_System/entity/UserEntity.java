package com.hieu.Booking_System.entity;

import com.hieu.Booking_System.enums.Role;
import jakarta.persistence.*;
import jdk.jfr.DataAmount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity extends BaseEntity implements UserDetails {
    private String name;

    private String password;
    @Column(unique = true, nullable = false)
    private String email;

    private String address;
    private String phone;
    private LocalDate dob;
    private Character gender;

    @OneToMany(mappedBy = "user" , fetch = FetchType.LAZY)
    private List<AppointmentEntity> patientAppointments;

    @OneToMany(mappedBy = "user" , fetch = FetchType.LAZY)
    private List<NotificationEntity> notifications;

    @ManyToMany
    private Set<RoleEntity> roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return "";
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}

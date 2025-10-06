package com.hieu.Booking_System.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class RoleEntity {
    @Id
    private String name;
    private String description;
    @ManyToMany
    private Set<PermissionEntity> permissions;
    @CreatedDate
    @Column(name = "create_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "update_at")
    private LocalDateTime updatedAt;

    @Column(name = "delete_at")
    private LocalDateTime deletedAt;
}

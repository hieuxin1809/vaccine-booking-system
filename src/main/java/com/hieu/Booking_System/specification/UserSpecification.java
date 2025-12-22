package com.hieu.Booking_System.specification;

import com.hieu.Booking_System.entity.UserEntity;
import com.hieu.Booking_System.enums.UserStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class UserSpecification {
    public static Specification<UserEntity> hasKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if(!StringUtils.hasText(keyword)){
                return null;
            }
            String likePattern = "%" + keyword.trim().toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("phone")), likePattern)
            );
        };
    }
    public static Specification<UserEntity> hasStatus(UserStatus status) {
        return (root, query, criteriaBuilder) -> {
            if(status == null){
                return null;
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }
    public static Specification<UserEntity> hasRole(String roleName) {
        return (root, query, criteriaBuilder) -> {
            if(!StringUtils.hasText(roleName)){
                return null;
            }
            return criteriaBuilder.equal(root.join("roles").get("name"), roleName);
        };
    }
}

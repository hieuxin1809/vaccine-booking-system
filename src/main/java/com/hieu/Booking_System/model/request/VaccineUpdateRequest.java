package com.hieu.Booking_System.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VaccineUpdateRequest {
    @NotBlank(message = "NAME_REQUIRED")
    String name;

    @NotNull(message = "PRICE_REQUIRED")
    @Positive(message = "PRICE_MUST_BE_POSITIVE")
    BigDecimal price;
    
    @Min(value = 1, message = "DOSES_MUST_BE_AT_LEAST_1")
    int dosesRequired;
    String description;
}

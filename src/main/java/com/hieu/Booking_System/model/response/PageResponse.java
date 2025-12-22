package com.hieu.Booking_System.model.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PageResponse<T> {
    int currentPage;    // Trang hiện tại
    int totalPages;     // Tổng số trang
    int pageSize;       // Số lượng phần tử mỗi trang
    long totalElements; // Tổng số phần tử trong DB
    List<T> data;       // Dữ liệu
}
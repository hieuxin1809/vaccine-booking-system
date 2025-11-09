package com.hieu.Booking_System.controller;

import com.hieu.Booking_System.model.request.InventoryRequest;
import com.hieu.Booking_System.model.request.UserCreateRequest;
import com.hieu.Booking_System.model.request.UserUpdateRequest;
import com.hieu.Booking_System.model.response.ApiResponse;
import com.hieu.Booking_System.model.response.InventoryResponse;
import com.hieu.Booking_System.model.response.UserResponse;
import com.hieu.Booking_System.service.InventoryService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryController {
    InventoryService inventoryService;
    @PostMapping()
    ApiResponse<InventoryResponse> createInventory(@RequestBody @Valid InventoryRequest request) {
        return ApiResponse.<InventoryResponse>builder()
                .data(inventoryService.createInventory(request))
                .build();
    }
    @GetMapping("")
    ApiResponse<List<InventoryResponse>> getAllInventory() {
        return ApiResponse.<List<InventoryResponse>>builder()
                .data(inventoryService.getAll())
                .build();
    }
    @GetMapping("/{id}")
    ApiResponse<InventoryResponse> getUser(@PathVariable Long id) {
        return ApiResponse.<InventoryResponse>builder()
                .data(inventoryService.getById(id))
                .build();
    }
    @DeleteMapping("/{id}")
    ApiResponse<Void> deleteUser(@PathVariable Long id){
        inventoryService.deleteInventory(id);
        return ApiResponse.<Void>builder()
                .build();
    }
    @PutMapping("/{id}")
    ApiResponse<InventoryResponse> updateUser(@PathVariable("id") Long userId, @RequestBody InventoryRequest request) {
        return ApiResponse.<InventoryResponse>builder()
                .data(inventoryService.updateInventory(userId, request))
                .build();
    }
}

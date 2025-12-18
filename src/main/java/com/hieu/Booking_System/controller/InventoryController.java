package com.hieu.Booking_System.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.hieu.Booking_System.model.request.InventoryRequest;
import com.hieu.Booking_System.model.response.ApiResponse;
import com.hieu.Booking_System.model.response.InventoryResponse;
import com.hieu.Booking_System.service.InventoryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryController {
    InventoryService inventoryService;

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<InventoryResponse> createInventory(@RequestBody @Valid InventoryRequest request) {
        return ApiResponse.<InventoryResponse>builder()
                .data(inventoryService.createInventory(request))
                .build();
    }

    @GetMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<List<InventoryResponse>> getAllInventory() {
        return ApiResponse.<List<InventoryResponse>>builder()
                .data(inventoryService.getAll())
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<InventoryResponse> getInventory(@PathVariable Long id) {
        return ApiResponse.<InventoryResponse>builder()
                .data(inventoryService.getById(id))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<Void> deleteInventory(@PathVariable Long id) {
        inventoryService.deleteInventory(id);
        return ApiResponse.<Void>builder().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<InventoryResponse> updateInventory(@PathVariable("id") Long id, @RequestBody InventoryRequest request) {
        return ApiResponse.<InventoryResponse>builder()
                .data(inventoryService.updateInventory(id, request))
                .build();
    }
}

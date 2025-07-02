package com.micronauticals.parcel.controller;


import com.micronauticals.parcel.dto.DeliveryOrderDTO;
import com.micronauticals.parcel.service.DeliveryOrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/delivery-orders")
public class DeliveryOrderController {

    private final DeliveryOrderService deliveryOrderService;

    public DeliveryOrderController(DeliveryOrderService deliveryOrderService) {
        this.deliveryOrderService = deliveryOrderService;
    }

    // Get today's delivery orders, or with filters
    @GetMapping("/today")
    public Page<DeliveryOrderDTO> getOrdersForToday(
            @RequestParam(required = false) String vendorName,
            @RequestParam(required = false) LocalDate date,
            Pageable pageable) {
        if (vendorName != null || date != null) {
            return deliveryOrderService.getOrdersForVendorAndDate(vendorName, date, pageable);
        } else {
            return deliveryOrderService.getOrdersForToday(pageable);
        }
    }

    // Upload API for vendors to upload order details
    @PostMapping("/upload")
    public ResponseEntity<DeliveryOrderDTO> uploadOrderFile(
            @RequestParam String vendorName,
            @RequestParam LocalDate deliveryDate,
            @RequestParam("file") MultipartFile file) {
        try {
            DeliveryOrderDTO dto = deliveryOrderService.uploadOrderFile(vendorName, deliveryDate, file);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}

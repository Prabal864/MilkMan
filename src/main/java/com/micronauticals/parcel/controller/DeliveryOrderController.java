package com.micronauticals.parcel.controller;

import com.micronauticals.parcel.dto.DeliveryOrderDTO;
import com.micronauticals.parcel.dto.PageResult;
import com.micronauticals.parcel.service.DeliveryOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/delivery-orders")
public class DeliveryOrderController {

    private final DeliveryOrderService deliveryOrderService;
    private static final Logger logger = LoggerFactory.getLogger(DeliveryOrderController.class);

    public DeliveryOrderController(DeliveryOrderService deliveryOrderService) {
        this.deliveryOrderService = deliveryOrderService;
    }

    @PreAuthorize("hasAnyRole('VENDOR','ADMIN')")
    @GetMapping("/today")
    public ResponseEntity<PageResult<DeliveryOrderDTO>> getOrdersForToday(
            @RequestParam(required = false) String vendorName,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String startKey
    ) {
        if (limit == null) {
            limit = 100;
        }
        PageResult<DeliveryOrderDTO> page;
        if (vendorName != null || date != null) {
            page = deliveryOrderService.getOrdersForVendorAndDate(vendorName, date, limit, startKey);
        } else {
            page = deliveryOrderService.getOrdersForToday(limit, startKey);
        }
        return ResponseEntity.ok(page);
    }

    // Upload API for vendors to upload order details
    @PreAuthorize("hasRole('VENDOR')")
    @PostMapping("/upload")
    public ResponseEntity<DeliveryOrderDTO> uploadOrderFile(
            @RequestParam String vendorName,
            @RequestParam LocalDate deliveryDate,
            @RequestParam("file") MultipartFile file) {
        try {
            DeliveryOrderDTO dto = deliveryOrderService.uploadOrderFile(vendorName, deliveryDate, file);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            logger.error("Failed to upload order file for vendor '{}', date '{}': {}", vendorName, deliveryDate, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
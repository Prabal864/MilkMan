package com.micronauticals.parcel.controller;


import com.micronauticals.parcel.dto.DeliveryOrderDTO;
import com.micronauticals.parcel.service.DeliveryOrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
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

    public DeliveryOrderController(DeliveryOrderService deliveryOrderService) {
        this.deliveryOrderService = deliveryOrderService;
    }

    // Get today's delivery orders, or with filters
    @PreAuthorize("hasAnyRole('VENDOR','ADMIN')")
    @GetMapping("/today")
    public ResponseEntity<?>  getOrdersForToday(
            @RequestParam(required = false) String vendorName,
            @RequestParam(required = false) LocalDate date,
            Pageable pageable,
            PagedResourcesAssembler<DeliveryOrderDTO> pagedResourcesAssembler) {
        Page<DeliveryOrderDTO> page;
        if (vendorName != null || date != null) {
            page = deliveryOrderService.getOrdersForVendorAndDate(vendorName, date, pageable);
        } else {
            page = deliveryOrderService.getOrdersForToday(pageable);
        }
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(page));
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}

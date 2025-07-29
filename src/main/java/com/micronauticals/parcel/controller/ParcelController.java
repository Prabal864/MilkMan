package com.micronauticals.parcel.controller;

import com.micronauticals.parcel.dto.PageResult;
import com.micronauticals.parcel.dto.ParcelDTO;
import com.micronauticals.parcel.service.ParcelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/parcels")
public class ParcelController {

    private final ParcelService parcelService;

    public ParcelController(ParcelService parcelService) {
        this.parcelService = parcelService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<PageResult<ParcelDTO>> getAllParcelsForToday(
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String startKey
    ) {
        PageResult<ParcelDTO> parcels = parcelService.getAllParcels(limit, startKey);
        return ResponseEntity.ok(parcels);
    }

    @GetMapping("track/{id}")
    public ResponseEntity<ParcelDTO> getParcelById(@PathVariable String id) {
        Optional<ParcelDTO> optionalParcel = parcelService.getParcelByTrackingId(id);
        return optionalParcel.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }
}
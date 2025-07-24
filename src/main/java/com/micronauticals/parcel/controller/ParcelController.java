package com.micronauticals.parcel.controller;

import com.micronauticals.parcel.dto.ParcelDTO;
import com.micronauticals.parcel.service.ParcelService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<PagedModel<EntityModel<ParcelDTO>>> getAllParcels(Pageable pageable, PagedResourcesAssembler<ParcelDTO> pagedResourcesAssembler) {
        Page<ParcelDTO> parcelPage = parcelService.getAllParcels(pageable);
        PagedModel<EntityModel<ParcelDTO>> pagedModel = pagedResourcesAssembler.toModel(parcelPage);
        return ResponseEntity.ok(pagedModel);
    }


    @GetMapping("track/{id}")
    public ResponseEntity<ParcelDTO> getParcelById(@PathVariable Long id) {
        Optional<ParcelDTO> optionalParcel = parcelService.getParcelByTrackingId(id);
        return optionalParcel.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }


}

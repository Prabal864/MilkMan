package com.micronauticals.parcel.controller;

import com.micronauticals.parcel.dto.ParcelDTO;
import com.micronauticals.parcel.service.ParcelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    /**
     * Upload parcel list response entity.
     *
     * @param parcelDTOList the parcel dto list
     * @return the response entity
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadParcelList(@RequestBody List<ParcelDTO> parcelDTOList) {
        parcelService.saveParcels(parcelDTOList);
        return ResponseEntity.ok("Parcels uploaded successfully");
    }

    /**
     * Gets all parcels.
     *
     * @return the all parcels
     */
    @GetMapping("/all")
    public ResponseEntity<List<ParcelDTO>> getAllParcels() {
        return ResponseEntity.ok(parcelService.getAllParcels());
    }

    /**
     * Gets parcel by id.
     *
     * @param id the id
     * @return the parcel by id
     */
    @GetMapping("findbyid/{id}")
    public ResponseEntity<ParcelDTO> getParcelById(@PathVariable Long id) {
        Optional<ParcelDTO> optionalParcel = parcelService.getParcelByTrackingId(id);
        return optionalParcel.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }


}

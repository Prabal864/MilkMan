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

    private ParcelService parcelService;

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
        Optional<ParcelDTO> optionalParcel = parcelService.getParcelById(id);
        return optionalParcel.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    /**
     * Delete parcel response entity.
     *
     * @param id the id
     * @return the response entity
     */
    @DeleteMapping("deletebyid/{id}")
    public ResponseEntity<String> deleteParcel(@PathVariable Long id) {
        boolean deleted = parcelService.deleteParcel(id);
        if (deleted) return ResponseEntity.ok("Parcel deleted successfully");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parcel not found");
    }

    @DeleteMapping("/delete-by-contact")
    public ResponseEntity<String> deleteByContact(@RequestParam String number) {
        boolean deleted = parcelService.deleteByContactNumber(number);
        if (deleted) {
            return ResponseEntity.ok("Deleted parcels with contact number: " + number);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No parcel found for contact number: " + number);
        }
    }



    /**
     * Search by customer name response entity.
     *
     * @param name the name
     * @return the response entity
     */
    @GetMapping("/search-by-name")
    public ResponseEntity<List<ParcelDTO>> searchByCustomerName(@RequestParam String name) {
        List<ParcelDTO> results = parcelService.searchParcelsByName(name);
        if (results.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(results);
    }

    /**
     * Search by contact number response entity.
     *
     * @param contactNumber the contact number
     * @return the response entity
     */
    @GetMapping("/search-by-number")
    public ResponseEntity<List<ParcelDTO>> searchByContactNumber(@RequestParam String contactNumber) {
        List<ParcelDTO> results = parcelService.searchParcelsByContactNumber(contactNumber);
        if (results.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(results);
    }

    /**
     * Update parcel response entity.
     *
     * @param id         the id
     * @param updatedDto the updated dto
     * @return the response entity
     */
    @PutMapping("update/{id}")
    public ResponseEntity<String> updateParcel(@PathVariable Long id, @RequestBody ParcelDTO updatedDto) {
        boolean updated = parcelService.updateParcel(id, updatedDto);
        if (updated) {
            return ResponseEntity.ok("Parcel updated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parcel not found with ID: " + id);
        }
    }

    /**
     * Gets parcels by size.
     *
     * @param size the size
     * @return the parcels by size
     */
    @GetMapping("filter/by-size")
    public ResponseEntity<List<ParcelDTO>> getParcelsBySize(@RequestParam String size) {
        List<ParcelDTO> parcels = parcelService.getParcelsBySize(size);
        if (parcels.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(parcels);
    }

    /**
     * Gets parcels by weight range.
     *
     * @param min the min
     * @param max the max
     * @return the parcels by weight range
     */
    @GetMapping("filter/by-weight")
    public ResponseEntity<List<ParcelDTO>> getParcelsByWeightRange(
            @RequestParam double min,
            @RequestParam double max) {
        List<ParcelDTO> parcels = parcelService.getParcelsByWeightRange(min, max);
        if (parcels.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(parcels);
        }
        return ResponseEntity.ok(parcels);
    }

}

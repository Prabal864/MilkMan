package com.micronauticals.parcel.service;

import com.micronauticals.parcel.dto.ParcelDTO;
import com.micronauticals.parcel.entity.Parcel;
import com.micronauticals.parcel.repo.ParcelRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class ParcelService {

    private final ParcelRepo parcelRepo;

    public ParcelService(ParcelRepo parcelRepo) {
        this.parcelRepo = parcelRepo;
    }


    private ParcelDTO mapToParcelDTO(Parcel parcel) {
        ParcelDTO dto = new ParcelDTO();
        dto.setCustomerName(parcel.getCustomerName());
        dto.setDeliveryAddress(parcel.getDeliveryAddress());
        dto.setContactNumber(parcel.getContactNumber());
        dto.setTrackingId(parcel.getTrackingId());
        return dto;
    }


    /**
     * Gets all parcels.
     *
     * @return the all parcels
     */
    public Page<ParcelDTO> getAllParcels(Pageable pageable) {
        return parcelRepo.findAll(pageable)
                .map(this::mapToParcelDTO);
    }

    /**
     * Gets parcel by id.
     *
     * @param id the id
     * @return the parcel by id
     */
    public Optional<ParcelDTO> getParcelByTrackingId(Long id) {
        return parcelRepo.findByTrackingId(id).map(this::mapToParcelDTO);
    }



}
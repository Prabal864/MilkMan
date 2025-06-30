package com.micronauticals.parcel.service;

import com.micronauticals.parcel.dto.ParcelDTO;
import com.micronauticals.parcel.entity.Parcel;
import com.micronauticals.parcel.repo.ParcelRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ParcelService {

    private ParcelRepo parcelRepo;

    public ParcelService(ParcelRepo parcelRepo) {
        this.parcelRepo = parcelRepo;
    }


    private ParcelDTO mapToParcelDTO(Parcel parcel) {
        ParcelDTO dto = new ParcelDTO();
        dto.setCustomerName(parcel.getCustomerName());
        dto.setDeliveryAddress(parcel.getDeliveryAddress());
        dto.setContactNumber(parcel.getContactNumber());
        dto.setSize(parcel.getSize());
        dto.setWeight(parcel.getWeight());
        return dto;
    }

    /**
     * Save parcels.
     *
     * @param parcelDTOList the parcel dto list
     */
    public void saveParcels(List<ParcelDTO> parcelDTOList) {
        List<Parcel> parcels = parcelDTOList.stream().map(dto -> {
            Parcel p = new Parcel();

            p.setCustomerName(dto.getCustomerName());
            p.setDeliveryAddress(dto.getDeliveryAddress());
            p.setContactNumber(dto.getContactNumber());
            p.setSize(dto.getSize());
            p.setWeight(dto.getWeight());
            return p;
        }).collect(Collectors.toList());
        parcelRepo.saveAll(parcels);
    }


    /**
     * Gets all parcels.
     *
     * @return the all parcels
     */
    public List<ParcelDTO> getAllParcels() {
        return parcelRepo.findAll().stream()
                .map(this::mapToParcelDTO)
                .collect(Collectors.toList());
    }

    /**
     * Gets parcel by id.
     *
     * @param id the id
     * @return the parcel by id
     */
    public Optional<ParcelDTO> getParcelById(Long id) {
        return parcelRepo.findById(id).map(this::mapToParcelDTO);
    }


}
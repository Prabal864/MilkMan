package com.micronauticals.parcel.service;

import com.micronauticals.parcel.dto.ParcelDTO;
import com.micronauticals.parcel.entity.Parcel;
import com.micronauticals.parcel.repo.ParcelRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class ParcelService {

    private ParcelRepo parcelRepo;

    public ParcelService(@Autowired ParcelRepo parcelRepo) {
        this.parcelRepo = parcelRepo;
    }

    /**
     * Generate unique parcel id long.
     *
     * @return the long
     */

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


    /**
     * Delete parcel boolean.
     *
     * @param id the id
     * @return the boolean
     */
    public boolean deleteParcel(Long id) {
        if (!parcelRepo.existsById(id)) return false;
        parcelRepo.deleteById(id);
        return true;
    }


    /**
     * Delete by contact number boolean.
     *
     * @param contactNumber the contact number
     * @return the boolean
     */
    public boolean deleteByContactNumber(String contactNumber) {
        contactNumber = contactNumber.trim();
        List<Parcel> parcels = parcelRepo.findAllByContactNumber(contactNumber);
        if (!parcels.isEmpty()) {
            parcelRepo.deleteAll(parcels);
            return true;
        }
        return false;
    }



    /**
     * Search parcels by name list.
     *
     * @param name the name
     * @return the list
     */
    public List<ParcelDTO> searchParcelsByName(String name) {
        name = name.trim();
        List<Parcel> parcels = parcelRepo.findByCustomerNameContainingIgnoreCase(name);
        return parcels.stream()
                .map(this::mapToParcelDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update parcel boolean.
     *
     * @param id         the id
     * @param updatedDto the updated dto
     * @return the boolean
     */
    public boolean updateParcel(Long id, ParcelDTO updatedDto) {
        Optional<Parcel> optionalParcel = parcelRepo.findById(id);
        if (optionalParcel.isPresent()) {
            Parcel parcel = optionalParcel.get();
            parcel.setCustomerName(updatedDto.getCustomerName());
            parcel.setDeliveryAddress(updatedDto.getDeliveryAddress());
            parcel.setContactNumber(updatedDto.getContactNumber());
            parcel.setSize(updatedDto.getSize());
            parcel.setWeight(updatedDto.getWeight());
            parcelRepo.save(parcel);
            return true;
        }
        return false;
    }

    /**
     * Gets parcels by size.
     *
     * @param size the size
     * @return the parcels by size
     */
    public List<ParcelDTO> getParcelsBySize(String size) {
        List<Parcel> parcels = parcelRepo.findBySizeIgnoreCase(size);
        return parcels.stream()
                .map(this::mapToParcelDTO)
                .collect(Collectors.toList());
    }

    /**
     * Gets parcels by weight range.
     *
     * @param min the min
     * @param max the max
     * @return the parcels by weight range
     */
    public List<ParcelDTO> getParcelsByWeightRange(double min, double max) {
        List<Parcel> parcels = parcelRepo.findByWeightBetween(min, max);
        return parcels.stream()
                .map(this::mapToParcelDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search parcels by contact number list.
     *
     * @param contactNumber the contact number
     * @return the list
     */
    public List<ParcelDTO> searchParcelsByContactNumber(String contactNumber){
        List<Parcel> parcels = parcelRepo.findByContactNumber(contactNumber);
        return parcels.stream()
                .map(this::mapToParcelDTO)
                .collect(Collectors.toList());
    }


}
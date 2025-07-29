package com.micronauticals.parcel.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.micronauticals.parcel.dto.PageResult;
import com.micronauticals.parcel.dto.ParcelDTO;
import com.micronauticals.parcel.entity.Parcel;
import com.micronauticals.parcel.repo.ParcelRepo;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import java.util.*;

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

    public PageResult<ParcelDTO> getAllParcels(Integer limit, String startKey) {
        PageResult<Parcel> page = parcelRepo.findAllPaginated(limit, startKey);
        List<ParcelDTO> dtos = new ArrayList<>();
        for (Parcel parcel : page.getItems()) {
            dtos.add(mapToParcelDTO(parcel));
        }

        PageResult<ParcelDTO> result = new PageResult<>();
        result.setItems(dtos);

        Map<String, AttributeValue> lastEvaluatedKey = page.getLastEvaluatedKey();
        result.setLastEvaluatedKey(lastEvaluatedKey);

        if (lastEvaluatedKey != null && !lastEvaluatedKey.isEmpty()) {
            Map<String, String> stringKeyMap = new HashMap<>();
            for (Map.Entry<String, AttributeValue> entry : lastEvaluatedKey.entrySet()) {
                AttributeValue value = entry.getValue();
                stringKeyMap.put(entry.getKey(), value.s());
            }

            try {
                String encodedKey = Base64.getEncoder().encodeToString(
                        new ObjectMapper().writeValueAsBytes(stringKeyMap)
                );
                result.setNextStartKey(encodedKey);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to encode nextStartKey", e);
            }
        } else {
            result.setNextStartKey(null);
        }
        return result;
    }

    public Optional<ParcelDTO> getParcelByTrackingId(String id) {
        return parcelRepo.findByTrackingId(id).map(this::mapToParcelDTO);
    }
}
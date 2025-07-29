package com.micronauticals.parcel.service;

import com.micronauticals.parcel.dto.DeliveryOrderDTO;
import com.micronauticals.parcel.dto.PageResult;
import com.micronauticals.parcel.entity.DeliveryOrder;
import com.micronauticals.parcel.entity.Parcel;
import com.micronauticals.parcel.entity.Vendor;
import com.micronauticals.parcel.repo.DeliveryOrderRepo;
import com.micronauticals.parcel.repo.ParcelRepo;
import com.micronauticals.parcel.repo.VendorRepo;
import com.micronauticals.parcel.utility.DynamoDbPaginationUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.*;

@Service
public class DeliveryOrderService {

    private final DeliveryOrderRepo deliveryOrderRepo;
    private final VendorRepo vendorRepo;
    private final ParcelRepo parcelRepo;
    private final DynamoDbPaginationUtil dynamoDbPaginationUtil;

    public DeliveryOrderService(DeliveryOrderRepo deliveryOrderRepo, VendorRepo vendorRepo, ParcelRepo parcelRepo, DynamoDbPaginationUtil dynamoDbPaginationUtil) {
        this.deliveryOrderRepo = deliveryOrderRepo;
        this.vendorRepo = vendorRepo;
        this.parcelRepo = parcelRepo;
        this.dynamoDbPaginationUtil = dynamoDbPaginationUtil;
    }

    private DeliveryOrderDTO mapToDTO(DeliveryOrder order) {
        DeliveryOrderDTO dto = new DeliveryOrderDTO();
        dto.setDeliveryDate(order.getDeliveryDate());
        dto.setVendorName(order.getVendorName());
        dto.setFileLink(order.getFileLink());
        return dto;
    }

    public PageResult<DeliveryOrderDTO> getOrdersForToday(Integer limit, String startKey) {
        PageResult<DeliveryOrder> orderPage = deliveryOrderRepo.findByDeliveryDate(LocalDate.now(), limit, startKey);
        List<DeliveryOrderDTO> dtos = new ArrayList<>();
        for (DeliveryOrder order : orderPage.getItems()) {
            dtos.add(mapToDTO(order));
        }

        PageResult<DeliveryOrderDTO> result = new PageResult<>();
        result.setItems(dtos);

        Map<String, AttributeValue> lastEvaluatedKey = orderPage.getLastEvaluatedKey();
        result.setLastEvaluatedKey(lastEvaluatedKey);

        if (lastEvaluatedKey != null && !lastEvaluatedKey.isEmpty()) {
            String encodedKey = dynamoDbPaginationUtil.encodeStartKey(lastEvaluatedKey);
            result.setNextStartKey(encodedKey);
        } else {
            result.setNextStartKey(null);
        }

        return result;
    }

    public PageResult<DeliveryOrderDTO> getOrdersForVendorAndDate(String vendorName, LocalDate date, Integer limit, String startKey) {
        PageResult<DeliveryOrder> orderPage;

        if (vendorName != null && date != null) {
            orderPage = deliveryOrderRepo.findByVendorNameAndDeliveryDate(vendorName, date, limit, startKey);
        } else if (vendorName != null) {
            orderPage = deliveryOrderRepo.findByVendorNameAndDeliveryDate(vendorName, LocalDate.now(), limit, startKey);
        } else if (date != null) {
            orderPage = deliveryOrderRepo.findByDeliveryDate(date, limit, startKey);
        } else {
            orderPage = deliveryOrderRepo.findByDeliveryDate(LocalDate.now(), limit, startKey);
        }

        List<DeliveryOrderDTO> dtos = new ArrayList<>();
        for (DeliveryOrder order : orderPage.getItems()) {
            dtos.add(mapToDTO(order));
        }

        PageResult<DeliveryOrderDTO> result = new PageResult<>();
        result.setItems(dtos);

        Map<String, AttributeValue> lastEvaluatedKey = orderPage.getLastEvaluatedKey();
        result.setLastEvaluatedKey(lastEvaluatedKey);

        if (lastEvaluatedKey != null && !lastEvaluatedKey.isEmpty()) {
            String encodedKey = dynamoDbPaginationUtil.encodeStartKey(lastEvaluatedKey);
            result.setNextStartKey(encodedKey);
        } else {
            result.setNextStartKey(null);
        }

        return result;
    }

    public DeliveryOrderDTO uploadOrderFile(String vendorName, LocalDate deliveryDate, MultipartFile file) {
        Vendor vendor = vendorRepo.findByName(vendorName);
        if (vendor == null) {
            vendor = new Vendor();
            vendor.setId(UUID.randomUUID().toString());
            vendor.setVendorName(vendorName);
            vendor = vendorRepo.save(vendor);
        }

        List<Parcel> parcels = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length < 5) throw new RuntimeException("Invalid line: " + line);
                    Parcel parcel = new Parcel();
                    parcel.setContactNumber(parts[0].trim());
                    parcel.setCustomerName(parts[1].trim());
                    parcel.setDeliveryAddress(parts[2].trim());
                    parcel.setSize(parts[3].trim());
                    parcel.setWeight(Double.parseDouble(parts[4].trim()));
                    parcel.setTrackingId(UUID.randomUUID().toString());
                    parcels.add(parcel);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to parse uploaded file", e);
        }

        DeliveryOrder order = new DeliveryOrder();
        order.setId(UUID.randomUUID().toString());
        order.setVendorId(vendor.getId());
        order.setVendorName(vendorName);
        order.setDeliveryDate(deliveryDate);
        order.setFileLink("/files/" + file.getOriginalFilename());

        deliveryOrderRepo.save(order);

        for (Parcel parcel : parcels) {
            parcel.setOrderId(order.getId());
            parcelRepo.save(parcel);
        }

        return mapToDTO(order);
    }
}

package com.micronauticals.parcel.service;

import com.micronauticals.parcel.dto.DeliveryOrderDTO;
import com.micronauticals.parcel.entity.DeliveryOrder;
import com.micronauticals.parcel.entity.Parcel;
import com.micronauticals.parcel.entity.Vendor;
import com.micronauticals.parcel.repo.DeliveryOrderRepo;
import com.micronauticals.parcel.repo.VendorRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class DeliveryOrderService {

    private final DeliveryOrderRepo deliveryOrderRepo;
    private final VendorRepo vendorRepo;

    public DeliveryOrderService(DeliveryOrderRepo deliveryOrderRepo, VendorRepo vendorRepo) {
        this.deliveryOrderRepo = deliveryOrderRepo;
        this.vendorRepo = vendorRepo;
    }

    private DeliveryOrderDTO mapToDTO(DeliveryOrder order) {
        DeliveryOrderDTO dto = new DeliveryOrderDTO();
        dto.setDeliveryDate(order.getDeliveryDate());
        dto.setVendorName(order.getVendor() != null ? order.getVendor().getName() : null);
        dto.setTotalOrders(order.getParcels() != null ? order.getParcels().size() : 0);
        dto.setFileLink(order.getFileLink());
        return dto;
    }

    public Page<DeliveryOrderDTO> getOrdersForToday(Pageable pageable) {
        LocalDate today = LocalDate.now();
        Page<DeliveryOrder> page = deliveryOrderRepo.findByDeliveryDate(today, pageable);
        return page.map(this::mapToDTO);
    }

    public Page<DeliveryOrderDTO> getOrdersForVendorAndDate(String vendorName, LocalDate date, Pageable pageable) {
        if (vendorName != null && date != null) {
            Page<DeliveryOrder> page = deliveryOrderRepo.findByVendorNameAndDeliveryDate(vendorName, date, pageable);
            return page.map(this::mapToDTO);
        } else if (vendorName != null) {
            Page<DeliveryOrder> page = deliveryOrderRepo.findByVendorNameAndDeliveryDate(vendorName, LocalDate.now(), pageable);
            return page.map(this::mapToDTO);
        } else if (date != null) {
            Page<DeliveryOrder> page = deliveryOrderRepo.findByDeliveryDate(date, pageable);
            return page.map(this::mapToDTO);
        } else {
            return getOrdersForToday(pageable);
        }
    }

    public DeliveryOrderDTO uploadOrderFile(String vendorName, LocalDate deliveryDate, MultipartFile file) {
        Vendor vendor = vendorRepo.findByName(vendorName);
        if (vendor == null) {
            vendor = new Vendor();
            vendor.setName(vendorName);
            vendor = vendorRepo.save(vendor);
        }

        List<Parcel> parcels = new ArrayList<>();
        boolean hasHeader = false;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isFirstLine = true;

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

                    parcels.add(parcel);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to parse uploaded file", e);
        }

        DeliveryOrder order = new DeliveryOrder();
        order.setVendor(vendor);
        order.setDeliveryDate(deliveryDate);
        order.setParcels(parcels);
        order.setFileLink("/files/" + file.getOriginalFilename());

        DeliveryOrder saved = deliveryOrderRepo.save(order);
        return mapToDTO(saved);
    }


}
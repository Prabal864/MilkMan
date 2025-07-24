package com.micronauticals.parcel.repo;
import com.micronauticals.parcel.entity.DeliveryOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface DeliveryOrderRepo extends JpaRepository<DeliveryOrder, Long> {
    Page<DeliveryOrder> findByVendorNameAndDeliveryDate(String vendorName, LocalDate deliveryDate, Pageable pageable);
    Page<DeliveryOrder> findByDeliveryDate(LocalDate deliveryDate, Pageable pageable);
}

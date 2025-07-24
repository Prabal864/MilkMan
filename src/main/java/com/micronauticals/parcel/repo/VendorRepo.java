package com.micronauticals.parcel.repo;
import com.micronauticals.parcel.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;


public interface VendorRepo extends JpaRepository<Vendor, Long> {
    Vendor findByName(String name);
}

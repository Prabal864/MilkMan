package com.micronauticals.parcel.repo;
import com.micronauticals.parcel.entity.Parcel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface ParcelRepo extends JpaRepository<Parcel, Long> {
    Optional<Parcel> findByTrackingId(Long trackingId);

}

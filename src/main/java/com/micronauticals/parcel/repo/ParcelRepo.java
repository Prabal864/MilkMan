package com.micronauticals.parcel.repo;
import com.micronauticals.parcel.entity.Parcel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ParcelRepo extends JpaRepository<Parcel, Long> {

}

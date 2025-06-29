package com.micronauticals.parcel.repo;
import com.micronauticals.parcel.entity.Parcel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ParcelRepo extends JpaRepository<Parcel, Long> {
    /**
     * Find by customer name ignore case list.
     *
     * @param name the name
     * @return the list
     */
    List<Parcel> findByCustomerNameContainingIgnoreCase(String name);

    /**
     * Find by size ignore case list.
     *
     * @param size the size
     * @return the list
     */
    List<Parcel> findBySizeIgnoreCase(String size);

    /**
     * Find by weight between list.
     *
     * @param min the min
     * @param max the max
     * @return the list
     */
    List<Parcel> findByWeightBetween(double min, double max);

    /**
     * Find by contact number list.
     *
     * @param contactNumber the contact number
     * @return the list
     */
    List<Parcel> findByContactNumber(String contactNumber);

    List<Parcel> findAllByContactNumber(String contactNumber);

}

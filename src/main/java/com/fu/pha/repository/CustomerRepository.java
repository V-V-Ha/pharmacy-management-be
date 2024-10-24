package com.fu.pha.repository;

import com.fu.pha.dto.response.CustomerDTOResponse;
import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {


    @Query("SELECT new com.fu.pha.dto.response.CustomerDTOResponse(c) FROM Customer c WHERE " +
            "(LOWER(c.phoneNumber) LIKE LOWER(CONCAT('%', :phoneNumber, '%')) OR :phoneNumber IS NULL OR :phoneNumber = '') " +
            "ORDER BY c.lastModifiedDate DESC")
    Page<CustomerDTOResponse> getListCustomerPaging(String phoneNumber, Pageable pageable);


    Optional<Customer> findByPhoneNumber(String phoneNumber);

    Optional<Customer> findById(Long id);

}

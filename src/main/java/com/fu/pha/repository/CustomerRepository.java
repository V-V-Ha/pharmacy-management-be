package com.fu.pha.repository;

import com.fu.pha.dto.response.CustomerDTOResponse;
import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.entity.Customer;
import com.fu.pha.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {


    @Query("SELECT new com.fu.pha.dto.response.CustomerDTOResponse(c) FROM Customer c WHERE " +
            "(LOWER(c.phoneNumber) LIKE LOWER(CONCAT('%', :phoneNumber, '%')) OR :phoneNumber IS NULL OR :phoneNumber = '') " +
            " AND (c.status = :status OR :status IS NULL OR :status = '') " +
            "ORDER BY c.lastModifiedDate DESC")
    Page<CustomerDTOResponse> getListCustomerPaging(@Param("phoneNumber") String phoneNumber,
                                                    @Param("status") Status status,
                                                    Pageable pageable);



    Optional<Customer> findByPhoneNumber(String phoneNumber);

    @Query("SELECT new com.fu.pha.dto.response.CustomerDTOResponse(c) FROM Customer c WHERE LOWER(c.phoneNumber) LIKE LOWER(CONCAT('%', :phoneNumber, '%'))")
    Optional<List<CustomerDTOResponse>> findByPhoneNumberContaining(String phoneNumber);

    Optional<Customer> findById(Long id);

    @Query("SELECT new com.fu.pha.dto.response.CustomerDTOResponse(c) FROM Customer c WHERE LOWER(c.customerName) LIKE LOWER(CONCAT('%', :customerName, '%'))")
    Optional<List<CustomerDTOResponse>> findByCustomerName(String customerName);

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.createDate BETWEEN :startDate AND :endDate")
    long countNewCustomersBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.createDate < :startDate")
    long countOldCustomersBeforeDate(@Param("startDate") Instant startDate);

    @Query("SELECT COUNT(c) FROM Customer c")
    long countTotalCustomers();
}

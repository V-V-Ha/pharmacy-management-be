package com.fu.pha.repository;

import com.fu.pha.dto.response.CustomerDTOResponse;
import com.fu.pha.dto.response.report.customer.CustomerInvoiceProjection;
import com.fu.pha.entity.Customer;
import com.fu.pha.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {


    @Query("SELECT new com.fu.pha.dto.response.CustomerDTOResponse(c) FROM Customer c WHERE " +
            "(LOWER(c.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            " OR LOWER(c.customerName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            " OR :keyword IS NULL OR :keyword = '') " +
            " AND (c.status = :status OR :status IS NULL OR :status = '') " +
            "ORDER BY c.lastModifiedDate DESC")
    Page<CustomerDTOResponse> getListCustomerPaging(@Param("keyword") String keyword,
                                                    @Param("status") Status status,
                                                    Pageable pageable);



    Optional<Customer> findByPhoneNumber(String phoneNumber);

    @Query("SELECT new com.fu.pha.dto.response.CustomerDTOResponse(c) FROM Customer c WHERE " +
            " LOWER(c.phoneNumber) LIKE LOWER(CONCAT('%', :phoneNumber, '%')) " +
            " OR LOWER(c.customerName) LIKE LOWER(CONCAT('%', :phoneNumber, '%')) and c.status = 'ACTIVE' ")
    Optional<List<CustomerDTOResponse>> findByPhoneNumberContaining(String phoneNumber);

    Optional<Customer> findById(Long id);

    @Query("SELECT new com.fu.pha.dto.response.CustomerDTOResponse(c) FROM Customer c WHERE LOWER(c.customerName) LIKE LOWER(CONCAT('%', :customerName, '%')) " +
            " AND c.status = 'ACTIVE'")
    Optional<List<CustomerDTOResponse>> findByCustomerName(String customerName);

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.createDate BETWEEN :startDate AND :endDate AND c.id != 1")
    long countNewCustomersBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.createDate < :startDate AND c.id != 1")
    long countOldCustomersBeforeDate(@Param("startDate") Instant startDate);

    @Query("SELECT COUNT(c) FROM Customer c")
    long countTotalCustomers();

    @Query(
            value = "SELECT c.id AS customerId, " +
                    "c.customer_name AS customerName, " +
                    "c.phone_number AS phoneNumber, " +
                    "COALESCE(sub.invoice_count, 0) AS invoiceCount, " +
                    "COALESCE(sub.total_quantity, 0) AS totalProductQuantity, " +
                    "COALESCE(sub.total_amount, 0) AS totalAmount " +
                    "FROM customer c " +
                    "LEFT JOIN ( " +
                    "    SELECT so.customer_id, " +
                    "           COUNT(DISTINCT so.id) AS invoice_count, " +
                    "           SUM(soi.quantity) AS total_quantity, " +
                    "           SUM(so.total_amount) AS total_amount " +
                    "    FROM sale_order so " +
                    "    LEFT JOIN sale_order_item soi ON so.id = soi.sale_order_id " +
                    "    WHERE so.payment_status = 'PAID' " +
                    "    GROUP BY so.customer_id " +
                    ") sub ON c.id = sub.customer_id " +
                    "WHERE (:searchTerm IS NULL OR LOWER(c.customer_name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                    "       OR c.phone_number LIKE CONCAT('%', :searchTerm, '%')) " +
                    "AND (:isNewCustomer IS NULL OR " +
                    "     (:isNewCustomer = TRUE AND c.create_date BETWEEN :startDate AND :endDate) OR " +
                    "     (:isNewCustomer = FALSE AND c.create_date < :startDate))" +
                    "ORDER BY COALESCE(sub.total_amount, 0) DESC",
            countQuery = "SELECT COUNT(*) FROM customer c " +
                    "LEFT JOIN ( " +
                    "    SELECT so.customer_id " +
                    "    FROM sale_order so " +
                    "    WHERE so.payment_status = 'PAID' " +
                    "    GROUP BY so.customer_id " +
                    ") sub ON c.id = sub.customer_id " +
                    "WHERE (:searchTerm IS NULL OR LOWER(c.customer_name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                    "       OR c.phone_number LIKE CONCAT('%', :searchTerm, '%')) " +
                    "AND (:isNewCustomer IS NULL OR " +
                    "     (:isNewCustomer = TRUE AND c.create_date BETWEEN :startDate AND :endDate) OR " +
                    "     (:isNewCustomer = FALSE AND c.create_date < :startDate))",
            nativeQuery = true
    )
    Page<CustomerInvoiceProjection> findCustomerInvoices(
            @Param("searchTerm") String searchTerm,
            @Param("isNewCustomer") Boolean isNewCustomer,
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate,
            Pageable pageable);


}

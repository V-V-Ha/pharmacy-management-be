package com.fu.pha.repository;

import com.fu.pha.dto.request.SupplierDto;
import com.fu.pha.dto.response.report.supplier.SupplierInvoiceProjection;
import com.fu.pha.entity.Supplier;
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

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    @Override
    Optional<Supplier> findById(Long id);

    Optional<Supplier> findByTax(String tax);

    //get all supplier
    @Query("SELECT new com.fu.pha.dto.request.SupplierDto(s.id, s.supplierName, s.address, s.phoneNumber, s.email, s.tax, s.status) FROM Supplier s")
    List<SupplierDto> findAllSupplier();

    @Query("SELECT new com.fu.pha.dto.request.SupplierDto(s.id, s.supplierName, s.address, s.phoneNumber, s.email, s.tax, s.status, " +
            "COALESCE(SUM(i.totalAmount), 0.0)) " +
            "FROM Supplier s " +
            "LEFT JOIN Import i ON s.id = i.supplier.id " +
            "WHERE (LOWER(s.supplierName) LIKE LOWER(CONCAT('%', :name, '%')) OR :name IS NULL OR :name = '') " +
            " AND (s.status = :status OR :status IS NULL OR :status = '') " +
            "GROUP BY s.id, s.supplierName, s.address, s.phoneNumber, s.email, s.tax " +
            "ORDER BY s.lastModifiedDate DESC")
    Page<SupplierDto> findAllByNameContaining(@Param("name") String name,
                                              @Param("status") Status status,
                                              Pageable pageable);


    @Query("SELECT new com.fu.pha.dto.request.SupplierDto(s.id, s.supplierName, s.address, s.phoneNumber, s.email, s.tax, s.status) " +
            "FROM Supplier s " +
            "WHERE (LOWER(s.supplierName) LIKE LOWER(CONCAT('%', :supplierName, '%')) OR :supplierName IS NULL OR :supplierName = '') " +
            " AND s.status = 'ACTIVE'")
    Optional<List<SupplierDto>> findSupplierBySupplierName(String supplierName);

    Optional<Supplier> findByPhoneNumber(String phoneNumber);

    //report
    @Query("SELECT COUNT(s) FROM Supplier s WHERE s.createDate BETWEEN :startDate AND :endDate")
    long countNewSuppliersBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT COUNT(s) FROM Supplier s WHERE s.createDate < :startDate")
    long countOldSuppliersBeforeDate(@Param("startDate") Instant startDate);

    @Query("SELECT COUNT(s) FROM Supplier s")
    long countTotalSuppliers();

    @Query(
            value = "SELECT s.id AS supplierId, " +
                    "s.supplier_name AS supplierName, " +
                    "s.phone_number AS phoneNumber, " +
                    "COALESCE(sub.invoice_count, 0) AS invoiceCount, " +
                    "COALESCE(sub.total_quantity, 0) AS totalProductQuantity, " +
                    "COALESCE(sub_return.total_return_amount, 0) AS totalReturnAmount, " +
                    "COALESCE(sub.total_import_amount, 0) AS totalImportAmount " +
                    "FROM supplier s " +
                    "LEFT JOIN ( " +
                    "    SELECT i.supplier_id, " +
                    "           COUNT(DISTINCT i.id) AS invoice_count, " +
                    "           SUM(ii.quantity) AS total_quantity, " +
                    "           SUM(i.total_amount) AS total_import_amount " +
                    "    FROM import i " +
                    "    JOIN import_item ii ON i.id = ii.import_id " +
                    "    WHERE i.status = 'CONFIRMED' " +
                    "    GROUP BY i.supplier_id " +
                    ") sub ON s.id = sub.supplier_id " +
                    "LEFT JOIN ( " +
                    "    SELECT es.supplier_id, " +
                    "           SUM(esi.total_amount) AS total_return_amount " +
                    "    FROM export_slip es " +
                    "    JOIN export_slip_item esi ON es.id = esi.export_slip_id " +
                    "    WHERE es.type_delivery = 'RETURN_TO_SUPPLIER' " +
                    "    GROUP BY es.supplier_id " +
                    ") sub_return ON s.id = sub_return.supplier_id " +
                    "WHERE (:name IS NULL OR LOWER(s.supplier_name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
                    "AND (:isNewSupplier IS NULL OR " +
                    "     (:isNewSupplier = TRUE AND s.create_date BETWEEN :startDate AND :endDate) OR " +
                    "     (:isNewSupplier = FALSE AND s.create_date < :startDate))" +
                    "ORDER BY COALESCE(sub.total_import_amount, 0) DESC",
            countQuery = "SELECT COUNT(*) FROM supplier s " +
                    "WHERE (:name IS NULL OR LOWER(s.supplier_name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
                    "AND (:isNewSupplier IS NULL OR " +
                    "     (:isNewSupplier = TRUE AND s.create_date BETWEEN :startDate AND :endDate) OR " +
                    "     (:isNewSupplier = FALSE AND s.create_date < :startDate))",
            nativeQuery = true)
    Page<SupplierInvoiceProjection> findSupplierInvoices(
            @Param("name") String name,
            @Param("isNewSupplier") Boolean isNewSupplier,
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate,
            Pageable pageable);
}

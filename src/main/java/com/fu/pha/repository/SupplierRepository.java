package com.fu.pha.repository;

import com.fu.pha.dto.request.SupplierDto;
import com.fu.pha.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    @Override
    Optional<Supplier> findById(Long id);

    Optional<Supplier> findByTax(String tax);

    //get all supplier
    @Query("SELECT new com.fu.pha.dto.request.SupplierDto(s.id, s.supplierName, s.address, s.phoneNumber, s.email, s.tax) FROM Supplier s")
    List<SupplierDto> findAllSupplier();

    @Query("SELECT new com.fu.pha.dto.request.SupplierDto(s.id, s.supplierName, s.address, s.phoneNumber, s.email, s.tax, " +
            "COALESCE(SUM(i.totalAmount), 0.0)) " +
            "FROM Supplier s " +
            "LEFT JOIN Import i ON s.id = i.supplier.id " +
            "WHERE (LOWER(s.supplierName) LIKE LOWER(CONCAT('%', :name, '%')) OR :name IS NULL OR :name = '') " +
            "GROUP BY s.id, s.supplierName, s.address, s.phoneNumber, s.email, s.tax " +
            "ORDER BY s.lastModifiedDate DESC")
    Page<SupplierDto> findAllByNameContaining(String name, Pageable pageable);


    @Query("SELECT new com.fu.pha.dto.request.SupplierDto(s.id, s.supplierName, s.address, s.phoneNumber, s.email, s.tax) " +
            "FROM Supplier s " +
            "WHERE (LOWER(s.supplierName) LIKE LOWER(CONCAT('%', :supplierName, '%')) OR :supplierName IS NULL OR :supplierName = '') ")
    Optional<List<SupplierDto>> findSupplierBySupplierName(String supplierName);

    Optional<Supplier> findByPhoneNumber(String phoneNumber);

    //report
    @Query("SELECT COUNT(s) FROM Supplier s WHERE s.createDate BETWEEN :startDate AND :endDate")
    long countNewSuppliersBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT COUNT(s) FROM Supplier s WHERE s.createDate < :startDate")
    long countOldSuppliersBeforeDate(@Param("startDate") Instant startDate);

    @Query("SELECT COUNT(s) FROM Supplier s")
    long countTotalSuppliers();
}

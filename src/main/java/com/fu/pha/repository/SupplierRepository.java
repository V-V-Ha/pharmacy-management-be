package com.fu.pha.repository;

import com.fu.pha.dto.request.SupplierDto;
import com.fu.pha.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    @Override
    Optional<Supplier> findById(Long id);

    Optional<Supplier> findByTax(String tax);

    //get all supplier
    @Query("SELECT new com.fu.pha.dto.request.SupplierDto(s.id, s.supplierName, s.address, s.phoneNumber, s.email, s.tax) FROM Supplier s")
    List<SupplierDto> findAllSupplier();

    @Query("SELECT new com.fu.pha.dto.request.SupplierDto(s.id, s.supplierName, s.address, s.phoneNumber, s.email, s.tax) FROM Supplier s WHERE " +
            "(LOWER(s.supplierName) LIKE LOWER(CONCAT('%', :name, '%')) OR :name IS NULL OR :name = '') " +
            "ORDER BY s.lastModifiedDate DESC")
    Page<SupplierDto> findAllByNameContaining(String name, Pageable pageable);




}

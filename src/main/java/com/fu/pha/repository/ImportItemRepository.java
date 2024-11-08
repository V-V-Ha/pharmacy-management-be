package com.fu.pha.repository;

import com.fu.pha.dto.response.importPack.ImportItemResponseDto;
import com.fu.pha.dto.response.importPack.ImportItemResponseForExport;
import com.fu.pha.dto.response.report.ImportStockDTO;
import com.fu.pha.dto.response.report.OpeningStockDTO;
import com.fu.pha.entity.ImportItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ImportItemRepository extends JpaRepository<ImportItem, Long> {



    @Modifying
    @Query("DELETE FROM ImportItem ii WHERE ii.importReceipt.id = :importId")
    void deleteByImportId(@Param("importId") Long importId);

    @Query("SELECT ii FROM ImportItem ii WHERE ii.importReceipt.id = :importId")
    List<ImportItem> findByImportId(@Param("importId") Long importId);


    @Query("SELECT i FROM ImportItem i JOIN i.product p WHERE LOWER(p.productName) LIKE LOWER(concat('%', :productName, '%'))")
    List<ImportItem> findImportItemsByProductName(@Param("productName") String productName);


    List<ImportItem> findByProductIdOrderByCreateDateAsc(Long productId);

    @Query("SELECT i FROM ImportItem i WHERE i.expiryDate <= :warningThreshold")
    List<ImportItem> findExpiringProducts(@Param("warningThreshold") Instant warningThreshold);

    @Query("SELECT " +
            "SUM(i.remainingQuantity) AS openingQuantity, " +
            "SUM((i.remainingQuantity * 1.0 / pu.conversionFactor) * i.unitPrice) AS openingValue " +
            "FROM ImportItem i " +
            "JOIN i.unit u " +
            "JOIN ProductUnit pu ON pu.unit.unitName = u AND pu.product = i.product " +
            "JOIN i.importReceipt imp " +
            "WHERE imp.importDate < :startOfPeriod AND i.remainingQuantity > 0")
    Optional<OpeningStockDTO> findOpeningStock(@Param("startOfPeriod") Instant startOfPeriod);



    @Query("SELECT " +
            "SUM(i.quantity * pu.conversionFactor) AS importQuantity, " +
            "SUM((i.quantity * 1.0 / pu.conversionFactor) * i.unitPrice) AS importValue " +
            "FROM ImportItem i " +
            "JOIN i.unit u " +
            "JOIN ProductUnit pu ON pu.unit.unitName = u AND pu.product = i.product " +
            "JOIN i.importReceipt imp " +
            "WHERE imp.importDate BETWEEN :startOfPeriod AND :endOfPeriod")
    Optional<ImportStockDTO> findImportStock(@Param("startOfPeriod") Instant startOfPeriod, @Param("endOfPeriod") Instant endOfPeriod);



    @Query(value = "SELECT COUNT(*) AS expired_quantity " +
            "FROM import_item i " +
            "WHERE i.expiry_date < CURRENT_DATE AND i.remaining_quantity > 0",
            nativeQuery = true)
    Integer findExpiredStock();


    @Query(value = "SELECT COUNT(*) AS near_expiry_quantity " +
            "FROM import_item i " +
            "WHERE i.expiry_date BETWEEN CURRENT_DATE AND (CURRENT_DATE + INTERVAL '90 days') AND i.remaining_quantity > 0",
            nativeQuery = true)
    Integer findNearExpiryStock();
}

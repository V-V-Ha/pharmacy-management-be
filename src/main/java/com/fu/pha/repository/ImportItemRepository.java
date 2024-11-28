package com.fu.pha.repository;

import com.fu.pha.dto.response.report.reportEntity.ImportItemReportDto;
import com.fu.pha.entity.ImportItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ImportItemRepository extends JpaRepository<ImportItem, Long> {



    @Modifying
    @Query("DELETE FROM ImportItem ii WHERE ii.importReceipt.id = :importId")
    void deleteByImportId(@Param("importId") Long importId);

    @Query("SELECT ii FROM ImportItem ii WHERE ii.importReceipt.id = :importId")
    List<ImportItem> findByImportId(@Param("importId") Long importId);


    @Query("SELECT i FROM ImportItem i JOIN i.product p WHERE LOWER(p.productName) LIKE LOWER(concat('%', :productName, '%')) AND i.importReceipt.status = com.fu.pha.enums.OrderStatus.CONFIRMED")
    List<ImportItem> findImportItemsByProductName(@Param("productName") String productName);


    @Query("SELECT ii FROM ImportItem ii WHERE ii.product.id = :productId AND ii.importReceipt.status = com.fu.pha.enums.OrderStatus.CONFIRMED ORDER BY ii.createDate ASC")
    List<ImportItem> findByProductIdOrderByCreateDateAsc(@Param("productId") Long productId);

    // Trong ImportItemRepository
    @Query("SELECT sib.importItem FROM SaleOrderItemBatch sib WHERE sib.saleOrderItem.id = :saleOrderItemId")
    List<ImportItem> findBatchesUsedInSaleOrderItem(@Param("saleOrderItemId") Long saleOrderItemId);

    // report

    @Query("SELECT COALESCE(SUM(ii.quantity * ii.conversionFactor), 0) FROM ImportItem ii WHERE ii.product.id = :productId AND ii.createDate  < :date AND ii.importReceipt.status = com.fu.pha.enums.OrderStatus.CONFIRMED")
    Integer sumQuantityBeforeDateByProduct(@Param("productId") Long productId, @Param("date") Instant date);

    @Query("SELECT COALESCE(SUM(ii.totalAmount), 0) FROM ImportItem ii WHERE ii.product.id = :productId AND ii.createDate < :date AND ii.importReceipt.status = com.fu.pha.enums.OrderStatus.CONFIRMED")
    Double sumAmountBeforeDateByProduct(@Param("productId") Long productId, @Param("date") Instant date);

    @Query("SELECT COALESCE(SUM(ii.quantity * ii.conversionFactor), 0) FROM ImportItem ii WHERE ii.product.id = :productId AND ii.createDate BETWEEN :startDate AND :endDate AND ii.importReceipt.status = com.fu.pha.enums.OrderStatus.CONFIRMED")
    Integer sumQuantityByProductBetweenDates(@Param("productId") Long productId, @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT COALESCE(SUM(ii.totalAmount), 0) FROM ImportItem ii WHERE ii.product.id = :productId AND ii.createDate BETWEEN :startDate AND :endDate AND ii.importReceipt.status = com.fu.pha.enums.OrderStatus.CONFIRMED")
    Double sumAmountByProductBetweenDates(@Param("productId") Long productId, @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);


    @Query("SELECT new com.fu.pha.dto.response.report.reportEntity.ImportItemReportDto(ii.id, ii.product.id, ii.product.productName, ii.remainingQuantity, (ii.remainingQuantity * ii.unitPrice / ii.conversionFactor), ii.expiryDate) " +
            "FROM ImportItem ii WHERE ii.expiryDate <= :currentDate AND ii.remainingQuantity > 0 AND ii.importReceipt.status = com.fu.pha.enums.OrderStatus.CONFIRMED")
    List<ImportItemReportDto> findExpiredItems(@Param("currentDate") Instant currentDate);

    @Query("SELECT new com.fu.pha.dto.response.report.reportEntity.ImportItemReportDto(ii.id, ii.product.id, ii.product.productName, ii.remainingQuantity, (ii.remainingQuantity * ii.unitPrice / ii.conversionFactor), ii.expiryDate) " +
            "FROM ImportItem ii WHERE ii.expiryDate > :currentDate AND ii.expiryDate <= :nearExpiryDate AND ii.remainingQuantity > 0 AND ii.importReceipt.status = com.fu.pha.enums.OrderStatus.CONFIRMED")
    List<ImportItemReportDto> findNearlyExpiredItems(@Param("currentDate") Instant currentDate, @Param("nearExpiryDate") Instant nearExpiryDate);

    @Query("SELECT COALESCE(SUM(ii.quantity * ii.conversionFactor), 0) FROM ImportItem ii WHERE ii.importReceipt.importDate < :startDate AND ii.importReceipt.status = com.fu.pha.enums.OrderStatus.CONFIRMED")
    Integer sumQuantityBeforeDate(@Param("startDate") Instant startDate);


    @Query("SELECT COALESCE(SUM((ii.totalAmount)), 0) FROM ImportItem ii WHERE ii.importReceipt.importDate < :startDate AND ii.importReceipt.status = com.fu.pha.enums.OrderStatus.CONFIRMED")
    Double sumAmountBeforeDate(@Param("startDate") Instant startDate);

    @Query("SELECT COALESCE(SUM(ii.totalAmount), 0) FROM ImportItem ii WHERE ii.importReceipt.status = com.fu.pha.enums.OrderStatus.CONFIRMED")
    Double sumAmountNow();

    @Query("SELECT COALESCE(SUM(ii.quantity * ii.conversionFactor), 0) FROM ImportItem ii WHERE ii.importReceipt.importDate BETWEEN :startDate AND :endDate AND ii.importReceipt.status = com.fu.pha.enums.OrderStatus.CONFIRMED")
    Integer sumQuantityBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT COALESCE(SUM(ii.totalAmount), 0) FROM ImportItem ii WHERE ii.importReceipt.importDate BETWEEN :startDate AND :endDate AND ii.importReceipt.status = com.fu.pha.enums.OrderStatus.CONFIRMED")
    Double sumAmountBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT COALESCE(SUM(ii.quantity * ii.conversionFactor), 0) FROM ImportItem ii WHERE ii.importReceipt.importDate BETWEEN :startDate AND :endDate AND ii.importReceipt.status = com.fu.pha.enums.OrderStatus.CONFIRMED")
    Integer sumTotalImportQuantityBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);


    @Query("SELECT COALESCE(SUM(ii.totalAmount), 0) FROM ImportItem ii WHERE ii.importReceipt.importDate BETWEEN :startDate AND :endDate AND ii.importReceipt.status = com.fu.pha.enums.OrderStatus.CONFIRMED")
    Integer sumTotalQuantityBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query(value = "SELECT ii FROM ImportItem ii " +
            "JOIN ii.product p " +
            "WHERE ii.expiryDate <= CURRENT_DATE " +
            "AND ii.remainingQuantity > 0 " +
            "AND p.status = 'ACTIVE'")
    List<ImportItem> findExpiredProducts();


    List<ImportItem> findByProductId(Long id);
}

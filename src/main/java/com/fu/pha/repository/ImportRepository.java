package com.fu.pha.repository;

import com.fu.pha.dto.request.importPack.ImportViewListDto;
import com.fu.pha.entity.Import;
import com.fu.pha.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ImportRepository extends JpaRepository<Import, Long> {

    @Query("SELECT i.invoiceNumber FROM Import i ORDER BY i.invoiceNumber DESC LIMIT 1")
    String getLastInvoiceNumber();

    @Query("SELECT new com.fu.pha.dto.request.importPack.ImportViewListDto(i) " +
            "FROM Import i " +
            "WHERE (:supplierName IS NULL OR i.supplier.supplierName = :supplierName OR :supplierName = '') " +
            "AND (:status IS NULL OR i.status = :status) " +
            "AND (i.importDate BETWEEN :fromDate AND :toDate) " +
            "ORDER BY i.lastModifiedDate DESC")
    Page<ImportViewListDto> getListImportPaging(@Param("supplierName") String supplierName,
                                                @Param("status") OrderStatus status,
                                                @Param("fromDate") Instant fromDate,
                                                @Param("toDate") Instant toDate,
                                                Pageable pageable);

    @Query("SELECT new com.fu.pha.dto.request.importPack.ImportViewListDto(i) " +
            "FROM Import i " +
            "WHERE (:supplierName IS NULL OR i.supplier.supplierName = :supplierName OR :supplierName = '') " +
            "AND (:status IS NULL OR i.status = :status) " +
            "ORDER BY i.lastModifiedDate DESC")
    Page<ImportViewListDto> getListImportPagingWithoutDate(@Param("supplierName") String supplierName,
                                                           @Param("status") OrderStatus status,
                                                           Pageable pageable);

    @Query("SELECT new com.fu.pha.dto.request.importPack.ImportViewListDto(i) " +
            "FROM Import i " +
            "WHERE (:supplierName IS NULL OR i.supplier.supplierName = :supplierName OR :supplierName = '') " +
            "AND (:status IS NULL OR i.status = :status) " +
            "AND i.importDate >= :fromDate " +
            "ORDER BY i.lastModifiedDate DESC")
    Page<ImportViewListDto> getListImportPagingFromDate(@Param("supplierName") String supplierName,
                                                        @Param("status") OrderStatus status,
                                                        @Param("fromDate") Instant fromDate,
                                                        Pageable pageable);

    @Query("SELECT new com.fu.pha.dto.request.importPack.ImportViewListDto(i) " +
            "FROM Import i " +
            "WHERE (:supplierName IS NULL OR i.supplier.supplierName = :supplierName OR :supplierName = '') " +
            "AND (:status IS NULL OR i.status = :status) " +
            "AND i.importDate <= :toDate " +
            "ORDER BY i.lastModifiedDate DESC")
    Page<ImportViewListDto> getListImportPagingToDate(@Param("supplierName") String supplierName,
                                                      @Param("status") OrderStatus status,
                                                      @Param("toDate") Instant toDate,
                                                      Pageable pageable);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Import i WHERE i.importDate BETWEEN :startDate AND :endDate AND i.status = com.fu.pha.enums.OrderStatus.CONFIRMED")
    Double sumTotalImportAmountBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT COALESCE(SUM(ii.quantity * ii.conversionFactor), 0) FROM ImportItem ii WHERE ii.importReceipt.importDate BETWEEN :startDate AND :endDate AND ii.importReceipt.status = com.fu.pha.enums.OrderStatus.CONFIRMED")
    Integer sumTotalImportQuantityBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) " +
            "FROM Import i " +
            "WHERE i.supplier.createDate BETWEEN :startDate AND :endDate AND i.createDate BETWEEN :startDate AND :endDate AND i.status = com.fu.pha.enums.OrderStatus.CONFIRMED ")
    Double sumTotalImportNewAmountBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) " +
            "FROM Import i " +
            "WHERE i.supplier.createDate < :startDate AND i.createDate BETWEEN :startDate AND :endDate AND i.status = com.fu.pha.enums.OrderStatus.CONFIRMED ")
    Double sumTotalImportAmountBeforeDate(@Param("startDate") Instant startDate ,@Param("endDate") Instant endDate);

    @Query("SELECT new com.fu.pha.dto.request.importPack.ImportViewListDto(i) FROM Import i " +
            "WHERE (i.importDate BETWEEN :fromDate AND :toDate) " +
            " ORDER BY i.lastModifiedDate DESC")
    List<ImportViewListDto> getImportsByDateRange(@Param("fromDate") Instant fromDate,
                                                  @Param("toDate") Instant toDate);

}

package com.fu.pha.repository;

import com.fu.pha.dto.request.importPack.ImportDto;
import com.fu.pha.dto.request.importPack.ImportViewListDto;
import com.fu.pha.entity.Import;
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
            " FROM Import i " +
            " WHERE (:supplierName IS NULL OR i.supplier.supplierName = :supplierName OR :supplierName = '') " +
            " AND (i.importDate IS NULL OR i.importDate BETWEEN :fromDate AND :toDate) " +
            " ORDER BY i.lastModifiedDate DESC")
    Page<ImportViewListDto> getListImportPaging(@Param("supplierName") String supplierName,
                                                @Param("fromDate") Instant fromDate,
                                                @Param("toDate") Instant toDate,
                                                Pageable pageable);

    @Query("SELECT new com.fu.pha.dto.request.importPack.ImportViewListDto(i) " +
            " FROM Import i " +
            " WHERE (:supplierName IS NULL OR i.supplier.supplierName = :supplierName OR :supplierName = '') " +
            " ORDER BY i.lastModifiedDate DESC")
    Page<ImportViewListDto> getListImportPagingWithoutDate(@Param("supplierName") String supplierName,
                                                           Pageable pageable);

    @Query("SELECT new com.fu.pha.dto.request.importPack.ImportViewListDto(i) " +
            " FROM Import i " +
            " WHERE (:supplierName IS NULL OR i.supplier.supplierName = :supplierName OR :supplierName = '') " +
            " AND i.importDate >= :fromDate " +
            " ORDER BY i.lastModifiedDate DESC")
    Page<ImportViewListDto> getListImportPagingFromDate(@Param("supplierName") String supplierName,
                                                        @Param("fromDate") Instant fromDate,
                                                        Pageable pageable);

    @Query("SELECT new com.fu.pha.dto.request.importPack.ImportViewListDto(i) " +
            " FROM Import i " +
            " WHERE (:supplierName IS NULL OR i.supplier.supplierName = :supplierName OR :supplierName = '') " +
            " AND i.importDate <= :toDate " +
            " ORDER BY i.lastModifiedDate DESC")
    Page<ImportViewListDto> getListImportPagingToDate(@Param("supplierName") String supplierName,
                                                      @Param("toDate") Instant toDate,
                                                      Pageable pageable);


}

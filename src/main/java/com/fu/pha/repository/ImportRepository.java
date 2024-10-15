package com.fu.pha.repository;

import com.fu.pha.dto.request.importPack.ImportViewListDto;
import com.fu.pha.entity.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ImportRepository extends JpaRepository<Import, Long> {

    @Query("SELECT i.invoiceNumber FROM Import i ORDER BY i.invoiceNumber DESC LIMIT 1")
    String getLastInvoiceNumber();

    @Query("SELECT new com.fu.pha.dto.request.importPack.ImportViewListDto(i) " +
            "FROM Import i " +
            "WHERE (:supplierName IS NULL OR i.supplier.supplierName LIKE %:supplierName%) " +
            "AND (:fromDate IS NULL OR i.importDate >= :fromDate) " +
            "AND (:toDate IS NULL OR i.importDate <= :toDate)")
    Page<ImportViewListDto> getAllImportAndPaging(Pageable pageable, String supplierName, Instant fromDate, Instant toDate);



}

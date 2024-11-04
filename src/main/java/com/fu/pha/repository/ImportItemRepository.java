package com.fu.pha.repository;

import com.fu.pha.dto.response.importPack.ImportItemResponseDto;
import com.fu.pha.dto.response.importPack.ImportItemResponseForExport;
import com.fu.pha.entity.ImportItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Query("SELECT ii.batchNumber FROM ImportItem ii WHERE ii.batchNumber LIKE 'SL%' ORDER BY ii.createDate DESC LIMIT 1")
    Optional<String> getLastBatchNumber();
}

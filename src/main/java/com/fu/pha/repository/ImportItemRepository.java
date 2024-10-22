package com.fu.pha.repository;

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


    @Query("SELECT ii FROM ImportItem ii WHERE ii.product.id = :productId AND ii.importReceipt.id = :importId")
    Optional<ImportItem> findByProductIdAndImportId(@Param("productId") Long productId, @Param("importId") Long importId);

    @Modifying
    @Query("DELETE FROM ImportItem ii WHERE ii.importReceipt.id = :importId")
    void deleteByImportId(@Param("importId") Long importId);

    @Query("SELECT ii FROM ImportItem ii WHERE ii.importReceipt.id = :importId")
    List<ImportItem> findByImportId(@Param("importId") Long importId);
}

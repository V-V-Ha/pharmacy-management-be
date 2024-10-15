package com.fu.pha.repository;

import com.fu.pha.dto.request.importPack.ImportItemRequestDto;
import com.fu.pha.dto.response.ImportItemResponseDto;
import com.fu.pha.entity.ImportItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImportItemRepository extends JpaRepository<ImportItem, Long> {


    @Query("SELECT ii FROM ImportItem ii WHERE ii.product.id = :productId AND ii.importR.id = :importId")
    Optional<ImportItem> findByProductIdAndImportId(@Param("productId") Long productId, @Param("importId") Long importId);
}

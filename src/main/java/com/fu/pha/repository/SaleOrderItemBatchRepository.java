package com.fu.pha.repository;

import com.fu.pha.entity.SaleOrderItemBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SaleOrderItemBatchRepository extends JpaRepository<SaleOrderItemBatch, Long> {
    List<SaleOrderItemBatch> findBySaleOrderItemId(Long saleOrderItemId);

    Optional<SaleOrderItemBatch> findByImportItemIdAndSaleOrderItemId(Long id, Long id1);

    List<SaleOrderItemBatch> findBySaleOrderItemIdOrderByImportItemIdAsc(Long id);
}
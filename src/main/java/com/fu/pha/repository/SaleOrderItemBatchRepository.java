package com.fu.pha.repository;

import com.fu.pha.entity.ImportItem;
import com.fu.pha.entity.ReturnOrderItem;
import com.fu.pha.entity.SaleOrderItem;
import com.fu.pha.entity.SaleOrderItemBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SaleOrderItemBatchRepository extends JpaRepository<SaleOrderItemBatch, Long> {
    List<SaleOrderItemBatch> findBySaleOrderItemId(Long saleOrderItemId);


    // Tìm SaleOrderItemBatch dựa trên SaleOrderItem và ImportItem
    Optional<SaleOrderItemBatch> findBySaleOrderItemAndImportItem(SaleOrderItem saleOrderItem, ImportItem importItem);

    List<SaleOrderItemBatch> findByReturnOrderItemId(Long returnOrderItemId);
    List<SaleOrderItemBatch> findByReturnOrderItem(ReturnOrderItem returnOrderItem);
}
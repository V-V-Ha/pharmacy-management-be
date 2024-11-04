package com.fu.pha.repository;

import com.fu.pha.entity.SaleOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleOrderItemRepository extends JpaRepository<SaleOrderItem, Long> {
    void deleteBySaleOrderId(Long saleOrderId);

    List<SaleOrderItem> findBySaleOrderId(Long saleOrderId);
}

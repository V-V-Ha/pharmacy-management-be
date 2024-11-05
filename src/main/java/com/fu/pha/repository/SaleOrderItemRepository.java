package com.fu.pha.repository;

import com.fu.pha.entity.Product;
import com.fu.pha.entity.SaleOrder;
import com.fu.pha.entity.SaleOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SaleOrderItemRepository extends JpaRepository<SaleOrderItem, Long> {
    void deleteBySaleOrderId(Long saleOrderId);

    List<SaleOrderItem> findBySaleOrderId(Long saleOrderId);

    Optional<SaleOrderItem> findBySaleOrderAndProduct(SaleOrder saleOrder, Product product);
}

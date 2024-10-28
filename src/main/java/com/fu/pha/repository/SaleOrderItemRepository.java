package com.fu.pha.repository;

import com.fu.pha.entity.SaleOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleOrderItemRepository extends JpaRepository<SaleOrderItem, Long> {
}

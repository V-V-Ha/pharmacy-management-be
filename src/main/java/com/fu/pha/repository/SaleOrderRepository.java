package com.fu.pha.repository;

import com.fu.pha.entity.SaleOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleOrderRepository extends JpaRepository<SaleOrder, Long> {

    @Query("SELECT s.invoiceNumber FROM SaleOrder s ORDER BY s.invoiceNumber DESC LIMIT 1")
    String getLastInvoiceNumber();
}

package com.fu.pha.repository;

import com.fu.pha.entity.ReturnOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
@Repository
public interface ReturnOrderRepository extends JpaRepository<ReturnOrder, Long> {
    List<ReturnOrder> findAllBySaleOrderId(Long saleOrderId);

    @Query("SELECT s.invoiceNumber FROM ReturnOrder s ORDER BY s.invoiceNumber DESC LIMIT 1")
    String getLastInvoiceNumber();

    @Query("SELECT SUM(r.refundAmount) FROM ReturnOrder r WHERE r.returnDate BETWEEN :startDate AND :endDate")
    Double sumTotalRefundsBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);
}

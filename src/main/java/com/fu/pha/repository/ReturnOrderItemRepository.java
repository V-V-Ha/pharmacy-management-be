package com.fu.pha.repository;


import com.fu.pha.entity.ReturnOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ReturnOrderItemRepository extends JpaRepository<ReturnOrderItem, Long> {
    List<ReturnOrderItem> findByReturnOrderId(Long returnOrderId);

    //report

    @Query("SELECT COALESCE(SUM(quantity * conversionFactor), 0) FROM ReturnOrderItem WHERE product.id = :productId AND createDate < :date")
    Integer sumQuantityBeforeDateByProduct(@Param("productId") Long productId, @Param("date") Instant date);

    @Query("SELECT COALESCE(SUM(totalAmount), 0) FROM ReturnOrderItem WHERE product.id = :productId AND createDate < :date")
    Double sumAmountBeforeDateByProduct(@Param("productId") Long productId, @Param("date") Instant date);

    @Query("SELECT COALESCE(SUM(quantity * conversionFactor), 0) FROM ReturnOrderItem WHERE product.id = :productId AND createDate BETWEEN :startDate AND :endDate")
    Integer sumQuantityByProductBetweenDates(@Param("productId") Long productId, @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT COALESCE(SUM(totalAmount), 0) FROM ReturnOrderItem WHERE product.id = :productId AND createDate BETWEEN :startDate AND :endDate")
    Double sumAmountByProductBetweenDates(@Param("productId") Long productId, @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT COALESCE(SUM(roi.quantity * roi.conversionFactor), 0) FROM ReturnOrderItem roi WHERE roi.returnOrder.returnDate < :startDate")
    Integer sumQuantityBeforeDate(@Param("startDate") Instant startDate);

    @Query("SELECT COALESCE(SUM(roi.totalAmount), 0) FROM ReturnOrderItem roi WHERE roi.returnOrder.returnDate < :startDate")
    Double sumAmountBeforeDate(@Param("startDate") Instant startDate);

    @Query("SELECT COALESCE(SUM(roi.quantity * roi.conversionFactor), 0) FROM ReturnOrderItem roi WHERE roi.returnOrder.returnDate BETWEEN :startDate AND :endDate")
    Integer sumQuantityBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT COALESCE(SUM(roi.totalAmount), 0) FROM ReturnOrderItem roi WHERE roi.returnOrder.returnDate BETWEEN :startDate AND :endDate")
    Double sumAmountBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);
}

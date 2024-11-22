package com.fu.pha.repository;

import com.fu.pha.entity.Product;
import com.fu.pha.entity.SaleOrder;
import com.fu.pha.entity.SaleOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleOrderItemRepository extends JpaRepository<SaleOrderItem, Long> {
    void deleteBySaleOrderId(Long saleOrderId);

    List<SaleOrderItem> findBySaleOrderId(Long saleOrderId);

    Optional<SaleOrderItem> findBySaleOrderAndProduct(SaleOrder saleOrder, Product product);

    List<SaleOrderItem> findBySaleOrderIdAndProductId(Long saleOrderId, Long productId);

    List<SaleOrderItem> findBySaleOrderIdAndProductIdOrderByIdAsc(Long id, Long productId);

    //report

    @Query("SELECT COALESCE(SUM(quantity * conversionFactor), 0) FROM SaleOrderItem WHERE product.id = :productId AND createDate < :date AND saleOrder.paymentStatus = com.fu.pha.enums.PaymentStatus.PAID")
    Integer sumQuantityBeforeDateByProduct(@Param("productId") Long productId, @Param("date") Instant date);

    @Query("SELECT COALESCE(SUM(totalAmount), 0) FROM SaleOrderItem WHERE product.id = :productId AND createDate < :date AND saleOrder.paymentStatus = com.fu.pha.enums.PaymentStatus.PAID")
    Double sumAmountBeforeDateByProduct(@Param("productId") Long productId, @Param("date") Instant date);

    @Query("SELECT COALESCE(SUM(quantity * conversionFactor), 0) FROM SaleOrderItem WHERE product.id = :productId AND createDate BETWEEN :startDate AND :endDate AND saleOrder.paymentStatus = com.fu.pha.enums.PaymentStatus.PAID")
    Integer sumQuantityByProductBetweenDates(@Param("productId") Long productId, @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT COALESCE(SUM(totalAmount), 0) FROM SaleOrderItem WHERE product.id = :productId AND createDate BETWEEN :startDate AND :endDate AND saleOrder.paymentStatus = com.fu.pha.enums.PaymentStatus.PAID")
    Double sumAmountByProductBetweenDates(@Param("productId") Long productId, @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);


    @Query("SELECT COALESCE(SUM(soi.quantity * soi.conversionFactor), 0) FROM SaleOrderItem soi WHERE soi.saleOrder.saleDate < :startDate AND saleOrder.paymentStatus = com.fu.pha.enums.PaymentStatus.PAID")
    Integer sumQuantityBeforeDate(@Param("startDate") Instant startDate);

    @Query("SELECT COALESCE(SUM(soi.totalAmount), 0) FROM SaleOrderItem soi WHERE soi.saleOrder.saleDate < :startDate AND soi.saleOrder.paymentStatus = com.fu.pha.enums.PaymentStatus.PAID")
    Double sumAmountBeforeDate(@Param("startDate") Instant startDate);

    @Query("SELECT COALESCE(SUM(soi.quantity * soi.conversionFactor), 0) FROM SaleOrderItem soi WHERE soi.saleOrder.saleDate BETWEEN :startDate AND :endDate AND soi.saleOrder.paymentStatus = com.fu.pha.enums.PaymentStatus.PAID")
    Integer sumQuantityBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT COALESCE(SUM(soi.totalAmount), 0) FROM SaleOrderItem soi WHERE soi.saleOrder.saleDate BETWEEN :startDate AND :endDate AND soi.saleOrder.paymentStatus = com.fu.pha.enums.PaymentStatus.PAID")
    Double sumAmountBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    // Phương thức cho báo cáo bán hàng
    @Query("SELECT COALESCE(SUM(soi.quantity * soi.conversionFactor), 0) FROM SaleOrderItem soi WHERE soi.saleOrder.saleDate BETWEEN :startDate AND :endDate AND soi.saleOrder.paymentStatus = com.fu.pha.enums.PaymentStatus.PAID")
    Integer sumTotalQuantityBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    // Phương thức cho báo cáo khách hàng
    @Query("SELECT COALESCE(SUM(soi.quantity * soi.conversionFactor), 0) FROM SaleOrderItem soi WHERE soi.saleOrder.saleDate BETWEEN :startDate AND :endDate AND soi.saleOrder.customer IS NOT NULL AND soi.saleOrder.paymentStatus = com.fu.pha.enums.PaymentStatus.PAID")
    Integer sumTotalQuantitySoldToCustomersBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);
}

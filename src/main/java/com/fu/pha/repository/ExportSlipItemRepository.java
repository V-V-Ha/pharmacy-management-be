package com.fu.pha.repository;

import com.fu.pha.entity.ExportSlipItem;
import com.fu.pha.enums.ExportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ExportSlipItemRepository extends JpaRepository<ExportSlipItem, Long> {

    @Query("SELECT i FROM ExportSlipItem i WHERE i.exportSlip.id = :exportSlipId")
    List<ExportSlipItem> findByExportSlipId(@Param("exportSlipId") Long exportSlipId);

    //report

    @Query("SELECT COALESCE(SUM(quantity * conversionFactor), 0) FROM ExportSlipItem  WHERE product.id = :productId AND createDate < :date AND exportSlip.status = com.fu.pha.enums.OrderStatus.CONFIRMED")
    Integer sumQuantityBeforeDateByProduct(@Param("productId") Long productId, @Param("date") Instant date);

    @Query("SELECT COALESCE(SUM(totalAmount), 0) FROM ExportSlipItem WHERE product.id = :productId AND createDate < :date AND exportSlip.typeDelivery != 'DESTROY' AND exportSlip.status = com.fu.pha.enums.OrderStatus.CONFIRMED" )
    Double sumAmountBeforeDateByProduct(@Param("productId") Long productId, @Param("date") Instant date);

    @Query("SELECT COALESCE(SUM(quantity * conversionFactor), 0) FROM ExportSlipItem WHERE product.id = :productId AND createDate BETWEEN :startDate AND :endDate AND exportSlip.status = com.fu.pha.enums.OrderStatus.CONFIRMED")
    Integer sumQuantityByProductBetweenDates(@Param("productId") Long productId, @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT COALESCE(SUM(totalAmount), 0) FROM ExportSlipItem WHERE product.id = :productId AND createDate BETWEEN :startDate AND :endDate AND exportSlip.typeDelivery != 'DESTROY' AND exportSlip.status = com.fu.pha.enums.OrderStatus.CONFIRMED")
    Double sumAmountByProductBetweenDates(@Param("productId") Long productId, @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT COALESCE(SUM(ei.quantity * ei.conversionFactor), 0) FROM ExportSlipItem ei WHERE ei.exportSlip.exportDate < :startDate AND ei.exportSlip.status = com.fu.pha.enums.OrderStatus.CONFIRMED")
    Integer sumQuantityBeforeDate(@Param("startDate") Instant startDate);

    @Query("SELECT COALESCE(SUM(ei.totalAmount), 0) " +
            "FROM ExportSlipItem ei " +
            "WHERE ei.exportSlip.exportDate < :startDate " +
            "AND ei.exportSlip.typeDelivery != 'DESTROY' AND ei.exportSlip.status = com.fu.pha.enums.OrderStatus.CONFIRMED")
    Double sumAmountBeforeDate(@Param("startDate") Instant startDate);


    @Query("SELECT COALESCE(SUM(ei.quantity * ei.conversionFactor), 0) FROM ExportSlipItem ei WHERE ei.exportSlip.exportDate BETWEEN :startDate AND :endDate AND ei.exportSlip.status = com.fu.pha.enums.OrderStatus.CONFIRMED")
    Integer sumQuantityBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT COALESCE(SUM(ei.totalAmount), 0) " +
            "FROM ExportSlipItem ei " +
            "WHERE ei.exportSlip.exportDate BETWEEN :startDate AND :endDate " +
            "AND ei.exportSlip.typeDelivery != 'DESTROY' AND ei.exportSlip.status = com.fu.pha.enums.OrderStatus.CONFIRMED")
    Double sumAmountBetweenDates(@Param("startDate") Instant startDate,
                                 @Param("endDate") Instant endDate);


    @Query("SELECT COALESCE(SUM(ei.quantity * ei.conversionFactor), 0) FROM ExportSlipItem ei WHERE ei.exportSlip.exportDate BETWEEN :startDate AND :endDate AND ei.exportSlip.typeDelivery = :typeDelivery AND ei.exportSlip.status = com.fu.pha.enums.OrderStatus.CONFIRMED")
    Integer sumQuantityByTypeBetweenDates(@Param("typeDelivery") ExportType typeDelivery, @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT COALESCE(SUM(ei.totalAmount ), 0) FROM ExportSlipItem ei WHERE ei.exportSlip.exportDate BETWEEN :startDate AND :endDate AND ei.exportSlip.typeDelivery = :typeDelivery AND ei.exportSlip.status = com.fu.pha.enums.OrderStatus.CONFIRMED")
    Double sumAmountByTypeBetweenDates(@Param("typeDelivery") ExportType typeDelivery, @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);
}

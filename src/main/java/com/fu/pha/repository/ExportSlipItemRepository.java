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

    @Query("SELECT i FROM ExportSlipItem i WHERE i.isDeleted = false AND i.exportSlip.id = :exportSlipId")
    List<ExportSlipItem> findByExportSlipId(@Param("exportSlipId") Long exportSlipId);

    //report
    @Query("SELECT COALESCE(SUM(ei.quantity * ei.conversionFactor), 0) FROM ExportSlipItem ei WHERE ei.exportSlip.exportDate < :startDate")
    Integer sumQuantityBeforeDate(@Param("startDate") Instant startDate);

    @Query("SELECT COALESCE(SUM(ei.quantity * ei.unitPrice), 0) " +
            "FROM ExportSlipItem ei " +
            "WHERE ei.exportSlip.exportDate < :startDate " +
            "AND ei.exportSlip.typeDelivery != 'DESTROY'")
    Double sumAmountBeforeDate(@Param("startDate") Instant startDate);


    @Query("SELECT COALESCE(SUM(ei.quantity * ei.conversionFactor), 0) FROM ExportSlipItem ei WHERE ei.exportSlip.exportDate BETWEEN :startDate AND :endDate")
    Integer sumQuantityBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT COALESCE(SUM(ei.quantity * ei.unitPrice), 0) " +
            "FROM ExportSlipItem ei " +
            "WHERE ei.exportSlip.exportDate BETWEEN :startDate AND :endDate " +
            "AND ei.exportSlip.typeDelivery != 'DESTROY'")
    Double sumAmountBetweenDates(@Param("startDate") Instant startDate,
                                 @Param("endDate") Instant endDate);


    @Query("SELECT COALESCE(SUM(ei.quantity * ei.conversionFactor), 0) FROM ExportSlipItem ei WHERE ei.exportSlip.exportDate BETWEEN :startDate AND :endDate AND ei.exportSlip.typeDelivery = :typeDelivery")
    Integer sumQuantityByTypeBetweenDates(@Param("typeDelivery") ExportType typeDelivery, @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT COALESCE(SUM(ei.quantity  * ei.unitPrice ), 0) FROM ExportSlipItem ei WHERE ei.exportSlip.exportDate BETWEEN :startDate AND :endDate AND ei.exportSlip.typeDelivery = :typeDelivery")
    Double sumAmountByTypeBetweenDates(@Param("typeDelivery") ExportType typeDelivery, @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);
}

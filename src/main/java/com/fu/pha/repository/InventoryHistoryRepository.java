package com.fu.pha.repository;

import com.fu.pha.entity.InventoryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface InventoryHistoryRepository extends JpaRepository<InventoryHistory, Long> {

    // Tổng thay đổi tồn kho đến một ngày cụ thể
    @Query("SELECT COALESCE(SUM(ih.changeQuantity), 0) FROM InventoryHistory ih WHERE ih.recordDate < :date")
    Integer sumTotalChangeUpToDate(@Param("date") Instant date);

    // Tổng giá trị thay đổi tồn kho đến một ngày cụ thể
    @Query("SELECT COALESCE(SUM(ih.changeQuantity * ii.unitPrice / ii.conversionFactor), 0) FROM InventoryHistory ih JOIN ih.importItem ii WHERE ih.recordDate < :date")
    Double sumTotalAmountChangeUpToDate(@Param("date") Instant date);

    // Tổng thay đổi dương (nhập) trong khoảng thời gian
    @Query("SELECT COALESCE(SUM(ih.changeQuantity), 0) FROM InventoryHistory ih WHERE ih.recordDate BETWEEN :startDate AND :endDate AND ih.changeQuantity > 0")
    Integer sumTotalPositiveChangeBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    // Tổng giá trị thay đổi dương (nhập) trong khoảng thời gian
    @Query("SELECT COALESCE(SUM(ih.changeQuantity * ii.unitPrice / ii.conversionFactor), 0) FROM InventoryHistory ih JOIN ih.importItem ii WHERE ih.recordDate BETWEEN :startDate AND :endDate AND ih.changeQuantity > 0")
    Double sumTotalPositiveAmountChangeBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    // Tổng thay đổi âm (xuất) trong khoảng thời gian
    @Query("SELECT COALESCE(SUM(ih.changeQuantity), 0) FROM InventoryHistory ih WHERE ih.recordDate BETWEEN :startDate AND :endDate AND ih.changeQuantity < 0")
    Integer sumTotalNegativeChangeBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    // Tổng giá trị thay đổi âm (xuất) trong khoảng thời gian
    @Query("SELECT COALESCE(SUM(ih.changeQuantity * ii.unitPrice / ii.conversionFactor), 0) FROM InventoryHistory ih JOIN ih.importItem ii WHERE ih.recordDate BETWEEN :startDate AND :endDate AND ih.changeQuantity < 0")
    Double sumTotalNegativeAmountChangeBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

}

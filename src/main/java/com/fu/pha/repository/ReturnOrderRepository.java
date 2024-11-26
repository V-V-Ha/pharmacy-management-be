package com.fu.pha.repository;

import com.fu.pha.dto.response.ReturnOrderResponseDto;
import com.fu.pha.entity.ReturnOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("SELECT new com.fu.pha.dto.response.ReturnOrderResponseDto(r) " +
            " FROM ReturnOrder r " +
            " WHERE (LOWER(r.invoiceNumber) LIKE LOWER(CONCAT('%', :invoiceNumber, '%')) OR :invoiceNumber IS NULL OR :invoiceNumber = '') " +
            " ORDER BY r.lastModifiedDate DESC")
    Page<ReturnOrderResponseDto> getListReturnOrderPagingWithoutDate(@Param("invoiceNumber") String invoiceNumber,
                                                                     Pageable pageable);

    @Query("SELECT new com.fu.pha.dto.response.ReturnOrderResponseDto(r) " +
            " FROM ReturnOrder r " +
            " WHERE (LOWER(r.invoiceNumber) LIKE LOWER(CONCAT('%', :invoiceNumber, '%')) OR :invoiceNumber IS NULL OR :invoiceNumber = '') " +
            " AND r.returnDate >= :fromDate " +
            " ORDER BY r.lastModifiedDate DESC")
    Page<ReturnOrderResponseDto> getListReturnOrderPagingFromDate(@Param("invoiceNumber") String invoiceNumber,
                                                                  @Param("fromDate") Instant fromDate,
                                                                  Pageable pageable);

    @Query("SELECT new com.fu.pha.dto.response.ReturnOrderResponseDto(r) " +
            " FROM ReturnOrder r " +
            " WHERE (LOWER(r.invoiceNumber) LIKE LOWER(CONCAT('%', :invoiceNumber, '%')) OR :invoiceNumber IS NULL OR :invoiceNumber = '') " +
            " AND r.returnDate <= :toDate " +
            " ORDER BY r.lastModifiedDate DESC")
    Page<ReturnOrderResponseDto> getListReturnOrderPagingToDate(@Param("invoiceNumber") String invoiceNumber,
                                                                @Param("toDate") Instant toDate,
                                                                Pageable pageable);

    @Query("SELECT new com.fu.pha.dto.response.ReturnOrderResponseDto(r) " +
            " FROM ReturnOrder r " +
            " WHERE (LOWER(r.invoiceNumber) LIKE LOWER(CONCAT('%', :invoiceNumber, '%')) OR :invoiceNumber IS NULL OR :invoiceNumber = '') " +
            " AND (r.returnDate IS NULL OR r.returnDate BETWEEN :fromDate AND :toDate)" +
            " ORDER BY r.lastModifiedDate DESC")
    Page<ReturnOrderResponseDto> getListReturnOrderPaging(@Param("invoiceNumber") String invoiceNumber,
                                                          @Param("fromDate") Instant fromDate,
                                                          @Param("toDate") Instant toDate,
                                                          Pageable pageable);

    @Query("SELECT new com.fu.pha.dto.response.ReturnOrderResponseDto(r) " +
            "FROM ReturnOrder r " +
            "WHERE r.returnDate BETWEEN :fromDate AND :toDate")
    List<ReturnOrderResponseDto> getReturnOrdersByDateRange(@Param("fromDate") Instant fromDate,
                                                            @Param("toDate") Instant toDate);

}

package com.fu.pha.repository;

import com.fu.pha.dto.response.SaleOrder.SaleOrderResponseDto;
import com.fu.pha.entity.SaleOrder;
import com.fu.pha.enums.OrderType;
import com.fu.pha.enums.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleOrderRepository extends JpaRepository<SaleOrder, Long> {

    @Query("SELECT s.invoiceNumber FROM SaleOrder s ORDER BY s.invoiceNumber DESC LIMIT 1")
    String getLastInvoiceNumber();

    @Query("SELECT new com.fu.pha.dto.response.SaleOrder.SaleOrderResponseDto(s) " +
            " FROM SaleOrder s " +
            " WHERE (:orderType IS NULL OR s.orderType = :orderType OR :orderType = '') " +
            " AND (:paymentMethod IS NULL OR s.paymentMethod = :paymentMethod OR :paymentMethod = '') " +
            " AND (LOWER(s.customer.phoneNumber) LIKE LOWER(CONCAT('%', :phoneNumber, '%')) OR :phoneNumber IS NULL OR :phoneNumber = '') " +
            " AND (s.saleDate IS NULL OR s.saleDate BETWEEN :startOfDay AND :endOfDay) " +
            " ORDER BY s.lastModifiedDate DESC")
    Page<SaleOrderResponseDto> getListSaleOrderPagingWithoutDate(@Param("orderType") OrderType orderType,
                                                                 @Param("paymentMethod") PaymentMethod paymentMethod,
                                                                 @Param("phoneNumber") String phoneNumber,
                                                                 @Param("startOfDay") Instant startOfDay,
                                                                 @Param("endOfDay") Instant endOfDay,
                                                                 Pageable pageable);

    @Query("SELECT new com.fu.pha.dto.response.SaleOrder.SaleOrderResponseDto(s) " +
            " FROM SaleOrder s " +
            " WHERE (:orderType IS NULL OR s.orderType = :orderType OR :orderType = '') " +
            " AND (:paymentMethod IS NULL OR s.paymentMethod = :paymentMethod OR :paymentMethod = '') " +
            " AND (LOWER(s.customer.phoneNumber) LIKE LOWER(CONCAT('%', :phoneNumber, '%')) OR :phoneNumber IS NULL OR :phoneNumber = '') " +
            " AND s.saleDate >= :fromDate " +
            " ORDER BY s.lastModifiedDate DESC")
    Page<SaleOrderResponseDto> getListSaleOrderPagingFromDate(@Param("orderType") OrderType orderType,
                                                      @Param("paymentMethod") PaymentMethod paymentMethod,
                                                      @Param("phoneNumber") String phoneNumber,
                                                      @Param("fromDate") Instant fromDate,
                                                      Pageable pageable);

    @Query("SELECT new com.fu.pha.dto.response.SaleOrder.SaleOrderResponseDto(s) " +
                  " FROM SaleOrder s " +
                  " WHERE (:orderType IS NULL OR s.orderType = :orderType OR :orderType = '') " +
                  " AND (:paymentMethod IS NULL OR s.paymentMethod = :paymentMethod OR :paymentMethod = '') " +
                  " AND (LOWER(s.customer.phoneNumber) LIKE LOWER(CONCAT('%', :phoneNumber, '%')) OR :phoneNumber IS NULL OR :phoneNumber = '') " +
                  " AND s.saleDate <= :toDate " +
                  " ORDER BY s.lastModifiedDate DESC")
    Page<SaleOrderResponseDto> getListSaleOrderPagingToDate(@Param("orderType") OrderType orderType,
                                                              @Param("paymentMethod") PaymentMethod paymentMethod,
                                                              @Param("phoneNumber") String phoneNumber,
                                                            @Param("toDate") Instant toDate,
                                                              Pageable pageable);

    @Query("SELECT new com.fu.pha.dto.response.SaleOrder.SaleOrderResponseDto(s) " +
            " FROM SaleOrder s " +
            " WHERE (:orderType IS NULL OR s.orderType = :orderType OR :orderType = '') " +
            " AND (:paymentMethod IS NULL OR s.paymentMethod = :paymentMethod OR :paymentMethod = '') " +
            " AND (LOWER(s.customer.phoneNumber) LIKE LOWER(CONCAT('%', :phoneNumber, '%')) OR :phoneNumber IS NULL OR :phoneNumber = '') " +
            " AND (s.saleDate IS NULL OR s.saleDate BETWEEN :fromDate AND :toDate)" +
            " ORDER BY s.lastModifiedDate DESC")
    Page<SaleOrderResponseDto> getListSaleOrderPaging(@Param("orderType") OrderType orderType,
                                                      @Param("paymentMethod") PaymentMethod paymentMethod,
                                                      @Param("phoneNumber") String phoneNumber,
                                                      @Param("fromDate") Instant fromDate,
                                                      @Param("toDate") Instant toDate,
                                                      Pageable pageable);


//    @Query("SELECT s FROM SaleOrder s WHERE s.invoiceNumber LIKE LOWER(CONCAT('%', :invoiceNumber, '%'))")
    Optional<SaleOrder> findByInvoiceNumber(String invoiceNumber);

    @Query("SELECT COUNT(s) FROM SaleOrder s WHERE s.saleDate BETWEEN :startDate AND :endDate")
    long countSaleOrdersBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT SUM(s.totalAmount) FROM SaleOrder s WHERE s.saleDate BETWEEN :startDate AND :endDate")
    Double sumTotalAmountBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT s.paymentMethod as paymentMethod, SUM(s.totalAmount) as totalAmount " +
            "FROM SaleOrder s WHERE s.saleDate BETWEEN :startDate AND :endDate " +
            "GROUP BY s.paymentMethod")
    List<Object[]> sumTotalAmountByPaymentMethodBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    // Phương thức cho báo cáo khách hàng
    @Query("SELECT SUM(s.totalAmount) FROM SaleOrder s WHERE s.saleDate BETWEEN :startDate AND :endDate AND s.customer IS NOT NULL")
    Double sumTotalAmountByCustomersBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT COUNT(s) FROM SaleOrder s WHERE s.saleDate BETWEEN :startDate AND :endDate AND s.customer IS NULL")
    long countWalkInCustomersBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);
}

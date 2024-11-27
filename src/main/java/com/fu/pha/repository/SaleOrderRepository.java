package com.fu.pha.repository;

import com.fu.pha.dto.response.SaleOrder.SaleOrderResponseDto;
import com.fu.pha.dto.response.report.FinancialTransactionDto;
import com.fu.pha.dto.response.report.product.ProductSalesDto;
import com.fu.pha.dto.response.report.sale.SalesTransactionDto;
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
            " AND (LOWER(s.invoiceNumber) LIKE LOWER(CONCAT('%', :invoiceNumber, '%')) OR :invoiceNumber IS NULL OR :invoiceNumber = '') " +
            " AND (s.saleDate IS NULL OR s.saleDate BETWEEN :startOfDay AND :endOfDay) " +
            " ORDER BY s.lastModifiedDate DESC")
    Page<SaleOrderResponseDto> getListSaleOrderPagingWithoutDate(@Param("orderType") OrderType orderType,
                                                                 @Param("paymentMethod") PaymentMethod paymentMethod,
                                                                 @Param("invoiceNumber") String invoiceNumber,
                                                                 @Param("startOfDay") Instant startOfDay,
                                                                 @Param("endOfDay") Instant endOfDay,
                                                                 Pageable pageable);

    @Query("SELECT new com.fu.pha.dto.response.SaleOrder.SaleOrderResponseDto(s) " +
            " FROM SaleOrder s " +
            " WHERE (:orderType IS NULL OR s.orderType = :orderType OR :orderType = '') " +
            " AND (:paymentMethod IS NULL OR s.paymentMethod = :paymentMethod OR :paymentMethod = '') " +
            " AND (LOWER(s.invoiceNumber) LIKE LOWER(CONCAT('%', :invoiceNumber, '%')) OR :invoiceNumber IS NULL OR :invoiceNumber = '') " +
            " AND s.saleDate >= :fromDate " +
            " ORDER BY s.lastModifiedDate DESC")
    Page<SaleOrderResponseDto> getListSaleOrderPagingFromDate(@Param("orderType") OrderType orderType,
                                                      @Param("paymentMethod") PaymentMethod paymentMethod,
                                                      @Param("invoiceNumber") String invoiceNumber,
                                                      @Param("fromDate") Instant fromDate,
                                                      Pageable pageable);

    @Query("SELECT new com.fu.pha.dto.response.SaleOrder.SaleOrderResponseDto(s) " +
                  " FROM SaleOrder s " +
                  " WHERE (:orderType IS NULL OR s.orderType = :orderType OR :orderType = '') " +
                  " AND (:paymentMethod IS NULL OR s.paymentMethod = :paymentMethod OR :paymentMethod = '') " +
                  " AND (LOWER(s.invoiceNumber) LIKE LOWER(CONCAT('%', :invoiceNumber, '%')) OR :invoiceNumber IS NULL OR :invoiceNumber = '') " +
                  " AND s.saleDate <= :toDate " +
                  " ORDER BY s.lastModifiedDate DESC")
    Page<SaleOrderResponseDto> getListSaleOrderPagingToDate(@Param("orderType") OrderType orderType,
                                                              @Param("paymentMethod") PaymentMethod paymentMethod,
                                                              @Param("invoiceNumber") String invoiceNumber,
                                                            @Param("toDate") Instant toDate,
                                                              Pageable pageable);

    @Query("SELECT new com.fu.pha.dto.response.SaleOrder.SaleOrderResponseDto(s) " +
            " FROM SaleOrder s " +
            " WHERE (:orderType IS NULL OR s.orderType = :orderType OR :orderType = '') " +
            " AND (:paymentMethod IS NULL OR s.paymentMethod = :paymentMethod OR :paymentMethod = '') " +
            " AND (LOWER(s.invoiceNumber) LIKE LOWER(CONCAT('%', :invoiceNumber, '%')) OR :invoiceNumber IS NULL OR :invoiceNumber = '') " +
            " AND (s.saleDate IS NULL OR s.saleDate BETWEEN :fromDate AND :toDate)" +
            " ORDER BY s.lastModifiedDate DESC")
    Page<SaleOrderResponseDto> getListSaleOrderPaging(@Param("orderType") OrderType orderType,
                                                      @Param("paymentMethod") PaymentMethod paymentMethod,
                                                      @Param("invoiceNumber") String invoiceNumber,
                                                      @Param("fromDate") Instant fromDate,
                                                      @Param("toDate") Instant toDate,
                                                      Pageable pageable);


    Optional<SaleOrder> findByInvoiceNumber(String invoiceNumber);

    @Query("SELECT COUNT(so) FROM SaleOrder so WHERE so.saleDate BETWEEN :startDate AND :endDate AND so.paymentStatus = com.fu.pha.enums.PaymentStatus.PAID")
    long countSaleOrdersBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT SUM(so.totalAmount) FROM SaleOrder so WHERE so.saleDate BETWEEN :startDate AND :endDate AND so.customer.createDate BETWEEN :startDate AND :endDate AND so.paymentStatus = com.fu.pha.enums.PaymentStatus.PAID AND so.customer.id != 1")
    Double sumTotalAmountFromNewCustomersBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);


    @Query("SELECT SUM(so.totalAmount) FROM SaleOrder so WHERE so.saleDate BETWEEN :startDate AND :endDate AND so.customer.createDate < :startDate AND so.paymentStatus = com.fu.pha.enums.PaymentStatus.PAID AND so.customer.id != 1")
    Double sumTotalAmountFromOldCustomersBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);


    @Query("SELECT COALESCE(SUM(so.totalAmount), 0) FROM SaleOrder so WHERE so.saleDate BETWEEN :startDate AND :endDate AND so.paymentStatus = com.fu.pha.enums.PaymentStatus.PAID")
    Double sumTotalAmountBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT COALESCE(SUM(so.totalAmount), 0) " +
            "FROM SaleOrder so " +
            "WHERE so.saleDate BETWEEN :startDate AND :endDate " +
            "AND so.paymentStatus = com.fu.pha.enums.PaymentStatus.PAID " +
            "AND (:paymentMethod IS NULL OR so.paymentMethod = :paymentMethod)")
    Double sumTotalAmountByPaymentMethodBetweenDates(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("paymentMethod") PaymentMethod paymentMethod);



    @Query("SELECT COALESCE(SUM(so.totalAmount), 0) FROM SaleOrder so WHERE so.customer IS NOT NULL AND so.saleDate BETWEEN :startDate AND :endDate AND so.paymentStatus = com.fu.pha.enums.PaymentStatus.PAID")
    Double sumTotalAmountByCustomersBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT COUNT(DISTINCT so.customer.id) " +
            "FROM SaleOrder so " +
            "WHERE so.customer.id = 1 " +
            "AND so.saleDate BETWEEN :startDate AND :endDate AND so.paymentStatus = com.fu.pha.enums.PaymentStatus.PAID")
    long countWalkInCustomersBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT SUM(so.totalAmount) " +
            "FROM SaleOrder so " +
            "WHERE so.customer.id = 1 " +
            "AND so.saleDate BETWEEN :startDate AND :endDate " +
            "AND so.paymentStatus = com.fu.pha.enums.PaymentStatus.PAID")
    Double sumTotalAmountFromWalkInCustomersBetweenDates(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);


    @Query(value = "SELECT * FROM (" +
            // Purchases (Nhập hàng) - Phiếu chi
            "    SELECT i.invoice_number AS invoiceNumber, " +
            "           'Phiếu chi' AS receiptType, " +
            "           i.import_date AS creationDate, " +
            "           'Nhập hàng' AS category, " +
            "           i.payment_method AS paymentMethod, " +
            "           COALESCE(i.total_amount,0) AS totalAmount " +
            "    FROM import i " +
            "    WHERE i.import_date BETWEEN :startDate AND :endDate " +
            "    AND i.status = 'CONFIRMED' " +  // Added condition for status
            "    AND (:paymentMethod IS NULL OR i.payment_method = :paymentMethod) " +
            "    AND (:category IS NULL OR :category = 'Nhập hàng') " +
            "    AND (:receiptType IS NULL OR :receiptType = 'Phiếu chi') " +
            // Returns to Supplier (Trả lại nhà cung cấp) - Phiếu thu
            "    UNION ALL " +
            "    SELECT e.invoice_number AS invoiceNumber, " +
            "           'Phiếu thu' AS receiptType, " +
            "           e.export_date AS creationDate, " +
            "           'Trả lại nhà cung cấp' AS category, " +
            "           NULL AS paymentMethod, " +
            "           e.total_amount AS totalAmount " +
            "    FROM export_slip e " +
            "    WHERE e.export_date BETWEEN :startDate AND :endDate " +
            "    AND e.status = 'CONFIRMED' " +  // Added condition for status
            "    AND e.type_delivery = 'RETURN_TO_SUPPLIER' " +
            "    AND (:category IS NULL OR :category = 'Trả lại nhà cung cấp') " +
            "    AND (:receiptType IS NULL OR :receiptType = 'Phiếu thu') " +
            // Sales (Bán hàng) - Phiếu thu
            "    UNION ALL " +
            "    SELECT so.invoice_number AS invoiceNumber, " +
            "           'Phiếu thu' AS receiptType, " +
            "           so.sale_date AS creationDate, " +
            "           'Bán hàng' AS category, " +
            "           so.payment_method AS paymentMethod, " +
            "           so.total_amount AS totalAmount " +
            "    FROM sale_order so " +
            "    WHERE so.sale_date BETWEEN :startDate AND :endDate " +
            "    AND so.payment_status = 'PAID' " +  // Added condition for payment_status
            "    AND (:paymentMethod IS NULL OR so.payment_method = :paymentMethod) " +
            "    AND (:category IS NULL OR :category = 'Bán hàng') " +
            "    AND (:receiptType IS NULL OR :receiptType = 'Phiếu thu') " +
            // Returns from Customers (Khách hàng trả lại) - Phiếu chi
            "    UNION ALL " +
            "    SELECT ro.invoice_number AS invoiceNumber, " +
            "           'Phiếu chi' AS receiptType, " +
            "           ro.return_date AS creationDate, " +
            "           'Khách hàng trả lại' AS category, " +
            "           NULL AS paymentMethod, " +
            "           ro.refund_amount AS totalAmount " +
            "    FROM return_order ro " +
            "    WHERE ro.return_date BETWEEN :startDate AND :endDate " +
            "    AND (:category IS NULL OR :category = 'Khách hàng trả lại') " +
            "    AND (:receiptType IS NULL OR :receiptType = 'Phiếu chi') " +
            ") AS transactions " +
            "ORDER BY transactions.creationDate DESC",
            countQuery = "SELECT COUNT(*) FROM (" +
                    // Purchases (Nhập hàng)
                    "    SELECT i.id " +
                    "    FROM import i " +
                    "    WHERE i.import_date BETWEEN :startDate AND :endDate " +
                    "    AND i.status = 'CONFIRMED' " +  // Added condition for status
                    "    AND (:paymentMethod IS NULL OR i.payment_method = :paymentMethod) " +
                    "    AND (:category IS NULL OR :category = 'Nhập hàng') " +
                    "    AND (:receiptType IS NULL OR :receiptType = 'Phiếu chi') " +
                    // Returns to Supplier (Trả lại nhà cung cấp)
                    "    UNION ALL " +
                    "    SELECT e.id " +
                    "    FROM export_slip e " +
                    "    WHERE e.export_date BETWEEN :startDate AND :endDate " +
                    "    AND e.status = 'CONFIRMED' " +  // Added condition for status
                    "    AND e.type_delivery = 'RETURN_TO_SUPPLIER' " +
                    "    AND (:category IS NULL OR :category = 'Trả lại nhà cung cấp') " +
                    "    AND (:receiptType IS NULL OR :receiptType = 'Phiếu thu') " +
                    // Sales (Bán hàng)
                    "    UNION ALL " +
                    "    SELECT so.id " +
                    "    FROM sale_order so " +
                    "    WHERE so.sale_date BETWEEN :startDate AND :endDate " +
                    "    AND so.payment_status = 'PAID' " +  // Added condition for payment_status
                    "    AND (:paymentMethod IS NULL OR so.payment_method = :paymentMethod) " +
                    "    AND (:category IS NULL OR :category = 'Bán hàng') " +
                    "    AND (:receiptType IS NULL OR :receiptType = 'Phiếu thu') " +
                    // Returns from Customers (Khách hàng trả lại)
                    "    UNION ALL " +
                    "    SELECT ro.id " +
                    "    FROM return_order ro " +
                    "    WHERE ro.return_date BETWEEN :startDate AND :endDate " +
                    "    AND (:category IS NULL OR :category = 'Khách hàng trả lại') " +
                    "    AND (:receiptType IS NULL OR :receiptType = 'Phiếu chi') " +
                    ") AS transactions",
            nativeQuery = true)
    Page<FinancialTransactionDto> findFinancialTransactions(
            @Param("paymentMethod") String paymentMethod,
            @Param("category") String category,
            @Param("receiptType") String receiptType,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );


    @Query(value = "SELECT * FROM (" +
            // Sale Orders with payment_status = 'PAID'
            "SELECT so.invoice_number AS invoiceNumber, " +
            "       so.sale_date AS creationDate, " +
            "       c.customer_name AS customerName, " +
            "       'Bán hàng' AS voucherType, " +
            "       so.payment_method AS paymentMethod, " +
            "       COALESCE(so.total_amount,0) AS totalAmount " +
            "FROM sale_order so " +
            "JOIN customer c ON so.customer_id = c.id " +
            "WHERE so.sale_date BETWEEN :startDate AND :endDate " +
            "AND so.payment_status = 'PAID' " +  // Added condition for payment_status
            "AND (:paymentMethod IS NULL OR so.payment_method = :paymentMethod) " +
            "AND (:voucherType IS NULL OR :voucherType = 'Bán hàng') " +
            "UNION ALL " +
            // Return Orders
            "SELECT ro.invoice_number AS invoiceNumber, " +
            "       ro.return_date AS creationDate, " +
            "       c.customer_name AS customerName, " +
            "       'Khách hàng trả lại' AS voucherType, " +
            "       NULL AS paymentMethod, " +
            "       ro.refund_amount AS totalAmount " +
            "FROM return_order ro " +
            "JOIN customer c ON ro.customer_id = c.id " +
            "WHERE ro.return_date BETWEEN :startDate AND :endDate " +
            "AND (:voucherType IS NULL OR :voucherType = 'Khách hàng trả lại') " +
            ") AS transactions " +
            "ORDER BY transactions.creationDate DESC",
            countQuery = "SELECT COUNT(*) FROM (" +
                    "SELECT so.id FROM sale_order so " +
                    "WHERE so.sale_date BETWEEN :startDate AND :endDate " +
                    "AND so.payment_status = 'PAID' " +  // Added condition for payment_status
                    "AND (:paymentMethod IS NULL OR so.payment_method = :paymentMethod) " +
                    "AND (:voucherType IS NULL OR :voucherType = 'Bán hàng') " +
                    "UNION ALL " +
                    "SELECT ro.id FROM return_order ro " +
                    "WHERE ro.return_date BETWEEN :startDate AND :endDate " +
                    "AND (:voucherType IS NULL OR :voucherType = 'Khách hàng trả lại') " +
                    ") AS transactions",
            nativeQuery = true)
    Page<SalesTransactionDto> findSalesTransactions(
            @Param("paymentMethod") String paymentMethod,
            @Param("voucherType") String voucherType,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );


    @Query(value = "SELECT p.product_code AS productCode, " +
            "       p.product_name AS productName, " +
            "       u.unit_name AS unit, " +
            "       COUNT(DISTINCT so.id) AS transactionCount, " +
            "       COALESCE(SUM(soi.quantity * soi.conversion_factor), 0) AS quantitySold, " +
            "       COALESCE(SUM(soi.total_amount), 0) AS totalAmount " +
            "FROM sale_order_item soi " +
            "JOIN sale_order so ON soi.sale_order_id = so.id " +
            "JOIN product p ON soi.product_id = p.id " +
            "JOIN product_unit pu ON pu.product_id = p.id AND pu.conversion_factor = 1 " +
            "JOIN unit u ON pu.unit_id = u.id " +
            "WHERE so.sale_date BETWEEN :startDate AND :endDate " +
            "AND so.payment_status = 'PAID' " +
            "AND (:productName IS NULL OR LOWER(p.product_name) LIKE LOWER(CONCAT('%', :productName, '%'))) " +
            "AND (:productCode IS NULL OR LOWER(p.product_code) LIKE LOWER(CONCAT('%', :productCode, '%'))) " +
            "GROUP BY p.product_code, p.product_name, u.unit_name " +
            "ORDER BY p.product_name ASC",
            countQuery = "SELECT COUNT(*) FROM (" +
                    "SELECT p.id " +
                    "FROM sale_order_item soi " +
                    "JOIN sale_order so ON soi.sale_order_id = so.id " +
                    "JOIN product p ON soi.product_id = p.id " +
                    "JOIN product_unit pu ON pu.product_id = p.id AND pu.conversion_factor = 1 " +
                    "JOIN unit u ON pu.unit_id = u.id " +
                    "WHERE so.sale_date BETWEEN :startDate AND :endDate " +
                    "AND so.payment_status = 'PAID' " +
                    "AND (:productName IS NULL OR LOWER(p.product_name) LIKE LOWER(CONCAT('%', :productName, '%'))) " +
                    "AND (:productCode IS NULL OR LOWER(p.product_code) LIKE LOWER(CONCAT('%', :productCode, '%'))) " +
                    "GROUP BY p.id) AS sub",
            nativeQuery = true)
    Page<ProductSalesDto> findProductSales(
            @Param("productName") String productName,
            @Param("productCode") String productCode,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );

    @Query("SELECT new com.fu.pha.dto.response.SaleOrder.SaleOrderResponseDto(s) " +
            "FROM SaleOrder s " +
            "WHERE s.saleDate BETWEEN :fromDate AND :toDate")
    List<SaleOrderResponseDto> getSaleOrdersByDateRange(@Param("fromDate") Instant fromDate,
                                                        @Param("toDate") Instant toDate);



}

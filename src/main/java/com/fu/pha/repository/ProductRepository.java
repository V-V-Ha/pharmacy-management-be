package com.fu.pha.repository;

import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.dto.response.report.product.ExpiredProductDto;
import com.fu.pha.dto.response.report.product.OutOfStockProductDto;
import com.fu.pha.dto.response.report.reportEntity.ProductReportDto;
import com.fu.pha.entity.Product;
import com.fu.pha.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> getProductById(Long id);

    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Product findProductById(@Param("id") Long id);

    Optional<Product> findByProductCode(String productCode);
    Optional<Product> findByRegistrationNumber(String registrationNumber);

    @Query("SELECT p.productCode FROM Product p ORDER BY p.productCode DESC LIMIT 1")
    String getLastProductCode();

    @Query("SELECT new com.fu.pha.dto.response.ProductDTOResponse(p) FROM Product p WHERE " +
            "((LOWER(p.productName) LIKE LOWER(CONCAT('%', :productName, '%')) OR :productName IS NULL OR :productName = '') AND " +
            "(p.categoryId.categoryName = :category OR :category IS NULL OR :category = '')) " +
            " AND (p.status = :status OR :status IS NULL OR :status = '') " +
            "ORDER BY p.lastModifiedDate DESC")
    Page<ProductDTOResponse> getListProductPaging(@Param("productName") String productName,
                                                  @Param("category") String category,
                                                  @Param("status") Status status,
                                                  Pageable pageable);

    @Query("SELECT new com.fu.pha.dto.response.ProductDTOResponse(p) FROM Product p WHERE " +
            "((LOWER(p.productName) LIKE LOWER(CONCAT('%', :productName, '%')) OR :productName IS NULL OR :productName = '') AND " +
            "(p.status = 'ACTIVE') AND " +
            "(p.totalQuantity >= 1) AND " +
            "(p.prescriptionDrug = :prescriptionDrug)) " +
            "ORDER BY p.lastModifiedDate DESC")
    Page<ProductDTOResponse> getListProductForSaleOrderPaging(@Param("productName") String productName,
                                                              @Param("prescriptionDrug") Boolean prescriptionDrug,
                                                              Pageable pageable);
    @Query("SELECT new com.fu.pha.dto.response.ProductDTOResponse(p) FROM Product p WHERE " +
            "((LOWER(p.productName) LIKE LOWER(CONCAT('%', :productName, '%')) OR :productName IS NULL OR :productName = '') AND " +
            "(p.status = 'ACTIVE') AND " +
            "(p.totalQuantity >= 1)) " +
            "ORDER BY p.lastModifiedDate DESC")
    Page<ProductDTOResponse> getListProductForSaleOrderPagingisPre(String productName, Pageable pageable);


//    @Query("SELECT new com.fu.pha.dto.response.ProductDTOResponse(p) FROM Product p " +
//            "JOIN p.importItems i " +
//            "WHERE ((LOWER(p.productName) LIKE LOWER(CONCAT('%', :productName, '%')) OR :productName IS NULL OR :productName = '') " +
//            "AND p.status = 'ACTIVE' " +
//            "AND i.expiryDate > CURRENT_TIMESTAMP) " +
//            "GROUP BY p.id " +
//            "ORDER BY p.lastModifiedDate DESC")
//    Page<ProductDTOResponse> getListProductForSaleOrderPaging(@Param("productName") String productName, Pageable pageable);


    @Query("SELECT new com.fu.pha.dto.response.ProductDTOResponse(p) FROM Product p WHERE (LOWER(p.productName) LIKE lower(concat('%', :productName, '%')))" +
            " and p.status = 'ACTIVE'")
    Optional<List<ProductDTOResponse>> findProductByProductName(String productName);

    @Query("SELECT new com.fu.pha.dto.response.ProductDTOResponse(p) " +
            "FROM Product p " +
            "JOIN p.categoryId c " +
            " join p.productUnitList pu ")
    List<ProductDTOResponse> getListProduct();

    // report

    @Query("SELECT COUNT(p) FROM Product p WHERE p.totalQuantity = 0 AND p.status = 'ACTIVE'")
    Integer countOutOfStock();


    @Query("SELECT new com.fu.pha.dto.response.report.reportEntity.ProductReportDto(p.id, p.productName, p.productCode, " +
            "CAST(SUM(COALESCE(ii.remainingQuantity, 0)) AS integer), " +
            "SUM(COALESCE(ii.remainingQuantity, 0) * COALESCE(ii.unitPrice, 0.0))) " +
            "FROM Product p " +
            "LEFT JOIN ImportItem ii ON ii.product.id = p.id " +
            "WHERE p.status = 'ACTIVE' " +
            "GROUP BY p.id, p.productName, p.productCode " +
            "HAVING SUM(COALESCE(ii.remainingQuantity, 0)) <= :threshold")
    List<ProductReportDto> findNearlyOutOfStockProducts(@Param("threshold") int threshold);




    @Query("SELECT SUM(p.totalQuantity) FROM Product p")
    Integer calculateCurrentInventoryQuantity();


    @Query(value = "SELECT DISTINCT ON (p.id) p.id AS productId, " +
            "       p.product_code AS productCode, " +
            "       p.product_name AS productName, " +
            "       c.category_name AS categoryName, " +
            "       u.unit_name AS unitName, " +
            "       p.number_warning AS numberWarning, " +
            "       p.total_quantity AS totalQuantity " +
            "FROM product p " +
            "JOIN category c ON p.category_id = c.id " +
            "JOIN product_unit pu ON pu.product_id = p.id AND pu.conversion_factor = 1 " +
            "JOIN unit u ON pu.unit_id = u.id " +
            "WHERE p.total_quantity <= p.number_warning " +
            "AND p.status = 'ACTIVE' " +
            "AND (:categoryId IS NULL OR p.category_id = :categoryId) " +
            "AND (:searchText IS NULL OR LOWER(p.product_name) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
            "     OR LOWER(p.product_code) LIKE LOWER(CONCAT('%', :searchText, '%'))) " +
            "ORDER BY p.id, p.total_quantity ASC",
            countQuery = "SELECT COUNT(DISTINCT p.id) FROM product p " +
                    "WHERE p.total_quantity <= p.number_warning " +
                    "AND p.status = 'ACTIVE' " +
                    "AND (:categoryId IS NULL OR p.category_id = :categoryId) " +
                    "AND (:searchText IS NULL OR LOWER(p.product_name) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
                    "     OR LOWER(p.product_code) LIKE LOWER(CONCAT('%', :searchText, '%')))",
            nativeQuery = true)
    Page<OutOfStockProductDto> findOutOfStockProducts(
            @Param("categoryId") Long categoryId,
            @Param("searchText") String searchText,
            Pageable pageable
    );




    @Query(
            value = "SELECT p.id AS productId, " +
                    "       p.product_code AS productCode, " +
                    "       p.product_name AS productName, " +
                    "       c.category_name AS categoryName, " +
                    "       u.unit_name AS unitName, " +
                    "       ii.batch_number AS batchNumber, " +
                    "       ii.expiry_date AS expiryDate, " +
                    "       GREATEST(FLOOR(EXTRACT(EPOCH FROM ii.expiry_date - NOW()) / 86400), 0) AS daysRemaining " +
                    "FROM import_item ii " +
                    "JOIN product p ON ii.product_id = p.id " +
                    "JOIN category c ON p.category_id = c.id " +
                    "JOIN product_unit pu ON pu.product_id = p.id AND pu.conversion_factor = 1 " +
                    "JOIN unit u ON pu.unit_id = u.id " +
                    "WHERE ii.expiry_date <= :warningDate " +
                    "AND ii.remaining_quantity > 0 " +
                    "AND p.status = 'ACTIVE' " +
                    "AND (:categoryId IS NULL OR p.category_id = :categoryId) " +
                    "AND (:searchText IS NULL OR LOWER(p.product_name) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
                    "     OR LOWER(p.product_code) LIKE LOWER(CONCAT('%', :searchText, '%'))) " +
                    "ORDER BY ii.expiry_date DESC",
            countQuery = "SELECT COUNT(*) FROM (" +
                    "SELECT ii.id " +
                    "FROM import_item ii " +
                    "JOIN product p ON ii.product_id = p.id " +
                    "WHERE ii.expiry_date <= :warningDate " +
                    "AND ii.remaining_quantity > 0 " +
                    "AND p.status = 'ACTIVE' " +
                    "AND (:categoryId IS NULL OR p.category_id = :categoryId) " +
                    "AND (:searchText IS NULL OR LOWER(p.product_name) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
                    "     OR LOWER(p.product_code) LIKE LOWER(CONCAT('%', :searchText, '%'))) " +
                    ") AS sub",
            nativeQuery = true
    )
    Page<ExpiredProductDto> findExpiredProducts(
            @Param("categoryId") Long categoryId,
            @Param("searchText") String searchText,
            @Param("warningDate") Instant warningDate,
            Pageable pageable
    );



    @Query(value = "SELECT p FROM Product p WHERE p.totalQuantity = 0 AND p.status = 'ACTIVE'")
    List<Product> findLowStockProducts();


}

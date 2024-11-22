package com.fu.pha.repository;

import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.dto.response.report.reportEntity.ProductReportDto;
import com.fu.pha.entity.Product;
import com.fu.pha.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

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
            " (p.status = 'ACTIVE' )) " +
            "ORDER BY p.lastModifiedDate DESC")
    Page<ProductDTOResponse> getListProductForSaleOrderPaging(@Param("productName") String productName,
                                                               Pageable pageable);

    @Query("SELECT new com.fu.pha.dto.response.ProductDTOResponse(p) FROM Product p WHERE (LOWER(p.productName) LIKE lower(concat('%', :productName, '%')))" +
            " and p.status = 'ACTIVE'")
    Optional<List<ProductDTOResponse>> findProductByProductName(String productName);

    @Query("SELECT new com.fu.pha.dto.response.ProductDTOResponse(p) " +
            "FROM Product p " +
            "JOIN p.categoryId c " +
            " join p.productUnitList pu ")
    List<ProductDTOResponse> getListProduct();

    // report
    @Query("SELECT new com.fu.pha.dto.response.report.reportEntity.ProductReportDto(p.id, p.productName, p.productCode, " +
            "CAST(SUM(COALESCE(ii.remainingQuantity, 0)) AS integer), " +
            "SUM(COALESCE(ii.remainingQuantity, 0) * COALESCE(ii.unitPrice, 0.0))) " +
            "FROM Product p " +
            "LEFT JOIN ImportItem ii ON ii.product.id = p.id " +
            "GROUP BY p.id, p.productName, p.productCode")
    List<ProductReportDto> findOutOfStockProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.totalQuantity = 0")
    Integer countOutOfStock();


    @Query("SELECT new com.fu.pha.dto.response.report.reportEntity.ProductReportDto(p.id, p.productName, p.productCode, " +
            "CAST(SUM(COALESCE(ii.remainingQuantity, 0)) AS integer), " +
            "SUM(COALESCE(ii.remainingQuantity, 0) * COALESCE(ii.unitPrice, 0.0))) " +
            "FROM Product p " +
            "LEFT JOIN ImportItem ii ON ii.product.id = p.id " +
            "GROUP BY p.id, p.productName, p.productCode " +
            "HAVING SUM(COALESCE(ii.remainingQuantity, 0)) <= :threshold")
    List<ProductReportDto> findNearlyOutOfStockProducts(@Param("threshold") int threshold);



    @Query("SELECT SUM(p.totalQuantity) FROM Product p")
    Integer calculateCurrentInventoryQuantity();

    @Query("SELECT SUM(p.remainingQuantity * p.unitPrice / p.conversionFactor) FROM ImportItem p")
    Double calculateCurrentInventoryAmount();







}

package com.fu.pha.repository;

import com.fu.pha.dto.request.ProductDTORequest;
import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.dto.response.report.CurrentStockDTO;
import com.fu.pha.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
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
            "ORDER BY p.lastModifiedDate DESC")
    Page<ProductDTOResponse> getListProductPaging(String productName, String category, Pageable pageable);

    @Query("SELECT new com.fu.pha.dto.response.ProductDTOResponse(p) FROM Product p WHERE (LOWER(p.productName) LIKE lower(concat('%', :productName, '%')))")
    Optional<List<ProductDTOResponse>> findProductByProductName(String productName);

    @Query("SELECT new com.fu.pha.dto.response.ProductDTOResponse(p) " +
            "FROM Product p " +
            "JOIN p.categoryId c " +
            " join p.productUnitList pu ")
    List<ProductDTOResponse> getListProduct();

    @Query("SELECT COUNT(p) AS outOfStockQuantity " +
            "FROM Product p " +
            "WHERE p.totalQuantity = 0")
    Integer findOutOfStock();

    @Query("SELECT COUNT(p) AS lowStockQuantity " +
            "FROM Product p " +
            "WHERE p.totalQuantity < :minimumStockThreshold")
    Integer findLowStock(@Param("minimumStockThreshold") Integer minimumStockThreshold);

    @Query("SELECT " +
            "SUM(i.remainingQuantity) AS currentQuantity, " +
            "SUM((i.remainingQuantity * 1.0 / pu.conversionFactor) * i.unitPrice) AS currentValue " +
            "FROM ImportItem i " +
            "JOIN i.unit u " +
            "JOIN ProductUnit pu ON pu.unit.unitName = u AND pu.product = i.product " +
            "WHERE i.remainingQuantity > 0")
    Optional<CurrentStockDTO> findCurrentStock();






}

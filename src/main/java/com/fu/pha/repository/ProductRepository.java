package com.fu.pha.repository;

import com.fu.pha.dto.request.ProductDTORequest;
import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Boolean existsByProductCode(String productCode);
    Boolean existsByRegistrationNumber(String registrationNumber);
    Product getProductByProductCode(String productCode);
    Product getProductByRegistrationNumber(String registrationNumber);
    Product getProductById(Long id);


    @Query("SELECT new com.fu.pha.dto.response.ProductDTOResponse(p) FROM Product p WHERE " +
            "((LOWER(p.productName) LIKE LOWER(CONCAT('%', :productName, '%')) OR :productName IS NULL OR :productName = '') AND " +
            "(p.categoryId.categoryName = :category OR :category IS NULL OR :category = '')) " +
            "ORDER BY p.lastModifiedDate DESC")
    Page<ProductDTOResponse> getListProductPaging(String productName, String category, Pageable pageable);
}

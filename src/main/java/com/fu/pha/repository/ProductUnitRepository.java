package com.fu.pha.repository;

import com.fu.pha.dto.request.UnitDto;
import com.fu.pha.entity.ProductUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductUnitRepository extends JpaRepository<ProductUnit, Long> {
    //use native query to get unit by product id
    @Query(value = "SELECT u.id, u.unit_name " +
            "FROM unit u JOIN product_unit pu ON u.id = pu.unit_id " +
            "WHERE pu.product_id = :productId", nativeQuery = true)
    List<Object[]> findUnitsByProductId(Long productId);

//    @Query("SELECT pu " +
//            " FROM ProductUnit pu " +
//            " WHERE pu.id = :id")
//    ProductUnit getProductUnitById(@Param("id") Long id);

    @Query("SELECT pu FROM ProductUnit pu WHERE pu.id = :id AND pu.productId.id = :productId")
    ProductUnit findProductUnitsByIdAndProductId(@Param("id") Long id, @Param("productId") Long productId);

    @Query("SELECT pu FROM ProductUnit pu WHERE pu.productId.id = :productId")
    List<ProductUnit> findByProductId(Long productId);
}

package com.fu.pha.repository;

import com.fu.pha.dto.request.UnitDto;
import com.fu.pha.entity.ProductUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductUnitRepository extends JpaRepository<ProductUnit, Long> {
    //use native query to get unit by product id
    @Query(value = "SELECT u.id, u.unit_name " +
            "FROM unit u JOIN product_unit pu ON u.id = pu.unit_id " +
            "WHERE pu.product_id = :productId", nativeQuery = true)
    List<Object[]> findUnitsByProductId(Long productId);
}

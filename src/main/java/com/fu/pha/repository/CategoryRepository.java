package com.fu.pha.repository;

import com.fu.pha.dto.request.CategoryDto;
import com.fu.pha.entity.Category;
import com.fu.pha.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category,Long> {

    Optional<Category> findById(Long id);

    Optional<Category> getCategoryByCategoryName(String categoryName);
    Optional<Category> findByCategoryName(String name);

   // get all category and paging with search name
    @Query("SELECT c FROM Category c WHERE " +
            "(LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :name, '%')) OR :name IS NULL OR :name = '') " +
            " AND (c.status = :status OR :status IS NULL OR :status = '') " +
            "ORDER BY c.lastModifiedDate DESC")
    Page<CategoryDto> findAllByNameContaining(@Param("name") String name,
                                              @Param("status") Status status,
                                              Pageable pageable);

    @Query("SELECT new com.fu.pha.dto.request.CategoryDto(c.id, c.categoryName) FROM Category c WHERE c.status = 'ACTIVE'")
    List<CategoryDto> findAllCategory();

    @Query("SELECT c FROM Category c WHERE c.id = :id")
    Optional<Category> getCategoryById(String id);
}

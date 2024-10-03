package com.fu.pha.repository;

import com.fu.pha.dto.request.CategoryDto;
import com.fu.pha.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category,Long> {
    @Override
    Optional<Category> findById(Long id);

    Optional<Category> getCategoryByCategoryName(String categoryName);
    Optional<Category> findByCategoryName(String name);

   // get all category and paging with search name
    @Query("SELECT c FROM Category c WHERE " +
            "(LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :name, '%')) OR :name IS NULL OR :name = '') " +
            "ORDER BY c.lastModifiedDate DESC")
    Page<CategoryDto> findAllByNameContaining(String name, Pageable pageable);


}

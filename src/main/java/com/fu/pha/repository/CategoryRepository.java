package com.fu.pha.repository;

import com.fu.pha.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category getCategoryByCategoryName(String categoryName);
}

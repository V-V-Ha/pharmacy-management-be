package com.fu.pha.dto.request;

import com.fu.pha.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {
    private Long id;
    private String name;
    private String description;
    private Instant createDate;
    private Instant lastModifiedDate;
    private String createBy;
    private String lastModifiedBy;


    public CategoryDto(Category category) {
        this.id = category.getId();
        this.name = category.getCategoryName();
        this.description = category.getDescription();
        this.createDate=category.getCreateDate();
        this.createBy=category.getCreateBy();
        this.lastModifiedDate=category.getLastModifiedDate();
        this.lastModifiedBy=category.getLastModifiedBy();
    }

    public CategoryDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

}

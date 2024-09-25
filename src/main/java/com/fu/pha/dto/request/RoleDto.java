package com.fu.pha.dto.request;

import com.fu.pha.entity.Role;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RoleDto {
    private Integer id;
    private String name;

    public RoleDto(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
}


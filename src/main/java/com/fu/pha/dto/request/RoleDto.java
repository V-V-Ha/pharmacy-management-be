package com.fu.pha.dto.request;

import com.fu.pha.entity.Role;
import com.fu.pha.enums.ERole;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDto {
    private Integer id;
    private String name;


}


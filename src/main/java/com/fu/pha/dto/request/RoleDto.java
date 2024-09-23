package com.fu.pha.dto.request;

import com.fu.pha.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleDto {
    private String name;

    public RoleDto(Role role) {
        this.name = role.getName().name();
    }
}


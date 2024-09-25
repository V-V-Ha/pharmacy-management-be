package com.fu.pha.dto.request;

import com.fu.pha.entity.Role;
import com.fu.pha.entity.User;
import com.fu.pha.enums.Gender;
import com.fu.pha.enums.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String password;
    private Set<RoleDto> rolesDto ;
    private String address;
    private Instant dob;
    private Gender gender;
    private String fullName;
    private String avatar;
    private String phone;
    private String cic;
    private UserStatus status;
    private String note;
    private Instant createDate;
    private Instant lastModifiedDate;
    private String createBy;
    private String lastModifiedBy;


    public UserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.rolesDto = user.getRoles() != null
                ? user.getRoles().stream().map(role -> new RoleDto(role.getId(), String.valueOf(role.getName()))).collect(Collectors.toSet())
                : new HashSet<>();

        this.address = user.getAddress();
        this.dob = user.getDob();
        this.gender = user.getGender();
        this.fullName = user.getFullName();
        this.avatar = user.getAvatar() != null ? user.getAvatar() : "";
        this.phone = user.getPhone();
        this.cic = user.getCic() != null ? user.getCic() : "";
        this.status = user.getStatus();
        this.note = user.getNote();
        this.createDate=user.getCreateDate();
        this.createBy=user.getCreateBy();
        this.lastModifiedDate=user.getLastModifiedDate();
        this.lastModifiedBy=user.getLastModifiedBy();
    }
}

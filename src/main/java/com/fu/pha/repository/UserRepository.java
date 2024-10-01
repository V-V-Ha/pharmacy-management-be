package com.fu.pha.repository;


import com.fu.pha.dto.request.UserDto;
import com.fu.pha.entity.User;
import com.fu.pha.enums.ERole;
import com.fu.pha.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    User getUserById(Long id);

    User getUserByCic(String cic);

    User getUserByPhone(String phone);

    User getUserByEmail(String email);


    // get user paging and search by full name , filter by role

    @Query("SELECT DISTINCT new com.fu.pha.dto.request.UserDto(u) FROM User u JOIN u.roles r WHERE " +
            "((LOWER(u.fullName) LIKE LOWER(CONCAT('%', :fullName, '%')) OR :fullName IS NULL OR :fullName = '') AND " +
            "(r.name = :role OR :role IS NULL OR :role = '') AND " +
            "(u.status = :status OR :status IS NULL OR :status = '')) " +
            "ORDER BY u.lastModifiedDate DESC")
    Page<UserDto> getAllUserPaging(@Param("fullName") String fullName,
                                   @Param("role") ERole role,
                                   @Param("status") Status status,
                                   Pageable pageable);


    //check condition delete user if user does not exist in other table



}

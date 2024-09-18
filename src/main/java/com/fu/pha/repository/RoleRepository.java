package com.sep.pha.repository;

import com.sep.pha.entity.Role;
import com.sep.pha.enums.Erole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role,Integer> {
    Optional<Role> findByName(Erole role);
}

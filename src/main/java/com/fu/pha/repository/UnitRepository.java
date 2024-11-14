package com.fu.pha.repository;

import com.fu.pha.dto.request.UnitDto;
import com.fu.pha.entity.Unit;
import com.fu.pha.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UnitRepository extends JpaRepository<Unit, Long> {
    @Query("SELECT u FROM Unit u WHERE " +
            "(LOWER(u.unitName) LIKE LOWER(CONCAT('%', :name, '%')) OR :name IS NULL OR :name = '') " +
            " AND (u.status = :status OR :status IS NULL OR :status = '') " +
            "ORDER BY u.lastModifiedDate DESC")
    Page<UnitDto> findAllByNameContaining(@Param("name") String name,
                                          @Param("status") Status status,
                                          Pageable pageable);

    Unit findByUnitName(String name);

    @Override
    Optional<Unit> findById(Long id);

    Unit findUnitById(Long id);

//
//    //get all unit
//    @Query("SELECT u FROM Unit u WHERE u.deleted = false ORDER BY u.lastModifiedDate DESC")
//    List<UnitDto> getAllUnit();

    @Query("SELECT new com.fu.pha.dto.request.UnitDto(u.id, u.unitName) FROM Unit u")
    List<UnitDto> getAllUnit();



//    @Query()
//    boolean isUnitAssociatedWithOtherEntities(Long id);
}

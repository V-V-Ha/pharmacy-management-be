package com.fu.pha.repository;

import com.fu.pha.entity.ExportSlipItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExportSlipItemRepository extends JpaRepository<ExportSlipItem, Long> {

    @Query("SELECT i FROM ExportSlipItem i WHERE i.isDeleted = false AND i.exportSlip.id = :exportSlipId")
    List<ExportSlipItem> findByExportSlipId(@Param("exportSlipId") Long exportSlipId);
}

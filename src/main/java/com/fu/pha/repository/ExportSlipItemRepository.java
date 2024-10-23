package com.fu.pha.repository;

import com.fu.pha.entity.ExportSlipItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExportSlipItemRepository extends JpaRepository<ExportSlipItem, Long> {
}

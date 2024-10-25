package com.fu.pha.repository;


import com.fu.pha.entity.ExportSlip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExportSlipRepository extends JpaRepository<ExportSlip, Long> {

    @Query("SELECT e.invoiceNumber FROM ExportSlip e ORDER BY e.invoiceNumber DESC LIMIT 1")
    String getLastInvoiceNumber();

    @Query("SELECT e FROM ExportSlip e WHERE e.isDeleted = false")
    List<ExportSlip> findAllActive();
}

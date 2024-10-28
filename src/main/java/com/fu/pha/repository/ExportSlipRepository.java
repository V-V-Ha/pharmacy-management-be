package com.fu.pha.repository;


import com.fu.pha.dto.response.exportSlip.ExportSlipResponseDto;
import com.fu.pha.entity.ExportSlip;
import com.fu.pha.enums.ExportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ExportSlipRepository extends JpaRepository<ExportSlip, Long> {

    @Query("SELECT e.invoiceNumber FROM ExportSlip e ORDER BY e.invoiceNumber DESC LIMIT 1")
    String getLastInvoiceNumber();

    @Query("SELECT e FROM ExportSlip e WHERE e.isDeleted = false")
    List<ExportSlip> findAllActive();

    @Query("SELECT new com.fu.pha.dto.response.exportSlip.ExportSlipResponseDto(e) " +
            " FROM ExportSlip e " +
            " WHERE (:exportType IS NULL OR e.typeDelivery = :exportType OR :exportType = '') " +
            " AND (e.exportDate IS NULL OR e.exportDate BETWEEN :fromDate AND :toDate) " +
            " ORDER BY e.lastModifiedDate DESC")
    Page<ExportSlipResponseDto> getListExportSlipPaging(@Param("exportType") ExportType exportType,
                                                        @Param("fromDate") Instant fromDate,
                                                        @Param("toDate") Instant toDate,
                                                        Pageable pageable);

    @Query("SELECT new com.fu.pha.dto.response.exportSlip.ExportSlipResponseDto(e) " +
            " FROM ExportSlip e " +
            " WHERE (:exportType IS NULL OR e.typeDelivery = :exportType OR :exportType = '') " +
            " ORDER BY e.lastModifiedDate DESC")
    Page<ExportSlipResponseDto> getListExportSlipPagingWithoutDate(@Param("exportType") ExportType exportType,
                                                                   Pageable pageable);

    @Query("SELECT new com.fu.pha.dto.response.exportSlip.ExportSlipResponseDto(e) " +
            " FROM ExportSlip e " +
            " WHERE (:exportType IS NULL OR e.typeDelivery = :exportType OR :exportType = '') " +
            " AND e.exportDate >= :fromDate " +
            " ORDER BY e.lastModifiedDate DESC")
    Page<ExportSlipResponseDto> getListExportSlipPagingFromDate(@Param("exportType") ExportType exportType,
                                                                @Param("fromDate") Instant fromDate,
                                                                Pageable pageable);

    @Query("SELECT new com.fu.pha.dto.response.exportSlip.ExportSlipResponseDto(e) " +
            " FROM ExportSlip e " +
            " WHERE (:exportType IS NULL OR e.typeDelivery = :exportType OR :exportType = '') " +
            " AND e.exportDate <= :toDate " +
            " ORDER BY e.lastModifiedDate DESC")
    Page<ExportSlipResponseDto> getListExportSlipPagingToDate(@Param("exportType") ExportType exportType,
                                                              @Param("toDate") Instant toDate,
                                                              Pageable pageable);


}

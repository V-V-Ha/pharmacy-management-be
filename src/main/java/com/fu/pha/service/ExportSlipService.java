package com.fu.pha.service;

import com.fu.pha.dto.request.exportSlip.ExportSlipRequestDto;
import com.fu.pha.dto.response.exportSlip.ExportSlipResponseDto;
import com.fu.pha.entity.ExportSlip;
import com.fu.pha.enums.ExportType;
import com.fu.pha.enums.OrderStatus;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

public interface ExportSlipService {

    void createExport(ExportSlipRequestDto exportDto);

    void updateExport(Long exportSlipId, ExportSlipRequestDto exportDto);

    void confirmExport(Long exportSlipId);

    void rejectExport(Long exportSlipId, String reason);

    ExportSlipResponseDto getActiveExportSlipById(Long exportSlipId);

    Page<ExportSlipResponseDto> getAllExportSlipPaging(int page, int size, ExportType exportType, OrderStatus status, Instant fromDate, Instant toDate);

    ExportSlipResponseDto getExportById(Long exportSlipId);

    void exportExportSlipsToExcel(HttpServletResponse response, Instant fromDate, Instant toDate) throws IOException;
}

package com.fu.pha.service;

import com.fu.pha.dto.request.exportSlip.ExportSlipRequestDto;
import com.fu.pha.dto.response.exportSlip.ExportSlipResponseDto;
import com.fu.pha.entity.ExportSlip;

import java.util.List;

public interface ExportSlipService {

    void createExport(ExportSlipRequestDto exportDto);

    void updateExport(Long exportSlipId, ExportSlipRequestDto exportDto);

    void softDeleteExportSlip(Long exportSlipId);

    ExportSlipResponseDto getActiveExportSlipById(Long exportSlipId);


}

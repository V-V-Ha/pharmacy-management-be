package com.fu.pha.service;

import com.fu.pha.dto.request.exportSlip.ExportSlipDto;

public interface ExportSlipService {

    void createExport(ExportSlipDto exportDto);
}

package com.fu.pha.service;

import com.fu.pha.dto.request.UnitDto;

import java.util.List;

public interface UnitService {
    List<UnitDto> getAllUnit();
    UnitDto getUnitById(Long id);
    UnitDto createUnit(UnitDto unitDto);
    UnitDto updateUnit(UnitDto unitDto);
    void deleteUnit(Long id);
}

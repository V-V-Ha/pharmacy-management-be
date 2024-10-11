package com.fu.pha.service;

import com.fu.pha.dto.request.UnitDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UnitService {
    Page<UnitDto> getAllUnitPaging(int page, int size, String name);
    UnitDto getUnitById(Long id);
    void createUnit(UnitDto unitDto);
    void updateUnit(UnitDto unitDto);
    void deleteUnit(Long id);

    List<UnitDto> getAllUnit();
}

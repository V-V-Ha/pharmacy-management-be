package com.fu.pha.service;

import com.fu.pha.dto.request.UnitDto;
import com.fu.pha.enums.Status;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UnitService {
    Page<UnitDto> getAllUnitPaging(int page, int size, String name, String status);
    UnitDto getUnitById(Long id);
    void createUnit(UnitDto unitDto);
    void updateUnit(UnitDto unitDto);
    void activeUnit(Long id);
    void deActiveUnit(Long id);

    List<UnitDto> getAllUnit();
}

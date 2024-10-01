package com.fu.pha.service.impl;

import com.fu.pha.dto.request.UnitDto;
import com.fu.pha.entity.Unit;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.UnitRepository;
import com.fu.pha.exception.Message;
import com.fu.pha.service.UnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
@Service
public class UnitServiceImpl implements UnitService {

    @Autowired
    UnitRepository unitRepository;

    @Override
    public Page<UnitDto> getAllUnitPaging(int page, int size, String name) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UnitDto> unitPage = unitRepository.findAllByNameContaining(name, pageable);
        if(unitPage.isEmpty()){
            throw new ResourceNotFoundException(Message.UNIT_NOT_FOUND);
        }
        return unitPage;
    }

    @Override
    public UnitDto getUnitById(Long id) {
        Unit unit = unitRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Message.UNIT_NOT_FOUND));
        return new UnitDto(unit);
    }

    @Override
    public void createUnit(UnitDto unitDto) {
        // Validate the request
        if (unitDto == null || unitDto.getUnitName() == null || unitDto.getUnitName().isEmpty()) {
            throw new BadRequestException(Message.NULL_FILED);
        }
        // Existing unit
        Unit unitExist = unitRepository.findByUnitName(unitDto.getUnitName());
        if (unitExist != null) {
            throw new BadRequestException(Message.UNIT_EXIST);
        }

        // Create a new unit entity
        Unit unit = new Unit();
        unit.setUnitName(unitDto.getUnitName());
        unit.setDescription(unitDto.getDescription());
        unit.setCreateDate(Instant.now());
        unit.setCreateBy(SecurityContextHolder.getContext().getAuthentication().getName());
        unit.setLastModifiedDate(Instant.now());
        unit.setLastModifiedBy(SecurityContextHolder.getContext().getAuthentication().getName());

        // Save the unit to the database
        unitRepository.save(unit);
    }

    @Override
    public void updateUnit(UnitDto unitDto) {
        // Validate the request
        if (unitDto == null || unitDto.getId() == null || unitDto.getUnitName() == null || unitDto.getUnitName().isEmpty()) {
            throw new BadRequestException(Message.NULL_FILED);
        }

        // Find the existing unit by ID
        Unit existingUnit = unitRepository.findById(unitDto.getId()).orElse(null);
        if (existingUnit == null) {
            throw new ResourceNotFoundException(Message.UNIT_NOT_FOUND);
        }

        // Check if the unit name is already taken
        Unit unit = unitRepository.findByUnitName(unitDto.getUnitName());
        if (unit != null && !unit.getId().equals(unitDto.getId())) {
            throw new BadRequestException(Message.UNIT_EXIST);
        }

        // Update the unit fields
        existingUnit.setUnitName(unitDto.getUnitName());
        existingUnit.setDescription(unitDto.getDescription());
        existingUnit.setLastModifiedDate(Instant.now());
        existingUnit.setLastModifiedBy(SecurityContextHolder.getContext().getAuthentication().getName());

        // Save the updated unit to the database
        unitRepository.save(existingUnit);
    }

    @Override
    public void deleteUnit(Long id) {

    }

}

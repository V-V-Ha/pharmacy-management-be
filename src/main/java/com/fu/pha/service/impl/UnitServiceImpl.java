package com.fu.pha.service.impl;

import com.fu.pha.dto.request.UnitDto;
import com.fu.pha.entity.Unit;
import com.fu.pha.enums.Status;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.UnitRepository;
import com.fu.pha.exception.Message;
import com.fu.pha.service.UnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UnitServiceImpl implements UnitService {

    @Autowired
    UnitRepository unitRepository;

    @Override
    public Page<UnitDto> getAllUnitPaging(int page, int size, String name, String status) {
        Pageable pageable = PageRequest.of(page, size);
        Status unitStatus = null;
        if (status != null) {
            try {
                unitStatus = Status.valueOf(status.toUpperCase());
            } catch (Exception e) {
                throw new ResourceNotFoundException(Message.STATUS_NOT_FOUND);
            }
        }
        Page<UnitDto> unitPage = unitRepository.findAllByNameContaining(name, unitStatus, pageable);
        if(unitPage.isEmpty()){
            throw new ResourceNotFoundException(Message.UNIT_NOT_FOUND);
        }
        return unitPage;
    }

    public List<UnitDto> getAllUnit() {
        return unitRepository.getAllUnit();
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
        String normalizedUnitName = capitalizeWords(unitDto.getUnitName());


        Unit unitExist = unitRepository.findByUnitName(normalizedUnitName);
        if (unitExist != null) {
            throw new BadRequestException(Message.UNIT_EXIST);
        }

        Unit unit = new Unit();
        unit.setUnitName(normalizedUnitName);
        unit.setDescription(unitDto.getDescription());
        unit.setStatus(Status.ACTIVE);

        // Save the unit to the database
        unitRepository.save(unit);
    }
    
    private String capitalizeWords(String str) {
        String[] words = str.toLowerCase().split("\\s+");
        StringBuilder capitalizedWords = new StringBuilder();

        for (String word : words) {
            if (word.length() > 0) {
                capitalizedWords.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        return capitalizedWords.toString().trim();
    }

    @Override
    @Transactional
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

        // Normalize the unit name
        String normalizedUnitName = capitalizeWords(unitDto.getUnitName());

        // Check if the unit name is already taken
        Unit unit = unitRepository.findByUnitName(normalizedUnitName);
        if (unit != null && !unit.getId().equals(unitDto.getId())) {
            throw new BadRequestException(Message.UNIT_EXIST);
        }

        // Update the unit fields
        existingUnit.setUnitName(normalizedUnitName);
        existingUnit.setDescription(unitDto.getDescription());

        // Save the updated unit to the database
        unitRepository.save(existingUnit);
    }
    
    @Override
    public void updateUnitStatus(Long id) {
        Unit unit = unitRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(Message.UNIT_NOT_FOUND));

        // Chuyển đổi trạng thái
        if (unit.getStatus() == Status.ACTIVE) {
            unit.setStatus(Status.INACTIVE);
        } else {
            unit.setStatus(Status.ACTIVE);
        }
        unitRepository.save(unit);
    }


}

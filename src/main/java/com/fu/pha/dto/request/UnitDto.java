package com.fu.pha.dto.request;

import com.fu.pha.entity.Unit;
import com.fu.pha.enums.Status;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnitDto {
    private Long id;
    private String unitName;
    private String description;
    private Instant createDate;
    private Instant lastModifiedDate;
    private String createBy;
    private String lastModifiedBy;
    private Status status;

    public UnitDto(Unit unit) {
        this.id = unit.getId();
        this.unitName = unit.getUnitName();
        this.description = unit.getDescription();
        this.createDate=unit.getCreateDate();
        this.createBy=unit.getCreateBy();
        this.lastModifiedDate=unit.getLastModifiedDate();
        this.lastModifiedBy=unit.getLastModifiedBy();
        this.status=unit.getStatus();
    }

    public UnitDto(Long id, String unitName) {
        this.id = id;
        this.unitName = unitName;
    }



}

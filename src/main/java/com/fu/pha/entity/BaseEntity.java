package com.fu.pha.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.time.Instant;
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@MappedSuperclass
public class BaseEntity {
    @Column(name = "create_by")
    private String createBy;
    @Column(name = "create_date")
    private Instant createDate = Instant.now();
    @Column(name = "last_modified_by")
    private String lastModifiedBy;
    @Column(name = "last_modified_date")
    private Instant lastModifiedDate = Instant.now() ;

}

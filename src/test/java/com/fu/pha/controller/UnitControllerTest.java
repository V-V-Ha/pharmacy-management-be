package com.fu.pha.controller;

import com.fu.pha.dto.request.UnitDto;
import com.fu.pha.dto.response.PageResponseModel;
import com.fu.pha.exception.Message;
import com.fu.pha.service.UnitService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UnitControllerTest {

    @Mock
    private UnitService unitService;

    @InjectMocks
    private UnitController unitController;

    public UnitControllerTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllUnitPaging() {
        Page<UnitDto> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(unitService.getAllUnitPaging(anyInt(), anyInt(), anyString())).thenReturn(page);

        ResponseEntity<PageResponseModel<UnitDto>> response = unitController.getAllUnitPaging(0, 10, "");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, Objects.requireNonNull(response.getBody()).getTotal());
    }

    @Test
    void testGetUnitById() {
        UnitDto unitDto = new UnitDto();
        when(unitService.getUnitById(anyLong())).thenReturn(unitDto);

        ResponseEntity<UnitDto> response = unitController.getUnitById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(unitDto, response.getBody());
    }

    @Test
    void testCreateUnit() {
        UnitDto unitDto = new UnitDto();
        doNothing().when(unitService).createUnit(any(UnitDto.class));

        ResponseEntity<String> response = unitController.createUnit(unitDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Message.CREATE_SUCCESS, response.getBody());
    }

    @Test
    void testUpdateUnit() {
        UnitDto unitDto = new UnitDto();
        doNothing().when(unitService).updateUnit(any(UnitDto.class));

        ResponseEntity<String> response = unitController.updateUnit(unitDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Message.UPDATE_SUCCESS, response.getBody());
    }

    @Test
    void testDeleteUnit() {
        doNothing().when(unitService).deleteUnit(anyLong());

        ResponseEntity<String> response = unitController.deleteUnit(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Message.DELETE_SUCCESS, response.getBody());
    }
}
package com.fu.pha.controller;

import com.fu.pha.dto.request.DoctorDTORequest;
import com.fu.pha.dto.response.DoctorDTOResponse;
import com.fu.pha.dto.response.PageResponseModel;
import com.fu.pha.exception.Message;
import com.fu.pha.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    @Autowired
    DoctorService doctorService;

    @GetMapping("/get-all-doctor")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<PageResponseModel<DoctorDTOResponse>> getAllDoctor(@RequestParam(defaultValue = "0") int page,
                                                                             @RequestParam(defaultValue = "10") int size,
                                                                             @RequestParam(defaultValue = "", name = "doctorName") String doctorName,
                                                                             @RequestParam(defaultValue = "") String status) {
        Page<DoctorDTOResponse> doctorDTOResponses = doctorService.getAllDoctorByPaging(page, size, doctorName, status);
        PageResponseModel<DoctorDTOResponse> response = PageResponseModel.<DoctorDTOResponse>builder()
                .page(page)
                .size(size)
                .total(doctorDTOResponses.getTotalElements())
                .listData(doctorDTOResponses.getContent())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-doctor-by-id")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<DoctorDTOResponse> getDoctorById(@RequestParam Long id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    @PutMapping("/change-status-doctor")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<String> updateDoctorStatus(@RequestParam Long id) {
        doctorService.updateDoctorStatus(id);
        return ResponseEntity.ok(Message.UPDATE_SUCCESS);
    }

    @PostMapping("/create-doctor")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<String> createDoctor(@Valid  @RequestBody DoctorDTORequest request) {
        doctorService.createDoctor(request);
        return ResponseEntity.ok(Message.CREATE_SUCCESS);
    }

    @PutMapping("/update-doctor")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<String> updateDoctor(@Valid @RequestBody DoctorDTORequest request) {
        doctorService.updateDoctor(request);
        return ResponseEntity.ok(Message.UPDATE_SUCCESS);
    }

}

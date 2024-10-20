package com.fu.pha.service;

import com.fu.pha.dto.request.SupplierDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SupplierService {
    //create supplier
    void createSupplier(SupplierDto supplierDto);
    //update supplier
    void updateSupplier(SupplierDto supplierDto);
    //delete supplier
    void deleteSupplier(Long id);
    //get supplier by id
    SupplierDto getSupplierById(Long id);
    //get all supplier
    List<SupplierDto> getAllSupplier();

    Page<SupplierDto> getAllSupplierAndPaging(int page, int size, String name);


}

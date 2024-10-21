package com.fu.pha.service.impl;

import com.fu.pha.dto.request.SupplierDto;
import com.fu.pha.entity.Supplier;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.repository.SupplierRepository;
import com.fu.pha.service.SupplierService;
import com.fu.pha.exception.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class SupplierServiceImpl implements SupplierService {
    @Autowired
    private SupplierRepository supplierRepository;
    @Override
    public void createSupplier(SupplierDto supplierDto) {
        //check exist tax
        Optional<Supplier> supplierExist = supplierRepository.findByTax(supplierDto.getTax());
        if(supplierExist.isPresent()){
            throw new BadRequestException(Message.SUPPLIER_EXIST);
        }

        //create new supplier
        Supplier supplier = new Supplier();
        supplier.setSupplierName(supplierDto.getSupplierName());
        supplier.setTax(supplierDto.getTax());
        supplier.setAddress(supplierDto.getAddress());
        supplier.setPhoneNumber(supplierDto.getPhoneNumber());
        supplier.setEmail(supplierDto.getEmail());

        supplierRepository.save(supplier);

    }

    @Override
    public void updateSupplier(SupplierDto supplierDto) {
        Optional<Supplier> supplierExist = supplierRepository.findById(supplierDto.getId());
        if (supplierExist.isEmpty()) {
            throw new BadRequestException(Message.SUPPLIER_NOT_FOUND);
        }
        //check exist tax
        Optional<Supplier> supplierExistTax = supplierRepository.findByTax(supplierDto.getTax());
        if(supplierExistTax.isPresent() && !supplierExistTax.get().getId().equals(supplierDto.getId())){
            throw new BadRequestException(Message.SUPPLIER_EXIST);
        }

        Supplier supplier = supplierExist.get();
        supplier.setSupplierName(supplierDto.getSupplierName());
        supplier.setTax(supplierDto.getTax());
        supplier.setAddress(supplierDto.getAddress());
        supplier.setPhoneNumber(supplierDto.getPhoneNumber());
        supplier.setEmail(supplierDto.getEmail());

        supplierRepository.save(supplier);

    }

    @Override
    public void deleteSupplier(Long id) {
        Optional<Supplier> supplier = supplierRepository.findById(id);
        if (supplier.isEmpty()) {
            throw new BadRequestException(Message.SUPPLIER_NOT_FOUND);
        }
        supplier.get().setDeleted(true);
        supplierRepository.save(supplier.get());
    }

    @Override
    public SupplierDto getSupplierById(Long id) {
        Optional<Supplier> supplier = supplierRepository.findById(id);
        if (supplier.isEmpty()) {
            throw new BadRequestException(Message.SUPPLIER_NOT_FOUND);
        }
        SupplierDto supplierDto = new SupplierDto();
        supplierDto.setId(supplier.get().getId());
        supplierDto.setSupplierName(supplier.get().getSupplierName());
        supplierDto.setTax(supplier.get().getTax());
        supplierDto.setAddress(supplier.get().getAddress());
        supplierDto.setPhoneNumber(supplier.get().getPhoneNumber());
        supplierDto.setEmail(supplier.get().getEmail());
        return supplierDto;
    }

    @Override
    public List<SupplierDto> getAllSupplier() {
        return supplierRepository.findAllSupplier();
    }

    @Override
    public Page<SupplierDto> getAllSupplierAndPaging(int page, int size , String name) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SupplierDto> supplierPage = supplierRepository.findAllByNameContaining(name, pageable);
        if(supplierPage.isEmpty()){
            throw new BadRequestException(Message.SUPPLIER_NOT_FOUND);
        }
        return supplierPage;
    }





}

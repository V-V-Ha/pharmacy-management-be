package com.fu.pha.service.impl;

import com.fu.pha.dto.request.SupplierDto;
import com.fu.pha.entity.Supplier;
import com.fu.pha.enums.Status;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.ResourceNotFoundException;
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
        // Check exist tax
        Optional<Supplier> supplierExist = supplierRepository.findByTax(supplierDto.getTax());
        if (supplierExist.isPresent()) {
            throw new BadRequestException(Message.SUPPLIER_EXIST);
        }

        // Check exist phone number
        Optional<Supplier> supplierExistByPhone = supplierRepository.findByPhoneNumber(supplierDto.getPhoneNumber());
        if (supplierExistByPhone.isPresent()) {
            throw new BadRequestException(Message.EXIST_PHONE);
        }

        // Normalize the supplier name to capitalize the first letter of each word
        String normalizedSupplierName = capitalizeWords(supplierDto.getSupplierName());

        // Create new supplier with normalized name
        Supplier supplier = new Supplier();
        supplier.setSupplierName(normalizedSupplierName); // Set the formatted name
        supplier.setTax(supplierDto.getTax());
        supplier.setAddress(supplierDto.getAddress());
        supplier.setPhoneNumber(supplierDto.getPhoneNumber());
        supplier.setEmail(supplierDto.getEmail());
        supplier.setStatus(Status.ACTIVE);

        // Save supplier to the database
        supplierRepository.save(supplier);
    }

    // Helper method to capitalize the first letter of each word
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

        // Kiểm tra số điện thoại trùng lặp
        Optional<Supplier> supplierWithSamePhone = supplierRepository.findByPhoneNumber(supplierDto.getPhoneNumber());
        if (supplierWithSamePhone.isPresent() && !supplierWithSamePhone.get().getId().equals(supplierDto.getId())) {
            throw new BadRequestException(Message.EXIST_PHONE);
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
    public void activeSupplier(Long id) {
        Optional<Supplier> supplier = supplierRepository.findById(id);
        if (supplier.isEmpty()) {
            throw new BadRequestException(Message.SUPPLIER_NOT_FOUND);
        }
        supplier.get().setStatus(Status.ACTIVE);
        supplierRepository.save(supplier.get());
    }

    @Override
    public void deActiveSupplier(Long id) {
        Optional<Supplier> supplier = supplierRepository.findById(id);
        if (supplier.isEmpty()) {
            throw new BadRequestException(Message.SUPPLIER_NOT_FOUND);
        }
        supplier.get().setStatus(Status.INACTIVE);
        supplierRepository.save(supplier.get());
    }

    @Override
    public void updateSupplierStatus(Long id, String status) {
        Optional<Supplier> supplier = supplierRepository.findById(id);
        if (supplier.isEmpty()) {
            throw new BadRequestException(Message.SUPPLIER_NOT_FOUND);
        }
        Status supplierStatus = null;
        if (status != null) {
            try {
                supplierStatus = Status.valueOf(status.toUpperCase());
            } catch (Exception e) {
                throw new ResourceNotFoundException(Message.STATUS_NOT_FOUND);
            }
        }
        supplier.get().setStatus(supplierStatus);
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
        supplierDto.setStatus(supplier.get().getStatus());
        return supplierDto;
    }

    @Override
    public List<SupplierDto> getAllSupplier() {
        return supplierRepository.findAllSupplier();
    }

    @Override
    public Page<SupplierDto> getAllSupplierAndPaging(int page, int size , String name, String status) {
        Pageable pageable = PageRequest.of(page, size);
        Status supplierStatus = null;
        if (status != null) {
            try {
                supplierStatus = Status.valueOf(status.toUpperCase());
            } catch (Exception e) {
                throw new ResourceNotFoundException(Message.STATUS_NOT_FOUND);
            }
        }
        Page<SupplierDto> supplierPage = supplierRepository.findAllByNameContaining(name, supplierStatus, pageable);
        if(supplierPage.isEmpty()){
            throw new BadRequestException(Message.SUPPLIER_NOT_FOUND);
        }
        return supplierPage;
    }
}

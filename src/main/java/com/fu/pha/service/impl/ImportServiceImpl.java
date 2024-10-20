package com.fu.pha.service.impl;

import com.fu.pha.dto.request.ProductDTORequest;
import com.fu.pha.dto.request.ProductUnitDTORequest;
import com.fu.pha.dto.request.SupplierDto;
import com.fu.pha.dto.request.importPack.ImportDto;
import com.fu.pha.convert.GenerateCode;
import com.fu.pha.dto.request.UnitDto;
import com.fu.pha.dto.request.importPack.ImportItemRequestDto;
import com.fu.pha.dto.request.importPack.ImportViewListDto;
import com.fu.pha.dto.response.ImportItemResponseDto;
import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.entity.*;
import com.fu.pha.enums.PaymentMethod;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.*;
import com.fu.pha.service.ImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ImportServiceImpl implements ImportService {

    @Autowired
    private ImportRepository importRepository;

    @Autowired
    private ProductUnitRepository productUnitRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ImportItemRepository importItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private GenerateCode generateCode;

    @Override
    public List<UnitDto> getUnitByProductId(Long productId) {
        Optional<Product> product = productRepository.getProductById(productId);
        if (product.isEmpty()) {
            throw new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND);
        }

        List<Object[]> units = productUnitRepository.findUnitsByProductId(productId);

        return units.stream()
                .map(result -> new UnitDto(
                        ((Number) result[0]).longValue(),
                        (String) result[1]
                ))
                .collect(Collectors.toList());
    }
    @Override
    public List<ProductDTOResponse> getProductByProductName(String productName) {
        Optional<List<ProductDTOResponse>> product = productRepository.findProductByProductName(productName);
        if (product.isEmpty()) {
            throw new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND);
        }
        return product.get();
    }

    public List<SupplierDto> getSuppplierBySupplierName(String supplierName) {
        Optional<List<SupplierDto>> supplier = supplierRepository.findSupplierBySupplierName(supplierName);
        if (supplier.isEmpty()) {
            throw new ResourceNotFoundException(Message.SUPPLIER_NOT_FOUND);
        }
        return supplier.get();
    }


    @Transactional
    @Override
    public void createImport(ImportDto importDto) {
        // Tìm user và supplier
        Optional<User> user = userRepository.findById(importDto.getUserId());
        if (user.isEmpty()) {
            throw new ResourceNotFoundException(Message.USER_NOT_FOUND);
        }

        Optional<Supplier> supplier = supplierRepository.findById(importDto.getSupplierId());
        if (supplier.isEmpty()) {
            throw new ResourceNotFoundException(Message.SUPPLIER_NOT_FOUND);
        }

        // Tạo Import entity và gán các giá trị
        Import importReceipt = new Import();
        if (importRepository.getLastInvoiceNumber() == null) {
            importReceipt.setInvoiceNumber("PN000001");
        } else {
            importReceipt.setInvoiceNumber(generateCode.generateNewProductCode(importRepository.getLastInvoiceNumber()));
        }

        importReceipt.setImportDate(Instant.now());
        importReceipt.setPaymentMethod(importDto.getPaymentMethod());
        importReceipt.setNote(importDto.getNote());
        importReceipt.setUser(user.get());
        importReceipt.setSupplier(supplier.get());
        importReceipt.setTax(importDto.getTax());
        importReceipt.setDiscount(importDto.getDiscount());

        // **Lưu Import entity trước**
        importRepository.save(importReceipt);

        // Tính tổng totalAmount từ các ImportItem
        double totalAmount = 0.0;

        // Lưu các ImportItem và tính tổng totalAmount
        for (ImportItemResponseDto itemDto : importDto.getImportItems()) {
            ImportItem importItem = new ImportItem();
            importItem.setImportReceipt(importReceipt); // Đã lưu Import trước, nên có thể gán nó vào ImportItem
            importItem.setProduct(productRepository.getProductById(itemDto.getProductId()).orElseThrow(() -> new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND)));
            importItem.setQuantity(itemDto.getQuantity());
            importItem.setUnit(itemDto.getUnit());
            importItem.setUnitPrice(itemDto.getUnitPrice());
            importItem.setDiscount(itemDto.getDiscount());
            importItem.setTax(itemDto.getTax());
            importItem.setBatchNumber(itemDto.getBatchNumber());
            importItem.setExpiryDate(itemDto.getExpiryDate());
            importItem.setTotalAmount(itemDto.getTotalAmount());

            List<ProductUnit> productUnits = productUnitRepository.findByProductId(itemDto.getProductId());

            // Lặp qua từng ProductUnit và cập nhật giá nhập cho tất cả các đơn vị nếu giá thay đổi
            for (ProductUnit productUnit : productUnits) {
                // Điều chỉnh giá nhập dựa trên conversionFactor của đơn vị nhập và đơn vị hiện tại
                double adjustedImportPrice = itemDto.getUnitPrice() / itemDto.getConversionFactor() * productUnit.getConversionFactor();

                // Kiểm tra nếu giá nhập mới khác với giá hiện tại
                if (!Objects.equals(productUnit.getImportPrice(), adjustedImportPrice)) {
                    productUnit.setImportPrice(adjustedImportPrice);
                    // Lưu ProductUnit đã được cập nhật giá nhập
                    productUnitRepository.save(productUnit);
                }
            }

            // Cộng dồn totalAmount
            totalAmount += itemDto.getTotalAmount();

            // Lưu ImportItem vào repository
            importItemRepository.save(importItem);
        }



        // Kiểm tra tổng totalAmount của Import với tổng các ImportItem
        if (importDto.getTotalAmount() != null && !importDto.getTotalAmount().equals(totalAmount)) {
            throw new BadRequestException(Message.TOTAL_AMOUNT_NOT_MATCH);
        }


        // Cập nhật lại tổng totalAmount cho Import
        importReceipt.setTotalAmount(totalAmount);
        importRepository.save(importReceipt); // Lưu lần nữa nếu muốn cập nhật totalAmount sau khi tính toán
    }

    @Override
    public Page<ImportViewListDto> getAllImportPaging(int page, int size, String supplierName, Instant fromDate, Instant toDate) {
        Pageable pageable = PageRequest.of(page, size);

        // Nếu cả fromDate và toDate đều null
        if (fromDate == null && toDate == null) {
            return importRepository.getListImportPagingWithoutDate(supplierName, pageable);
        }
        // Nếu chỉ có fromDate
        else if (fromDate != null && toDate == null) {
            return importRepository.getListImportPagingFromDate(supplierName, fromDate, pageable);
        }
        // Nếu chỉ có toDate
        else if (fromDate == null) {
            return importRepository.getListImportPagingToDate(supplierName, toDate, pageable);
        }
        // Nếu cả fromDate và toDate đều có giá trị
        else {
            return importRepository.getListImportPaging(supplierName, fromDate, toDate, pageable);
        }
    }


    @Transactional
    @Override
    public void updateImport(Long importId, ImportDto importDto) {
        // Tìm Import hiện tại
        Import importReceipt = importRepository.findById(importId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.IMPORT_NOT_FOUND));

        // Tìm user và supplier
        Optional<User> user = userRepository.findById(importDto.getUserId());
        if (user.isEmpty()) {
            throw new ResourceNotFoundException(Message.USER_NOT_FOUND);
        }

        Optional<Supplier> supplier = supplierRepository.findById(importDto.getSupplierId());
        if (supplier.isEmpty()) {
            throw new ResourceNotFoundException(Message.SUPPLIER_NOT_FOUND);
        }

        // Cập nhật thông tin Import
        importReceipt.setPaymentMethod(importDto.getPaymentMethod());
        importReceipt.setNote(importDto.getNote());
        importReceipt.setUser(user.get());
        importReceipt.setSupplier(supplier.get());
        importReceipt.setTax(importDto.getTax());
        importReceipt.setDiscount(importDto.getDiscount());
        importReceipt.setImportDate(Instant.now());

        // Xóa các ImportItem hiện có của Import này để cập nhật lại các mục mới
        importItemRepository.deleteByImportId(importId);

        // Tính tổng totalAmount từ các ImportItem mới
        double totalAmount = 0.0;

        // Lưu các ImportItem mới và tính tổng totalAmount
        for (ImportItemResponseDto itemDto : importDto.getImportItems()) {
            ImportItem importItem = new ImportItem();
            importItem.setImportReceipt(importReceipt); // Gán importReceipt đã cập nhật vào ImportItem
            importItem.setProduct(productRepository.getProductById(itemDto.getProductId()).orElseThrow(() -> new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND)));
            importItem.setQuantity(itemDto.getQuantity());
            importItem.setUnit(itemDto.getUnit());
            importItem.setUnitPrice(itemDto.getUnitPrice());
            importItem.setDiscount(itemDto.getDiscount());
            importItem.setTax(itemDto.getTax());
            importItem.setBatchNumber(itemDto.getBatchNumber());
            importItem.setExpiryDate(itemDto.getExpiryDate());
            importItem.setTotalAmount(itemDto.getTotalAmount());

            List<ProductUnit> productUnits = productUnitRepository.findByProductId(itemDto.getProductId());

            // Lặp qua từng ProductUnit và cập nhật giá nhập cho tất cả các đơn vị nếu giá thay đổi
            for (ProductUnit productUnit : productUnits) {
                // Điều chỉnh giá nhập dựa trên conversionFactor của đơn vị nhập và đơn vị hiện tại
                double adjustedImportPrice = itemDto.getUnitPrice() / itemDto.getConversionFactor() * productUnit.getConversionFactor();

                // Kiểm tra nếu giá nhập mới khác với giá hiện tại
                if (!Objects.equals(productUnit.getImportPrice(), adjustedImportPrice)) {
                    productUnit.setImportPrice(adjustedImportPrice);
                    // Lưu ProductUnit đã được cập nhật giá nhập
                    productUnitRepository.save(productUnit);
                }
            }

            // Cộng dồn totalAmount
            totalAmount += itemDto.getTotalAmount();

            // Lưu ImportItem vào repository
            importItemRepository.save(importItem);
        }

        // Kiểm tra tổng totalAmount của Import với tổng các ImportItem
        if (importDto.getTotalAmount() != null && !importDto.getTotalAmount().equals(totalAmount)) {
            throw new BadRequestException(Message.TOTAL_AMOUNT_NOT_MATCH);
        }

        // Cập nhật lại tổng totalAmount cho Import và lưu vào repository
        importReceipt.setTotalAmount(totalAmount);
        importRepository.save(importReceipt);
    }


    @Override
    public ImportDto getImportById(Long importId) {
        Import importReceipt = importRepository.findById(importId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.IMPORT_NOT_FOUND));

        // Chuyển đổi Import sang ImportDto và trả về
        return new ImportDto(importReceipt);
    }








}
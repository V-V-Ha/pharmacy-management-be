package com.fu.pha.service.impl;

import com.fu.pha.dto.request.SupplierDto;
import com.fu.pha.dto.request.importPack.ImportDto;
import com.fu.pha.convert.GenerateCode;
import com.fu.pha.dto.request.UnitDto;
import com.fu.pha.dto.request.importPack.ImportItemRequestDto;
import com.fu.pha.dto.request.importPack.ImportViewListDto;
import com.fu.pha.dto.response.importPack.ImportItemResponseDto;
import com.fu.pha.dto.response.importPack.ImportItemResponseForExport;
import com.fu.pha.dto.response.importPack.ImportResponseDto;
import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.entity.*;
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
import java.util.List;
import java.util.Map;
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

    @Override
    public List<ImportItemResponseForExport> getImportItemByProductName(String productName) {
        // Tìm tất cả sản phẩm dựa trên tên sản phẩm
         List<ImportItemResponseForExport> importItems = importItemRepository.findImportItemsByProductName(productName);
         return importItems;

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
        User user = userRepository.findById(importDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.USER_NOT_FOUND));

        Supplier supplier = supplierRepository.findById(importDto.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.SUPPLIER_NOT_FOUND));

        // Tạo Import entity và gán các giá trị
        Import importReceipt = new Import();
        String lastInvoiceNumber = importRepository.getLastInvoiceNumber();
        importReceipt.setInvoiceNumber(lastInvoiceNumber == null ? "PN000001" : generateCode.generateNewProductCode(lastInvoiceNumber));

        importReceipt.setImportDate(Instant.now());
        importReceipt.setPaymentMethod(importDto.getPaymentMethod());
        importReceipt.setNote(importDto.getNote());
        importReceipt.setUser(user);
        importReceipt.setSupplier(supplier);
        importReceipt.setTax(importDto.getTax());
        importReceipt.setDiscount(importDto.getDiscount());

        // Lưu Import entity trước
        importRepository.save(importReceipt);

        // Kiểm tra danh sách importItems không rỗng
        if (importDto.getImportItems() == null || importDto.getImportItems().isEmpty()) {
            throw new BadRequestException(Message.IMPORT_ITEMS_EMPTY);
        }

        double totalAmount = 0.0;

        // Lưu các ImportItem và tính tổng totalAmount
        for (ImportItemRequestDto itemDto : importDto.getImportItems()) {
            ImportItem importItem = new ImportItem();
            importItem.setImportReceipt(importReceipt);

            // Lấy sản phẩm từ repository
            Product product = productRepository.getProductById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND));

            // Chuyển đổi quantity về đơn vị nhỏ nhất
            int smallestQuantity = itemDto.getQuantity() * itemDto.getConversionFactor();

            importItem.setProduct(product);
            importItem.setQuantity(itemDto.getQuantity());
            importItem.setUnit(itemDto.getUnit());
            importItem.setUnitPrice(itemDto.getUnitPrice());
            importItem.setDiscount(itemDto.getDiscount());
            importItem.setTax(itemDto.getTax());
            importItem.setBatchNumber(itemDto.getBatchNumber());
            importItem.setExpiryDate(itemDto.getExpiryDate());
            importItem.setTotalAmount(itemDto.getTotalAmount());
            importItem.setRemainingQuantity(smallestQuantity);  // Cập nhật số lượng còn lại theo đơn vị nhỏ nhất

            // Cập nhật totalQuantity của sản phẩm trong kho
            Integer currentTotalQuantity = product.getTotalQuantity();
            if (currentTotalQuantity == null) {
                currentTotalQuantity = 0;  // Gán giá trị mặc định nếu null
            }
            // Cập nhật tổng số lượng sản phẩm dựa trên đơn vị nhỏ nhất
            product.setTotalQuantity(currentTotalQuantity + smallestQuantity);
            productRepository.save(product);

            // Cập nhật giá nhập cho từng ProductUnit
            List<ProductUnit> productUnits = productUnitRepository.findByProductId(itemDto.getProductId());
            for (ProductUnit productUnit : productUnits) {
                if (itemDto.getConversionFactor() != 0) {
                    double adjustedImportPrice = itemDto.getUnitPrice() / itemDto.getConversionFactor() * productUnit.getConversionFactor();
                    if (!Objects.equals(productUnit.getImportPrice(), adjustedImportPrice)) {
                        productUnit.setImportPrice(adjustedImportPrice);
                        productUnitRepository.save(productUnit);
                    }
                } else {
                    throw new BadRequestException(Message.INVALID_CONVERSION_FACTOR);
                }
            }

            totalAmount += itemDto.getTotalAmount();
            importItemRepository.save(importItem);
        }

        // Kiểm tra sự khác biệt giữa totalAmount được tính và totalAmount trong DTO
        if (importDto.getTotalAmount() != null && !importDto.getTotalAmount().equals(totalAmount)) {
            throw new BadRequestException(Message.TOTAL_AMOUNT_NOT_MATCH);
        }

        // Cập nhật tổng số tiền vào Import
        importReceipt.setTotalAmount(totalAmount);
        importRepository.save(importReceipt);
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
        User user = userRepository.findById(importDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.USER_NOT_FOUND));

        Supplier supplier = supplierRepository.findById(importDto.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.SUPPLIER_NOT_FOUND));

        // Cập nhật thông tin Import
        importReceipt.setPaymentMethod(importDto.getPaymentMethod());
        importReceipt.setNote(importDto.getNote());
        importReceipt.setUser(user);
        importReceipt.setSupplier(supplier);
        importReceipt.setTax(importDto.getTax());
        importReceipt.setDiscount(importDto.getDiscount());
        importReceipt.setImportDate(Instant.now());

        // Tính tổng totalAmount từ các ImportItem mới
        double totalAmount = 0.0;

        // Lấy danh sách ImportItem hiện tại
        List<ImportItem> existingImportItems = importItemRepository.findByImportId(importId);

        // Sử dụng Map để tiện tra cứu các mục hiện tại theo ProductId
        Map<Long, ImportItem> existingItemMap = existingImportItems.stream()
                .collect(Collectors.toMap(item -> item.getProduct().getId(), item -> item));

        for (ImportItemRequestDto itemDto : importDto.getImportItems()) {
            ImportItem importItem = existingItemMap.get(itemDto.getProductId());

            // Lấy sản phẩm từ repository
            Product product = productRepository.getProductById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND));

            // Chuyển đổi quantity về đơn vị nhỏ nhất
            int smallestQuantity = itemDto.getQuantity() * itemDto.getConversionFactor();

            if (importItem != null) {
                // Nếu ImportItem đã tồn tại, cập nhật lại `totalQuantity` dựa trên sự chênh lệch giữa số lượng cũ và mới
                int oldSmallestQuantity = importItem.getRemainingQuantity();  // Số lượng còn lại trước đó
                int quantityDifference = smallestQuantity - oldSmallestQuantity;
                product.setTotalQuantity(product.getTotalQuantity() + quantityDifference);

                // Cập nhật lại thông tin của ImportItem
                importItem.setQuantity(itemDto.getQuantity());  // Giữ nguyên đơn vị nhập
                importItem.setUnit(itemDto.getUnit());
                importItem.setUnitPrice(itemDto.getUnitPrice());
                importItem.setDiscount(itemDto.getDiscount());
                importItem.setTax(itemDto.getTax());
                importItem.setBatchNumber(itemDto.getBatchNumber());
                importItem.setExpiryDate(itemDto.getExpiryDate());
                importItem.setTotalAmount(itemDto.getTotalAmount());
                importItem.setRemainingQuantity(smallestQuantity);  // Cập nhật số lượng còn lại theo đơn vị nhỏ nhất

                // Cập nhật giá nhập cho ProductUnit nếu cần
                updateProductUnits(product, itemDto);

                // Lưu ImportItem đã cập nhật
                importItemRepository.save(importItem);
            } else {
                // Nếu ImportItem không tồn tại, tạo mới
                importItem = new ImportItem();
                importItem.setImportReceipt(importReceipt);

                importItem.setProduct(product);
                importItem.setQuantity(itemDto.getQuantity());  // Giữ nguyên đơn vị nhập
                importItem.setUnit(itemDto.getUnit());
                importItem.setUnitPrice(itemDto.getUnitPrice());
                importItem.setDiscount(itemDto.getDiscount());
                importItem.setTax(itemDto.getTax());
                importItem.setBatchNumber(itemDto.getBatchNumber());
                importItem.setExpiryDate(itemDto.getExpiryDate());
                importItem.setTotalAmount(itemDto.getTotalAmount());
                importItem.setRemainingQuantity(smallestQuantity);  // Chuyển đổi số lượng còn lại theo đơn vị nhỏ nhất

                // Cập nhật tổng số lượng sản phẩm trong Product khi thêm mới
                product.setTotalQuantity((product.getTotalQuantity() == null ? 0 : product.getTotalQuantity()) + smallestQuantity);
                productRepository.save(product);

                // Cập nhật giá nhập cho ProductUnit nếu cần
                updateProductUnits(product, itemDto);

                // Lưu ImportItem mới
                importItemRepository.save(importItem);
            }

            // Cộng dồn totalAmount
            totalAmount += itemDto.getTotalAmount();
        }

        // Kiểm tra tổng totalAmount của Import với tổng các ImportItem
        if (importDto.getTotalAmount() != null && !importDto.getTotalAmount().equals(totalAmount)) {
            throw new BadRequestException(Message.TOTAL_AMOUNT_NOT_MATCH);
        }

        // Cập nhật lại tổng totalAmount cho Import và lưu vào repository
        importReceipt.setTotalAmount(totalAmount);
        importRepository.save(importReceipt);
    }

    // Hàm cập nhật giá nhập cho ProductUnit
    private void updateProductUnits(Product product, ImportItemRequestDto itemDto) {
        List<ProductUnit> productUnits = productUnitRepository.findByProductId(product.getId()); // Cập nhật đúng theo productId
        for (ProductUnit productUnit : productUnits) {
            if (itemDto.getConversionFactor() != 0) {
                double adjustedImportPrice = itemDto.getUnitPrice() / itemDto.getConversionFactor() * productUnit.getConversionFactor();
                if (!Objects.equals(productUnit.getImportPrice(), adjustedImportPrice)) {
                    productUnit.setImportPrice(adjustedImportPrice);
                    productUnitRepository.save(productUnit);
                }
            } else {
                throw new BadRequestException(Message.INVALID_CONVERSION_FACTOR);
            }
        }
    }


    @Override
    public ImportResponseDto getImportById(Long importId) {
        Import importReceipt = importRepository.findById(importId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.IMPORT_NOT_FOUND));

        // Chuyển đổi Import sang ImportDto và trả về
        return new ImportResponseDto(importReceipt);
    }
}
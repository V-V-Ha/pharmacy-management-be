package com.fu.pha.service.impl;

import com.fu.pha.dto.request.ProductDTORequest;
import com.fu.pha.dto.request.ProductUnitDTORequest;
import com.fu.pha.dto.request.importPack.ImportDto;
import com.fu.pha.convert.GenerateCode;
import com.fu.pha.dto.request.UnitDto;
import com.fu.pha.dto.request.importPack.ImportItemRequestDto;
import com.fu.pha.dto.request.importPack.ImportViewListDto;
import com.fu.pha.dto.response.ImportItemResponseDto;
import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.entity.*;
import com.fu.pha.enums.PaymentMethod;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.*;
import com.fu.pha.service.ImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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
    public List<ImportViewListDto> getAllImportAndPaging() {
        return null;
    }



    public void convertUnit(Long productId){
        Optional<Product> product = productRepository.getProductById(productId);
        if (product.isEmpty()) {
            throw new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND);
        }

        List<Object[]> units = productUnitRepository.findUnitsByProductId(productId);
    }

   // IMPORT ITEM
   private final List<ImportItemResponseDto> temporaryImportItems = new ArrayList<>();

    @Override
    public List<ImportItemResponseDto> addItemToImport(ImportItemResponseDto importItemDto) {

        // Lấy sản phẩm từ repository
        Optional<Product> productOpt = productRepository.getProductById(importItemDto.getProductId());
        if (productOpt.isEmpty()) {
            throw new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND);
        }

        Product product = productOpt.get();

        // Tìm kiếm sản phẩm trong danh sách tạm thời dựa trên productId
        Optional<ImportItemResponseDto> existingItemOpt = temporaryImportItems.stream()
                .filter(item -> item.getProductId().equals(importItemDto.getProductId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            // Nếu đã có sản phẩm trong danh sách tạm, cập nhật số lượng
            ImportItemResponseDto existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + importItemDto.getQuantity());
            existingItem.setTotalAmount(calculateTotalAmount(existingItem));
        } else {
            // Nếu chưa có, tạo mới ImportItemResponseDto tạm thời
            ImportItemResponseDto newImportItem = new ImportItemResponseDto();
            newImportItem.setProductId(product.getId());
            newImportItem.setQuantity(importItemDto.getQuantity());
            newImportItem.setUnitPrice(importItemDto.getUnitPrice());
            newImportItem.setDiscount(importItemDto.getDiscount());
            newImportItem.setTax(importItemDto.getTax());
            newImportItem.setBatchNumber(importItemDto.getBatchNumber());
            newImportItem.setExpiryDate(importItemDto.getExpiryDate());
            newImportItem.setTotalAmount(calculateTotalAmount(newImportItem));

            temporaryImportItems.add(newImportItem);
        }

        return temporaryImportItems;
    }


    @Override
    public List<ImportItemResponseDto> updateItemInImport(ImportItemResponseDto importItemDto) {

        Optional<ImportItemResponseDto> existingItemOpt = temporaryImportItems.stream()
                .filter(item -> item.getProductId().equals(importItemDto.getProductId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            ImportItemResponseDto existingItem = existingItemOpt.get();
            existingItem.setQuantity(importItemDto.getQuantity());
            existingItem.setUnitPrice(importItemDto.getUnitPrice());
            existingItem.setDiscount(importItemDto.getDiscount());
            existingItem.setTax(importItemDto.getTax());
            existingItem.setBatchNumber(importItemDto.getBatchNumber());
            existingItem.setExpiryDate(importItemDto.getExpiryDate());
            existingItem.setTotalAmount(calculateTotalAmount(existingItem));
        } else {
            throw new ResourceNotFoundException(Message.IMPORT_ITEM_NOT_FOUND);
        }

        return temporaryImportItems;
    }

    @Override
    public List<ImportItemResponseDto> getTemporaryImportItems() {
        if (temporaryImportItems.isEmpty()) {
            throw new ResourceNotFoundException(Message.IMPORT_ITEM_NOT_FOUND);
        }
        return temporaryImportItems;
    }



    @Override
    public void removeItemFromImport(Long productId) {

        Optional<ImportItemResponseDto> existingItemOpt = temporaryImportItems.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            temporaryImportItems.remove(existingItemOpt.get());
        } else {
            throw new ResourceNotFoundException(Message.IMPORT_ITEM_NOT_FOUND);
        }
    }


    private double calculateTotalAmount(ImportItemResponseDto item) {
        double subTotal = item.getUnitPrice() * item.getQuantity();
        double discountedAmount = subTotal - (subTotal * item.getDiscount() / 100);
        return discountedAmount + (discountedAmount * item.getTax() / 100);
    }



    @Override
    public void createImport(ImportDto importDto) {

        // Tìm user
        Optional<User> user = userRepository.findById(importDto.getUserId());
        if (user.isEmpty()) {
            throw new ResourceNotFoundException(Message.USER_NOT_FOUND);
        }

        // Tìm supplier
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
        importReceipt.setPaymentMethod(PaymentMethod.valueOf(importDto.getPaymentMethod()));
        importReceipt.setNote(importDto.getNote());
        importReceipt.setUser(user.get());
        importReceipt.setSupplier(supplier.get());
        importReceipt.setTax(importDto.getTax());
        importReceipt.setDiscount(importDto.getDiscount());
        importReceipt.setTotalAmount(importDto.getTotalAmount());

        // Lưu Import entity
        importRepository.save(importReceipt);

        // Tạo các ImportItem từ danh sách importItems trong importDto
        List<ImportItem> importItems = importDto.getImportItems().stream().map(importItemDto -> {
            ImportItem importItem = new ImportItem();
            importItem.setQuantity(importItemDto.getQuantity());
            importItem.setUnitPrice(importItemDto.getUnitPrice());
            importItem.setDiscount(importItemDto.getDiscount());
            importItem.setTax(importItemDto.getTax());
            importItem.setBatchNumber(importItemDto.getBatchNumber());
            importItem.setExpiryDate(importItemDto.getExpiryDate());
            importItem.setTotalAmount(importItemDto.getTotalAmount());

            // Tìm product dựa vào productId
            Optional<Product> product = productRepository.findById(importItemDto.getProductId());
            if (product.isEmpty()) {
                throw new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND);
            }
            importItem.setProduct(product.get());

            // Gán Import item vào Import Receipt
            importItem.setImportReceipt(importReceipt);

            return importItem;
        }).collect(Collectors.toList());

        // Lưu danh sách ImportItem
        importItemRepository.saveAll(importItems);
    }





    public Import getImportById(Long importId) {
        Optional<Import> importReceipt = importRepository.findById(importId);
        if (importReceipt.isEmpty()) {
            throw new ResourceNotFoundException(Message.IMPORT_NOT_FOUND);
        }
        return importReceipt.get();
    }


}
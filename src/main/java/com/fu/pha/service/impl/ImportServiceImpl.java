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
    private final List<ImportItem> temporaryImportItems = new ArrayList<>();
    @Override
    public List<ImportItem> addItemToImport(ImportItemResponseDto importItemDto) {

        // Lấy sản phẩm từ repository
        Optional<Product> productOpt = productRepository.getProductById(importItemDto.getProductId());
        if (productOpt.isEmpty()) {
            throw new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND);
        }

        Product product = productOpt.get();

        // Tìm kiếm sản phẩm trong danh sách tạm thời dựa trên productId
        Optional<ImportItem> existingItemOpt = temporaryImportItems.stream()
                .filter(item -> item.getProduct().getId().equals(importItemDto.getProductId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            // Nếu đã có sản phẩm trong danh sách tạm, cập nhật số lượng
            ImportItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + importItemDto.getQuantity());
            existingItem.setTotalAmount(calculateTotalAmount(existingItem));
        } else {
            // Nếu chưa có, tạo mới ImportItem tạm thời
            ImportItem newImportItem = new ImportItem();
            newImportItem.setProduct(product);
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
    public List<ImportItem> updateItemInImport(ImportItemResponseDto importItemDto) {

        Optional<ImportItem> existingItemOpt = temporaryImportItems.stream()
                .filter(item -> item.getProduct().getId().equals(importItemDto.getProductId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            ImportItem existingItem = existingItemOpt.get();
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
    public List<ImportItem> getTemporaryImportItems() {
        if (temporaryImportItems.isEmpty()) {
            throw new ResourceNotFoundException(Message.IMPORT_ITEM_NOT_FOUND);
        }
        return temporaryImportItems;
    }


    @Override
    public void removeItemFromImport(Long productId) {

        Optional<ImportItem> existingItemOpt = temporaryImportItems.stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            temporaryImportItems.remove(existingItemOpt.get());
        } else {
            throw new ResourceNotFoundException(Message.IMPORT_ITEM_NOT_FOUND);
        }
    }

    private double calculateTotalAmount(ImportItem item) {
        double subTotal = item.getUnitPrice() * item.getQuantity();
        double discountedAmount = subTotal - (subTotal * item.getDiscount() / 100);
        return discountedAmount + (discountedAmount * item.getTax() / 100);
    }



    @Override
    public void createImport(ImportDto importDto) {


        Optional<User> user = userRepository.findById(importDto.getUserId());
        if (user.isEmpty()) {
            throw new ResourceNotFoundException(Message.USER_NOT_FOUND);
        }

        Optional<Supplier> supplier = supplierRepository.findById(importDto.getSupplierId());
        if (supplier.isEmpty()) {
            throw new ResourceNotFoundException(Message.SUPPLIER_NOT_FOUND);
        }

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
//
//        importRepository.save(importReceipt);
//
//        if (importDto.getImportItemListDTO() != null) {
//            List<ImportItem> importItemList = new ArrayList<>();
//
//            for (ImportItemRequestDto importItemRequestDto : importDto.getImportItemListDTO()) {
//                Product product = productRepository.findById(importItemRequestDto.getProductId())
//                        .orElseThrow(() -> new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND));
//                ImportItem importItem = new ImportItem();
//                importItem.setProduct(product);
//                importItem.setImportReceipt(importReceipt);
//                importItem.setQuantity(importItemRequestDto.getQuantity());
//                importItem.setUnitPrice(importItemRequestDto.getUnitPrice());
//                importItem.setDiscount(importItemRequestDto.getDiscount());
//                importItem.setTax(importItemRequestDto.getTax());
//                importItem.setTotalAmount(importItemRequestDto.getTotalAmount());
//                importItem.setBatchNumber(importItemRequestDto.getBatchNumber());
//                importItem.setExpiryDate(importItemRequestDto.getExpiryDate());
//                if (productUnitDTORequest.getProductId().equals(importItem.getProduct().getId())) {
//                    List<ProductUnit> productUnitList = product.getProductUnitList();
//                        //check product unit exist
//                    ProductUnit productUnit = productUnitRepository.findProductUnitsByIdAndProductId(productUnitDTORequest.getProductId(), productUnitDTORequest.getUnitId());
//                    if (productUnit != null){
//                        //update product unit
//                        productUnit.setRetailPrice(importItemRequestDto.getUnitPrice());
//                        productUnitRepository.save(productUnit);
//                        productUnitList.add(productUnit);
//                    }
//                    product.setProductUnitList(productUnitList);
//                    productRepository.save(product);
//                }
//
//            }
//            importItemRepository.saveAll(importItemList);
//        }

        //create import item
        //code here

    }


    // Phương thức để lưu tất cả các ImportItem vào DB sau khi xác nhận
//    public void saveImportAndItems(Import importReceipt) {
//
//        // Kiểm tra danh sách tạm thời
//        if (temporaryImportItems.isEmpty()) {
//            throw new ResourceNotFoundException(Message.IMPORT_ITEM_NOT_FOUND);
//        }
//
//        // Lưu từng ImportItem trong danh sách vào DB, liên kết với phiếu nhập
//        for (ImportItem item : temporaryImportItems) {
//            item.setImportR(importReceipt);  // Liên kết với phiếu nhập kho
//            importItemRepository.save(item); // Lưu vào DB
//        }
//
//        // Xóa danh sách tạm sau khi lưu thành công
//        temporaryImportItems.clear();
//    }


    public Import getImportById(Long importId) {
        Optional<Import> importReceipt = importRepository.findById(importId);
        if (importReceipt.isEmpty()) {
            throw new ResourceNotFoundException(Message.IMPORT_NOT_FOUND);
        }
        return importReceipt.get();
    }

    public void addItemToImport (Long importId , ImportItemResponseDto importItem) {

        Import importReceipt = getImportById(importId);

        Optional<Product> product = productRepository.getProductById(importItem.getProductId());
        if (product.isEmpty()) {
            throw new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND);
        }
        Optional<ImportItem> importItemOpt = importItemRepository.findByProductIdAndImportId(importItem.getProductId(), importId);
        if (importItemOpt.isPresent()) {
            // If importItem already exists, update the quantity
            ImportItem existingImportItem = importItemOpt.get();
            existingImportItem.setQuantity(existingImportItem.getQuantity() + importItem.getQuantity());
            importItemRepository.save(existingImportItem);
        } else {
            // If importItem does not exist, create a new one
            ImportItem newImportItem = new ImportItem();
            newImportItem.setProduct(product.get());
            newImportItem.setImportReceipt(importReceipt);
            newImportItem.setQuantity(importItem.getQuantity());
            newImportItem.setUnitPrice(importItem.getUnitPrice());
            newImportItem.setDiscount(importItem.getDiscount());
            newImportItem.setTax(importItem.getTax());
            newImportItem.setTotalAmount(importItem.getTotalAmount());
            newImportItem.setBatchNumber(importItem.getBatchNumber());
            newImportItem.setExpiryDate(importItem.getExpiryDate());
            importItemRepository.save(newImportItem);
        }
    }

}
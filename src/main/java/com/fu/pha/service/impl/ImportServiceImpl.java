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
    public ProductDTOResponse getProductByProductName(String productName) {
        Optional<ProductDTOResponse> product = productRepository.findProductByProductName(productName);
        if (product.isEmpty()) {
            throw new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND);
        }
        return product.get();
    }

    @Override
    public List<ImportViewListDto> getAllImportAndPaging() {
        return null;
    }

    @Override
    public void createImport(ProductUnitDTORequest productUnitDTORequest ,ImportDto importDto) {

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

    public void convertUnit(Long productId){
        Optional<Product> product = productRepository.getProductById(productId);
        if (product.isEmpty()) {
            throw new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND);
        }

        List<Object[]> units = productUnitRepository.findUnitsByProductId(productId);
    }

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
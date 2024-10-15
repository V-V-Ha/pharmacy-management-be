package com.fu.pha.service.impl;

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

        importRepository.save(importReceipt);
        if (importDto.getImportItemList() != null) {
            for (ImportItemDto importItemDto : importDto.getImportItemList()) {
                ImportItem importItem = new ImportItem();
                importItem.setImportReceipt(importReceipt);
                importItem.setProduct(productRepository.findById(importItemDto.getProductId()).get());
                importItem.setQuantity(importItemDto.getQuantity());
                importItem.setUnitPrice(importItemDto.getUnitPrice());
                importItem.setTotalAmount(importItemDto.getTotalAmount());
                importItemRepository.save(importItem);
            }
        }
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
            newImportItem.setImportR(importReceipt);
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

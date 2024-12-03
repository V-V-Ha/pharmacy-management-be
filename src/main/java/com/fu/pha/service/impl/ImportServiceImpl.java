package com.fu.pha.service.impl;

import com.fu.pha.dto.request.SupplierDto;
import com.fu.pha.dto.request.importPack.ImportDto;
import com.fu.pha.convert.GenerateCode;
import com.fu.pha.dto.request.UnitDto;
import com.fu.pha.dto.request.importPack.ImportItemRequestDto;
import com.fu.pha.dto.request.importPack.ImportViewListDto;
import com.fu.pha.dto.response.CloudinaryResponse;
import com.fu.pha.dto.response.ProductDtoResponseForExport;
import com.fu.pha.dto.response.ProductUnitDTOResponse;
import com.fu.pha.dto.response.importPack.ImportItemResponseForExport;
import com.fu.pha.dto.response.importPack.ImportResponseDto;
import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.entity.*;
import com.fu.pha.enums.ERole;
import com.fu.pha.enums.OrderStatus;
import com.fu.pha.exception.*;
import com.fu.pha.repository.*;
import com.fu.pha.service.CloudinaryService;
import com.fu.pha.service.ImportService;
import com.fu.pha.service.NotificationService;
import com.fu.pha.util.FileUploadUtil;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private InventoryHistoryRepository inventoryHistoryRepository;

    @Autowired
    private NotificationService notificationService;

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
    public List<ProductDtoResponseForExport> getProductImportByProductName(String productName) {
        // Tìm tất cả các ImportItem theo productName
        List<ImportItem> importItems = importItemRepository.findImportItemsByProductName(productName);

        // Nhóm các ImportItem theo Product để tránh trùng lặp sản phẩm
        Map<Product, List<ImportItem>> importItemsGroupedByProduct = importItems.stream()
                .collect(Collectors.groupingBy(ImportItem::getProduct));

        // Tạo danh sách ProductDtoResponseForExport từ nhóm sản phẩm
        List<ProductDtoResponseForExport> productDtoResponses = importItemsGroupedByProduct.entrySet().stream()
                .map(entry -> {
                    Product product = entry.getKey();
                    List<ImportItem> itemsForProduct = entry.getValue();

                    // Tạo danh sách ImportItemResponseForExport cho từng sản phẩm
                    List<ImportItemResponseForExport> importItemResponses = itemsForProduct.stream()
                            .map(importItem -> {
                                List<ProductUnitDTOResponse> productUnits = importItem.getProduct().getProductUnitList()
                                        .stream()
                                        .map(ProductUnitDTOResponse::new)
                                        .collect(Collectors.toList());

                                Long supplierId = null;
                                String invoiceNumber = null;
                                if (importItem.getImportReceipt() != null && importItem.getImportReceipt().getSupplier() != null && importItem.getImportReceipt().getInvoiceNumber() != null) {
                                    supplierId = importItem.getImportReceipt().getSupplier().getId();
                                    invoiceNumber = importItem.getImportReceipt().getInvoiceNumber();
                                }


                                return new ImportItemResponseForExport(
                                        importItem.getId(),
                                        invoiceNumber,
                                        importItem.getQuantity(),
                                        importItem.getUnitPrice(),
                                        importItem.getUnit(),
                                        importItem.getDiscount(),
                                        importItem.getTax(),
                                        importItem.getTotalAmount(),
                                        importItem.getBatchNumber(),
                                        importItem.getImportReceipt().getId(),
                                        importItem.getExpiryDate(),
                                        importItem.getCreateDate(),
                                        importItem.getRemainingQuantity(),
                                        supplierId
                                );
                            })
                            .collect(Collectors.toList());

                    // Tạo ProductDtoResponseForExport cho từng sản phẩm
                    return new ProductDtoResponseForExport(
                            product.getProductCode(),
                            product.getProductName(),
                            product.getProductUnitList().stream().map(ProductUnitDTOResponse::new).collect(Collectors.toList()),
                            product.getRegistrationNumber(),
                            product.getManufacturer(),
                            product.getCountryOfOrigin(),
                            importItemResponses,
                            product.getTotalQuantity() // Lấy totalQuantity có sẵn từ Product
                    );
                })
                .collect(Collectors.toList());

        return productDtoResponses;
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
    public void createImport(ImportDto importRequestDto, MultipartFile file) {
        // Lấy người dùng hiện tại
        User currentUser = getCurrentUser();


        // Xác định trạng thái dựa trên vai trò
        OrderStatus status = determineImportStatus(currentUser);

        // Tìm nhà cung cấp
        Supplier supplier = supplierRepository.findById(importRequestDto.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.SUPPLIER_NOT_FOUND));

        // Tạo phiếu nhập
        Import importReceipt = createImportReceipt(importRequestDto, currentUser, supplier, status);

        if (file != null && !file.isEmpty()) {
            String imageUrl = uploadImage(file);
            importReceipt.setImage(imageUrl);
        } else {
            throw new BadRequestException(Message.IMAGE_IMPORT_NOT_NULL);
        }

        // Lưu phiếu nhập
        importRepository.save(importReceipt);

        // Kiểm tra danh sách ImportItem
        if (importRequestDto.getImportItems() == null || importRequestDto.getImportItems().isEmpty()) {
            throw new BadRequestException(Message.IMPORT_ITEMS_EMPTY);
        }

        double totalAmount = saveImportItems(importRequestDto, importReceipt, status);

        // So sánh tổng tiền giữa BE và FE
        if (importRequestDto.getTotalAmount() != null) {
            double feTotalAmount = importRequestDto.getTotalAmount();

            if (Math.abs(totalAmount - feTotalAmount) > 0.01) { // Cho phép sai số nhỏ
                throw new BadRequestException(Message.TOTAL_AMOUNT_NOT_MATCH);
            }
        } else {
            throw new BadRequestException(Message.TOTAL_AMOUNT_REQUIRED);
        }

        // Cập nhật tổng tiền và lưu lại
        importReceipt.setTotalAmount(totalAmount);
        importRepository.save(importReceipt);

        // Lấy danh sách chủ cửa hàng
        List<User> storeOwners = userRepository.findAllByRoles_Name(ERole.ROLE_PRODUCT_OWNER);

        if(!currentUser.getRoles().stream().anyMatch(r -> r.getName().equals(ERole.ROLE_PRODUCT_OWNER))){
            for (User storeOwner : storeOwners) {
                String title = "Phiếu nhập mới";
                String message = "Nhân viên " + currentUser.getUsername() + " đã tạo một phiếu nhập mới.";
                String url = "/import/receipt/detail/" +  importReceipt.getId();
                notificationService.sendNotificationToUser(title, message, storeOwner ,url);
            }
        }
    }

    @Override
    public Page<ImportViewListDto> getAllImportPaging(int page, int size, String supplierName, OrderStatus status, Instant fromDate, Instant toDate) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ImportViewListDto> importViewListDto;

        // Nếu cả fromDate và toDate đều null
        if (fromDate == null && toDate == null) {
            importViewListDto = importRepository.getListImportPagingWithoutDate(supplierName, status, pageable);
        }
        // Nếu chỉ có fromDate
        else if (fromDate != null && toDate == null) {
            importViewListDto = importRepository.getListImportPagingFromDate(supplierName, status, fromDate, pageable);
        }
        // Nếu chỉ có toDate
        else if (fromDate == null) {
            importViewListDto = importRepository.getListImportPagingToDate(supplierName, status, toDate, pageable);
        }
        // Nếu cả fromDate và toDate đều có giá trị
        else {
            importViewListDto = importRepository.getListImportPaging(supplierName, status, fromDate, toDate, pageable);
        }

        if (importViewListDto.isEmpty()) {
            throw new ResourceNotFoundException(Message.IMPORT_NOT_FOUND);
        }
        return importViewListDto;
    }

    @Transactional
    @Override
    public void updateImport(Long importId, ImportDto importDto, MultipartFile file) {
        // Lấy người dùng hiện tại
        User currentUser = getCurrentUser();

        // Tìm phiếu nhập
        Import importReceipt = importRepository.findById(importId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.IMPORT_NOT_FOUND));

        // Chỉ cho phép cập nhật khi trạng thái là PENDING hoặc REJECT
        if (importReceipt.getStatus() == OrderStatus.CONFIRMED) {
            throw new BadRequestException(Message.NOT_PENDING_IMPORT);
        }

        // Kiểm tra quyền hạn (nếu cần)
        if (!importReceipt.getUser().getId().equals(currentUser.getId())
                && !userHasRole(currentUser, ERole.ROLE_PRODUCT_OWNER)) {
            throw new UnauthorizedException(Message.REJECT_AUTHORIZATION);
        }

        // Nếu trạng thái hiện tại là REJECT và người cập nhật không phải là chủ cửa hàng, đặt lại trạng thái về PENDING
        if (importReceipt.getStatus() == OrderStatus.REJECT && !userHasRole(currentUser, ERole.ROLE_PRODUCT_OWNER)) {
            importReceipt.setStatus(OrderStatus.PENDING);
        } else if (importReceipt.getStatus() == OrderStatus.REJECT && userHasRole(currentUser, ERole.ROLE_PRODUCT_OWNER)) {
            importReceipt.setStatus(OrderStatus.CONFIRMED);
        } else if(importReceipt.getStatus() == OrderStatus.PENDING && userHasRole(currentUser, ERole.ROLE_PRODUCT_OWNER)){
            importReceipt.setStatus(OrderStatus.CONFIRMED);
        }


        // Tìm user và supplier mới
        User user = userRepository.findById(importDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.USER_NOT_FOUND));

        Supplier supplier = supplierRepository.findById(importDto.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.SUPPLIER_NOT_FOUND));

        // Cập nhật thông tin phiếu nhập
        importReceipt.setPaymentMethod(importDto.getPaymentMethod());
        importReceipt.setNote(importDto.getNote());
        importReceipt.setUser(user);
        importReceipt.setSupplier(supplier);
        importReceipt.setTax(importDto.getTax());
        importReceipt.setDiscount(importDto.getDiscount());
        importReceipt.setLastModifiedBy(currentUser.getFullName());
        importReceipt.setLastModifiedDate(Instant.now());

        if (file != null && !file.isEmpty()) {
            String imageUrl = uploadImage(file);
            importReceipt.setImage(imageUrl);
        }

        // Lấy danh sách ImportItem hiện tại
        List<ImportItem> existingImportItems = importItemRepository.findByImportId(importReceipt.getId());

        // Sử dụng Map để tiện tra cứu các mục hiện tại theo ProductId
        Map<Long, ImportItem> existingItemMap = existingImportItems.stream()
                .collect(Collectors.toMap(item -> item.getProduct().getId(), item -> item));

        double totalAmount = 0.0;

        // Xử lý các ImportItem mới
        for (ImportItemRequestDto itemDto : importDto.getImportItems()) {
            Long productId = itemDto.getProductId();
            ImportItem importItem = existingItemMap.get(productId);

            double itemTotalAmount = calculateImportItemTotalAmount(itemDto);

            // Cập nhật lại totalAmount
            totalAmount += itemTotalAmount;

            // Lấy sản phẩm
            Product product = productRepository.getProductById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND));

            int smallestQuantity = itemDto.getQuantity() * itemDto.getConversionFactor();

            if (importItem != null) {
                // Cập nhật ImportItem
                int oldSmallestQuantity = importItem.getRemainingQuantity();
                int quantityDifference = smallestQuantity - oldSmallestQuantity;

                if (importReceipt.getStatus() == OrderStatus.CONFIRMED) {
                    // Khôi phục tồn kho từ số lượng cũ
                    product.setTotalQuantity(product.getTotalQuantity() - oldSmallestQuantity);
                    // Cập nhật tồn kho với số lượng mới
                    product.setTotalQuantity(product.getTotalQuantity() + smallestQuantity);
                    productRepository.save(product);
                }

                importItem.setQuantity(itemDto.getQuantity());
                importItem.setUnit(itemDto.getUnit());
                importItem.setUnitPrice(itemDto.getUnitPrice());
                importItem.setDiscount(itemDto.getDiscount());
                importItem.setTax(itemDto.getTax());
                importItem.setBatchNumber(itemDto.getBatchNumber());
                importItem.setExpiryDate((itemDto.getExpiryDate() == null)
                        ? LocalDate.now().plusYears(100).atStartOfDay(ZoneId.systemDefault()).toInstant()
                        : itemDto.getExpiryDate());
                itemDto.setTotalAmount(itemTotalAmount);
                importItem.setTotalAmount(itemTotalAmount);
                importItem.setConversionFactor(itemDto.getConversionFactor());
                importItem.setRemainingQuantity(smallestQuantity);

                updateProductUnits(product, itemDto);
                importItemRepository.save(importItem);

                saveInventoryHistory(importItem, -smallestQuantity,
                        "Update import confirmed (ExportSlip ID: " + importReceipt.getId() + ")");

                // Xóa khỏi Map để xử lý các mục không còn trong danh sách mới
                existingItemMap.remove(productId);
            } else {
                // Tạo mới ImportItem
                ImportItem newImportItem = createImportItem(itemDto, importReceipt);
                importItemRepository.save(newImportItem);

                if (importReceipt.getStatus() == OrderStatus.CONFIRMED) {
                    product.setTotalQuantity(product.getTotalQuantity() + smallestQuantity);
                    productRepository.save(product);

                    saveInventoryHistory(importItem, -smallestQuantity,
                            "Update import confirmed (ExportSlip ID: " + importReceipt.getId() + ")");
                }

                updateProductUnits(product, itemDto);
            }
        }

        // Xử lý các ImportItem không còn trong danh sách mới
        for (ImportItem remainingItem : existingItemMap.values()) {
            Product product = remainingItem.getProduct();
            int smallestQuantity = remainingItem.getRemainingQuantity();

            if (importReceipt.getStatus() == OrderStatus.CONFIRMED) {
                // Giảm số lượng sản phẩm trong kho
                product.setTotalQuantity(product.getTotalQuantity() - smallestQuantity);
                productRepository.save(product);
            }

            // Xóa ImportItem
            importItemRepository.delete(remainingItem);
        }

        // Kiểm tra tổng totalAmount của Import với tổng các ImportItem
        if (importDto.getTotalAmount() != null && !importDto.getTotalAmount().equals(totalAmount)) {
            throw new BadRequestException(Message.TOTAL_AMOUNT_NOT_MATCH);
        }

        if (importDto.getTotalAmount() != null) {
            double feTotalAmount = importDto.getTotalAmount();

            if (Math.abs(totalAmount - feTotalAmount) > 0.01) { // Cho phép sai số nhỏ
                throw new BadRequestException(Message.TOTAL_AMOUNT_NOT_MATCH);
            }
        } else {
            throw new BadRequestException(Message.TOTAL_AMOUNT_REQUIRED);
        }

        // Cập nhật lại tổng totalAmount cho Import và lưu vào repository
        importReceipt.setTotalAmount(totalAmount);
        importRepository.save(importReceipt);

        // Lấy danh sách chủ cửa hàng
        List<User> storeOwners = userRepository.findAllByRoles_Name(ERole.ROLE_PRODUCT_OWNER);

        if(!currentUser.getRoles().stream().anyMatch(r -> r.getName().equals(ERole.ROLE_PRODUCT_OWNER))){
            for (User storeOwner : storeOwners) {
                String title = "Phiếu nhập mới";
                String message = "Nhân viên " + currentUser.getUsername() + " đã tạo một phiếu nhập mới.";
                String url = "/import/receipt/detail/" +  importReceipt.getId();
                notificationService.sendNotificationToUser(title, message, storeOwner ,url);
            }
        }
    }

    @Transactional
    @Override
    public void confirmImport(Long importId, Long userId) {
        // Lấy người dùng hiện tại
        User currentUser = getCurrentUser();

        // Kiểm tra quyền hạn
        if (!userHasRole(currentUser, ERole.ROLE_PRODUCT_OWNER)) {
            throw new UnauthorizedException(Message.REJECT_AUTHORIZATION);
        }

        // Tìm phiếu nhập
        Import importReceipt = importRepository.findById(importId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.IMPORT_NOT_FOUND));

        // Kiểm tra trạng thái
        if (importReceipt.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException(Message.NOT_PENDING_IMPORT);
        }

        // Cập nhật trạng thái
        importReceipt.setStatus(OrderStatus.CONFIRMED);
        importRepository.save(importReceipt);

        // Cập nhật số lượng sản phẩm và giá nhập
        for (ImportItem importItem : importReceipt.getImportItems()) {
            ImportItemRequestDto itemDto = mapImportItemToDto(importItem);
            updateProductQuantityAndPrice(itemDto, importItem);
            // Lưu lịch sử kho hàng
            int changeQuantity = itemDto.getQuantity() * itemDto.getConversionFactor();
            saveInventoryHistory(importItem, changeQuantity, "Confirmed Import");
        }

        User importCreator = importReceipt.getUser();
        String title = "Phiếu nhập đã được xác nhận";
        String message = "Phiếu nhập của bạn đã được xác nhận bởi chủ cửa hàng.";
        String url = "/import/receipt/detail/" + importId;
        notificationService.sendNotificationToUser(title, message, importCreator ,url);
    }

    @Transactional
    @Override
    public void rejectImport(Long importId, String reason) {
        // Lấy người dùng hiện tại
        User currentUser = getCurrentUser();

        // Kiểm tra quyền hạn
        if (!userHasRole(currentUser, ERole.ROLE_PRODUCT_OWNER)) {
            throw new UnauthorizedException(Message.REJECT_AUTHORIZATION);
        }

        // Tìm phiếu nhập
        Import importReceipt = importRepository.findById(importId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.IMPORT_NOT_FOUND));

        // Kiểm tra trạng thái
        if (importReceipt.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException(Message.NOT_PENDING_IMPORT);
        }

        // Kiểm tra lý do từ chối
        if (reason == null || reason.trim().isEmpty()) {
            throw new BadRequestException(Message.REASON_REQUIRED);
        }



        // Cập nhật trạng thái và ghi chú
        importReceipt.setStatus(OrderStatus.REJECT);
        importReceipt.setNote(reason); // Ghi lý do từ chối vào trường note
        importRepository.save(importReceipt);

        User importCreator = importReceipt.getUser();
        String title = "Phiếu nhập bị từ chối";
        String message = "Phiếu nhập của bạn đã bị từ chối. Lý do: " + reason;
        String url = "/import/receipt/detail/" + importId;
        notificationService.sendNotificationToUser(title, message, importCreator ,url);
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException(Message.NOT_LOGIN);
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(Message.USER_NOT_FOUND));
    }

    private OrderStatus determineImportStatus(User user) {
        if (userHasRole(user, ERole.ROLE_PRODUCT_OWNER)) {
            return OrderStatus.CONFIRMED;
        } else {
            return OrderStatus.PENDING;
        }
    }

    private boolean userHasRole(User user, ERole role) {
        return user.getRoles().stream().anyMatch(r -> r.getName().equals(role));
    }

    private Import createImportReceipt(ImportDto importDto, User user, Supplier supplier, OrderStatus status) {
        Import importReceipt = new Import();
        String lastInvoiceNumber = importRepository.getLastInvoiceNumber();
        importReceipt.setInvoiceNumber(lastInvoiceNumber == null ? "PN000001"
                : generateCode.generateNewProductCode(lastInvoiceNumber));
        importReceipt.setImportDate(Instant.now());
        importReceipt.setPaymentMethod(importDto.getPaymentMethod());
        importReceipt.setNote(importDto.getNote());
        importReceipt.setUser(user);
        importReceipt.setSupplier(supplier);
        importReceipt.setTax(importDto.getTax());
        importReceipt.setDiscount(importDto.getDiscount());
        importReceipt.setStatus(status);
        return importReceipt;
    }

    public double saveImportItems(ImportDto importDto, Import importReceipt, OrderStatus status) {
        double totalAmount = 0.0;

        for (ImportItemRequestDto itemDto : importDto.getImportItems()) {
            // Tính tổng tiền của ImportItem trên BE
            double itemTotalAmount = calculateImportItemTotalAmount(itemDto);

            // Cập nhật lại totalAmount
            totalAmount += itemTotalAmount;

            // Cập nhật itemDto với totalAmount đã tính toán
            itemDto.setTotalAmount(itemTotalAmount);

            // Tạo và lưu ImportItem
            ImportItem importItem = createImportItem(itemDto, importReceipt);
            importItemRepository.save(importItem);

            // Nếu trạng thái là CONFIRMED, cập nhật sản phẩm
            if (status == OrderStatus.CONFIRMED) {
                updateProductQuantityAndPrice(itemDto, importItem);
            }
        }

        return totalAmount;
    }

    public double calculateImportItemTotalAmount(ImportItemRequestDto itemDto) {
        double unitPrice = itemDto.getUnitPrice();
        int quantity = itemDto.getQuantity();
        double discount = itemDto.getDiscount() != null ? itemDto.getDiscount() : 0.0;
        double tax = itemDto.getTax() != null ? itemDto.getTax() : 0.0;

        // Tính tổng tiền trước thuế và chiết khấu
        double total = unitPrice * quantity;

        // Áp dụng chiết khấu
        total = total - (total * discount / 100);

        // Áp dụng thuế
        total = total + (total * tax / 100);

        return total;
    }

    private ImportItem createImportItem(ImportItemRequestDto itemDto, Import importReceipt) {
        ImportItem importItem = new ImportItem();
        importItem.setImportReceipt(importReceipt);

        Product product = productRepository.getProductById(itemDto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND));

        int smallestQuantity = itemDto.getQuantity() * itemDto.getConversionFactor();

        importItem.setProduct(product);
        importItem.setQuantity(itemDto.getQuantity());
        importItem.setUnit(itemDto.getUnit());
        importItem.setUnitPrice(itemDto.getUnitPrice());
        importItem.setDiscount(itemDto.getDiscount());
        importItem.setTax(itemDto.getTax());
        importItem.setBatchNumber(itemDto.getBatchNumber());
        importItem.setExpiryDate((itemDto.getExpiryDate() == null)
                ? LocalDate.now().plusYears(100).atStartOfDay(ZoneId.systemDefault()).toInstant()
                : itemDto.getExpiryDate());
        importItem.setTotalAmount(itemDto.getTotalAmount()); // Sử dụng totalAmount đã tính
        importItem.setConversionFactor(itemDto.getConversionFactor());
        importItem.setRemainingQuantity(smallestQuantity);

        return importItem;
    }

    private void updateProductQuantityAndPrice(ImportItemRequestDto itemDto, ImportItem importItem) {
        Product product = importItem.getProduct();
        Integer currentTotalQuantity = product.getTotalQuantity();
        if (currentTotalQuantity == null) {
            currentTotalQuantity = 0;
        }

        int smallestQuantity = itemDto.getQuantity() * itemDto.getConversionFactor();
        product.setTotalQuantity(currentTotalQuantity + smallestQuantity);
        productRepository.save(product);

        updateProductUnits(product, itemDto);
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

    public void saveInventoryHistory(ImportItem importItem, int totalChangeQuantity, String reason) {
        if (totalChangeQuantity == 0) return;

        InventoryHistory inventoryHistory = new InventoryHistory();
        inventoryHistory.setImportItem(importItem);
        inventoryHistory.setRecordDate(Instant.now());
        inventoryHistory.setRemainingQuantity(importItem.getRemainingQuantity());
        inventoryHistory.setChangeQuantity(totalChangeQuantity);
        inventoryHistory.setReason(reason);
        inventoryHistoryRepository.save(inventoryHistory);
    }

    public String uploadImage(final MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException(Message.EMPTY_FILE);
        }

        if (!FileUploadUtil.isAllowedExtension(file.getOriginalFilename(), FileUploadUtil.IMAGE_PATTERN)) {
            throw new BadRequestException(Message.INVALID_FILE);
        }

        if (file.getSize() > FileUploadUtil.MAX_FILE_SIZE) {
            throw new MaxUploadSizeExceededException(Message.INVALID_FILE_SIZE);
        }

        String customFileName = "import_" + System.currentTimeMillis();

        CloudinaryResponse cloudinaryResponse = cloudinaryService.upLoadFile(file, FileUploadUtil.getFileName(customFileName));

        return cloudinaryResponse.getUrl();
    }

    private ImportItemRequestDto convertItemToDto(ImportItem importItem) {
        ImportItemRequestDto itemDto = new ImportItemRequestDto();
        itemDto.setId(importItem.getId());
        itemDto.setProductId(importItem.getProduct().getId());
        itemDto.setQuantity(importItem.getQuantity());
        itemDto.setUnit(importItem.getUnit());
        itemDto.setUnitPrice(importItem.getUnitPrice());
        itemDto.setDiscount(importItem.getDiscount());
        itemDto.setTax(importItem.getTax());
        itemDto.setBatchNumber(importItem.getBatchNumber());
        itemDto.setExpiryDate((itemDto.getExpiryDate() == null)
                ? LocalDate.now().plusYears(100).atStartOfDay(ZoneId.systemDefault()).toInstant()
                : itemDto.getExpiryDate());
        itemDto.setTotalAmount(importItem.getTotalAmount());
        itemDto.setConversionFactor(importItem.getConversionFactor());
        return itemDto;
    }

    private ImportItemRequestDto mapImportItemToDto(ImportItem importItem) {
        ImportItemRequestDto itemDto = new ImportItemRequestDto();
        itemDto.setProductId(importItem.getProduct().getId());
        itemDto.setQuantity(importItem.getQuantity());
        itemDto.setUnit(importItem.getUnit());
        itemDto.setUnitPrice(importItem.getUnitPrice());
        itemDto.setDiscount(importItem.getDiscount());
        itemDto.setTax(importItem.getTax());
        itemDto.setBatchNumber(importItem.getBatchNumber());
        itemDto.setExpiryDate((itemDto.getExpiryDate() == null)
                ? LocalDate.now().plusYears(100).atStartOfDay(ZoneId.systemDefault()).toInstant()
                : itemDto.getExpiryDate());
        itemDto.setTotalAmount(importItem.getTotalAmount());
        itemDto.setConversionFactor(importItem.getConversionFactor());
        return itemDto;
    }

    @Override
    public ImportResponseDto getImportById(Long importId) {
        Import importReceipt = importRepository.findById(importId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.IMPORT_NOT_FOUND));

        // Chuyển đổi Import sang ImportDto và trả về
        return new ImportResponseDto(importReceipt);
    }

    @Override
    public void exportImportsToExcel(HttpServletResponse response, Instant fromInstant, Instant toInstant) throws IOException {
        // Fetch import data
        List<ImportViewListDto> imports = importRepository.getImportsByDateRange(fromInstant, toInstant);

        // Check if there is data to export
        if (imports.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy dữ liệu phiếu nhập trong khoảng thời gian đã chọn.");
        }

        // Create workbook and sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Danh sách phiếu nhập");

        // Header styling
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.DARK_BLUE.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerCellStyle.setBorderBottom(BorderStyle.THIN);
        headerCellStyle.setBorderTop(BorderStyle.THIN);
        headerCellStyle.setBorderLeft(BorderStyle.THIN);
        headerCellStyle.setBorderRight(BorderStyle.THIN);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Data styling
        CellStyle dataCellStyle = workbook.createCellStyle();
        dataCellStyle.setBorderBottom(BorderStyle.THIN);
        dataCellStyle.setBorderTop(BorderStyle.THIN);
        dataCellStyle.setBorderLeft(BorderStyle.THIN);
        dataCellStyle.setBorderRight(BorderStyle.THIN);
        dataCellStyle.setAlignment(HorizontalAlignment.CENTER);
        dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Date styling
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.cloneStyleFrom(dataCellStyle);
        dateStyle.setDataFormat(workbook.createDataFormat().getFormat("dd-MM-yyyy     HH:mm"));

        // Currency styling
        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.cloneStyleFrom(dataCellStyle);
        currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("₫ #,##0"));

        // Define column headers
        String[] headers = {"STT", "Mã phiếu", "Ngày tạo phiếu", "Người tạo", "Số lượng sản phẩm", "Nhà cung cấp", "Tổng tiền"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerCellStyle);
        }

        // Fill data rows
        int rowNum = 1;
        for (int i = 0; i < imports.size(); i++) {
            ImportViewListDto importDto = imports.get(i);
            Row row = sheet.createRow(rowNum++);

            // STT
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(i + 1);
            cell0.setCellStyle(dataCellStyle);

            // Mã phiếu
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(importDto.getInvoiceNumber());
            cell1.setCellStyle(dataCellStyle);

            // Ngày tạo phiếu
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(DateTimeFormatter.ofPattern("dd-MM-yyyy     HH:mm")
                    .withZone(ZoneOffset.ofHours(7))
                    .format(importDto.getImportDate()));
            cell2.setCellStyle(dateStyle);


            // Người tạo
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(importDto.getFullName());
            cell3.setCellStyle(dataCellStyle);

            // Số lượng sản phẩm
            Cell cell4 = row.createCell(4);
            cell4.setCellValue(importDto.getProductCount());
            cell4.setCellStyle(dataCellStyle);

            // Nhà cung cấp
            Cell cell5 = row.createCell(5);
            cell5.setCellValue(importDto.getSupplierName());
            cell5.setCellStyle(dataCellStyle);

            // Tổng tiền
            Cell cell6 = row.createCell(6);
            cell6.setCellValue(importDto.getTotalAmount());
            cell6.setCellStyle(currencyStyle);
        }

        // Auto-size columns to fit the content
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write workbook to response output stream
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.flush();
        outputStream.close();
    }

}
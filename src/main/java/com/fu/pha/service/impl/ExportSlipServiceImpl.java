package com.fu.pha.service.impl;

import com.fu.pha.convert.GenerateCode;
import com.fu.pha.dto.request.exportSlip.ExportSlipItemRequestDto;
import com.fu.pha.dto.request.exportSlip.ExportSlipRequestDto;
import com.fu.pha.dto.response.exportSlip.ExportSlipResponseDto;
import com.fu.pha.entity.*;
import com.fu.pha.enums.ERole;
import com.fu.pha.enums.ExportType;
import com.fu.pha.enums.OrderStatus;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.exception.UnauthorizedException;
import com.fu.pha.repository.*;
import com.fu.pha.service.ExportSlipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExportSlipServiceImpl implements ExportSlipService {

    @Autowired
    private ExportSlipRepository exportSlipRepository;

    @Autowired
    private ImportItemRepository importItemRepository;

    @Autowired
    private ExportSlipItemRepository exportSlipItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private GenerateCode generateCode;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    @Override
    public void createExport(ExportSlipRequestDto exportDto) {
        // Lấy người dùng hiện tại
        User currentUser = getCurrentUser();

        // Xác định trạng thái dựa trên vai trò
        OrderStatus status = determineExportStatus(currentUser);

        // Tạo phiếu xuất
        ExportSlip exportSlip = createExportSlip(exportDto, currentUser, status);

        // Lưu phiếu xuất
        exportSlipRepository.save(exportSlip);

        // Kiểm tra danh sách ExportSlipItem
        if (exportDto.getExportSlipItems() == null || exportDto.getExportSlipItems().isEmpty()) {
            throw new BadRequestException(Message.EXPORT_ITEMS_EMPTY);
        }

        // Lưu các ExportSlipItem và xử lý tồn kho nếu trạng thái là CONFIRMED
        double totalAmount = saveExportItems(exportDto, exportSlip, status);

        // So sánh tổng tiền giữa BE và FE nếu không phải là phiếu hủy
        if (exportDto.getTypeDelivery() != ExportType.DESTROY) {
            if (exportDto.getTotalAmount() != null) {
                double feTotalAmount = exportDto.getTotalAmount();

                if (Math.abs(totalAmount - feTotalAmount) > 0.01) { // Cho phép sai số nhỏ
                    throw new BadRequestException(Message.TOTAL_AMOUNT_NOT_MATCH);
                }
            } else {
                throw new BadRequestException(Message.TOTAL_AMOUNT_REQUIRED);
            }
        }

        // Cập nhật tổng tiền vào ExportSlip nếu không phải là phiếu hủy
        if (exportDto.getTypeDelivery() != ExportType.DESTROY) {
            exportSlip.setTotalAmount(totalAmount);
        } else {
            exportSlip.setTotalAmount(0.0); // Phiếu hủy không cần giá trị tiền tệ
        }

        exportSlipRepository.save(exportSlip);
    }

    @Transactional
    @Override
    public void updateExport(Long exportSlipId, ExportSlipRequestDto exportDto) {
        // Lấy người dùng hiện tại
        User currentUser = getCurrentUser();

        // Tìm phiếu xuất
        ExportSlip exportSlip = exportSlipRepository.findById(exportSlipId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.EXPORT_SLIP_NOT_FOUND));

        // Chỉ cho phép cập nhật khi trạng thái là PENDING hoặc REJECT
        if (exportSlip.getStatus() == OrderStatus.CONFIRMED) {
            throw new BadRequestException(Message.NOT_UPDATE_CONFIRMED);
        }

        // Kiểm tra quyền hạn
        if (!exportSlip.getUser().getId().equals(currentUser.getId())
                && !userHasRole(currentUser, ERole.ROLE_PRODUCT_OWNER)) {
            throw new UnauthorizedException(Message.REJECT_AUTHORIZATION);
        }

        // Nếu trạng thái hiện tại là REJECT và người cập nhật không phải là chủ cửa hàng, đặt lại trạng thái về PENDING
        if (exportSlip.getStatus() == OrderStatus.REJECT && !userHasRole(currentUser, ERole.ROLE_PRODUCT_OWNER)) {
            exportSlip.setStatus(OrderStatus.PENDING);
        }

        // Cập nhật thông tin cơ bản
        exportSlip.setExportDate(Instant.now());
        exportSlip.setTypeDelivery(exportDto.getTypeDelivery());
        exportSlip.setDiscount(exportDto.getDiscount());
        exportSlip.setNote(exportDto.getNote());
        exportSlip.setUser(currentUser);

        // Kiểm tra loại phiếu xuất kho
        if (exportDto.getTypeDelivery() == ExportType.RETURN_TO_SUPPLIER) {
            Supplier supplier = supplierRepository.findById(exportDto.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException(Message.SUPPLIER_NOT_FOUND));
            exportSlip.setSupplier(supplier);
        } else if (exportDto.getTypeDelivery() == ExportType.DESTROY) {
            exportSlip.setSupplier(null); // Với phiếu hủy, không có supplier
        } else {
            throw new BadRequestException(Message.INVALID_EXPORT_TYPE);
        }

        // Lấy danh sách ExportSlipItem hiện tại
        List<ExportSlipItem> existingItems = exportSlipItemRepository.findByExportSlipId(exportSlipId);

        // Sử dụng Map để tiện tra cứu các mục hiện tại theo ProductId và ImportItemId
        Map<String, ExportSlipItem> existingItemMap = existingItems.stream()
                .collect(Collectors.toMap(
                        item -> item.getProduct().getId() + "-" + item.getImportItem().getId(),
                        item -> item));

        double totalAmount = 0.0;

        // Xử lý các ExportSlipItem mới
        for (ExportSlipItemRequestDto itemDto : exportDto.getExportSlipItems()) {
            String key = itemDto.getProductId() + "-" + itemDto.getImportItemId();
            ExportSlipItem exportSlipItem = existingItemMap.get(key);

            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND));

            ImportItem importItem = importItemRepository.findById(itemDto.getImportItemId())
                    .orElseThrow(() -> new ResourceNotFoundException(Message.IMPORT_ITEM_NOT_FOUND));

            // Kiểm tra nhà cung cấp nếu là phiếu trả lại nhà cung cấp
            if (exportSlip.getTypeDelivery() == ExportType.RETURN_TO_SUPPLIER) {
                if (!importItem.getImportReceipt().getSupplier().equals(exportSlip.getSupplier())) {
                    throw new BadRequestException(Message.SUPPLIER_NOT_MATCH);
                }
            }

            int smallestQuantity = itemDto.getQuantity() * itemDto.getConversionFactor();

            // Tính tổng tiền của ExportSlipItem trên BE
            double itemTotalAmount = 0.0;
            if (exportSlip.getTypeDelivery() != ExportType.DESTROY) {
                itemTotalAmount = calculateExportItemTotalAmount(itemDto);
                // Cập nhật lại totalAmount
                totalAmount += itemTotalAmount;
                // Cập nhật itemDto với totalAmount đã tính toán
                itemDto.setTotalAmount(itemTotalAmount);
            }

            if (exportSlipItem != null) {
                // Nếu ExportSlipItem đã tồn tại, cập nhật thông tin
                int oldSmallestQuantity = exportSlipItem.getQuantity() * exportSlipItem.getConversionFactor();
                int quantityDifference = smallestQuantity - oldSmallestQuantity;

                if (exportSlip.getStatus() == OrderStatus.CONFIRMED) {
                    // Khôi phục tồn kho từ số lượng cũ
                    product.setTotalQuantity(product.getTotalQuantity() + oldSmallestQuantity);
                    importItem.setRemainingQuantity(importItem.getRemainingQuantity() + oldSmallestQuantity);

                    // Giảm tồn kho theo số lượng mới
                    product.setTotalQuantity(product.getTotalQuantity() - smallestQuantity);
                    importItem.setRemainingQuantity(importItem.getRemainingQuantity() - smallestQuantity);

                    // Kiểm tra tồn kho sau khi cập nhật
                    if (product.getTotalQuantity() < 0) {
                        throw new BadRequestException(Message.NOT_ENOUGH_STOCK);
                    }
                    if (importItem.getRemainingQuantity() < 0) {
                        throw new BadRequestException(Message.NOT_ENOUGH_STOCK_IN_BATCH);
                    }

                    productRepository.save(product);
                    importItemRepository.save(importItem);
                }

                // Cập nhật thông tin ExportSlipItem
                exportSlipItem.setQuantity(itemDto.getQuantity());
                exportSlipItem.setUnit(itemDto.getUnit());
                exportSlipItem.setBatch_number(itemDto.getBatchNumber());
                exportSlipItem.setConversionFactor(itemDto.getConversionFactor());
                exportSlipItem.setExpiryDate(itemDto.getExpiryDate());

                if (exportSlip.getTypeDelivery() != ExportType.DESTROY) {
                    exportSlipItem.setUnitPrice(itemDto.getUnitPrice());
                    exportSlipItem.setDiscount(itemDto.getDiscount());
                    exportSlipItem.setTotalAmount(itemTotalAmount);
                }

                exportSlipItemRepository.save(exportSlipItem);

                // Xóa khỏi Map để xử lý các mục không còn trong danh sách mới
                existingItemMap.remove(key);
            } else {
                // Nếu ExportSlipItem không tồn tại, tạo mới
                exportSlipItem = createExportSlipItem(itemDto, exportSlip);

                if (exportSlip.getStatus() == OrderStatus.CONFIRMED) {
                    // Giảm tồn kho theo số lượng mới
                    product.setTotalQuantity(product.getTotalQuantity() - smallestQuantity);
                    importItem.setRemainingQuantity(importItem.getRemainingQuantity() - smallestQuantity);

                    // Kiểm tra tồn kho sau khi cập nhật
                    if (product.getTotalQuantity() < 0) {
                        throw new BadRequestException(Message.NOT_ENOUGH_STOCK);
                    }
                    if (importItem.getRemainingQuantity() < 0) {
                        throw new BadRequestException(Message.NOT_ENOUGH_STOCK_IN_BATCH);
                    }

                    productRepository.save(product);
                    importItemRepository.save(importItem);
                }

                if (exportSlip.getTypeDelivery() != ExportType.DESTROY) {
                    // totalAmount đã được cập nhật ở trên
                }

                exportSlipItemRepository.save(exportSlipItem);
            }
        }

        // Xử lý các ExportSlipItem không còn trong danh sách mới
        for (ExportSlipItem remainingItem : existingItemMap.values()) {
            if (exportSlip.getStatus() == OrderStatus.CONFIRMED) {
                int smallestQuantity = remainingItem.getQuantity() * remainingItem.getConversionFactor();
                Product product = remainingItem.getProduct();
                ImportItem importItem = remainingItem.getImportItem();

                // Khôi phục tồn kho từ số lượng cũ
                product.setTotalQuantity(product.getTotalQuantity() + smallestQuantity);
                importItem.setRemainingQuantity(importItem.getRemainingQuantity() + smallestQuantity);

                productRepository.save(product);
                importItemRepository.save(importItem);
            }

            // Xóa ExportSlipItem
            exportSlipItemRepository.delete(remainingItem);
        }

        // Kiểm tra tổng tiền
        if (exportSlip.getTypeDelivery() != ExportType.DESTROY) {
            if (exportDto.getTotalAmount() != null) {
                double feTotalAmount = exportDto.getTotalAmount();
                if (Math.abs(totalAmount - feTotalAmount) > 0.01) { // Cho phép sai số nhỏ
                    throw new BadRequestException(Message.TOTAL_AMOUNT_NOT_MATCH);
                }
            } else {
                throw new BadRequestException(Message.TOTAL_AMOUNT_REQUIRED);
            }
        }

        // Cập nhật tổng tiền vào ExportSlip nếu không phải là phiếu hủy
        if (exportSlip.getTypeDelivery() != ExportType.DESTROY) {
            exportSlip.setTotalAmount(totalAmount);
        } else {
            exportSlip.setTotalAmount(0.0); // Phiếu hủy không cần giá trị tiền tệ
        }

        exportSlipRepository.save(exportSlip);
    }


    @Transactional
    @Override
    public void confirmExport(Long exportSlipId) {
        // Lấy người dùng hiện tại
        User currentUser = getCurrentUser();

        // Kiểm tra quyền hạn
        if (!userHasRole(currentUser, ERole.ROLE_PRODUCT_OWNER)) {
            throw new UnauthorizedException(Message.REJECT_AUTHORIZATION);
        }

        // Tìm phiếu xuất
        ExportSlip exportSlip = exportSlipRepository.findById(exportSlipId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.EXPORT_SLIP_NOT_FOUND));

        // Kiểm tra trạng thái
        if (exportSlip.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException(Message.NOT_PENDING_EXPORT);
        }

        // Cập nhật trạng thái
        exportSlip.setStatus(OrderStatus.CONFIRMED);
        exportSlipRepository.save(exportSlip);

        // Xử lý tồn kho
        for (ExportSlipItem exportSlipItem : exportSlip.getExportSlipItemList()) {
            processStockForConfirmedExport(exportSlipItem);
        }
    }

    @Transactional
    @Override
    public void rejectExport(Long exportSlipId, String reason) {
        // Lấy người dùng hiện tại
        User currentUser = getCurrentUser();

        // Kiểm tra quyền hạn
        if (!userHasRole(currentUser, ERole.ROLE_PRODUCT_OWNER)) {
            throw new UnauthorizedException(Message.REJECT_AUTHORIZATION);
        }

        // Kiểm tra lý do từ chối
        if (reason == null || reason.trim().isEmpty()) {
            throw new BadRequestException(Message.REASON_REQUIRED);
        }

        // Tìm phiếu xuất
        ExportSlip exportSlip = exportSlipRepository.findById(exportSlipId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.EXPORT_SLIP_NOT_FOUND));

        // Kiểm tra trạng thái
        if (exportSlip.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException(Message.NOT_REJECT);
        }

        // Cập nhật trạng thái và ghi chú
        exportSlip.setStatus(OrderStatus.REJECT);
        exportSlip.setNote(reason); // Ghi lý do từ chối vào trường note
        exportSlipRepository.save(exportSlip);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException(Message.NOT_LOGIN);
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(Message.USER_NOT_FOUND));
    }

    private OrderStatus determineExportStatus(User user) {
        if (userHasRole(user, ERole.ROLE_PRODUCT_OWNER)) {
            return OrderStatus.CONFIRMED;
        } else {
            return OrderStatus.PENDING;
        }
    }

    private boolean userHasRole(User user, ERole role) {
        return user.getRoles().stream().anyMatch(r -> r.getName().equals(role));
    }

    private ExportSlip createExportSlip(ExportSlipRequestDto exportDto, User user, OrderStatus status) {
        ExportSlip exportSlip = new ExportSlip();
        String lastInvoiceNumber = exportSlipRepository.getLastInvoiceNumber();
        exportSlip.setInvoiceNumber(lastInvoiceNumber == null ? "EX000001" : generateCode.generateNewProductCode(lastInvoiceNumber));
        exportSlip.setExportDate(Instant.now());
        exportSlip.setTypeDelivery(exportDto.getTypeDelivery());
        exportSlip.setDiscount(exportDto.getDiscount());
        exportSlip.setNote(exportDto.getNote());
        exportSlip.setUser(user);
        exportSlip.setStatus(status);

        // Kiểm tra loại phiếu xuất kho
        if (exportDto.getTypeDelivery() == ExportType.RETURN_TO_SUPPLIER) {
            Supplier supplier = supplierRepository.findById(exportDto.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException(Message.SUPPLIER_NOT_FOUND));
            exportSlip.setSupplier(supplier);
        } else if (exportDto.getTypeDelivery() == ExportType.DESTROY) {
            exportSlip.setSupplier(null); // Với phiếu hủy, không có supplier
        } else {
            throw new BadRequestException(Message.INVALID_EXPORT_TYPE);
        }

        return exportSlip;
    }

    private double saveExportItems(ExportSlipRequestDto exportDto, ExportSlip exportSlip, OrderStatus status) {
        double totalAmount = 0.0;

        for (ExportSlipItemRequestDto itemDto : exportDto.getExportSlipItems()) {
            // Tính tổng tiền của ExportSlipItem trên BE
            double itemTotalAmount = calculateExportItemTotalAmount(itemDto);

            // Cập nhật lại totalAmount
            totalAmount += itemTotalAmount;

            // Cập nhật itemDto với totalAmount đã tính toán
            itemDto.setTotalAmount(itemTotalAmount);

            // Tạo và lưu ExportSlipItem
            ExportSlipItem exportSlipItem = createExportSlipItem(itemDto, exportSlip);
            exportSlipItemRepository.save(exportSlipItem);

            // Nếu trạng thái là CONFIRMED, xử lý tồn kho
            if (status == OrderStatus.CONFIRMED) {
                processStockForConfirmedExport(exportSlipItem);
            }
        }

        return totalAmount;
    }

    private double calculateExportItemTotalAmount(ExportSlipItemRequestDto itemDto) {
        double unitPrice = itemDto.getUnitPrice();
        int quantity = itemDto.getQuantity();
        double discount = itemDto.getDiscount() != null ? itemDto.getDiscount() : 0.0;

        // Tính tổng tiền trước chiết khấu
        double total = unitPrice * quantity;

        // Áp dụng chiết khấu
        total = total - (total * discount / 100);

        return total;
    }




    private void processStockForConfirmedExport(ExportSlipItem exportSlipItem) {
        Product product = exportSlipItem.getProduct();
        Integer currentTotalQuantity = product.getTotalQuantity();
        int smallestQuantity = exportSlipItem.getQuantity() * exportSlipItem.getConversionFactor();

        // Kiểm tra tồn kho
        if (currentTotalQuantity == null || currentTotalQuantity < smallestQuantity) {
            throw new BadRequestException(Message.NOT_ENOUGH_STOCK);
        }

        // Cập nhật lại số lượng sản phẩm trong Product
        product.setTotalQuantity(currentTotalQuantity - smallestQuantity);
        productRepository.save(product);

        // Cập nhật số lượng remainingQuantity trong ImportItem
        ImportItem importItem = exportSlipItem.getImportItem();
        if (importItem.getRemainingQuantity() < smallestQuantity) {
            throw new BadRequestException(Message.NOT_ENOUGH_STOCK_IN_BATCH);
        }
        importItem.setRemainingQuantity(importItem.getRemainingQuantity() - smallestQuantity);
        importItemRepository.save(importItem);
    }

    private ExportSlipItem createExportSlipItem(ExportSlipItemRequestDto itemDto, ExportSlip exportSlip) {
        ExportSlipItem exportSlipItem = new ExportSlipItem();
        exportSlipItem.setExportSlip(exportSlip);

        Product product = productRepository.findById(itemDto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND));

        // Tìm ImportItem theo importItemId
        ImportItem importItem = importItemRepository.findById(itemDto.getImportItemId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.IMPORT_NOT_FOUND));

        // Kiểm tra nhà cung cấp nếu là phiếu trả lại nhà cung cấp
        if (exportSlip.getTypeDelivery() == ExportType.RETURN_TO_SUPPLIER) {
            if (!importItem.getImportReceipt().getSupplier().equals(exportSlip.getSupplier())) {
                throw new BadRequestException(Message.SUPPLIER_NOT_MATCH);
            }
        }

        // Gán các thông tin cho ExportSlipItem
        exportSlipItem.setProduct(product);
        exportSlipItem.setQuantity(itemDto.getQuantity());
        exportSlipItem.setImportItem(importItem);
        exportSlipItem.setUnit(itemDto.getUnit());
        exportSlipItem.setBatch_number(itemDto.getBatchNumber());
        exportSlipItem.setConversionFactor(itemDto.getConversionFactor());
        exportSlipItem.setExpiryDate(itemDto.getExpiryDate());

        // Nếu không phải là phiếu hủy, gán thông tin tài chính
        if (exportSlip.getTypeDelivery() != ExportType.DESTROY) {
            exportSlipItem.setUnitPrice(itemDto.getUnitPrice());
            exportSlipItem.setDiscount(itemDto.getDiscount());
            exportSlipItem.setTotalAmount(itemDto.getTotalAmount());
        }

        return exportSlipItem;
    }



    @Transactional
    @Override
    public void softDeleteExportSlip(Long exportSlipId) {
        // Tìm ExportSlip bằng ID
        ExportSlip exportSlip = exportSlipRepository.findById(exportSlipId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.EXPORT_SLIP_NOT_FOUND));

        // Đánh dấu xóa mềm
        exportSlip.setIsDeleted(true);
        exportSlipRepository.save(exportSlip);

        // Xóa mềm các ExportSlipItem liên quan
        List<ExportSlipItem> exportSlipItems = exportSlipItemRepository.findByExportSlipId(exportSlipId);
        for (ExportSlipItem exportSlipItem : exportSlipItems) {
            exportSlipItem.setIsDeleted(true);
            exportSlipItemRepository.save(exportSlipItem);
        }
    }

    // Lấy danh sách các phiếu xuất kho chưa bị xóa mềm
    @Override
    public ExportSlipResponseDto getActiveExportSlipById(Long exportSlipId) {
        // Lấy phiếu xuất kho từ repository
        ExportSlip exportSlip = exportSlipRepository.findById(exportSlipId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.EXPORT_SLIP_NOT_FOUND));

        // Kiểm tra xem phiếu xuất kho có bị xóa mềm không
        if (Boolean.TRUE.equals(exportSlip.getIsDeleted())) {
            throw new ResourceNotFoundException(Message.EXPORT_SLIP_NOT_FOUND);
        }

        // Chuyển đổi từ ExportSlip sang DTO và trả về DTO
        return new ExportSlipResponseDto(exportSlip);
    }

    @Override
    public ExportSlipResponseDto getExportById(Long exportSlipId) {
        // Tìm ExportSlip bằng ID
        ExportSlip exportSlip = exportSlipRepository.findById(exportSlipId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.EXPORT_SLIP_NOT_FOUND));

        // Chuyển đổi ExportSlip entity sang ExportSlipResponseDto
        ExportSlipResponseDto exportSlipResponseDto = new ExportSlipResponseDto(exportSlip);

        return exportSlipResponseDto;
    }

    @Override
    public Page<ExportSlipResponseDto> getAllExportSlipPaging(int page, int size, ExportType exportType, OrderStatus status, Instant fromDate, Instant toDate) {
        Pageable pageable = PageRequest.of(page, size);

        // Nếu không có fromDate và toDate
        if (fromDate == null && toDate == null) {
            return exportSlipRepository.getListExportSlipPagingWithoutDate(exportType, status, pageable);
        }
        // Nếu có fromDate và không có toDate
        else if (fromDate != null && toDate == null) {
            return exportSlipRepository.getListExportSlipPagingFromDate(exportType, status, fromDate, pageable);
        }
        // Nếu không có fromDate và có toDate
        else if (fromDate == null) {
            return exportSlipRepository.getListExportSlipPagingToDate(exportType, status, toDate, pageable);
        }
        // Nếu có cả fromDate và toDate
        else {
            return exportSlipRepository.getListExportSlipPaging(exportType, status, fromDate, toDate, pageable);
        }
    }

    public List<ExportSlipResponseDto> getAllActiveExportSlips() {
        // Lấy danh sách các phiếu xuất kho chưa bị xóa mềm từ repository
        List<ExportSlip> exportSlips = exportSlipRepository.findAllActive();

        // Chuyển đổi từ thực thể ExportSlip sang DTO và trả về danh sách DTO
        return exportSlips.stream()
                .map(ExportSlipResponseDto::new)  // Sử dụng constructor của DTO để chuyển đổi
                .collect(Collectors.toList());
    }



}

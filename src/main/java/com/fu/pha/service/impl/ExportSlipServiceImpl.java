package com.fu.pha.service.impl;

import com.fu.pha.convert.GenerateCode;
import com.fu.pha.dto.request.exportSlip.ExportSlipItemRequestDto;
import com.fu.pha.dto.request.exportSlip.ExportSlipRequestDto;
import com.fu.pha.dto.response.exportSlip.ExportSlipResponseDto;
import com.fu.pha.entity.*;
import com.fu.pha.enums.ExportType;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.*;
import com.fu.pha.service.ExportSlipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
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
        // Tìm user
        User user = userRepository.findById(exportDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.USER_NOT_FOUND));

        ExportSlip exportSlip = new ExportSlip();
        String lastInvoiceNumber = exportSlipRepository.getLastInvoiceNumber();
        exportSlip.setInvoiceNumber(lastInvoiceNumber == null ? "EX000001" : generateCode.generateNewProductCode(lastInvoiceNumber));

        exportSlip.setExportDate(Instant.now());
        exportSlip.setTypeDelivery(exportDto.getTypeDelivery());
        exportSlip.setDiscount(exportDto.getDiscount());
        exportSlip.setNote(exportDto.getNote());
        exportSlip.setUser(user);

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

        // Lưu Export entity trước
        exportSlipRepository.save(exportSlip);

        // Kiểm tra danh sách exportSlipItems không rỗng
        if (exportDto.getExportSlipItems() == null || exportDto.getExportSlipItems().isEmpty()) {
            throw new BadRequestException(Message.EXPORT_ITEMS_EMPTY);
        }

        double totalAmount = 0.0;

        for (ExportSlipItemRequestDto itemDto : exportDto.getExportSlipItems()) {
            ExportSlipItem exportSlipItem = new ExportSlipItem();
            exportSlipItem.setExportSlip(exportSlip);

            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND));

            // Kiểm tra số lượng tồn kho
            Integer currentTotalQuantity = product.getTotalQuantity();
            if (currentTotalQuantity == null || currentTotalQuantity < itemDto.getQuantity() * itemDto.getConversionFactor()) {
                throw new BadRequestException(Message.NOT_ENOUGH_STOCK);
            }

            // Tìm ImportItem theo importItemId
            ImportItem importItem = importItemRepository.findById(itemDto.getImportItemId())
                    .orElseThrow(() -> new ResourceNotFoundException(Message.IMPORT_NOT_FOUND));

            if (exportDto.getTypeDelivery() == ExportType.RETURN_TO_SUPPLIER) {
                if (!importItem.getImportReceipt().getSupplier().equals(exportSlip.getSupplier())) {
                    throw new BadRequestException(Message.SUPPLIER_NOT_MATCH);
                }
            }

            // Chuyển đổi quantity về đơn vị nhỏ nhất
            int smallestQuantity = itemDto.getQuantity() * itemDto.getConversionFactor();

            // Cập nhật số lượng remainingQuantity trong ImportItem
            if (importItem.getRemainingQuantity() < smallestQuantity) {
                throw new BadRequestException(Message.NOT_ENOUGH_STOCK_IN_BATCH);
            }
            importItem.setRemainingQuantity(importItem.getRemainingQuantity() - smallestQuantity);
            importItemRepository.save(importItem);

            // Cập nhật lại số lượng sản phẩm trong Product
            product.setTotalQuantity(currentTotalQuantity - smallestQuantity);
            productRepository.save(product);

            // Gán các thông tin cho ExportSlipItem
            exportSlipItem.setProduct(product);
            exportSlipItem.setQuantity(itemDto.getQuantity());
            exportSlipItem.setImportItem(importItem);  // Gán ImportItem cho ExportSlipItem
            exportSlipItem.setUnit(itemDto.getUnit());
            exportSlipItem.setBatch_number(itemDto.getBatchNumber());
            exportSlipItem.setExpiryDate(itemDto.getExpiryDate());

            // Nếu là phiếu hủy, không cần gán thông tin tài chính
            if (exportDto.getTypeDelivery() != ExportType.DESTROY) {
                exportSlipItem.setUnitPrice(itemDto.getUnitPrice());
                exportSlipItem.setDiscount(itemDto.getDiscount());
                exportSlipItem.setTotalAmount(itemDto.getTotalAmount());
                totalAmount += itemDto.getTotalAmount();
            }

            exportSlipItemRepository.save(exportSlipItem);
        }

        // Nếu không phải là phiếu hủy, kiểm tra sự khác biệt giữa totalAmount được tính và totalAmount trong DTO
        if (exportDto.getTypeDelivery() != ExportType.DESTROY &&
                (exportDto.getTotalAmount() != null && !exportDto.getTotalAmount().equals(totalAmount))) {
            throw new BadRequestException(Message.TOTAL_AMOUNT_NOT_MATCH);
        }

        // Cập nhật tổng số tiền vào ExportSlip nếu không phải là phiếu hủy
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
        // Tìm ExportSlip bằng ID
        ExportSlip exportSlip = exportSlipRepository.findById(exportSlipId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.EXPORT_SLIP_NOT_FOUND));

        // Tìm user
        User user = userRepository.findById(exportDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.USER_NOT_FOUND));

        // Cập nhật các thông tin cơ bản
        exportSlip.setExportDate(Instant.now());
        exportSlip.setTypeDelivery(exportDto.getTypeDelivery());
        exportSlip.setDiscount(exportDto.getDiscount());
        exportSlip.setNote(exportDto.getNote());
        exportSlip.setUser(user);

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

        // Xóa các ExportSlipItem hiện tại của ExportSlip
        List<ExportSlipItem> existingItems = exportSlipItemRepository.findByExportSlipId(exportSlipId);
        for (ExportSlipItem existingItem : existingItems) {
            Product product = existingItem.getProduct();

            // Chuyển đổi quantity về đơn vị nhỏ nhất (sử dụng conversionFactor từ DTO)
            ExportSlipItemRequestDto correspondingDto = exportDto.getExportSlipItems().stream()
                    .filter(dto -> dto.getProductId().equals(existingItem.getProduct().getId()))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException(Message.INVALID_CONVERSION_FACTOR));

            int smallestQuantity = existingItem.getQuantity() * correspondingDto.getConversionFactor();

            // Cộng lại số lượng vào kho
            product.setTotalQuantity(product.getTotalQuantity() + smallestQuantity);
            productRepository.save(product);

            // Cập nhật lại số lượng remaining trong ImportItem
            ImportItem importItem = existingItem.getImportItem();
            importItem.setRemainingQuantity(importItem.getRemainingQuantity() + smallestQuantity);
            importItemRepository.save(importItem);

            // Xóa ExportSlipItem hiện tại
            exportSlipItemRepository.delete(existingItem);
        }

        // Cập nhật các ExportSlipItem mới
        double totalAmount = 0.0;
        for (ExportSlipItemRequestDto itemDto : exportDto.getExportSlipItems()) {
            ExportSlipItem exportSlipItem = new ExportSlipItem();
            exportSlipItem.setExportSlip(exportSlip);

            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND));

            // Kiểm tra số lượng tồn kho
            Integer currentTotalQuantity = product.getTotalQuantity();
            int smallestQuantity = itemDto.getQuantity() * itemDto.getConversionFactor();
            if (currentTotalQuantity == null || currentTotalQuantity < smallestQuantity) {
                throw new BadRequestException(Message.NOT_ENOUGH_STOCK);
            }

            // Tìm ImportItem theo importItemId
            ImportItem importItem = importItemRepository.findById(itemDto.getImportItemId())
                    .orElseThrow(() -> new ResourceNotFoundException(Message.IMPORT_NOT_FOUND));

            if (exportDto.getTypeDelivery() == ExportType.RETURN_TO_SUPPLIER) {
                if (!importItem.getImportReceipt().getSupplier().equals(exportSlip.getSupplier())) {
                    throw new BadRequestException(Message.SUPPLIER_NOT_MATCH);
                }
            }

            // Cập nhật số lượng remainingQuantity trong ImportItem
            if (importItem.getRemainingQuantity() < smallestQuantity) {
                throw new BadRequestException(Message.NOT_ENOUGH_STOCK_IN_BATCH);
            }
            importItem.setRemainingQuantity(importItem.getRemainingQuantity() - smallestQuantity);
            importItemRepository.save(importItem);

            // Cập nhật lại số lượng sản phẩm trong Product
            product.setTotalQuantity(currentTotalQuantity - smallestQuantity);
            productRepository.save(product);

            // Gán các thông tin cho ExportSlipItem
            exportSlipItem.setProduct(product);
            exportSlipItem.setQuantity(itemDto.getQuantity());
            exportSlipItem.setImportItem(importItem);  // Gán ImportItem cho ExportSlipItem
            exportSlipItem.setUnit(itemDto.getUnit());
            exportSlipItem.setBatch_number(itemDto.getBatchNumber());
            exportSlipItem.setExpiryDate(itemDto.getExpiryDate());

            // Nếu là phiếu hủy, không cần gán thông tin tài chính
            if (exportDto.getTypeDelivery() != ExportType.DESTROY) {
                exportSlipItem.setUnitPrice(itemDto.getUnitPrice());
                exportSlipItem.setDiscount(itemDto.getDiscount());
                exportSlipItem.setTotalAmount(itemDto.getTotalAmount());
                totalAmount += itemDto.getTotalAmount();
            }

            exportSlipItemRepository.save(exportSlipItem);
        }

        // Nếu không phải là phiếu hủy, kiểm tra sự khác biệt giữa totalAmount được tính và totalAmount trong DTO
        if (exportDto.getTypeDelivery() != ExportType.DESTROY &&
                (exportDto.getTotalAmount() != null && !exportDto.getTotalAmount().equals(totalAmount))) {
            throw new BadRequestException(Message.TOTAL_AMOUNT_NOT_MATCH);
        }

        // Cập nhật tổng số tiền vào ExportSlip nếu không phải là phiếu hủy
        if (exportDto.getTypeDelivery() != ExportType.DESTROY) {
            exportSlip.setTotalAmount(totalAmount);
        } else {
            exportSlip.setTotalAmount(0.0); // Phiếu hủy không cần giá trị tiền tệ
        }

        exportSlipRepository.save(exportSlip);
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
    public Page<ExportSlipResponseDto> getAllExportSlipPaging(int page, int size, ExportType exportType, Instant fromDate, Instant toDate) {
        Pageable pageable = PageRequest.of(page, size);

        // Nếu không có fromDate và toDate
        if (fromDate == null && toDate == null) {
            return exportSlipRepository.getListExportSlipPagingWithoutDate(exportType, pageable);
        }
        //Nếu có fromDate và không có toDate
        else if (fromDate != null && toDate == null) {
            return exportSlipRepository.getListExportSlipPagingFromDate(exportType, fromDate, pageable);
        }
        //Nếu không có fromDate và có toDate
        else if (fromDate == null) {
            return exportSlipRepository.getListExportSlipPagingToDate(exportType, toDate, pageable);
        }
        //Nếu có cả fromDate và toDate
        else {
            return exportSlipRepository.getListExportSlipPaging(exportType, fromDate, toDate, pageable);
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

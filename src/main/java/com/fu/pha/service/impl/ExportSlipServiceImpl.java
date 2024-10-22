package com.fu.pha.service.impl;

import com.fu.pha.convert.GenerateCode;
import com.fu.pha.dto.request.exportSlip.ExportSlipDto;
import com.fu.pha.dto.request.exportSlip.ExportSlipItemRequestDto;
import com.fu.pha.entity.ExportSlip;
import com.fu.pha.entity.ExportSlipItem;
import com.fu.pha.entity.Supplier;
import com.fu.pha.entity.User;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.*;
import com.fu.pha.service.ExportSlipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class ExportSlipServiceImpl implements ExportSlipService {

    @Autowired
    private ExportSlipRepository exportSlipRepository;

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
    public void createExport(ExportSlipDto exportDto) {
        Optional<User> user = userRepository.findById(exportDto.getUserId());
        if (user.isEmpty()) {
            throw new ResourceNotFoundException(Message.USER_NOT_FOUND);
        }

        Supplier supplier = null;
        if ("return_to_supplier".equals(exportDto.getTypeDelivery())) {
            supplier = supplierRepository.findById(exportDto.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException(Message.SUPPLIER_NOT_FOUND));
        }

        // Tạo Export entity và gán các giá trị
        ExportSlip exportSlip = new ExportSlip();
        if (exportSlipRepository.getLastInvoiceNumber() == null) {
            exportSlip.setInvoiceNumber("PX000001");
        } else {
            exportSlip.setInvoiceNumber(generateCode.generateNewProductCode(exportSlipRepository.getLastInvoiceNumber()));
        }

        exportSlip.setExportDate(Instant.now());
        exportSlip.setTypeDelivery(exportDto.getTypeDelivery());
        exportSlip.setNote(exportDto.getNote());
        exportSlip.setUser(user.get());
        exportSlip.setSupplier(supplier); // Chỉ gán supplier nếu không null
        exportSlip.setDiscount(exportDto.getDiscount());

        // **Lưu Export entity trước**
        exportSlipRepository.save(exportSlip);

        // Tính tổng totalAmount từ các ExportSlipItem
        double totalAmount = 0.0;

        // Lưu các ExportSlipItem và tính tổng totalAmount
        for (ExportSlipItemRequestDto itemDto : exportDto.getExportSlipItems()) {
            ExportSlipItem exportSlipItem = new ExportSlipItem();
            exportSlipItem.setExportSlip(exportSlip); // Đã lưu Export trước, nên có thể gán nó vào ExportSlipItem
            exportSlipItem.setProduct(productRepository.getProductById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND)));
            exportSlipItem.setQuantity(itemDto.getQuantity());
            exportSlipItem.setUnit(itemDto.getUnit());
            exportSlipItem.setUnitPrice(itemDto.getUnitPrice());
            exportSlipItem.setDiscount(itemDto.getDiscount());
            exportSlipItem.setBatchNumber(itemDto.getBatchNumber());
            exportSlipItem.setExpirationDate(itemDto.getExpirationDate());
            exportSlipItem.setTotalAmount(itemDto.getTotalAmount());

            // Cộng dồn totalAmount
            totalAmount += itemDto.getTotalAmount();

            // Lưu ExportSlipItem vào repository
            exportSlipItemRepository.save(exportSlipItem);
        }

        // Cập nhật tổng số tiền vào ExportSlip
        exportSlip.setTotalAmount(totalAmount);
        exportSlipRepository.save(exportSlip);
    }

}

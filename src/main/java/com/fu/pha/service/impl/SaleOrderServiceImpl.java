package com.fu.pha.service.impl;

import com.fu.pha.convert.GenerateCode;
import com.fu.pha.dto.request.SaleOrder.SaleOrderItemRequestDto;
import com.fu.pha.dto.request.SaleOrder.SaleOrderRequestDto;
import com.fu.pha.dto.response.SaleOrder.SaleOrderResponseDto;
import com.fu.pha.entity.*;
import com.fu.pha.enums.OrderType;
import com.fu.pha.enums.PaymentMethod;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.*;
import com.fu.pha.service.SaleOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SaleOrderServiceImpl implements SaleOrderService {

    @Autowired
    private SaleOrderRepository saleOrderRepository;

    @Autowired
    private SaleOrderItemRepository saleOrderItemRepository;


    @Autowired
    private ImportItemRepository importItemRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private GenerateCode generateCode;


    @Override
    @Transactional
    public void createSaleOrder(SaleOrderRequestDto saleOrderRequestDto) {
        // 1. Kiểm tra và lấy các đối tượng liên quan: Customer, User, Doctor (nếu có)
        Customer customer = customerRepository.findById(saleOrderRequestDto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.CUSTOMER_NOT_FOUND));

        User user = userRepository.findById(saleOrderRequestDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.USER_NOT_FOUND));

        if (saleOrderRequestDto.getOrderType() == OrderType.PRESCRIPTION && saleOrderRequestDto.getDoctorId() == null) {
            throw new BadRequestException(Message.DOCTOR_REQUIRED);
        }

        Doctor doctor = null;
        if (saleOrderRequestDto.getDoctorId() != null && saleOrderRequestDto.getOrderType() == OrderType.PRESCRIPTION) {
            doctor = doctorRepository.findById(saleOrderRequestDto.getDoctorId())
                    .orElseThrow(() -> new ResourceNotFoundException(Message.DOCTOR_NOT_FOUND));
        }

        // 2. Khởi tạo và thiết lập các thuộc tính cho SaleOrder từ SaleOrderRequestDto
        SaleOrder saleOrder = new SaleOrder();
        String lastInvoiceNumber = saleOrderRepository.getLastInvoiceNumber();
        saleOrder.setInvoiceNumber(lastInvoiceNumber == null ? "XB000001" : generateCode.generateNewProductCode(lastInvoiceNumber));
        saleOrder.setSaleDate(saleOrderRequestDto.getSaleDate() != null ? saleOrderRequestDto.getSaleDate() : Instant.now());
        saleOrder.setOrderType(saleOrderRequestDto.getOrderType());
        saleOrder.setPaymentMethod(saleOrderRequestDto.getPaymentMethod());
        saleOrder.setDiscount(saleOrderRequestDto.getDiscount() != null ? saleOrderRequestDto.getDiscount() : 0.0);
        saleOrder.setCustomer(customer);
        saleOrder.setUser(user);
        saleOrder.setDoctor(doctor);
        saleOrder.setDiagnosis(saleOrderRequestDto.getDiagnosis());

        saleOrderRepository.save(saleOrder);

        double totalOrderAmount = 0.0;

        // 3. Xử lý từng SaleOrderItemRequestDto trong danh sách saleOrderItems
        for (SaleOrderItemRequestDto itemRequestDto : saleOrderRequestDto.getSaleOrderItems()) {
            Long productId = itemRequestDto.getProductId();
            Integer quantityToSell = itemRequestDto.getQuantity();
            Double unitPrice = itemRequestDto.getUnitPrice();

            // Lấy sản phẩm và tính toán số lượng nhỏ nhất cần bán
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND));

            int smallestQuantityToSell = quantityToSell * itemRequestDto.getConversionFactor();

            // Lấy danh sách lô hàng theo FIFO
            List<ImportItem> batches = importItemRepository.findByProductIdOrderByCreateDateAsc(productId);
            int remainingQuantity = smallestQuantityToSell;

            for (ImportItem batch : batches) {
                if (batch.getRemainingQuantity() > 0) {
                    int quantityFromBatch = Math.min(batch.getRemainingQuantity(), remainingQuantity);

                    // Cập nhật số lượng còn lại trong lô
                    batch.setRemainingQuantity(batch.getRemainingQuantity() - quantityFromBatch);
                    importItemRepository.save(batch);

                    remainingQuantity -= quantityFromBatch;
                    if (remainingQuantity <= 0) break;
                }
            }

            // Nếu không đủ tồn kho, ném ngoại lệ
            if (remainingQuantity > 0) {
                throw new BadRequestException(Message.OUT_OF_STOCK);
            }

            // Tạo một bản ghi SaleOrderItem duy nhất với tổng quantity
            SaleOrderItem saleOrderItem = new SaleOrderItem();
            saleOrderItem.setSaleOrder(saleOrder);
            saleOrderItem.setProduct(product);
            saleOrderItem.setQuantity(quantityToSell); // Lưu tổng số lượng từ DTO
            saleOrderItem.setUnitPrice(unitPrice);
            saleOrderItem.setDiscount(itemRequestDto.getDiscount());
            saleOrderItem.setTotalAmount((unitPrice * quantityToSell) -
                    (itemRequestDto.getDiscount() != null ? itemRequestDto.getDiscount() : 0.0));
            saleOrderItem.setDosage(itemRequestDto.getDosage());
            saleOrderItem.setUnit(itemRequestDto.getUnit());

            // Lưu SaleOrderItem vào cơ sở dữ liệu
            saleOrderItemRepository.save(saleOrderItem);

            // Cập nhật tổng tiền đơn hàng
            totalOrderAmount += saleOrderItem.getTotalAmount();

            // Cập nhật lại tổng tồn kho của sản phẩm
            int updatedTotalQuantity = product.getTotalQuantity() - smallestQuantityToSell;
            product.setTotalQuantity(updatedTotalQuantity);
            productRepository.save(product);
        }

        // 4. Cập nhật tổng số tiền của SaleOrder và lưu lại
        saleOrder.setTotalAmount(totalOrderAmount - saleOrder.getDiscount());
        saleOrderRepository.save(saleOrder);
    }


    @Override
    @Transactional
    public void updateSaleOrder(Long saleOrderId, SaleOrderRequestDto saleOrderRequestDto) {
        // 1. Lấy SaleOrder từ cơ sở dữ liệu và kiểm tra sự tồn tại
        SaleOrder saleOrder = saleOrderRepository.findById(saleOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.SALE_ORDER_NOT_FOUND));

        // 2. Kiểm tra và cập nhật các thông tin liên quan: Customer, User, Doctor (nếu có)
        Customer customer = customerRepository.findById(saleOrderRequestDto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.CUSTOMER_NOT_FOUND));

        User user = userRepository.findById(saleOrderRequestDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.USER_NOT_FOUND));

        if (saleOrderRequestDto.getOrderType() == OrderType.PRESCRIPTION && saleOrderRequestDto.getDoctorId() == null) {
            throw new BadRequestException(Message.DOCTOR_REQUIRED);
        }

        Doctor doctor = null;
        if (saleOrderRequestDto.getDoctorId() != null && saleOrderRequestDto.getOrderType() == OrderType.PRESCRIPTION) {
            doctor = doctorRepository.findById(saleOrderRequestDto.getDoctorId())
                    .orElseThrow(() -> new ResourceNotFoundException(Message.DOCTOR_NOT_FOUND));
        }

        // 3. Khôi phục lại tồn kho từ các SaleOrderItem hiện tại trước khi cập nhật
        List<SaleOrderItem> existingItems = saleOrderItemRepository.findBySaleOrderId(saleOrderId);
        for (SaleOrderItem existingItem : existingItems) {
            Product product = existingItem.getProduct();
            int quantityToRestore = existingItem.getQuantity(); // Số lượng cần khôi phục

            // Khôi phục lại tồn kho trong các lô hàng (FIFO) đúng theo thứ tự đã trừ
            List<ImportItem> batches = importItemRepository.findByProductIdOrderByCreateDateAsc(product.getId());
            for (ImportItem batch : batches) {
                if (batch.getRemainingQuantity() < batch.getQuantity()) {
                    int restoreAmount = Math.min(batch.getQuantity() - batch.getRemainingQuantity(), quantityToRestore);
                    batch.setRemainingQuantity(batch.getRemainingQuantity() + restoreAmount);
                    importItemRepository.save(batch);
                    quantityToRestore -= restoreAmount;
                    if (quantityToRestore <= 0) break;
                }
            }

            // Khôi phục lại tổng tồn kho của sản phẩm
            product.setTotalQuantity(product.getTotalQuantity() + existingItem.getQuantity());
            productRepository.save(product);
        }

        // 4. Xóa các SaleOrderItem cũ không còn trong danh sách mới
        Map<Long, SaleOrderItemRequestDto> requestItemMap = saleOrderRequestDto.getSaleOrderItems().stream()
                .collect(Collectors.toMap(SaleOrderItemRequestDto::getProductId, item -> item));

        for (SaleOrderItem existingItem : existingItems) {
            if (!requestItemMap.containsKey(existingItem.getProduct().getId())) {
                saleOrderItemRepository.delete(existingItem);
            }
        }

        double totalOrderAmount = 0.0;

        // 5. Cập nhật hoặc tạo mới các SaleOrderItem từ request
        for (SaleOrderItemRequestDto itemRequestDto : saleOrderRequestDto.getSaleOrderItems()) {
            Long productId = itemRequestDto.getProductId();
            Integer quantityToSell = itemRequestDto.getQuantity();
            Double unitPrice = itemRequestDto.getUnitPrice();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND));

            int smallestQuantityToSell = quantityToSell * itemRequestDto.getConversionFactor();

            List<ImportItem> batches = importItemRepository.findByProductIdOrderByCreateDateAsc(productId);
            int remainingQuantity = smallestQuantityToSell;

            for (ImportItem batch : batches) {
                if (batch.getRemainingQuantity() > 0) {
                    int quantityFromBatch = Math.min(batch.getRemainingQuantity(), remainingQuantity);
                    batch.setRemainingQuantity(batch.getRemainingQuantity() - quantityFromBatch);
                    importItemRepository.save(batch);
                    remainingQuantity -= quantityFromBatch;
                    if (remainingQuantity <= 0) break;
                }
            }

            if (remainingQuantity > 0) {
                throw new BadRequestException(Message.OUT_OF_STOCK);
            }

            SaleOrderItem saleOrderItem = existingItems.stream()
                    .filter(item -> item.getProduct().getId().equals(productId))
                    .findFirst()
                    .orElse(null);

            if (saleOrderItem != null) {
                // Cập nhật SaleOrderItem đã tồn tại
                saleOrderItem.setQuantity(quantityToSell); // Ghi đè số lượng mới
                saleOrderItem.setUnitPrice(unitPrice);
                saleOrderItem.setDiscount(itemRequestDto.getDiscount());
                saleOrderItem.setTotalAmount((unitPrice * quantityToSell) -
                        (itemRequestDto.getDiscount() != null ? itemRequestDto.getDiscount() : 0.0));
                saleOrderItem.setDosage(itemRequestDto.getDosage());
                saleOrderItem.setUnit(itemRequestDto.getUnit());
            } else {
                // Tạo mới SaleOrderItem
                saleOrderItem = new SaleOrderItem();
                saleOrderItem.setSaleOrder(saleOrder);
                saleOrderItem.setProduct(product);
                saleOrderItem.setQuantity(quantityToSell); // Sử dụng số lượng từ request
                saleOrderItem.setUnitPrice(unitPrice);
                saleOrderItem.setDiscount(itemRequestDto.getDiscount());
                saleOrderItem.setTotalAmount((unitPrice * quantityToSell) -
                        (itemRequestDto.getDiscount() != null ? itemRequestDto.getDiscount() : 0.0));
                saleOrderItem.setDosage(itemRequestDto.getDosage());
                saleOrderItem.setUnit(itemRequestDto.getUnit());
            }

            saleOrderItemRepository.save(saleOrderItem);

            totalOrderAmount += saleOrderItem.getTotalAmount();
            
            // Cập nhật lại tổng tồn kho của sản phẩm sau khi trừ số lượng bán ra
            int updatedTotalQuantity = product.getTotalQuantity() - smallestQuantityToSell;
            product.setTotalQuantity(updatedTotalQuantity);
            productRepository.save(product);

        }

        // 6. Cập nhật các thông tin của SaleOrder và lưu lại
        saleOrder.setSaleDate(saleOrderRequestDto.getSaleDate() != null ? saleOrderRequestDto.getSaleDate() : Instant.now());
        saleOrder.setOrderType(saleOrderRequestDto.getOrderType());
        saleOrder.setPaymentMethod(saleOrderRequestDto.getPaymentMethod());
        saleOrder.setDiscount(saleOrderRequestDto.getDiscount() != null ? saleOrderRequestDto.getDiscount() : 0.0);
        saleOrder.setCustomer(customer);
        saleOrder.setUser(user);
        saleOrder.setDoctor(doctor);
        saleOrder.setDiagnosis(saleOrderRequestDto.getDiagnosis());
        saleOrder.setTotalAmount(totalOrderAmount - saleOrder.getDiscount());

        saleOrderRepository.save(saleOrder);
    }



    public SaleOrderResponseDto getSaleOrderById(Long saleOrderId) {
        // 1. Truy vấn SaleOrder từ cơ sở dữ liệu
        SaleOrder saleOrder = saleOrderRepository.findById(saleOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.SALE_ORDER_NOT_FOUND));

        return new SaleOrderResponseDto(saleOrder);
    }

    @Override
    public Page<SaleOrderResponseDto> getAllSaleOrderPaging(int page, int size, OrderType orderType, PaymentMethod paymentMethod, String phoneNumber, Instant fromDate, Instant toDate) {
        Pageable pageable = PageRequest.of(page, size);

        //Nếu không có ngày bắt đầu và ngày kết thúc
        if (fromDate == null && toDate == null) {
            Instant startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant endOfDay = LocalDate.now().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();
            return saleOrderRepository.getListSaleOrderPagingWithoutDate(orderType, paymentMethod, phoneNumber, startOfDay, endOfDay, pageable);
        }
        //Nếu chỉ có ngày bắt đầu
        else if (fromDate != null && toDate == null) {
            return saleOrderRepository.getListSaleOrderPagingFromDate(orderType, paymentMethod, phoneNumber, fromDate, pageable);
        }
        //Nếu chỉ có ngày kết thúc
        else if (fromDate == null) {
            return saleOrderRepository.getListSaleOrderPagingToDate(orderType, paymentMethod, phoneNumber, toDate, pageable);
        }
        //Nếu có cả ngày bắt đầu và ngày kết thúc
        else {
            return saleOrderRepository.getListSaleOrderPaging(orderType, paymentMethod, phoneNumber, fromDate, toDate, pageable);
        }
    }
}


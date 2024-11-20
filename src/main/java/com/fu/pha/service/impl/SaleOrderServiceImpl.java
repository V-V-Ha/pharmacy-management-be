package com.fu.pha.service.impl;

import com.fu.pha.convert.GenerateCode;
import com.fu.pha.dto.request.SaleOrder.SaleOrderItemRequestDto;
import com.fu.pha.dto.request.SaleOrder.SaleOrderRequestDto;
import com.fu.pha.dto.response.SaleOrder.SaleOrderResponseDto;
import com.fu.pha.entity.*;
import com.fu.pha.enums.OrderType;
import com.fu.pha.enums.PaymentMethod;
import com.fu.pha.enums.PaymentStatus;
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
import java.util.*;
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

    @Autowired
    private SaleOrderItemBatchRepository saleOrderItemBatchRepository;

    @Autowired
    private InventoryHistoryRepository inventoryHistoryRepository;

    @Override
    @Transactional
    public int createSaleOrder(SaleOrderRequestDto saleOrderRequestDto) {
        // Kiểm tra và lấy các đối tượng liên quan
        Customer customer = customerRepository.findById(saleOrderRequestDto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.CUSTOMER_NOT_FOUND));

        User user = userRepository.findById(saleOrderRequestDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.USER_NOT_FOUND));

        Doctor doctor = null;
        if (saleOrderRequestDto.getOrderType() == OrderType.PRESCRIPTION && saleOrderRequestDto.getDoctorId() != null) {
            doctor = doctorRepository.findById(saleOrderRequestDto.getDoctorId())
                    .orElseThrow(() -> new ResourceNotFoundException(Message.DOCTOR_NOT_FOUND));
        }

        // Tạo SaleOrder
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

        // Thiết lập trạng thái thanh toán
        if (saleOrderRequestDto.getPaymentMethod() == PaymentMethod.CASH) {
            saleOrder.setPaymentStatus(PaymentStatus.PAID);
        } else {
            saleOrder.setPaymentStatus(PaymentStatus.UNPAID);
        }

        // Lưu SaleOrder
        saleOrderRepository.save(saleOrder);

        // Lưu các SaleOrderItem
        double totalOrderAmount = 0.0;
        List<SaleOrderItem> saleOrderItems = new ArrayList<>();
        for (SaleOrderItemRequestDto itemRequestDto : saleOrderRequestDto.getSaleOrderItems()) {
            Product product = productRepository.findById(itemRequestDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND));

            SaleOrderItem saleOrderItem = new SaleOrderItem();
            saleOrderItem.setSaleOrder(saleOrder);
            saleOrderItem.setProduct(product);
            saleOrderItem.setQuantity(itemRequestDto.getQuantity());
            saleOrderItem.setUnitPrice(itemRequestDto.getUnitPrice());
            saleOrderItem.setDiscount(itemRequestDto.getDiscount() != null ? itemRequestDto.getDiscount() : 0.0);
            saleOrderItem.setConversionFactor(itemRequestDto.getConversionFactor());
            saleOrderItem.setDosage(itemRequestDto.getDosage());
            saleOrderItem.setUnit(itemRequestDto.getUnit());
            saleOrderItem.setReturnedQuantity(0);

            double itemTotalAmount = calculateSaleOrderItemTotalAmount(itemRequestDto);
            saleOrderItem.setTotalAmount(itemTotalAmount);

            saleOrderItemRepository.save(saleOrderItem);
            saleOrderItems.add(saleOrderItem);

            totalOrderAmount += itemTotalAmount;
        }

        if (saleOrderRequestDto.getTotalAmount() != null) {
            double feTotalAmount = saleOrderRequestDto.getTotalAmount();
            if (Math.abs(totalOrderAmount - feTotalAmount) > 0.01) { // Cho phép sai số nhỏ
                throw new BadRequestException(Message.TOTAL_AMOUNT_NOT_MATCH);
            }
        } else {
            throw new BadRequestException(Message.TOTAL_AMOUNT_REQUIRED);
        }

        // Cập nhật tổng tiền cho SaleOrder
        saleOrder.setTotalAmount(totalOrderAmount - saleOrder.getDiscount());
        saleOrderRepository.save(saleOrder);

        // Nếu thanh toán là tiền mặt, thực hiện cập nhật kho
        if (saleOrder.getPaymentStatus() == PaymentStatus.PAID) {
            processOrderInventory(saleOrder);
        }

        return saleOrder.getId().intValue();
    }

    // Xử lý logic cập nhật kho, batch, product khi thanh toán hoàn tất
    private void processOrderInventory(SaleOrder saleOrder) {
        for (SaleOrderItem saleOrderItem : saleOrder.getSaleOrderItemList()) {
            Product product = saleOrderItem.getProduct();

            int smallestQuantityToSell = saleOrderItem.getQuantity() * saleOrderItem.getConversionFactor();
            List<ImportItem> batches = importItemRepository.findByProductIdOrderByCreateDateAsc(product.getId());
            int remainingQuantity = smallestQuantityToSell;

            
            for (ImportItem batch : batches) {
                if (batch.getRemainingQuantity() > 0) {
                    int quantityFromBatch = Math.min(batch.getRemainingQuantity(), remainingQuantity);
                    batch.setRemainingQuantity(batch.getRemainingQuantity() - quantityFromBatch);
                    importItemRepository.save(batch);

                    SaleOrderItemBatch saleOrderItemBatch = new SaleOrderItemBatch();
                    saleOrderItemBatch.setImportItem(batch);
                    saleOrderItemBatch.setQuantity(quantityFromBatch);
                    saleOrderItemBatch.setSaleOrderItem(saleOrderItem);
                    saleOrderItemBatchRepository.save(saleOrderItemBatch);

                    remainingQuantity -= quantityFromBatch;
                    if (remainingQuantity <= 0) break;
                }
            }

            if (remainingQuantity > 0) {
                throw new BadRequestException(Message.OUT_OF_STOCK);
            }

            // Cập nhật tồn kho của sản phẩm
            product.setTotalQuantity(product.getTotalQuantity() - smallestQuantityToSell);
            productRepository.save(product);
        }
    }


    @Override
    @Transactional
    public void completePayment(long orderId) {
        // Lấy SaleOrder từ DB
        SaleOrder saleOrder = saleOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.SALE_ORDER_NOT_FOUND));

        if (saleOrder.getPaymentStatus() == PaymentStatus.PAID) {
            throw new BadRequestException(Message.ORDER_ALREADY_PAID);
        }

        // Cập nhật trạng thái thành PAID
        saleOrder.setPaymentStatus(PaymentStatus.PAID);
        saleOrderRepository.save(saleOrder);

        // Gọi hàm xử lý cập nhật kho
        processOrderInventory(saleOrder);
    }


    private double calculateSaleOrderItemTotalAmount(SaleOrderItemRequestDto itemRequestDto) {
        double unitPrice = itemRequestDto.getUnitPrice();
        int quantity = itemRequestDto.getQuantity();
        double discount = itemRequestDto.getDiscount() != null ? itemRequestDto.getDiscount() : 0.0;

        // Tính tổng tiền trước chiết khấu
        double total = unitPrice * quantity;

        // Áp dụng chiết khấu
        total = total - (total * discount / 100);

        return total;
    }


    @Override
    @Transactional
    public void updateSaleOrder(Long saleOrderId, SaleOrderRequestDto saleOrderRequestDto) {
        // 1. Lấy SaleOrder từ cơ sở dữ liệu và kiểm tra sự tồn tại
        SaleOrder saleOrder = saleOrderRepository.findById(saleOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.SALE_ORDER_NOT_FOUND));

        // 2. Không cho phép cập nhật nếu trạng thái là PAID
        if (saleOrder.getPaymentStatus() == PaymentStatus.PAID) {
            throw new BadRequestException(Message.CANNOT_UPDATE_PAID_ORDER);
        }

        // 3. Cập nhật các thông tin cơ bản
        Customer customer = customerRepository.findById(saleOrderRequestDto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.CUSTOMER_NOT_FOUND));

        User user = userRepository.findById(saleOrderRequestDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.USER_NOT_FOUND));

        Doctor doctor = null;
        if (saleOrderRequestDto.getDoctorId() != null && saleOrderRequestDto.getOrderType() == OrderType.PRESCRIPTION) {
            doctor = doctorRepository.findById(saleOrderRequestDto.getDoctorId())
                    .orElseThrow(() -> new ResourceNotFoundException(Message.DOCTOR_NOT_FOUND));
        }

        // Cập nhật thông tin cơ bản của SaleOrder
        saleOrder.setCustomer(customer);
        saleOrder.setUser(user);
        saleOrder.setDoctor(doctor);
        saleOrder.setOrderType(saleOrderRequestDto.getOrderType());
        saleOrder.setSaleDate(saleOrderRequestDto.getSaleDate() != null ? saleOrderRequestDto.getSaleDate() : Instant.now());
        saleOrder.setPaymentMethod(saleOrderRequestDto.getPaymentMethod());
        saleOrder.setDiscount(saleOrderRequestDto.getDiscount() != null ? saleOrderRequestDto.getDiscount() : 0.0);
        saleOrder.setDiagnosis(saleOrderRequestDto.getDiagnosis());

        // 4. Cập nhật hoặc thêm mới các SaleOrderItem
        List<SaleOrderItem> existingItems = saleOrderItemRepository.findBySaleOrderId(saleOrderId);
        Map<Long, SaleOrderItem> existingItemsMap = existingItems.stream()
                .collect(Collectors.toMap(item -> item.getProduct().getId(), item -> item));

        double totalOrderAmount = 0.0;
        for (SaleOrderItemRequestDto itemRequestDto : saleOrderRequestDto.getSaleOrderItems()) {
            Long productId = itemRequestDto.getProductId();
            Integer quantity = itemRequestDto.getQuantity();
            Double unitPrice = itemRequestDto.getUnitPrice();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND));

            SaleOrderItem saleOrderItem = existingItemsMap.get(productId);
            if (saleOrderItem != null) {
                // Cập nhật thông tin SaleOrderItem hiện có
                saleOrderItem.setQuantity(quantity);
                saleOrderItem.setUnitPrice(unitPrice);
                saleOrderItem.setDiscount(itemRequestDto.getDiscount() != null ? itemRequestDto.getDiscount() : 0.0);
                saleOrderItem.setConversionFactor(itemRequestDto.getConversionFactor());
                saleOrderItem.setDosage(itemRequestDto.getDosage());
                saleOrderItem.setUnit(itemRequestDto.getUnit());
                saleOrderItem.setTotalAmount(calculateSaleOrderItemTotalAmount(itemRequestDto));

                saleOrderItemRepository.save(saleOrderItem);
                existingItemsMap.remove(productId);
            } else {
                // Tạo mới SaleOrderItem
                SaleOrderItem saleOrderItemNew = new SaleOrderItem();
                saleOrderItemNew.setSaleOrder(saleOrder);
                saleOrderItemNew.setProduct(product);
                saleOrderItemNew.setQuantity(quantity);
                saleOrderItemNew.setUnitPrice(unitPrice);
                saleOrderItemNew.setDiscount(itemRequestDto.getDiscount() != null ? itemRequestDto.getDiscount() : 0.0);
                saleOrderItemNew.setConversionFactor(itemRequestDto.getConversionFactor());
                saleOrderItemNew.setDosage(itemRequestDto.getDosage());
                saleOrderItemNew.setUnit(itemRequestDto.getUnit());
                saleOrderItemNew.setTotalAmount(calculateSaleOrderItemTotalAmount(itemRequestDto));

                saleOrderItemRepository.save(saleOrderItemNew);
            }

            totalOrderAmount += calculateSaleOrderItemTotalAmount(itemRequestDto);
        }

        // Xóa các SaleOrderItem không còn trong yêu cầu
        if (!existingItemsMap.isEmpty()) {
            saleOrderItemRepository.deleteAll(existingItemsMap.values());
        }

        // Cập nhật tổng tiền
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
    public Page<SaleOrderResponseDto> getAllSaleOrderPaging(int page, int size, OrderType orderType, PaymentMethod paymentMethod, String invoiceNumber, Instant fromDate, Instant toDate) {
        Pageable pageable = PageRequest.of(page, size);

        Page<SaleOrderResponseDto> saleOrderResponseDto;

        //Nếu không có ngày bắt đầu và ngày kết thúc
        if (fromDate == null && toDate == null) {
            Instant startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant endOfDay = LocalDate.now().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();
            saleOrderResponseDto = saleOrderRepository.getListSaleOrderPagingWithoutDate(orderType, paymentMethod, invoiceNumber, startOfDay, endOfDay, pageable);
        }
        //Nếu chỉ có ngày bắt đầu
        else if (fromDate != null && toDate == null) {
            saleOrderResponseDto = saleOrderRepository.getListSaleOrderPagingFromDate(orderType, paymentMethod, invoiceNumber, fromDate, pageable);
        }
        //Nếu chỉ có ngày kết thúc
        else if (fromDate == null) {
            saleOrderResponseDto = saleOrderRepository.getListSaleOrderPagingToDate(orderType, paymentMethod, invoiceNumber, toDate, pageable);
        }
        //Nếu có cả ngày bắt đầu và ngày kết thúc
        else {
            saleOrderResponseDto = saleOrderRepository.getListSaleOrderPaging(orderType, paymentMethod, invoiceNumber, fromDate, toDate, pageable);
        }

        if (saleOrderResponseDto.isEmpty()) {
            throw new ResourceNotFoundException(Message.SALE_ORDER_NOT_FOUND);
        }
        return saleOrderResponseDto;
    }
}


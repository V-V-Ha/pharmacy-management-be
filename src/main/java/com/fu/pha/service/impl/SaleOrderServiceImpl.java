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

    @Override
    @Transactional
    public int createSaleOrder(SaleOrderRequestDto saleOrderRequestDto) {
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

            int conversionFactor = itemRequestDto.getConversionFactor();
            int smallestQuantityToSell = quantityToSell * conversionFactor;

            // Lấy danh sách lô hàng theo FIFO
            List<ImportItem> batches = importItemRepository.findByProductIdOrderByCreateDateAsc(productId);
            int remainingQuantity = smallestQuantityToSell;

            // Danh sách để lưu các SaleOrderItemBatch
            List<SaleOrderItemBatch> saleOrderItemBatches = new ArrayList<>();

            for (ImportItem batch : batches) {
                if (batch.getRemainingQuantity() > 0) {
                    int quantityFromBatch = Math.min(batch.getRemainingQuantity(), remainingQuantity);

                    // Cập nhật số lượng còn lại trong lô
                    batch.setRemainingQuantity(batch.getRemainingQuantity() - quantityFromBatch);
                    importItemRepository.save(batch);

                    // Tạo và lưu SaleOrderItemBatch
                    SaleOrderItemBatch saleOrderItemBatch = new SaleOrderItemBatch();
                    saleOrderItemBatch.setImportItem(batch);
                    saleOrderItemBatch.setQuantity(quantityFromBatch);
                    saleOrderItemBatches.add(saleOrderItemBatch);

                    remainingQuantity -= quantityFromBatch;
                    if (remainingQuantity <= 0) break;
                }
            }

            // Nếu không đủ tồn kho, ném ngoại lệ
            if (remainingQuantity > 0) {
                throw new BadRequestException(Message.OUT_OF_STOCK);
            }

            // Tạo một bản ghi SaleOrderItem với tổng quantity
            SaleOrderItem saleOrderItem = new SaleOrderItem();
            saleOrderItem.setSaleOrder(saleOrder);
            saleOrderItem.setProduct(product);
            saleOrderItem.setQuantity(quantityToSell); // Lưu tổng số lượng từ DTO
            saleOrderItem.setUnitPrice(unitPrice);
            saleOrderItem.setDiscount(itemRequestDto.getDiscount());
            saleOrderItem.setTotalAmount((unitPrice * quantityToSell) -
                    (itemRequestDto.getDiscount() != null ? itemRequestDto.getDiscount() : 0.0));
            saleOrderItem.setDosage(itemRequestDto.getDosage());
            saleOrderItem.setConversionFactor(conversionFactor);
            saleOrderItem.setUnit(itemRequestDto.getUnit());
            saleOrderItem.setReturnedQuantity(0);

            // Lưu SaleOrderItem vào cơ sở dữ liệu
            saleOrderItemRepository.save(saleOrderItem);

            // Lưu các SaleOrderItemBatch với liên kết đến SaleOrderItem vừa tạo
            for (SaleOrderItemBatch saleOrderItemBatch : saleOrderItemBatches) {
                saleOrderItemBatch.setSaleOrderItem(saleOrderItem);
                saleOrderItemBatchRepository.save(saleOrderItemBatch);
            }

            double itemTotalAmount = calculateSaleOrderItemTotalAmount(itemRequestDto);

            // Cập nhật tổng tiền đơn hàng
            totalOrderAmount += itemTotalAmount;

            // Cập nhật lại tổng tồn kho của sản phẩm
            int updatedTotalQuantity = product.getTotalQuantity() - smallestQuantityToSell;
            product.setTotalQuantity(updatedTotalQuantity);
            productRepository.save(product);
        }

        if (saleOrderRequestDto.getTotalAmount() != null) {
            double feTotalAmount = saleOrderRequestDto.getTotalAmount();
            if (Math.abs(totalOrderAmount - feTotalAmount) > 0.01) { // Cho phép sai số nhỏ
                throw new BadRequestException(Message.TOTAL_AMOUNT_NOT_MATCH);
            }
        } else {
            throw new BadRequestException(Message.TOTAL_AMOUNT_REQUIRED);
        }

        // 4. Cập nhật tổng số tiền của SaleOrder và lưu lại
        saleOrder.setTotalAmount(totalOrderAmount - saleOrder.getDiscount());
        saleOrderRepository.save(saleOrder);
        return saleOrder.getId().intValue();
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

        // 3. Lấy các SaleOrderItem hiện tại từ cơ sở dữ liệu
        List<SaleOrderItem> existingItems = saleOrderItemRepository.findBySaleOrderId(saleOrderId);
        Map<Long, SaleOrderItem> existingItemsMap = existingItems.stream()
                .collect(Collectors.toMap(item -> item.getProduct().getId(), item -> item));

        // Danh sách các sản phẩm trong yêu cầu
        List<SaleOrderItemRequestDto> requestedItems = saleOrderRequestDto.getSaleOrderItems();

        double totalOrderAmount = 0.0;

        // 4. Cập nhật hoặc thêm mới các SaleOrderItem từ request
        for (SaleOrderItemRequestDto itemRequestDto : requestedItems) {
            Long productId = itemRequestDto.getProductId();
            Integer quantityToSell = itemRequestDto.getQuantity();
            Double unitPrice = itemRequestDto.getUnitPrice();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND));

            int conversionFactor = itemRequestDto.getConversionFactor();
            int smallestQuantityToSell = quantityToSell * conversionFactor;

            SaleOrderItem saleOrderItem = existingItemsMap.get(productId);
            if (saleOrderItem != null) {
                // Cập nhật thông tin nếu sản phẩm đã tồn tại trong SaleOrder
                // Tính toán sự khác biệt giữa số lượng mới và số lượng cũ
                int oldSmallestQuantity = saleOrderItem.getQuantity() * saleOrderItem.getConversionFactor();
                int difference = smallestQuantityToSell - oldSmallestQuantity;

                if (difference > 0) {
                    // Cần tăng số lượng đã phân bổ
                    allocateAdditionalQuantity(saleOrderItem, difference, product);
                } else if (difference < 0) {
                    // Cần giảm số lượng đã phân bổ
                    deallocateQuantity(saleOrderItem, -difference, product);
                }
                // Nếu difference == 0, không cần thay đổi gì về phân bổ

                // Cập nhật các thuộc tính của SaleOrderItem
                saleOrderItem.setQuantity(quantityToSell);
                saleOrderItem.setUnitPrice(unitPrice);
                saleOrderItem.setDiscount(itemRequestDto.getDiscount());
                saleOrderItem.setTotalAmount((unitPrice * quantityToSell) -
                        (itemRequestDto.getDiscount() != null ? itemRequestDto.getDiscount() : 0.0));
                saleOrderItem.setDosage(itemRequestDto.getDosage());
                saleOrderItem.setUnit(itemRequestDto.getUnit());
                saleOrderItem.setConversionFactor(conversionFactor);
                // Không thay đổi returnedQuantity

                saleOrderItemRepository.save(saleOrderItem);

                double itemTotalAmount = calculateSaleOrderItemTotalAmount(itemRequestDto);

                // Cập nhật tổng tiền đơn hàng
                totalOrderAmount += itemTotalAmount;

                // Loại bỏ khỏi map để xác định các item cần xóa sau này
                existingItemsMap.remove(productId);
            } else {
                // Nếu không tồn tại, tạo mới SaleOrderItem
                SaleOrderItem saleOrderItemNew = new SaleOrderItem();
                saleOrderItemNew.setSaleOrder(saleOrder);
                saleOrderItemNew.setProduct(product);
                saleOrderItemNew.setQuantity(quantityToSell);
                saleOrderItemNew.setUnitPrice(unitPrice);
                saleOrderItemNew.setDiscount(itemRequestDto.getDiscount());
                saleOrderItemNew.setTotalAmount((unitPrice * quantityToSell) -
                        (itemRequestDto.getDiscount() != null ? itemRequestDto.getDiscount() : 0.0));
                saleOrderItemNew.setDosage(itemRequestDto.getDosage());
                saleOrderItemNew.setUnit(itemRequestDto.getUnit());
                saleOrderItemNew.setConversionFactor(conversionFactor);
                saleOrderItemNew.setReturnedQuantity(0);

                saleOrderItemRepository.save(saleOrderItemNew);

                // Phân bổ số lượng từ các batches hiện có
                allocateAdditionalQuantity(saleOrderItemNew, smallestQuantityToSell, product);

                double itemTotalAmount = calculateSaleOrderItemTotalAmount(itemRequestDto);

                // Cập nhật tổng tiền đơn hàng
                totalOrderAmount += itemTotalAmount;
            }
        }

        // 5. Xóa các SaleOrderItem không còn trong yêu cầu
        if (!existingItemsMap.isEmpty()) {
            for (SaleOrderItem saleOrderItemToDelete : existingItemsMap.values()) {
                // Khôi phục lại tồn kho từ các batch hiện tại
                List<SaleOrderItemBatch> saleOrderItemBatches = saleOrderItemBatchRepository.findBySaleOrderItemId(saleOrderItemToDelete.getId());
                for (SaleOrderItemBatch saleOrderItemBatch : saleOrderItemBatches) {
                    ImportItem batch = saleOrderItemBatch.getImportItem();
                    batch.setRemainingQuantity(batch.getRemainingQuantity() + saleOrderItemBatch.getQuantity());
                    importItemRepository.save(batch);
                }

                // Cập nhật tổng tồn kho sản phẩm
                Product product = saleOrderItemToDelete.getProduct();
                int smallestQuantity = saleOrderItemToDelete.getQuantity() * saleOrderItemToDelete.getConversionFactor();
                product.setTotalQuantity(product.getTotalQuantity() + smallestQuantity);
                productRepository.save(product);

                // Xóa các SaleOrderItemBatch
                saleOrderItemBatchRepository.deleteAll(saleOrderItemBatches);

                // Xóa SaleOrderItem
                saleOrderItemRepository.delete(saleOrderItemToDelete);
            }
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

        // So sánh tổng tiền giữa BE và FE
        if (saleOrderRequestDto.getTotalAmount() != null) {
            double feTotalAmount = saleOrderRequestDto.getTotalAmount();
            if (Math.abs(totalOrderAmount - feTotalAmount) > 0.01) { // Cho phép sai số nhỏ
                throw new BadRequestException(Message.TOTAL_AMOUNT_NOT_MATCH);
            }
        } else {
            throw new BadRequestException(Message.TOTAL_AMOUNT_REQUIRED);
        }

        saleOrder.setTotalAmount(totalOrderAmount - saleOrderRequestDto.getDiscount());

        saleOrderRepository.save(saleOrder);
    }

    /**
     * Phương thức để phân bổ thêm số lượng cần bán vào các batches hiện có.
     */
    private void allocateAdditionalQuantity(SaleOrderItem saleOrderItem, int additionalQuantity, Product product) {
        List<ImportItem> batches = importItemRepository.findByProductIdOrderByCreateDateAsc(product.getId());
        List<SaleOrderItemBatch> saleOrderItemBatchesToAdd = new ArrayList<>();
        int totalAllocated = 0; // Biến để theo dõi tổng số lượng đã phân bổ

        for (ImportItem batch : batches) {
            if (batch.getRemainingQuantity() > 0 && additionalQuantity > 0) {
                int quantityFromBatch = Math.min(batch.getRemainingQuantity(), additionalQuantity);

                // Cập nhật số lượng còn lại trong lô
                batch.setRemainingQuantity(batch.getRemainingQuantity() - quantityFromBatch);
                importItemRepository.save(batch);

                // Kiểm tra xem SaleOrderItemBatch đã tồn tại cho cặp importId và saleOrderItemId chưa
                Optional<SaleOrderItemBatch> existingBatchOpt = saleOrderItemBatchRepository.findByImportItemIdAndSaleOrderItemId(batch.getId(), saleOrderItem.getId());

                if (existingBatchOpt.isPresent()) {
                    // Nếu tồn tại, cập nhật số lượng
                    SaleOrderItemBatch existingBatch = existingBatchOpt.get();
                    existingBatch.setQuantity(existingBatch.getQuantity() + quantityFromBatch);
                    saleOrderItemBatchRepository.save(existingBatch);
                } else {
                    // Nếu không tồn tại, tạo mới
                    SaleOrderItemBatch saleOrderItemBatch = new SaleOrderItemBatch();
                    saleOrderItemBatch.setImportItem(batch);
                    saleOrderItemBatch.setQuantity(quantityFromBatch);
                    saleOrderItemBatch.setSaleOrderItem(saleOrderItem);
                    saleOrderItemBatchesToAdd.add(saleOrderItemBatch);
                }

                totalAllocated += quantityFromBatch;
                additionalQuantity -= quantityFromBatch;
            }
        }

        if (additionalQuantity > 0) {
            throw new BadRequestException(Message.OUT_OF_STOCK);
        }

        // Lưu các SaleOrderItemBatch mới nếu có
        if (!saleOrderItemBatchesToAdd.isEmpty()) {
            saleOrderItemBatchRepository.saveAll(saleOrderItemBatchesToAdd);
        }

        // Cập nhật tổng tồn kho của sản phẩm
        product.setTotalQuantity(product.getTotalQuantity() - totalAllocated);
        productRepository.save(product);
    }

    /**
     * Phương thức để giảm số lượng đã phân bổ từ các SaleOrderItemBatch hiện có.
     */
    private void deallocateQuantity(SaleOrderItem saleOrderItem, int deallocateQuantity, Product product) {
        List<SaleOrderItemBatch> existingBatches = saleOrderItemBatchRepository.findBySaleOrderItemId(saleOrderItem.getId())
                .stream()
                .sorted(Comparator.comparing(SaleOrderItemBatch::getId).reversed()) // Giảm dần theo ID để giảm từ các batches cuối
                .collect(Collectors.toList());
        int totalDeallocated = 0; // Biến để theo dõi tổng số lượng đã giải phóng

        for (SaleOrderItemBatch batch : existingBatches) {
            if (deallocateQuantity <= 0) break;

            int batchQuantity = batch.getQuantity();
            if (batchQuantity <= deallocateQuantity) {
                // Khôi phục lại tồn kho
                ImportItem importItem = batch.getImportItem();
                importItem.setRemainingQuantity(importItem.getRemainingQuantity() + batchQuantity);
                importItemRepository.save(importItem);

                // Xóa SaleOrderItemBatch
                saleOrderItemBatchRepository.delete(batch);

                totalDeallocated += batchQuantity;
                deallocateQuantity -= batchQuantity;
            } else {
                // Giảm số lượng trong batch
                batch.setQuantity(batch.getQuantity() - deallocateQuantity);
                saleOrderItemBatchRepository.save(batch);

                // Khôi phục lại tồn kho
                ImportItem importItem = batch.getImportItem();
                importItem.setRemainingQuantity(importItem.getRemainingQuantity() + deallocateQuantity);
                importItemRepository.save(importItem);

                totalDeallocated += deallocateQuantity;
                deallocateQuantity = 0;
            }
        }

        // Cập nhật tổng tồn kho của sản phẩm
        product.setTotalQuantity(product.getTotalQuantity() + totalDeallocated);
        productRepository.save(product);
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


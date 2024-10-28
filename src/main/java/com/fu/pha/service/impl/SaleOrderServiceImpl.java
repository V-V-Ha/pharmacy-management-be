package com.fu.pha.service.impl;

import com.fu.pha.dto.request.SaleOrder.SaleOrderItemRequestDto;
import com.fu.pha.dto.request.SaleOrder.SaleOrderRequestDto;
import com.fu.pha.entity.*;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.*;
import com.fu.pha.service.SaleOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class SaleOrderServiceImpl implements SaleOrderService {

    @Autowired
    private SaleOrderRepository saleOrderRepository;

    @Autowired
    private SaleOrderItemRepository saleOrderItemRepository;

    @Autowired
    private ImportRepository importRepository;

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


    @Override
    @Transactional
    public void createSaleOrder(SaleOrderRequestDto saleOrderRequestDto) {
        // 1. Kiểm tra và lấy các đối tượng liên quan: Customer, User, Doctor (nếu có)
        Customer customer = customerRepository.findById(saleOrderRequestDto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.CUSTOMER_NOT_FOUND));

        User user = userRepository.findById(saleOrderRequestDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.USER_NOT_FOUND));

        Doctor doctor = null;
        if (saleOrderRequestDto.getDoctorId() != null) {
            doctor = doctorRepository.findById(saleOrderRequestDto.getDoctorId())
                    .orElseThrow(() -> new ResourceNotFoundException(Message.DOCTOR_NOT_FOUND));
        }

        // 2. Khởi tạo và thiết lập các thuộc tính cho SaleOrder từ SaleOrderRequestDto
        SaleOrder saleOrder = new SaleOrder();
        saleOrder.setInvoiceNumber(saleOrderRequestDto.getInvoiceNumber());
        saleOrder.setSaleDate(saleOrderRequestDto.getSaleDate() != null ? saleOrderRequestDto.getSaleDate() : Instant.now());
        saleOrder.setOrderType(saleOrderRequestDto.getOrderType());
        saleOrder.setPaymentMethod(saleOrderRequestDto.getPaymentMethod());
        saleOrder.setDiscount(saleOrderRequestDto.getDiscount() != null ? saleOrderRequestDto.getDiscount() : 0.0);
        saleOrder.setCustomer(customer);
        saleOrder.setUser(user);
        saleOrder.setDoctor(doctor);

        saleOrderRepository.save(saleOrder);

        double totalOrderAmount = 0.0;

        // 3. Xử lý từng SaleOrderItemRequestDto trong danh sách saleOrderItems
        for (SaleOrderItemRequestDto itemRequestDto : saleOrderRequestDto.getSaleOrderItems()) {
            Long productId = itemRequestDto.getProductId();
            Integer quantityToSell = itemRequestDto.getQuantity();
            Double unitPrice = itemRequestDto.getUnitPrice();

            // Lấy sản phẩm và thông tin conversion factor (đơn vị nhỏ nhất)
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND));

            int smallestQuantityToSell = quantityToSell * itemRequestDto.getConversionFactor();

            // Lấy danh sách lô hàng theo thứ tự nhập FIFO cho sản phẩm
            List<ImportItem> batches = importItemRepository.findByProductIdOrderByCreateDateAsc(productId);

            int remainingQuantity = smallestQuantityToSell;

            for (ImportItem batch : batches) {
                if (batch.getRemainingQuantity() > 0) {
                    int quantityFromBatch = Math.min(batch.getRemainingQuantity(), remainingQuantity);

                    // Tạo SaleOrderItem cho sản phẩm từ lô hiện tại
                    SaleOrderItem saleOrderItem = new SaleOrderItem();
                    saleOrderItem.setSaleOrder(saleOrder);
                    saleOrderItem.setProduct(product);
                    saleOrderItem.setBatchNumber(batch.getBatchNumber());
                    saleOrderItem.setQuantity(quantityToSell); // Lưu theo đơn vị yêu cầu từ DTO
                    saleOrderItem.setUnitPrice(unitPrice);
                    saleOrderItem.setDiscount(itemRequestDto.getDiscount());

                    // Tính toán totalAmount cho SaleOrderItem với discount
                    double itemTotalAmount = (unitPrice * quantityToSell)
                            - (itemRequestDto.getDiscount() != null ? itemRequestDto.getDiscount() : 0.0);
                    saleOrderItem.setTotalAmount(itemTotalAmount);

                    saleOrderItem.setPrescriptionRequired(itemRequestDto.getPrescriptionRequired());
                    saleOrderItem.setDosage(itemRequestDto.getDosage());
                    saleOrderItem.setUnit(itemRequestDto.getUnit());

                    // Cập nhật số lượng còn lại trong lô
                    batch.setRemainingQuantity(batch.getRemainingQuantity() - quantityFromBatch);
                    importItemRepository.save(batch);

                    // Lưu SaleOrderItem vào cơ sở dữ liệu
                    saleOrderItemRepository.save(saleOrderItem);

                    // Cập nhật tổng tiền đơn hàng
                    totalOrderAmount += itemTotalAmount;

                    // Cập nhật số lượng còn lại cần bán
                    remainingQuantity -= quantityFromBatch;
                    if (remainingQuantity <= 0) break;
                }
            }

            // Nếu không đủ tồn kho, ném ngoại lệ
            if (remainingQuantity > 0) {
                throw new BadRequestException(Message.OUT_OF_STOCK);
            }

            // Cập nhật lại tổng tồn kho của sản phẩm
            int updatedTotalQuantity = product.getTotalQuantity() - smallestQuantityToSell;
            product.setTotalQuantity(updatedTotalQuantity);
            productRepository.save(product);
        }

        // 4. Cập nhật tổng số tiền của SaleOrder và lưu lại
        saleOrder.setTotalAmount(totalOrderAmount - saleOrder.getDiscount());
        saleOrderRepository.save(saleOrder);
    }




}

package com.fu.pha.Service.Customer;

import com.fu.pha.dto.request.CustomerDTORequest;
import com.fu.pha.dto.response.CustomerDTOResponse;
import com.fu.pha.entity.Customer;
import com.fu.pha.entity.SaleOrder;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.CustomerRepository;
import com.fu.pha.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerViewDetailTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;
    private CustomerDTORequest customerDTORequest;
    private Customer customer;

    @BeforeEach
    void setUpGetCustomerById() {
        MockitoAnnotations.openMocks(this);

        customer = new Customer();
        customer.setId(1L);
        customer.setCustomerName("Vũ Văn Hà");
        customer.setPhoneNumber("0987654321");

        // Khởi tạo saleOrderList và thêm một số đối tượng SaleOrder
        SaleOrder order1 = new SaleOrder();
        order1.setTotalAmount(100.0);
        SaleOrder order2 = new SaleOrder();
        order2.setTotalAmount(150.0);
        customer.setSaleOrderList(List.of(order1, order2)); // Đặt danh sách với dữ liệu mẫu
    }

    // Test trường hợp lấy thông tin khách hàng thành công
    @Test
    void UTCCVD01() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        CustomerDTOResponse response = customerService.getCustomerById(1L);

        assertNotNull(response);
        assertEquals(customer.getId(), response.getId());
        assertEquals(customer.getCustomerName(), response.getCustomerName());
        assertEquals(customer.getPhoneNumber(), response.getPhoneNumber());
        assertEquals(customer.getSaleOrderList().stream().mapToDouble(saleOrder -> saleOrder.getTotalAmount()).sum(), response.getTotalAmount());
    }

    // Test trường hợp lấy thông tin khách hàng không thành công vì không tìm thấy khách hàng
    @Test
    void UTCCVD02() {
        when(customerRepository.findById(200L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            customerService.getCustomerById(200L);
        });

        assertEquals(Message.CUSTOMER_NOT_FOUND, exception.getMessage());
    }

}

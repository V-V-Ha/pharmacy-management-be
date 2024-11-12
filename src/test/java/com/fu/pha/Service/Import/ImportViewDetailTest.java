package com.fu.pha.Service.Import;

import com.fu.pha.dto.response.importPack.ImportResponseDto;
import com.fu.pha.entity.*;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.ImportRepository;
import com.fu.pha.service.impl.ImportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class ImportViewDetailTest {

    @Mock
    private ImportRepository importRepository;

    @InjectMocks
    private ImportServiceImpl importService;

    private Import importReceipt;

    @BeforeEach
    void setUp() {
        // Tạo đối tượng Import và gán ID
        importReceipt = new Import();
        importReceipt.setId(1L);

        // Tạo và gán User cho importReceipt
        User user = new User();
        user.setId(1L);
        importReceipt.setUser(user);

        // Tạo và gán Supplier cho importReceipt
        Supplier supplier = new Supplier();
        supplier.setId(1L);
        supplier.setSupplierName("Traphaco");
        importReceipt.setSupplier(supplier);

        // Thiết lập các giá trị khác của importReceipt
        importReceipt.setTotalAmount(1000.0);

        // Tạo danh sách ImportItems và gán vào importReceipt
        ImportItem importItem = new ImportItem();
        importItem.setId(1L);
        importItem.setQuantity(10);
        importItem.setUnitPrice(100.0);
        importItem.setTotalAmount(1000.0);
        importItem.setImportReceipt(importReceipt); // Gán ImportReceipt cho ImportItem

        // Tạo và gán Category cho Product
        Category category = new Category();
        category.setCategoryName("Thuốc ho");

        // Tạo và gán Product cho ImportItem
        Product product = new Product();
        product.setCategoryId(category);

        // Tạo và gán ProductUnit cho Product với một Unit hợp lệ
        ProductUnit productUnit = new ProductUnit();
        productUnit.setConversionFactor(1);
        productUnit.setImportPrice(100.0);
        productUnit.setProduct(product); // Gán Product cho ProductUnit

        Unit unit = new Unit(); // Khởi tạo và gán đối tượng Unit cho ProductUnit
        unit.setId(1L);
        unit.setUnitName("Hộp");
        productUnit.setUnit(unit);

        product.setProductUnitList(Collections.singletonList(productUnit)); // Gán ProductUnitList cho Product

        importItem.setProduct(product);
        importReceipt.setImportItems(Collections.singletonList(importItem));
    }

    @Test
    void UTCIVD01() {
        when(importRepository.findById(1L)).thenReturn(Optional.of(importReceipt));

        ImportResponseDto response = importService.getImportById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(importRepository, times(1)).findById(1L);
    }

    @Test
    void UTCIVD02() {
        // Giả lập trường hợp không tìm thấy Import với ID 200
        when(importRepository.findById(200L)).thenReturn(Optional.empty());

        // Kiểm tra nếu ném ra ResourceNotFoundException khi gọi getImportById với ID 200
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            importService.getImportById(200L);
        });

        // Xác nhận thông báo lỗi khớp với thông báo IMPORT_NOT_FOUND
        assertEquals(Message.IMPORT_NOT_FOUND, exception.getMessage());

        // Xác nhận phương thức findById của importRepository được gọi 1 lần với ID 200
        verify(importRepository, times(1)).findById(200L);
    }

}

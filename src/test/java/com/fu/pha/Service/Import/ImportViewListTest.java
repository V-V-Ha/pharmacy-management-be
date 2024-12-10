package com.fu.pha.Service.Import;

import com.fu.pha.dto.request.importPack.ImportViewListDto;
import com.fu.pha.entity.*;
import com.fu.pha.enums.OrderStatus;
import com.fu.pha.exception.Message;
import com.fu.pha.service.impl.ImportServiceImpl;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.fu.pha.dto.response.importPack.ImportResponseDto;
import com.fu.pha.enums.PaymentMethod;
import com.fu.pha.repository.ImportRepository;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.service.ImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
@ExtendWith(MockitoExtension.class)
public class ImportViewListTest {

    @Mock
    private ImportRepository importRepository;

    @InjectMocks
    private ImportServiceImpl importService;

    private Import importMock;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        importMock = new Import();
        importMock.setId(1L);
        importMock.setInvoiceNumber("INV123");
        importMock.setImportDate(Instant.now());
        importMock.setPaymentMethod(PaymentMethod.CASH);
        importMock.setTax(100.0);
        importMock.setDiscount(50.0);
        importMock.setTotalAmount(5000.0);
        importMock.setNote("Import note");

        User mockUser = new User();
        mockUser.setId(1L);
        importMock.setUser(mockUser);

        Supplier mockSupplier = new Supplier();
        mockSupplier.setId(1L);
        mockSupplier.setSupplierName("Supplier Name");
        importMock.setSupplier(mockSupplier);

        importMock.setCreateDate(Instant.now());
        importMock.setLastModifiedDate(Instant.now());
        importMock.setCreateBy("admin");
        importMock.setLastModifiedBy("admin");

        importMock.setStatus(OrderStatus.PENDING);
        importMock.setImage("image-url");

        Category mockCategory = new Category();
        mockCategory.setId(1L);
        mockCategory.setCategoryName("Category A");

        Product product1 = new Product();
        product1.setId(1L);
        product1.setProductName("Product A");
        product1.setCategoryId(mockCategory);
        product1.setProductUnitList(Collections.emptyList());

        Product product2 = new Product();
        product2.setId(2L);
        product2.setProductName("Product B");
        product2.setCategoryId(mockCategory);
        product2.setProductUnitList(Collections.emptyList());

        ImportItem importItem1 = new ImportItem();
        importItem1.setProduct(product1);
        importItem1.setImportReceipt(importMock);

        ImportItem importItem2 = new ImportItem();
        importItem2.setProduct(product2);
        importItem2.setImportReceipt(importMock);

        List<ImportItem> importItems = Arrays.asList(importItem1, importItem2);
        importMock.setImportItems(importItems);
    }

    // Test case for getImport list
    @Test
    public void UTCIL01() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<ImportViewListDto> importList = Arrays.asList(new ImportViewListDto(importMock));
        Page<ImportViewListDto> importPage = new PageImpl<>(importList, pageRequest, importList.size());

        when(importRepository.getListImportPagingWithoutDate(anyString(), any(OrderStatus.class), eq(pageRequest)))
                .thenReturn(importPage);

        Page<ImportViewListDto> result = importService.getAllImportPaging(0, 10, "Traphaco", OrderStatus.PENDING, null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(importRepository, times(1)).getListImportPagingWithoutDate(anyString(), any(OrderStatus.class), eq(pageRequest));
    }

    @Test
    public void UTCIL02() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<ImportViewListDto> importList = Arrays.asList(new ImportViewListDto(importMock));
        Page<ImportViewListDto> importPage = new PageImpl<>(importList, pageRequest, importList.size());
        Instant fromDate = Instant.now().minusSeconds(3600);

        when(importRepository.getListImportPagingFromDate(anyString(), any(OrderStatus.class), eq(fromDate), eq(pageRequest)))
                .thenReturn(importPage);

        Page<ImportViewListDto> result = importService.getAllImportPaging(0, 10, "Supplier Name", OrderStatus.PENDING, fromDate, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(importRepository, times(1)).getListImportPagingFromDate(anyString(), any(OrderStatus.class), eq(fromDate), eq(pageRequest));
    }

    @Test
    public void UTCIL03() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<ImportViewListDto> importList = Arrays.asList(new ImportViewListDto(importMock));
        Page<ImportViewListDto> importPage = new PageImpl<>(importList, pageRequest, importList.size());
        Instant toDate = Instant.now();

        when(importRepository.getListImportPagingToDate(anyString(), any(OrderStatus.class), eq(toDate), eq(pageRequest)))
                .thenReturn(importPage);

        Page<ImportViewListDto> result = importService.getAllImportPaging(0, 10, "Traphaco", OrderStatus.PENDING, null, toDate);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(importRepository, times(1)).getListImportPagingToDate(anyString(), any(OrderStatus.class), eq(toDate), eq(pageRequest));
    }

    @Test
    public void UTCIL04() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<ImportViewListDto> importList = Arrays.asList(new ImportViewListDto(importMock));
        Page<ImportViewListDto> importPage = new PageImpl<>(importList, pageRequest, importList.size());
        Instant fromDate = Instant.now().minusSeconds(3600);
        Instant toDate = Instant.now();

        when(importRepository.getListImportPaging(anyString(), any(OrderStatus.class), eq(fromDate), eq(toDate), eq(pageRequest)))
                .thenReturn(importPage);

        Page<ImportViewListDto> result = importService.getAllImportPaging(0, 10, "Traphaco", OrderStatus.PENDING, fromDate, toDate);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(importRepository, times(1)).getListImportPaging(anyString(), any(OrderStatus.class), eq(fromDate), eq(toDate), eq(pageRequest));
    }

    @Test
    public void UTCIL05() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<ImportViewListDto> emptyPage = Page.empty(pageRequest);

        when(importRepository.getListImportPagingWithoutDate(anyString(), any(OrderStatus.class), eq(pageRequest)))
                .thenReturn(emptyPage);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            importService.getAllImportPaging(0, 10, "Dược Nam Hà", OrderStatus.PENDING, null, null);
        });
        assertEquals(Message.IMPORT_NOT_FOUND, exception.getMessage());

        verify(importRepository, times(1)).getListImportPagingWithoutDate(anyString(), any(OrderStatus.class), eq(pageRequest));
    }
}
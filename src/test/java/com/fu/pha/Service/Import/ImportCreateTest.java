package com.fu.pha.Service.Import;

import com.fu.pha.dto.request.importPack.ImportDto;
import com.fu.pha.dto.request.importPack.ImportItemRequestDto;
import com.fu.pha.entity.*;
import com.fu.pha.enums.PaymentMethod;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.exception.UnauthorizedException;
import com.fu.pha.repository.ImportRepository;
import com.fu.pha.repository.ImportItemRepository;
import com.fu.pha.repository.ProductRepository;
import com.fu.pha.repository.ProductUnitRepository;
import com.fu.pha.repository.SupplierRepository;
import com.fu.pha.repository.UserRepository;
import com.fu.pha.service.CloudinaryService;
import com.fu.pha.service.NotificationService;
import com.fu.pha.service.impl.ImportServiceImpl;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ImportCreateTest {

    @InjectMocks
    private ImportServiceImpl importService;

    @Mock
    private ImportRepository importRepository;
    @Mock
    private ProductUnitRepository productUnitRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ImportItemRepository importItemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private CloudinaryService cloudinaryService;
    @Mock
    private NotificationService notificationService;

    private ImportDto importRequestDto;
    private MultipartFile file;

    private User mockUser;
    private Supplier mockSupplier;
    private Product mockProduct;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock user
        mockUser = new User();
        mockUser.setUsername("testUser");

        // Mock supplier
        mockSupplier = new Supplier();
        mockSupplier.setId(1L);

        // Mock product
        mockProduct = new Product();
        mockProduct.setId(1L);
        mockProduct.setProductName("Product Name");

        // Mock import request DTO
        importRequestDto = new ImportDto();
        importRequestDto.setSupplierId(1L);
        importRequestDto.setPaymentMethod(PaymentMethod.CASH);
        importRequestDto.setNote("Test import");
        importRequestDto.setTax(5.0);
        importRequestDto.setDiscount(10.0);

        // Mock file upload
        file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[1]);
    }

    @Test
    void testCreateImport_WhenUserNotLoggedIn_ShouldThrowUnauthorizedException() {
        // Mock SecurityContext to simulate no logged-in user
        SecurityContextHolder.clearContext();

        // Call method and assert exception
        assertThrows(UnauthorizedException.class, () -> importService.createImport(importRequestDto, file));
    }

    @Test
    void testCreateImport_WhenSupplierNotFound_ShouldThrowResourceNotFoundException() {
        // Mock SecurityContext with logged-in user
        Mockito.when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(mockUser));
        SecurityContextHolder.getContext().setAuthentication(mock(Authentication.class));

        // Simulate supplier not found
        Mockito.when(supplierRepository.findById(importRequestDto.getSupplierId()))
                .thenReturn(Optional.empty());

        // Call method and assert exception
        assertThrows(ResourceNotFoundException.class, () -> importService.createImport(importRequestDto, file));
    }

    @Test
    void testCreateImport_WhenFileNotProvided_ShouldThrowBadRequestException() {
        // Mock SecurityContext with logged-in user
        Mockito.when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(mockUser));
        SecurityContextHolder.getContext().setAuthentication(mock(Authentication.class));

        // Simulate supplier found
        Mockito.when(supplierRepository.findById(importRequestDto.getSupplierId()))
                .thenReturn(Optional.of(mockSupplier));

        // Simulate empty file
        MultipartFile emptyFile = new MockMultipartFile("file", new byte[0]);

        // Call method and assert exception
        assertThrows(BadRequestException.class, () -> importService.createImport(importRequestDto, emptyFile));
    }

    @Test
    void testCreateImport_WhenImportItemsEmpty_ShouldThrowBadRequestException() {
        // Mock SecurityContext with logged-in user
        Mockito.when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(mockUser));
        SecurityContextHolder.getContext().setAuthentication(mock(Authentication.class));

        // Simulate supplier found
        Mockito.when(supplierRepository.findById(importRequestDto.getSupplierId()))
                .thenReturn(Optional.of(mockSupplier));

        // Set empty import items
        importRequestDto.setImportItems(Collections.emptyList());

        // Call method and assert exception
        assertThrows(BadRequestException.class, () -> importService.createImport(importRequestDto, file));
    }

    @Test
    void testCreateImport_WhenTotalAmountDoesNotMatch_ShouldThrowBadRequestException() {
        // Mock SecurityContext with logged-in user
        Mockito.when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(mockUser));
        SecurityContextHolder.getContext().setAuthentication(mock(Authentication.class));

        // Simulate supplier found
        Mockito.when(supplierRepository.findById(importRequestDto.getSupplierId()))
                .thenReturn(Optional.of(mockSupplier));

        // Mock ImportItemRequestDto
        ImportItemRequestDto item = new ImportItemRequestDto();
        item.setProductId(1L);
        item.setQuantity(10);
        item.setUnitPrice(100.0);
        item.setDiscount(5.0);
        item.setTax(5.0);
        item.setTotalAmount(950.0); // Incorrect total amount
        importRequestDto.setImportItems(Collections.singletonList(item));

        // Simulate totalAmount mismatch between BE and FE
        importRequestDto.setTotalAmount(1000.0);

        // Call method and assert exception
        assertThrows(BadRequestException.class, () -> importService.createImport(importRequestDto, file));
    }

    @Test
    void testCreateImport_WhenImportSuccess() {
        // Mock SecurityContext with logged-in user
        Mockito.when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(mockUser));
        SecurityContextHolder.getContext().setAuthentication(mock(Authentication.class));

        // Simulate supplier found
        Mockito.when(supplierRepository.findById(importRequestDto.getSupplierId()))
                .thenReturn(Optional.of(mockSupplier));

        // Mock the behavior of saving import
        Mockito.when(importRepository.save(any(Import.class))).thenReturn(new Import());

        // Mock notification service
        doNothing().when(notificationService).sendNotificationToUser(anyString(), anyString(), any(User.class), anyLong());

        // Call method and assert no exceptions
        assertDoesNotThrow(() -> importService.createImport(importRequestDto, file));
    }
}

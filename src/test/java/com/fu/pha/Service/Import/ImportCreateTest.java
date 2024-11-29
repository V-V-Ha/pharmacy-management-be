package com.fu.pha.Service.Import;

import com.fu.pha.dto.request.importPack.ImportDto;
import com.fu.pha.dto.request.importPack.ImportItemRequestDto;
import com.fu.pha.dto.response.CloudinaryResponse;
import com.fu.pha.entity.*;
import com.fu.pha.enums.ERole;
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


    @Test
    void testCreateImport_WhenUserNotLoggedIn_ShouldThrowUnauthorizedException() {
        // Arrange
        ImportServiceImpl importServiceSpy = Mockito.spy(importService);
        doThrow(new UnauthorizedException(Message.REJECT_AUTHORIZATION)).when(importServiceSpy).getCurrentUser();

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            importServiceSpy.createImport(importRequestDto, file);
        });

        assertEquals(Message.REJECT_AUTHORIZATION, exception.getMessage());
    }

    @Test
    void testCreateImport_WhenSupplierNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        ImportServiceImpl importServiceSpy = Mockito.spy(importService);
        mockUser = new User();
        mockUser.setRoles(Collections.singleton(new Role(ERole.ROLE_STOCK.name())));
        doReturn(mockUser).when(importServiceSpy).getCurrentUser();
        when(supplierRepository.findById(anyLong())).thenReturn(Optional.empty());

        importRequestDto = new ImportDto();
        importRequestDto.setSupplierId(1L); // Set a valid supplier ID

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            importServiceSpy.createImport(importRequestDto, file);
        });

        assertEquals(Message.SUPPLIER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void testCreateImport_WhenFileNotProvided_ShouldThrowBadRequestException() {
        // Arrange
        ImportServiceImpl importServiceSpy = Mockito.spy(importService);
        mockUser = new User();
        mockUser.setRoles(Collections.singleton(new Role(ERole.ROLE_STOCK.name())));
        doReturn(mockUser).when(importServiceSpy).getCurrentUser();

        mockSupplier = new Supplier();
        when(supplierRepository.findById(anyLong())).thenReturn(Optional.of(mockSupplier));

        importRequestDto = new ImportDto();
        importRequestDto.setSupplierId(1L); // Set a valid supplier ID

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            importServiceSpy.createImport(importRequestDto, null);
        });

        assertEquals(Message.IMAGE_IMPORT_NOT_NULL, exception.getMessage());
    }

    @Test
    void testCreateImport_WhenImportItemsEmpty_ShouldThrowBadRequestException() {
        // Arrange
        ImportServiceImpl importServiceSpy = Mockito.spy(importService);
        mockUser = new User();
        mockUser.setRoles(Collections.singleton(new Role(ERole.ROLE_STOCK.name())));
        doReturn(mockUser).when(importServiceSpy).getCurrentUser();

        mockSupplier = new Supplier();
        when(supplierRepository.findById(anyLong())).thenReturn(Optional.of(mockSupplier));

        file = Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.png");

        CloudinaryResponse mockCloudinaryResponse = new CloudinaryResponse();
        mockCloudinaryResponse.setUrl("image_url");
        when(cloudinaryService.upLoadFile(eq(file), anyString())).thenReturn(mockCloudinaryResponse);

        importRequestDto = new ImportDto();
        importRequestDto.setSupplierId(1L); // Set a valid supplier ID
        importRequestDto.setImportItems(Collections.emptyList());

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            importServiceSpy.createImport(importRequestDto, file);
        });

        assertEquals(Message.IMPORT_ITEMS_EMPTY, exception.getMessage());
    }

    @Test
    void testCreateImport_WhenTotalAmountDoesNotMatch_ShouldThrowBadRequestException() {
        // Arrange
        ImportServiceImpl importServiceSpy = Mockito.spy(importService);
        mockUser = new User();
        mockUser.setRoles(Collections.singleton(new Role(ERole.ROLE_STOCK.name())));
        doReturn(mockUser).when(importServiceSpy).getCurrentUser();

        mockSupplier = new Supplier();
        when(supplierRepository.findById(anyLong())).thenReturn(Optional.of(mockSupplier));

        file = Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.png");

        CloudinaryResponse mockCloudinaryResponse = new CloudinaryResponse();
        mockCloudinaryResponse.setUrl("image_url");
        when(cloudinaryService.upLoadFile(eq(file), anyString())).thenReturn(mockCloudinaryResponse);

        importRequestDto = new ImportDto();
        importRequestDto.setSupplierId(1L); // Set a valid supplier ID
        importRequestDto.setImportItems(Collections.singletonList(new ImportItemRequestDto()));
        importRequestDto.setTotalAmount(100.0);

        doReturn(90.0).when(importServiceSpy).saveImportItems(any(), any(), any());

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            importServiceSpy.createImport(importRequestDto, file);
        });

        assertEquals(Message.TOTAL_AMOUNT_NOT_MATCH, exception.getMessage());
    }

    @Test
    void testCreateImport_WhenImportSuccess() {
        // Arrange
        ImportServiceImpl importServiceSpy = Mockito.spy(importService);
        mockUser = new User();
        mockUser.setRoles(Collections.singleton(new Role(ERole.ROLE_STOCK.name())));
        doReturn(mockUser).when(importServiceSpy).getCurrentUser();

        mockSupplier = new Supplier();
        when(supplierRepository.findById(anyLong())).thenReturn(Optional.of(mockSupplier));

        file = Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.png");

        CloudinaryResponse mockCloudinaryResponse = new CloudinaryResponse();
        mockCloudinaryResponse.setUrl("image_url");
        when(cloudinaryService.upLoadFile(eq(file), anyString())).thenReturn(mockCloudinaryResponse);

        importRequestDto = new ImportDto();
        importRequestDto.setSupplierId(1L); // Set a valid supplier ID
        importRequestDto.setImportItems(Collections.singletonList(new ImportItemRequestDto()));
        importRequestDto.setTotalAmount(100.0);

        doReturn(100.0).when(importServiceSpy).saveImportItems(any(), any(), any());

        // Act
        importServiceSpy.createImport(importRequestDto, file);

        // Assert
        verify(importRepository, times(2)).save(any());
    }
}

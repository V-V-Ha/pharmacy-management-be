package com.fu.pha.Service.Import;

import com.fu.pha.dto.response.CloudinaryResponse;
import com.fu.pha.enums.ERole;
import com.fu.pha.enums.OrderStatus;
import com.fu.pha.service.CloudinaryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fu.pha.dto.request.importPack.ImportDto;
import com.fu.pha.dto.request.importPack.ImportItemRequestDto;
import com.fu.pha.entity.*;
import com.fu.pha.exception.*;
import com.fu.pha.repository.*;
import com.fu.pha.service.impl.ImportServiceImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;


@ExtendWith(MockitoExtension.class)
public class ImportUpdateTest {

    @InjectMocks
    private ImportServiceImpl importService;

    @Mock
    private ImportRepository importRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductUnitRepository productUnitRepository;
    @Mock
    private ImportItemRepository importItemRepository;
    @Mock
    private CloudinaryService cloudinaryService;
    @Mock
    private InventoryHistoryRepository inventoryHistoryRepository;

    private ImportDto importRequestDto;
    private MultipartFile file;

    private User mockUser;
    private Product mockProduct;
    private Supplier mockSupplier;
    private ProductUnit mockProductUnit;

    @Test
    void testUpdateImport_NotFound() {
        // Arrange
        ImportServiceImpl importServiceSpy = Mockito.spy(importService);
        doReturn(mockUser).when(importServiceSpy).getCurrentUser();
        when(importRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            importServiceSpy.updateImport(1L, importRequestDto, file);
        });

        assertEquals(Message.IMPORT_NOT_FOUND, exception.getMessage());
    }

    @Test
    void testUpdateImport_NotPending() {
        // Arrange
        ImportServiceImpl importServiceSpy = Mockito.spy(importService);
        doReturn(mockUser).when(importServiceSpy).getCurrentUser();

        Import importMock = new Import();
        importMock.setStatus(OrderStatus.CONFIRMED);
        when(importRepository.findById(anyLong())).thenReturn(Optional.of(importMock));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            importServiceSpy.updateImport(1L, importRequestDto, file);
        });

        assertEquals(Message.NOT_PENDING_IMPORT, exception.getMessage());
    }

    @Test
    void testUpdateImport_Unauthorized() {
        // Arrange
        ImportServiceImpl importServiceSpy = Mockito.spy(importService);
        User unauthorizedUser = new User();
        unauthorizedUser.setRoles(Collections.singleton(new Role(ERole.ROLE_SALE.name())));
        doReturn(unauthorizedUser).when(importServiceSpy).getCurrentUser();

        User importUser = new User();
        importUser.setId(1L); // Set the ID of the user
        Import importMock = new Import();
        importMock.setUser(importUser);
        when(importRepository.findById(anyLong())).thenReturn(Optional.of(importMock));

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            importServiceSpy.updateImport(1L, importRequestDto, file);
        });

        assertEquals(Message.REJECT_AUTHORIZATION, exception.getMessage());
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)  // Lenient stubbing
    void testUpdateImport_Success() {
        // Arrange
        ImportServiceImpl importServiceSpy = Mockito.spy(importService);
        User authorizedUser = new User();
        authorizedUser.setId(1L); // Set the ID of the user
        authorizedUser.setRoles(Collections.singleton(new Role(ERole.ROLE_PRODUCT_OWNER.name())));
        doReturn(authorizedUser).when(importServiceSpy).getCurrentUser();

        Import importMock = new Import();
        importMock.setStatus(OrderStatus.PENDING);
        importMock.setUser(authorizedUser);
        when(importRepository.findById(anyLong())).thenReturn(Optional.of(importMock));

        mockSupplier = new Supplier();
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mockSupplier));

        mockUser = new User();
        mockUser.setId(1L); // Set the ID of the user
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        mockProduct = new Product();
        when(productRepository.getProductById(anyLong())).thenReturn(Optional.of(mockProduct));

        mockProductUnit = new ProductUnit();
        when(productUnitRepository.findByProductId(anyLong())).thenReturn(Collections.singletonList(mockProductUnit));
        when(productUnitRepository.findByProductId(0L)).thenReturn(Collections.emptyList()); // lenient stub for 0L
        when(productUnitRepository.findByProductId(null)).thenReturn(Collections.emptyList()); // lenient stub for null

        ImportItemRequestDto importItemRequestDto = new ImportItemRequestDto();
        importItemRequestDto.setProductId(1L); // Set the product ID
        importItemRequestDto.setUnitPrice(50.0); // Set the unit price
        importItemRequestDto.setQuantity(2); // Set the quantity
        importItemRequestDto.setConversionFactor(1); // Set the conversion factor

        importRequestDto = new ImportDto();
        importRequestDto.setUserId(1L); // Set the user ID
        importRequestDto.setSupplierId(1L); // Set the supplier ID
        importRequestDto.setImportItems(Collections.singletonList(importItemRequestDto));
        importRequestDto.setTotalAmount(100.0);

        // Act
        importServiceSpy.updateImport(1L, importRequestDto, file);

        // Assert
        verify(importRepository, times(1)).save(importMock);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void testUpdateImport_TotalAmountNotMatch() {
        // Arrange
        ImportServiceImpl importServiceSpy = Mockito.spy(importService);
        User authorizedUser = new User();
        authorizedUser.setId(1L); // Set the ID of the user
        authorizedUser.setRoles(Collections.singleton(new Role(ERole.ROLE_PRODUCT_OWNER.name())));
        doReturn(authorizedUser).when(importServiceSpy).getCurrentUser();

        // Mock the Import
        Import importMock = new Import();
        importMock.setId(1L); // Set a valid ID for the importReceipt
        importMock.setStatus(OrderStatus.PENDING);
        importMock.setUser(authorizedUser);
        when(importRepository.findById(anyLong())).thenReturn(Optional.of(importMock));

        mockSupplier = new Supplier();
        mockSupplier.setId(1L); // Set the ID of the supplier
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mockSupplier));

        mockUser = new User();
        mockUser.setId(1L); // Set the ID of the user
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        // Mock the product repository to return a valid product
        mockProduct = new Product();
        mockProduct.setId(1L);  // Ensure the product has the correct ID
        when(productRepository.getProductById(1L)).thenReturn(Optional.of(mockProduct));  // Mock getProductById to return the mockProduct

        // Prepare ImportItemRequestDto
        ImportItemRequestDto importItemRequestDto = new ImportItemRequestDto();
        importItemRequestDto.setProductId(1L);  // Set to valid product ID
        importItemRequestDto.setUnitPrice(50.0);  // Set unitPrice to a non-null value
        importItemRequestDto.setQuantity(2);     // Set the quantity
        importItemRequestDto.setConversionFactor(1);  // Set conversion factor if needed

        // Prepare ImportDto
        ImportDto importRequestDto = new ImportDto();
        importRequestDto.setUserId(1L); // Ensure userId is set
        importRequestDto.setSupplierId(1L); // Ensure supplierId is set
        importRequestDto.setImportItems(Collections.singletonList(importItemRequestDto)); // Set import item
        importRequestDto.setTotalAmount(100.0);  // This is the expected total amount to mismatch with

        // Mock the method that calculates the total amount for import items
        // This will mock the total amount returned from the calculation to be 90.0 (instead of 100.0)
        doReturn(90.0).when(importServiceSpy).calculateImportItemTotalAmount(any());

        // Mock the importItemRepository.findByImportId method to return some mock items
        ImportItem mockImportItem = new ImportItem();
        mockImportItem.setProduct(mockProduct);  // Set the product for the mock ImportItem
        mockImportItem.setQuantity(2);  // Set some sample data
        mockImportItem.setRemainingQuantity(2); // Ensure remainingQuantity is set
        when(importItemRepository.findByImportId(1L)).thenReturn(Collections.singletonList(mockImportItem));

        // Act & Assert
        // Expect a BadRequestException to be thrown due to the total amount mismatch
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            importServiceSpy.updateImport(1L, importRequestDto, file);
        });

        // Verify the exception message
        assertEquals(Message.TOTAL_AMOUNT_NOT_MATCH, exception.getMessage());

        // Verify the necessary methods were invoked
        verify(importRepository, times(1)).findById(anyLong());
        verify(supplierRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).getProductById(1L); // Adjusted to expect 1 invocation
        verify(importItemRepository, times(1)).findByImportId(1L);  // Ensure this method is called with correct ID
        verify(inventoryHistoryRepository, atLeastOnce()).save(any()); // Ensure inventoryHistoryRepository.save is called
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void testUpdateImport_WithFileUpload() {
        // Arrange
        ImportServiceImpl importServiceSpy = Mockito.spy(importService);
        User authorizedUser = new User();
        authorizedUser.setId(1L); // Ensure the ID is set
        authorizedUser.setRoles(Collections.singleton(new Role(ERole.ROLE_PRODUCT_OWNER.name())));
        doReturn(authorizedUser).when(importServiceSpy).getCurrentUser();

        Import importMock = new Import();
        importMock.setId(1L); // Ensure the Import has an ID
        importMock.setStatus(OrderStatus.PENDING);
        importMock.setUser(authorizedUser);
        when(importRepository.findById(anyLong())).thenReturn(Optional.of(importMock));

        // Mock the supplier
        mockSupplier = new Supplier();
        mockSupplier.setId(1L); // Ensure the ID is set for the supplier
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mockSupplier)); // Mock supplierRepository to return a valid supplier

        // Mock the user
        mockUser = new User();
        mockUser.setId(1L); // Ensure the ID is set for the user
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser)); // Mock userRepository to return a valid user


        // Mock the product repository to return a valid product
        mockProduct = new Product();
        mockProduct.setId(1L);  // Ensure the product has the correct ID
        when(productRepository.getProductById(1L)).thenReturn(Optional.of(mockProduct));  // Mock getProductById to return the mockProduct


        // Prepare ImportDto with valid userId and supplierId
        importRequestDto = new ImportDto();
        importRequestDto.setUserId(1L); // Set a valid userId here
        importRequestDto.setSupplierId(1L); // Set a valid supplierId here
        importRequestDto.setImportItems(Collections.singletonList(new ImportItemRequestDto()));
        importRequestDto.setTotalAmount(100.0);

        // Prepare the ImportItemRequestDto with a valid unitPrice
        ImportItemRequestDto importItemRequestDto = new ImportItemRequestDto();
        importItemRequestDto.setProductId(1L);  // Set a valid product ID
        importItemRequestDto.setUnitPrice(50.0); // Ensure unitPrice is set
        importItemRequestDto.setQuantity(2);     // Set quantity
        importItemRequestDto.setConversionFactor(1); // Set conversion factor if needed
        importRequestDto.setImportItems(Collections.singletonList(importItemRequestDto));

        // Mock MultipartFile for file upload
        file = Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.png");

        // Mock CloudinaryService upload
        CloudinaryResponse mockCloudinaryResponse = new CloudinaryResponse();
        mockCloudinaryResponse.setUrl("image_url");
        when(cloudinaryService.upLoadFile(eq(file), anyString())).thenReturn(mockCloudinaryResponse);

        // Act
        importServiceSpy.updateImport(1L, importRequestDto, file);

        // Assert
        verify(importRepository, times(1)).save(importMock);  // Verify that save was called on the importRepository
        assertEquals("image_url", importMock.getImage());  // Verify the image URL was set correctly
    }





}

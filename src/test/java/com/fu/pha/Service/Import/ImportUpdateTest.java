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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

    // Test cases import not found
    @Test
    void UTCIU01() {
        // Arrange
        ImportServiceImpl importServiceSpy = Mockito.spy(importService);
        doReturn(mockUser).when(importServiceSpy).getCurrentUser();
        when(importRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            importServiceSpy.updateImport(200L, importRequestDto, file);
        });

        assertEquals(Message.IMPORT_NOT_FOUND, exception.getMessage());
    }

    @Test
    void UTCIU02() {
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

    // Test cases user not authorized
    @Test
    void UTCIU03() {
        // Arrange
        ImportServiceImpl importServiceSpy = Mockito.spy(importService);
        User unauthorizedUser = new User();
        unauthorizedUser.setRoles(Collections.singleton(new Role(ERole.ROLE_SALE.name())));
        doReturn(unauthorizedUser).when(importServiceSpy).getCurrentUser();

        User importUser = new User();
        importUser.setId(200L); // Set the ID of the user
        Import importMock = new Import();
        importMock.setUser(importUser);
        when(importRepository.findById(anyLong())).thenReturn(Optional.of(importMock));

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            importServiceSpy.updateImport(1L, importRequestDto, file);
        });

        assertEquals(Message.REJECT_AUTHORIZATION, exception.getMessage());
    }

    // Test cases update import success
    @Test
    void UTCIU04() {
        // Arrange
        // 1. Mock the current user
        User authorizedUser = new User();
        authorizedUser.setId(1L);
        authorizedUser.setRoles(Collections.singleton(new Role(ERole.ROLE_PRODUCT_OWNER.name())));

        // Create a spy to mock getCurrentUser
        ImportServiceImpl importServiceSpy = Mockito.spy(importService);
        doReturn(authorizedUser).when(importServiceSpy).getCurrentUser();

        // 2. Mock importRepository.findById
        Import importMock = new Import();
        importMock.setId(1L); // Ensure import has an ID
        importMock.setStatus(OrderStatus.PENDING);
        importMock.setUser(authorizedUser);
        when(importRepository.findById(1L)).thenReturn(Optional.of(importMock));

        // 3. Mock supplierRepository.findById
        Supplier mockSupplier = new Supplier();
        mockSupplier.setId(1L);
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mockSupplier));

        // 4. Mock userRepository.findById
        User mockUser = new User();
        mockUser.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        // 5. Mock productRepository.getProductById
        Product mockProduct = new Product();
        mockProduct.setId(1L);
        mockProduct.setTotalQuantity(100);
        when(productRepository.getProductById(1L)).thenReturn(Optional.of(mockProduct));

        // 6. Mock productUnitRepository.findByProductId
        ProductUnit mockProductUnit = new ProductUnit();
        mockProductUnit.setId(1L);
        mockProductUnit.setProduct(mockProduct);
        mockProductUnit.setConversionFactor(1); // Initialize conversionFactor to prevent NPE
        when(productUnitRepository.findByProductId(1L)).thenReturn(Collections.singletonList(mockProductUnit));
        when(productUnitRepository.findByProductId(0L)).thenReturn(Collections.emptyList());
        when(productUnitRepository.findByProductId(null)).thenReturn(Collections.emptyList());

        // 7. Prepare ImportItemRequestDto
        ImportItemRequestDto importItemRequestDto = new ImportItemRequestDto();
        importItemRequestDto.setProductId(1L);
        importItemRequestDto.setUnitPrice(50.0);
        importItemRequestDto.setQuantity(2);
        importItemRequestDto.setConversionFactor(1);

        // 8. Prepare ImportDto
        ImportDto importRequestDto = new ImportDto();
        importRequestDto.setUserId(1L);
        importRequestDto.setSupplierId(1L);
        importRequestDto.setImportItems(Collections.singletonList(importItemRequestDto));
        importRequestDto.setTotalAmount(100.0);

        // 9. Mock importItemRepository.findByImportId to return a valid ImportItem
        ImportItem importItem = new ImportItem();
        importItem.setId(1L);
        importItem.setProduct(mockProduct);
        importItem.setQuantity(2);
        importItem.setRemainingQuantity(2);
        when(importItemRepository.findByImportId(1L)).thenReturn(Collections.singletonList(importItem));
        when(importItemRepository.findByImportId(null)).thenReturn(Collections.emptyList()); // Handle null if necessary

        // 10. Mock saveImportItems if it's a separate method
        // Assuming saveImportItems is a method within ImportServiceImpl that returns a double
        // If not, remove this line
        doReturn(100.0).when(importServiceSpy).saveImportItems(any(), any(), any());

        // 11. Initialize the file variable
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("phieu.png");

        // 12. Mock cloudinaryService.upLoadFile
        CloudinaryResponse mockCloudinaryResponse = new CloudinaryResponse();
        mockCloudinaryResponse.setUrl("image_url");
        when(cloudinaryService.upLoadFile(eq(file), anyString())).thenReturn(mockCloudinaryResponse);

        // Act
        importServiceSpy.updateImport(1L, importRequestDto, file);

        // Assert
        verify(importRepository, times(1)).save(importMock);
        verify(importItemRepository, times(1)).findByImportId(1L);
        // Optionally verify other interactions
    }

    // Test cases total amount mismatch
    @Test
    void UTCIU05() {
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

        Supplier mockSupplier = new Supplier();
        mockSupplier.setId(1L); // Set the ID of the supplier
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mockSupplier));

        User mockUser = new User();
        mockUser.setId(1L); // Set the ID of the user
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        // Mock the product repository to return a valid product
        Product mockProduct = new Product();
        mockProduct.setId(1L);  // Ensure the product has the correct ID
        mockProduct.setTotalQuantity(100); // Ensure totalQuantity is set
        when(productRepository.getProductById(anyLong())).thenReturn(Optional.of(mockProduct));  // Mock getProductById to return the mockProduct

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

        // Initialize the file variable
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("phieu.png");

        // Mock cloudinaryService.upLoadFile
        CloudinaryResponse mockCloudinaryResponse = new CloudinaryResponse();
        mockCloudinaryResponse.setUrl("image_url");
        when(cloudinaryService.upLoadFile(eq(file), anyString())).thenReturn(mockCloudinaryResponse);

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
        verify(productRepository, times(1)).getProductById(anyLong()); // Adjusted to expect anyLong()
        verify(importItemRepository, times(1)).findByImportId(1L);
        verify(inventoryHistoryRepository, atLeastOnce()).save(any()); // Ensure inventoryHistoryRepository.save is called
    }

    @Test
    void UTCIU06() {
        // Arrange
        // 1. Mock the current user
        User authorizedUser = new User();
        authorizedUser.setId(1L);
        authorizedUser.setRoles(Collections.singleton(new Role(ERole.ROLE_PRODUCT_OWNER.name())));
        ImportServiceImpl importServiceSpy = Mockito.spy(importService);
        doReturn(authorizedUser).when(importServiceSpy).getCurrentUser();

        // 2. Mock importRepository.findById
        Import importMock = new Import();
        importMock.setId(1L);
        importMock.setStatus(OrderStatus.PENDING);
        importMock.setUser(authorizedUser);
        when(importRepository.findById(1L)).thenReturn(Optional.of(importMock));

        // 3. Mock supplierRepository.findById
        Supplier mockSupplier = new Supplier();
        mockSupplier.setId(1L);
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mockSupplier));

        // 4. Mock userRepository.findById
        User mockUser = new User();
        mockUser.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        // 5. Mock productRepository.getProductById and set totalQuantity
        Product mockProduct = new Product();
        mockProduct.setId(1L);
        mockProduct.setTotalQuantity(100); // Set to prevent NPE
        when(productRepository.getProductById(1L)).thenReturn(Optional.of(mockProduct));

        // 6. Mock productUnitRepository.findByProductId
        ProductUnit mockProductUnit = new ProductUnit();
        mockProductUnit.setId(1L);
        mockProductUnit.setProduct(mockProduct);
        mockProductUnit.setConversionFactor(1); // Prevent NPE
        when(productUnitRepository.findByProductId(1L)).thenReturn(Collections.singletonList(mockProductUnit));
        when(productUnitRepository.findByProductId(0L)).thenReturn(Collections.emptyList());
        when(productUnitRepository.findByProductId(null)).thenReturn(Collections.emptyList());

        // 7. Prepare ImportItemRequestDto with valid fields
        ImportItemRequestDto importItemRequestDto = new ImportItemRequestDto();
        importItemRequestDto.setProductId(1L);
        importItemRequestDto.setUnitPrice(50.0);
        importItemRequestDto.setQuantity(2);
        importItemRequestDto.setConversionFactor(1);

        // 8. Prepare ImportDto with valid userId, supplierId, and import items
        ImportDto importRequestDto = new ImportDto();
        importRequestDto.setUserId(1L);
        importRequestDto.setSupplierId(1L);
        importRequestDto.setImportItems(Collections.singletonList(importItemRequestDto));
        importRequestDto.setTotalAmount(100.0);

        // 9. Mock importItemRepository.findByImportId to return a valid ImportItem
        ImportItem mockImportItem = new ImportItem();
        mockImportItem.setId(1L);
        mockImportItem.setProduct(mockProduct);
        mockImportItem.setQuantity(2);
        mockImportItem.setRemainingQuantity(2);
        when(importItemRepository.findByImportId(1L)).thenReturn(Collections.singletonList(mockImportItem));
        when(importItemRepository.findByImportId(null)).thenReturn(Collections.emptyList());

        // 10. Mock saveImportItems if it's a separate method (optional)
        // If saveImportItems is a private method, consider using partial mocking or refactoring for better testability
        doReturn(100.0).when(importServiceSpy).saveImportItems(any(), any(), any());

        // 11. Initialize and mock the MultipartFile for file upload
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("phieu.png");

        // 12. Mock CloudinaryService upload
        CloudinaryResponse mockCloudinaryResponse = new CloudinaryResponse();
        mockCloudinaryResponse.setUrl("image_url");
        when(cloudinaryService.upLoadFile(eq(file), anyString())).thenReturn(mockCloudinaryResponse);

        // 13. Mock inventoryHistoryRepository.save if used in updateImport
        // Assuming that inventoryHistoryRepository.save is called within updateImport
        when(inventoryHistoryRepository.save(any())).thenReturn(new InventoryHistory());

        // Act
        importServiceSpy.updateImport(1L, importRequestDto, file);

        // Assert
        verify(importRepository, times(1)).save(importMock);  // Verify that save was called on the importRepository
        assertEquals("image_url", importMock.getImage());  // Verify the image URL was set correctly
        verify(inventoryHistoryRepository, atLeastOnce()).save(any()); // Verify inventoryHistoryRepository.save was called
    }
}

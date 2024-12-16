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

import java.time.Instant;
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
        // 1. Mock người dùng hiện tại
        User authorizedUser = new User();
        authorizedUser.setId(1L); // Đặt ID cho người dùng
        authorizedUser.setRoles(Collections.singleton(new Role(ERole.ROLE_PRODUCT_OWNER.name())));

        // Tạo spy để mock phương thức getCurrentUser
        ImportServiceImpl importServiceSpy = Mockito.spy(importService);
        doReturn(authorizedUser).when(importServiceSpy).getCurrentUser();

        // 2. Mock phương thức importRepository.findById
        Import importMock = new Import();
        importMock.setId(1L); // Đảm bảo import có ID
        importMock.setStatus(OrderStatus.PENDING);
        importMock.setUser(authorizedUser);
        when(importRepository.findById(1L)).thenReturn(Optional.of(importMock));

        // 3. Mock phương thức supplierRepository.findById
        Supplier mockSupplier = new Supplier();
        mockSupplier.setId(1L); // Đặt ID cho nhà cung cấp
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mockSupplier));

        // 4. Mock phương thức userRepository.findById
        User mockUser = new User();
        mockUser.setId(1L); // Đặt ID cho người dùng
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        // 5. Mock phương thức productRepository.findById
        Product mockProduct = new Product();
        mockProduct.setId(1L);  // Đảm bảo sản phẩm có ID đúng
        mockProduct.setTotalQuantity(100); // Đảm bảo totalQuantity được đặt
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));

        // 6. Mock phương thức productUnitRepository.findByProductId
        ProductUnit mockProductUnit = new ProductUnit();
        mockProductUnit.setId(1L);
        mockProductUnit.setProduct(mockProduct);
        mockProductUnit.setConversionFactor(1); // Khởi tạo conversionFactor để tránh NPE
        when(productUnitRepository.findByProductId(1L)).thenReturn(Collections.singletonList(mockProductUnit));
        when(productUnitRepository.findByProductId(0L)).thenReturn(Collections.emptyList());
        // Loại bỏ trường hợp null vì thường không khuyến khích truyền null cho các kiểu dữ liệu nguyên thủy

        // 7. Chuẩn bị ImportItemRequestDto
        ImportItemRequestDto importItemRequestDto = new ImportItemRequestDto();
        importItemRequestDto.setProductId(1L); // Đặt ID sản phẩm hợp lệ
        importItemRequestDto.setUnitPrice(50.0); // Đặt giá đơn vị hợp lệ
        importItemRequestDto.setQuantity(2); // Đặt số lượng
        importItemRequestDto.setConversionFactor(1); // Đặt conversion factor nếu cần
        importItemRequestDto.setExpiryDate(Instant.now()); // **Đặt expiryDate hợp lệ**

        // 8. Chuẩn bị ImportDto
        ImportDto importRequestDto = new ImportDto();
        importRequestDto.setUserId(1L); // Đảm bảo userId được thiết lập
        importRequestDto.setSupplierId(1L); // Đảm bảo supplierId được thiết lập
        importRequestDto.setImportItems(Collections.singletonList(importItemRequestDto)); // Thiết lập sản phẩm nhập khẩu
        importRequestDto.setTotalAmount(100.0); // Đây là tổng số tiền

        // 9. Mock phương thức importItemRepository.findByImportId để trả về một ImportItem hợp lệ
        ImportItem importItem = new ImportItem();
        importItem.setId(1L);
        importItem.setProduct(mockProduct);
        importItem.setQuantity(2);
        importItem.setRemainingQuantity(2);
        when(importItemRepository.findByImportId(1L)).thenReturn(Collections.singletonList(importItem));
        when(importItemRepository.findByImportId(null)).thenReturn(Collections.emptyList()); // Xử lý trường hợp null nếu cần

        // 10. Mock phương thức saveImportItems nếu đây là một phương thức riêng
        // Giả sử saveImportItems là một phương thức trong ImportServiceImpl trả về một giá trị double
        // Nếu không, có thể bỏ qua dòng này
        doReturn(100.0).when(importServiceSpy).saveImportItems(any(), any(), any());

        // 11. Khởi tạo biến file
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("phieu.png");

        // 12. Mock phương thức cloudinaryService.upLoadFile
        CloudinaryResponse mockCloudinaryResponse = new CloudinaryResponse();
        mockCloudinaryResponse.setUrl("image_url");
        when(cloudinaryService.upLoadFile(eq(file), anyString())).thenReturn(mockCloudinaryResponse);

        // Act
        importServiceSpy.updateImport(1L, importRequestDto, file);

        // Assert
        verify(importRepository, times(1)).save(importMock); // Kiểm tra rằng phương thức save của importRepository đã được gọi 1 lần
        verify(importItemRepository, times(1)).findByImportId(1L); // Kiểm tra rằng phương thức findByImportId đã được gọi 1 lần
        // Có thể kiểm tra thêm các tương tác khác nếu cần thiết
    }


    // Test cases total amount mismatch
    @Test
    void UTCIU05() {
        // Arrange
        // 1. Mock người dùng hiện tại
        User authorizedUser = new User();
        authorizedUser.setId(1L); // Đặt ID cho người dùng
        authorizedUser.setRoles(Collections.singleton(new Role(ERole.ROLE_PRODUCT_OWNER.name())));

        // Tạo spy để mock phương thức getCurrentUser
        ImportServiceImpl importServiceSpy = Mockito.spy(importService);
        doReturn(authorizedUser).when(importServiceSpy).getCurrentUser();

        // 2. Mock phương thức importRepository.findById
        Import importMock = new Import();
        importMock.setId(1L); // Đảm bảo import có ID
        importMock.setStatus(OrderStatus.PENDING);
        importMock.setUser(authorizedUser);
        when(importRepository.findById(anyLong())).thenReturn(Optional.of(importMock));

        // 3. Mock phương thức supplierRepository.findById
        Supplier mockSupplier = new Supplier();
        mockSupplier.setId(1L); // Đặt ID cho nhà cung cấp
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mockSupplier));

        // 4. Mock phương thức userRepository.findById
        User mockUser = new User();
        mockUser.setId(1L); // Đặt ID cho người dùng
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        // 5. Mock repository sản phẩm để trả về sản phẩm hợp lệ
        Product mockProduct = new Product();
        mockProduct.setId(1L);  // Đảm bảo sản phẩm có ID đúng
        mockProduct.setTotalQuantity(100); // Đảm bảo totalQuantity được đặt
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(mockProduct));  // **Đã sửa Mocking**

        // 6. Mock phương thức productUnitRepository.findByProductId
        ProductUnit mockProductUnit = new ProductUnit();
        mockProductUnit.setId(1L);
        mockProductUnit.setProduct(mockProduct);
        mockProductUnit.setConversionFactor(1); // Khởi tạo conversionFactor để tránh NPE
        when(productUnitRepository.findByProductId(1L)).thenReturn(Collections.singletonList(mockProductUnit));
        when(productUnitRepository.findByProductId(0L)).thenReturn(Collections.emptyList());
        // Loại bỏ trường hợp null vì thường không khuyến khích truyền null cho các kiểu dữ liệu nguyên thủy

        // 7. Chuẩn bị ImportItemRequestDto
        ImportItemRequestDto importItemRequestDto = new ImportItemRequestDto();
        importItemRequestDto.setProductId(1L);  // Đặt ID sản phẩm hợp lệ
        importItemRequestDto.setUnitPrice(50.0);  // Đặt giá đơn vị hợp lệ
        importItemRequestDto.setQuantity(2);     // Đặt số lượng
        importItemRequestDto.setConversionFactor(1);  // Đặt conversion factor nếu cần
        importItemRequestDto.setExpiryDate(Instant.now()); // **Đặt expiryDate hợp lệ**

        // 8. Chuẩn bị ImportDto
        ImportDto importRequestDto = new ImportDto();
        importRequestDto.setUserId(1L); // Đảm bảo userId được thiết lập
        importRequestDto.setSupplierId(1L); // Đảm bảo supplierId được thiết lập
        importRequestDto.setImportItems(Collections.singletonList(importItemRequestDto)); // Thiết lập sản phẩm nhập khẩu
        importRequestDto.setTotalAmount(100.0);  // Đây là tổng số tiền dự kiến để kiểm tra sự không khớp

        // 9. Mock phương thức tính toán tổng số tiền cho các sản phẩm nhập khẩu
        // Phương thức này sẽ mock tổng số tiền trả về là 90.0 (thay vì 100.0)
        doReturn(90.0).when(importServiceSpy).calculateImportItemTotalAmount(any());

        // 10. Mock phương thức importItemRepository.findByImportId để trả về một số sản phẩm giả
        ImportItem mockImportItem = new ImportItem();
        mockImportItem.setProduct(mockProduct);  // Đặt sản phẩm cho ImportItem giả
        mockImportItem.setQuantity(2);  // Đặt dữ liệu mẫu
        mockImportItem.setRemainingQuantity(2); // Đảm bảo remainingQuantity được thiết lập
        when(importItemRepository.findByImportId(1L)).thenReturn(Collections.singletonList(mockImportItem));
        when(importItemRepository.findByImportId(null)).thenReturn(Collections.emptyList()); // Xử lý trường hợp null nếu cần

        // 11. Khởi tạo biến file
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("phieu.png");

        // 12. Mock phương thức cloudinaryService.upLoadFile
        CloudinaryResponse mockCloudinaryResponse = new CloudinaryResponse();
        mockCloudinaryResponse.setUrl("image_url");
        when(cloudinaryService.upLoadFile(eq(file), anyString())).thenReturn(mockCloudinaryResponse);

        // Act & Assert
        // Mong đợi một BadRequestException được ném ra do sự không khớp của tổng số tiền
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            importServiceSpy.updateImport(1L, importRequestDto, file);
        });

        // Kiểm tra thông điệp ngoại lệ
        assertEquals(Message.TOTAL_AMOUNT_NOT_MATCH, exception.getMessage());

        // Kiểm tra các phương thức cần thiết đã được gọi
        verify(importRepository, times(1)).findById(anyLong());
        verify(supplierRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).findById(anyLong()); // **Đã điều chỉnh để mong đợi findById**
        verify(importItemRepository, times(1)).findByImportId(1L);
        verify(inventoryHistoryRepository, atLeastOnce()).save(any()); // Đảm bảo inventoryHistoryRepository.save được gọi ít nhất một lần
    }

    //test case phiếu nhập không tồn tại
    @Test
    void UTCIU06() {
        // Arrange
        // 1. Mock người dùng hiện tại
        User authorizedUser = new User();
        authorizedUser.setId(1L); // Đặt ID cho người dùng
        authorizedUser.setRoles(Collections.singleton(new Role(ERole.ROLE_PRODUCT_OWNER.name())));

        // Tạo spy để mock phương thức getCurrentUser
        ImportServiceImpl importServiceSpy = Mockito.spy(importService);
        doReturn(authorizedUser).when(importServiceSpy).getCurrentUser();

        // 2. Mock phương thức importRepository.findById
        Import importMock = new Import();
        importMock.setId(1L); // Đảm bảo import có ID
        importMock.setStatus(OrderStatus.PENDING);
        importMock.setUser(authorizedUser);
        when(importRepository.findById(1L)).thenReturn(Optional.of(importMock));

        // 3. Mock phương thức supplierRepository.findById
        Supplier mockSupplier = new Supplier();
        mockSupplier.setId(1L); // Đặt ID cho nhà cung cấp
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mockSupplier));

        // 4. Mock phương thức userRepository.findById
        User mockUser = new User();
        mockUser.setId(1L); // Đặt ID cho người dùng
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        // 5. Mock phương thức productRepository.findById
        Product mockProduct = new Product();
        mockProduct.setId(1L);  // Đảm bảo sản phẩm có ID đúng
        mockProduct.setTotalQuantity(100); // Đặt totalQuantity để tránh NPE
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));

        // 6. Mock phương thức productUnitRepository.findByProductId
        ProductUnit mockProductUnit = new ProductUnit();
        mockProductUnit.setId(1L);
        mockProductUnit.setProduct(mockProduct);
        mockProductUnit.setConversionFactor(1); // Đặt conversionFactor để tránh NPE
        when(productUnitRepository.findByProductId(1L)).thenReturn(Collections.singletonList(mockProductUnit));
        when(productUnitRepository.findByProductId(0L)).thenReturn(Collections.emptyList());
        // Loại bỏ trường hợp null vì thường không khuyến khích truyền null cho các kiểu dữ liệu nguyên thủy

        // 7. Chuẩn bị ImportItemRequestDto với các trường hợp hợp lệ
        ImportItemRequestDto importItemRequestDto = new ImportItemRequestDto();
        importItemRequestDto.setProductId(1L); // Đặt ID sản phẩm hợp lệ
        importItemRequestDto.setUnitPrice(50.0); // Đặt giá đơn vị hợp lệ
        importItemRequestDto.setQuantity(2); // Đặt số lượng
        importItemRequestDto.setConversionFactor(1); // Đặt conversion factor nếu cần
        importItemRequestDto.setExpiryDate(Instant.now()); // **Đặt expiryDate hợp lệ**

        // 8. Chuẩn bị ImportDto với userId, supplierId và các item nhập khẩu hợp lệ
        ImportDto importRequestDto = new ImportDto();
        importRequestDto.setUserId(1L); // Đảm bảo userId được thiết lập
        importRequestDto.setSupplierId(1L); // Đảm bảo supplierId được thiết lập
        importRequestDto.setImportItems(Collections.singletonList(importItemRequestDto)); // Thiết lập item nhập khẩu
        importRequestDto.setTotalAmount(100.0); // Đây là tổng số tiền

        // 9. Mock phương thức importItemRepository.findByImportId để trả về một ImportItem hợp lệ
        ImportItem mockImportItem = new ImportItem();
        mockImportItem.setId(1L);
        mockImportItem.setProduct(mockProduct);
        mockImportItem.setQuantity(2);
        mockImportItem.setRemainingQuantity(2);
        when(importItemRepository.findByImportId(1L)).thenReturn(Collections.singletonList(mockImportItem));
        when(importItemRepository.findByImportId(null)).thenReturn(Collections.emptyList());

        // 10. Mock phương thức saveImportItems nếu đây là một phương thức riêng (tuỳ chọn)
        // Nếu saveImportItems là phương thức private, bạn có thể sử dụng partial mocking hoặc tái cấu trúc lại mã để kiểm tra dễ dàng hơn
        doReturn(100.0).when(importServiceSpy).saveImportItems(any(), any(), any());

        // 11. Khởi tạo và mock MultipartFile để tải lên file
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false); // Đảm bảo file không rỗng
        when(file.getOriginalFilename()).thenReturn("phieu.png"); // Đặt tên file

        // 12. Mock phương thức CloudinaryService.upload
        CloudinaryResponse mockCloudinaryResponse = new CloudinaryResponse();
        mockCloudinaryResponse.setUrl("image_url"); // Đặt URL trả về cho hình ảnh
        when(cloudinaryService.upLoadFile(eq(file), anyString())).thenReturn(mockCloudinaryResponse);

        // 13. Mock phương thức inventoryHistoryRepository.save nếu nó được gọi trong updateImport
        // Giả sử inventoryHistoryRepository.save được gọi trong updateImport
        when(inventoryHistoryRepository.save(any())).thenReturn(new InventoryHistory());

        // Act
        importServiceSpy.updateImport(1L, importRequestDto, file);

        // Assert
        verify(importRepository, times(1)).save(importMock);  // Kiểm tra rằng phương thức save đã được gọi trên importRepository
        assertEquals("image_url", importMock.getImage());  // Kiểm tra rằng URL hình ảnh đã được thiết lập đúng
        verify(inventoryHistoryRepository, atLeastOnce()).save(any()); // Kiểm tra rằng phương thức inventoryHistoryRepository.save đã được gọi ít nhất 1 lần
    }
}

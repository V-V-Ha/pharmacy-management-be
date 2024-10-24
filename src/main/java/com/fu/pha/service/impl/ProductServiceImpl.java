package com.fu.pha.service.impl;

import com.fu.pha.convert.GenerateCode;
import com.fu.pha.dto.request.ProductDTORequest;
import com.fu.pha.dto.request.ProductUnitDTORequest;
import com.fu.pha.dto.request.UnitDto;
import com.fu.pha.dto.response.CloudinaryResponse;
import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.entity.*;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.MaxUploadSizeExceededException;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.CategoryRepository;
import com.fu.pha.repository.ProductRepository;
import com.fu.pha.repository.ProductUnitRepository;
import com.fu.pha.repository.UnitRepository;
import com.fu.pha.service.CloudinaryService;
import com.fu.pha.service.ProductService;
import com.fu.pha.util.FileUploadUtil;
import com.fu.pha.validate.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private ProductUnitRepository productUnitRepository;

    @Autowired
    GenerateCode generateCode;

    @Autowired
    CloudinaryService cloudinaryService;

    @Transactional
    @Override
    public void createProduct(ProductDTORequest productDTORequest, MultipartFile file) {

        //validate the request
        checkValidateProduct(productDTORequest);

        //check product code and registration number exist
        Optional<Product> registrationNumber = productRepository.findByRegistrationNumber(productDTORequest.getRegistrationNumber());
        if (registrationNumber.isPresent()) {
            throw new BadRequestException(Message.EXIST_REGISTRATION_NUMBER);
        }

        Category category = categoryRepository.findById(productDTORequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.CATEGORY_NOT_FOUND));

        Product product = new Product();
        product.setProductName(productDTORequest.getProductName());
        product.setCategoryId(category);
        product.setRegistrationNumber(productDTORequest.getRegistrationNumber());
        product.setActiveIngredient(productDTORequest.getActiveIngredient());
        product.setDosageConcentration(productDTORequest.getDosageConcentration());
        product.setPackingMethod(productDTORequest.getPackingMethod());
        product.setManufacturer(productDTORequest.getManufacturer());
        product.setCountryOfOrigin(productDTORequest.getCountryOfOrigin());

        if (productRepository.getLastProductCode() == null) {
            product.setProductCode("SP00001");
        } else {
            product.setProductCode(generateCode.generateNewProductCode(productRepository.getLastProductCode()));
        }
//        product.setProductCode(productDTORequest.getProductCode());
        product.setIndication(productDTORequest.getIndication());
        product.setContraindication(productDTORequest.getContraindication());
        product.setSideEffect(productDTORequest.getSideEffect());
        product.setDosageForms(productDTORequest.getDosageForms());
        product.setDescription(productDTORequest.getDescription());
        product.setPrescriptionDrug(productDTORequest.getPrescriptionDrug());
        // Upload the image product if there is a file
        if (file != null && !file.isEmpty()) {
            String imageProduct = uploadImage(file);
            product.setImageProduct(imageProduct);
        }

        productRepository.save(product);
        List<ProductUnit> productUnitList = new ArrayList<>();
        for (ProductUnitDTORequest productUnitDTORequest : productDTORequest.getProductUnitListDTO()) {
            Unit unit = unitRepository.findUnitById(productUnitDTORequest.getUnitId());
            ProductUnit productUnit = new ProductUnit();
            productUnit.setProductId(product);
            productUnit.setUnitId(unit);
            productUnit.setConversionFactor(productUnitDTORequest.getConversionFactor());
            productUnit.setImportPrice(productUnitDTORequest.getImportPrice());
            productUnit.setRetailPrice(productUnitDTORequest.getRetailPrice());
            productUnitList.add(productUnit);
        }
        productUnitRepository.saveAll(productUnitList);
    }


    @Override
    @Transactional
    public void updateProduct(ProductDTORequest productDTORequest, MultipartFile file) {
        //validate the request
        checkValidateProduct(productDTORequest);

        //check product exist
        Optional<Product> productOptional = productRepository.getProductById(productDTORequest.getId());
        Product product = productOptional.orElseThrow(() -> new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND));

        //check product code and registration number exist
        Optional<Product> registrationNumberExist = productRepository.findByRegistrationNumber(productDTORequest.getRegistrationNumber());
        if (registrationNumberExist.isPresent() && !registrationNumberExist.get().getId().equals(productDTORequest.getId())) {
            throw new BadRequestException(Message.EXIST_REGISTRATION_NUMBER);
        }

        Category category = categoryRepository.findById(productDTORequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.CATEGORY_NOT_FOUND));

        product.setProductName(productDTORequest.getProductName());
        product.setCategoryId(category);
        product.setRegistrationNumber(productDTORequest.getRegistrationNumber());
        product.setActiveIngredient(productDTORequest.getActiveIngredient());
        product.setDosageConcentration(productDTORequest.getDosageConcentration());
        product.setPackingMethod(productDTORequest.getPackingMethod());
        product.setManufacturer(productDTORequest.getManufacturer());
        product.setCountryOfOrigin(productDTORequest.getCountryOfOrigin());
        product.setIndication(productDTORequest.getIndication());
        product.setContraindication(productDTORequest.getContraindication());
        product.setSideEffect(productDTORequest.getSideEffect());
        product.setDosageForms(productDTORequest.getDosageForms());
        product.setDescription(productDTORequest.getDescription());
        product.setPrescriptionDrug(productDTORequest.getPrescriptionDrug());

        // Upload the image product if there is a file
        if (file != null && !file.isEmpty()) {
            String imageProduct = uploadImage(file);
            product.setImageProduct(imageProduct);
        }
        productRepository.save(product);
        if (productDTORequest.getProductUnitListDTO() != null) {
            List<ProductUnit> productUnitList = product.getProductUnitList();

            for (ProductUnitDTORequest productUnitDTORequest : productDTORequest.getProductUnitListDTO()) {

                // Check if the product unit exists
                ProductUnit productUnit = productUnitRepository.findProductUnitsByIdAndProductId(productUnitDTORequest.getId(),
                        productDTORequest.getId());

                if (productUnit != null) {
                    // Update product unit
                    productUnit.setUnitId(unitRepository.findUnitById(productUnitDTORequest.getUnitId()));
                    productUnit.setConversionFactor(productUnitDTORequest.getConversionFactor());
                    productUnit.setImportPrice(productUnitDTORequest.getImportPrice());
                    productUnit.setRetailPrice(productUnitDTORequest.getRetailPrice());
                    productUnitRepository.save(productUnit);
                } else {
                    // Create a new product unit
                    productUnit = new ProductUnit();
                    Unit unit = unitRepository.findUnitById(productUnitDTORequest.getUnitId());
                    product = productRepository.findProductById(productDTORequest.getId());
                    productUnit.setUnitId(unit);
                    productUnit.setProductId(product);
                    productUnit.setConversionFactor(productUnitDTORequest.getConversionFactor());
                    productUnit.setImportPrice(productUnitDTORequest.getImportPrice());
                    productUnit.setRetailPrice(productUnitDTORequest.getRetailPrice());
                    productUnitRepository.save(productUnit);
                }

                // Add the product unit to the list
                productUnitList.add(productUnit);
            }

            product.setProductUnitList(productUnitList);
            productRepository.save(product);
        }
    }

    private void checkValidateProduct(ProductDTORequest productDTORequest){
        if(productDTORequest.getProductName().isEmpty() || productDTORequest.getCategoryId() == null ||
                productDTORequest.getRegistrationNumber().isEmpty() || productDTORequest.getActiveIngredient().isEmpty() ||
                productDTORequest.getDosageConcentration().isEmpty() || productDTORequest.getPackingMethod().isEmpty() ||
                productDTORequest.getManufacturer().isEmpty() || productDTORequest.getCountryOfOrigin().isEmpty() ||
                productDTORequest.getDosageForms().isEmpty()){
            throw new BadRequestException(Message.NULL_FILED);
        }

    }

    public String uploadImage( final MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException(Message.EMPTY_FILE);
        }

        if (!FileUploadUtil.isAllowedExtension(file.getOriginalFilename(), FileUploadUtil.IMAGE_PATTERN)) {
            throw new BadRequestException(Message.INVALID_FILE);
        }

        if (file.getSize() > FileUploadUtil.MAX_FILE_SIZE) {
            throw new MaxUploadSizeExceededException(Message.INVALID_FILE_SIZE);
        }



        String customFileName = UUID.randomUUID() + "image";

        CloudinaryResponse cloudinaryResponse = cloudinaryService.upLoadFile(file, FileUploadUtil.getFileName(customFileName));

        return cloudinaryResponse.getUrl();
    }

    @Override
    public Page<ProductDTOResponse> getAllProductPaging(int page, int size,  String productName, String category) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTOResponse> products = productRepository.getListProductPaging(productName, category, pageable);
        if (products.isEmpty()) {
            throw new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND);
        }
        return products;
    }

    @Override
    public ProductDTOResponse getProductById(Long id) {
        Product product = productRepository.getProductById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND));
        return new ProductDTOResponse(product);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.getProductById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND));

        product.setDeleted(true);
        productRepository.save(product);
    }

    @Override
    public List<UnitDto> getAllUnit() {
        List<Unit> units = unitRepository.findAll();
        List<UnitDto> unitDtos = new ArrayList<>();
        for (Unit unit : units) {
            UnitDto unitDto = new UnitDto();
            unitDto.setId(unit.getId());
            unitDto.setUnitName(unit.getUnitName());
            unitDtos.add(unitDto);
        }
        return unitDtos;
    }
}

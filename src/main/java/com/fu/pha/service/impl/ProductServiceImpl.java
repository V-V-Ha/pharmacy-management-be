package com.fu.pha.service.impl;

import com.fu.pha.convert.GenerateCode;
import com.fu.pha.dto.request.ProductDTORequest;
import com.fu.pha.dto.request.ProductUnitDTORequest;
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
        Optional<Product> productCode = productRepository.findByProductCode(productDTORequest.getProductCode());
        Optional<Product> registrationNumber = productRepository.findByRegistrationNumber(productDTORequest.getRegistrationNumber());
        if (productCode.isPresent() || registrationNumber.isPresent()) {
            String errorMessage = productCode.isPresent() ? Message.EXIST_PRODUCT_CODE :
                    Message.EXIST_REGISTRATION_NUMBER;
            throw new BadRequestException(errorMessage);
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
        for (ProductUnitDTORequest productUnitDTORequest : productDTORequest.getProductUnitList()) {
            Unit unit = unitRepository.findByUnitName(productUnitDTORequest.getUnitName());
            ProductUnit productUnit = new ProductUnit();
            productUnit.setProductId(product);
            productUnit.setUnitId(unit);
            productUnit.setConversionFactor(productUnitDTORequest.getConversionFactor());

            productUnit.setRetailPrice(productUnitDTORequest.getRetailPrice());
            productUnitList.add(productUnit);
        }
        productUnitRepository.saveAll(productUnitList);
    }


    @Override
    @Transactional
    public void updateProduct(ProductDTORequest request, MultipartFile file) {
        //validate the request
        checkValidateProduct(request);

        //check product exist
        Optional<Product> productOptional = productRepository.getProductById(request.getId());
        Product product = productOptional.orElseThrow(() -> new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND));

        //check product code and registration number exist
        Optional<Product> productCodeExist = productRepository.findByProductCode(request.getProductCode());
        Optional<Product> registrationNumberExist = productRepository.findByRegistrationNumber(request.getRegistrationNumber());
        if ((productCodeExist.isPresent() && !productCodeExist.get().getId().equals(request.getId())) ||
                (registrationNumberExist.isPresent() && !registrationNumberExist.get().getId().equals(request.getId()))) {
            String errorMessage = productCodeExist.isPresent() ? Message.EXIST_PRODUCT_CODE : Message.EXIST_REGISTRATION_NUMBER;
            throw new BadRequestException(errorMessage);
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.CATEGORY_NOT_FOUND));

        product.setProductName(request.getProductName());
        product.setCategoryId(category);
        product.setRegistrationNumber(request.getRegistrationNumber());
        product.setActiveIngredient(request.getActiveIngredient());
        product.setDosageConcentration(request.getDosageConcentration());
        product.setPackingMethod(request.getPackingMethod());
        product.setManufacturer(request.getManufacturer());
        product.setCountryOfOrigin(request.getCountryOfOrigin());
        product.setProductCode(request.getProductCode());
        product.setIndication(request.getIndication());
        product.setContraindication(request.getContraindication());
        product.setSideEffect(request.getSideEffect());
        product.setDosageForms(request.getDosageForms());
        product.setDescription(request.getDescription());
        product.setPrescriptionDrug(request.getPrescriptionDrug());

        // Upload the image product if there is a file
        if (file != null && !file.isEmpty()) {
            String imageProduct = uploadImage(file);
            product.setImageProduct(imageProduct);
        }
        productRepository.save(product);
        List<ProductUnit> productUnitList = new ArrayList<>();
        for (ProductUnitDTORequest productUnitDTORequest : request.getProductUnitList()) {
            Unit unit = unitRepository.findByUnitName(productUnitDTORequest.getUnitName());
            ProductUnit productUnit = new ProductUnit();
            productUnit.setProductId(product);
            productUnit.setUnitId(unit);
            productUnit.setConversionFactor(productUnitDTORequest.getConversionFactor());

            productUnit.setRetailPrice(productUnitDTORequest.getRetailPrice());
            productUnitList.add(productUnit);
        }
        productUnitRepository.saveAll(productUnitList);
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


}

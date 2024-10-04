package com.fu.pha.service.impl;

import com.fu.pha.dto.request.ProductDTORequest;
import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.entity.Category;
import com.fu.pha.entity.Product;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.CategoryRepository;
import com.fu.pha.repository.ProductRepository;
import com.fu.pha.service.ProductService;
import com.fu.pha.validate.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    Validate validate;

    @Override
    public void createProduct(ProductDTORequest productDTORequest) {

        checkValidateProduct(productDTORequest);

        if (productRepository.existsByProductCode(productDTORequest.getProductCode())){
            throw new BadRequestException(Message.EXIST_PRODUCT_CODE);
        }
        if (productRepository.existsByRegistrationNumber(productDTORequest.getRegistrationNumber())){
            throw new BadRequestException(Message.EXIST_REGISTRATION_NUMBER);
        }

        Optional<Category> categoryOptional = categoryRepository.getCategoryByCategoryName(productDTORequest.getCategoryId());
        Category category = categoryOptional.orElseThrow(() -> new ResourceNotFoundException(Message.CATEGORY_NOT_FOUND));

        Product product = new Product();
        product.setProductName(productDTORequest.getProductName());
        product.setCategoryId(category);
        product.setRegistrationNumber(productDTORequest.getRegistrationNumber());
        product.setActiveIngredient(productDTORequest.getActiveIngredient());
        product.setDosageConcentration(productDTORequest.getDosageConcentration());
        product.setPackingMethod(productDTORequest.getPackingMethod());
        product.setManufacturer(productDTORequest.getManufacturer());
        product.setCountryOfOrigin(productDTORequest.getCountryOfOrigin());
        product.setUnit(productDTORequest.getUnit());
        product.setImportPrice(productDTORequest.getImportPrice());
        product.setProductCode(productDTORequest.getProductCode());
        product.setIndication(productDTORequest.getIndication());
        product.setContraindication(productDTORequest.getContraindication());
        product.setSideEffect(productDTORequest.getSideEffect());
        product.setDosageForms(productDTORequest.getDosageForms());
        product.setDescription(productDTORequest.getDescription());
        product.setImageProduct(productDTORequest.getImageProduct());
        product.setCreateDate(Instant.now());
        product.setCreateBy(SecurityContextHolder.getContext().getAuthentication().getName());
        product.setLastModifiedDate(Instant.now());
        product.setLastModifiedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        productRepository.save(product);
    }


    @Override
    public void updateProduct(ProductDTORequest request) {

        checkValidateProduct(request);

        Product product = productRepository.getProductById(request.getId());
        if (product == null) {
            throw new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND);
        }
        Product productCode = productRepository.getProductByProductCode(request.getProductCode());
        Product registrationNumber = productRepository.getProductByRegistrationNumber(request.getRegistrationNumber());
        if (productCode != null && !productCode.equals(product))
            throw new BadRequestException(Message.EXIST_PRODUCT_CODE);
        if (registrationNumber != null && !registrationNumber.equals(product))
            throw new BadRequestException(Message.EXIST_REGISTRATION_NUMBER);

        Optional<Category> categoryOptional = categoryRepository.getCategoryByCategoryName(request.getCategoryId());
        Category category = categoryOptional.orElseThrow(() -> new ResourceNotFoundException(Message.CATEGORY_NOT_FOUND));

        product.setProductName(request.getProductName());
        product.setCategoryId(category);
        product.setRegistrationNumber(request.getRegistrationNumber());
        product.setActiveIngredient(request.getActiveIngredient());
        product.setDosageConcentration(request.getDosageConcentration());
        product.setPackingMethod(request.getPackingMethod());
        product.setManufacturer(request.getManufacturer());
        product.setCountryOfOrigin(request.getCountryOfOrigin());
        product.setUnit(request.getUnit());
        product.setImportPrice(request.getImportPrice());
        product.setProductCode(request.getProductCode());
        product.setIndication(request.getIndication());
        product.setContraindication(request.getContraindication());
        product.setSideEffect(request.getSideEffect());
        product.setDosageForms(request.getDosageForms());
        product.setDescription(request.getDescription());
        product.setImageProduct(request.getImageProduct());
        product.setLastModifiedDate(Instant.now());
        product.setLastModifiedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        productRepository.save(product);
    }

    private void checkValidateProduct(ProductDTORequest productDTORequest){
        if(productDTORequest.getProductName() == null || productDTORequest.getCategoryId() == null ||
                productDTORequest.getRegistrationNumber() == null || productDTORequest.getActiveIngredient() == null ||
                productDTORequest.getDosageConcentration() == null || productDTORequest.getPackingMethod() == null ||
                productDTORequest.getManufacturer() == null || productDTORequest.getCountryOfOrigin() == null ||
                productDTORequest.getUnit() == null || productDTORequest.getImportPrice() == null ||
                productDTORequest.getProductCode() == null || productDTORequest.getDosageForms() == null){
            throw new BadRequestException(Message.NULL_FILED);
        }

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
        Product product = productRepository.getProductById(id);
        if (product == null) {
            throw new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND);
        }
        return new ProductDTOResponse(product);
    }
}

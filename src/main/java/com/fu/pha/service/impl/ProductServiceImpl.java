package com.fu.pha.service.impl;

import com.fu.pha.dto.request.ProductDTORequest;
import com.fu.pha.dto.response.MessageResponse;
import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.entity.Category;
import com.fu.pha.entity.Product;
import com.fu.pha.exception.Message;
import com.fu.pha.repository.CategoryRepository;
import com.fu.pha.repository.ProductRepository;
import com.fu.pha.service.ProductService;
import com.fu.pha.validate.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    Validate validate;

    @Override
    public ResponseEntity<Object> createProduct(ProductDTORequest productDTORequest) {

        if(!validate.validateProduct(productDTORequest,"create").getStatusCode().equals(HttpStatus.OK))
            return validate.validateProduct(productDTORequest,"create");

        Category category = categoryRepository.getCategoryByCategoryName(productDTORequest.getCategoryId());

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
        product.setCreateDate(Instant.now());
        product.setCreateBy(SecurityContextHolder.getContext().getAuthentication().getName());
        product.setLastModifiedDate(Instant.now());
        product.setLastModifiedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        productRepository.save(product);
        return ResponseEntity.status(HttpStatus.OK).body(new MessageResponse(Message.PRODUCT_SUCCESS, HttpStatus.OK.value()));
    }

    @Override
    public ResponseEntity<Object> getAllProductPaging(int size, int index,  String productName, String category) {
        Pageable pageable = PageRequest.of(index - 1, size);
        Page<ProductDTOResponse> products = productRepository.getListProductPaging(productName, category, pageable);
        if (products.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(Message.PRODUCT_NOT_FOUND, HttpStatus.NOT_FOUND.value()));
        }
        return ResponseEntity.ok(products);
    }





}

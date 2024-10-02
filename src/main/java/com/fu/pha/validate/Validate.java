package com.fu.pha.validate;

import com.fu.pha.dto.request.ProductDTORequest;
import com.fu.pha.dto.request.UserDto;
import com.fu.pha.dto.response.MessageResponse;
import com.fu.pha.entity.Product;
import com.fu.pha.entity.User;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.ProductRepository;
import com.fu.pha.repository.RoleRepository;
import com.fu.pha.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Optional;

@Component
public class Validate {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    ProductRepository productRepository;


    public boolean checkUserAge(UserDto userDto) {
        LocalDate birthDate = userDto.getDob().atZone(ZoneId.systemDefault()).toLocalDate();
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        return age >= 18;
    }




    public ResponseEntity<Object> validateProduct(ProductDTORequest productDTORequest, String option) {
        if (productDTORequest.getProductName() == null || productDTORequest.getCategoryId() == null ||
                productDTORequest.getRegistrationNumber() == null || productDTORequest.getActiveIngredient() == null ||
                productDTORequest.getDosageConcentration() == null || productDTORequest.getPackingMethod() == null ||
                productDTORequest.getManufacturer() == null || productDTORequest.getCountryOfOrigin() == null ||
                productDTORequest.getUnit() == null || productDTORequest.getImportPrice() == null ||
                productDTORequest.getProductCode() == null || productDTORequest.getDosageForms() == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Message.NULL_FILED);}
        if (option.equals("create")) {
            if (productRepository.existsByProductCode(productDTORequest.getProductCode()))
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Message.EXIST_PRODUCT_CODE, HttpStatus.BAD_REQUEST.value()));
            if (productRepository.existsByRegistrationNumber(productDTORequest.getRegistrationNumber()))
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Message.EXIST_REGISTRATION_NUMBER, HttpStatus.BAD_REQUEST.value()));
        } else if (option.equals("update")) {
            Product product = productRepository.getProductById(productDTORequest.getId());
            if (product == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Message.PRODUCT_NOT_FOUND, HttpStatus.BAD_REQUEST.value()));
            }
            Product productCode = productRepository.getProductByProductCode(productDTORequest.getProductCode());
            Product registrationNumber = productRepository.getProductByRegistrationNumber(productDTORequest.getRegistrationNumber());
            if (productCode != null && !productCode.equals(product))
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Message.EXIST_PRODUCT_CODE, HttpStatus.BAD_REQUEST.value()));
            if (registrationNumber != null && !registrationNumber.equals(product))
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Message.EXIST_REGISTRATION_NUMBER, HttpStatus.BAD_REQUEST.value()));
        }
        return ResponseEntity.status(HttpStatus.OK).body(Message.VALID_INFORMATION);
    }
}

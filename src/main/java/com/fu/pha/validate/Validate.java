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


    public boolean validateUser(UserDto userDto, String option) throws BadRequestException, ResourceNotFoundException {
        if (userDto.getFullName() == null || userDto.getEmail() == null || userDto.getPhone() == null ||
                userDto.getDob() == null || userDto.getAddress() == null ||
                userDto.getGender() == null || userDto.getCic() == null || userDto.getUsername() == null ||
                userDto.getPassword() == null || userDto.getRolesDto() == null || userDto.getStatus() == null) {
            throw new BadRequestException(Message.NULL_FILED);
        }
        if (!checkUserAge(userDto)) {
            throw new BadRequestException(new MessageResponse(Message.INVALID_AGE, HttpStatus.BAD_REQUEST.value()).toString());
        }
        if (!userDto.getFullName().matches(Constants.REGEX_NAME)) {
            throw new BadRequestException(new MessageResponse(Message.INVALID_NAME, HttpStatus.BAD_REQUEST.value()).toString());
        }
        if (!userDto.getEmail().matches(Constants.REGEX_GMAIL)) {
            throw new BadRequestException(new MessageResponse(Message.INVALID_GMAIL, HttpStatus.BAD_REQUEST.value()).toString());
        }
        if (!userDto.getPhone().matches(Constants.REGEX_PHONE)) {
            throw new BadRequestException(new MessageResponse(Message.INVALID_PHONE, HttpStatus.BAD_REQUEST.value()).toString());
        }
        if (!userDto.getAddress().matches(Constants.REGEX_ADDRESS)) {
            throw new BadRequestException(new MessageResponse(Message.INVALID_ADDRESS, HttpStatus.BAD_REQUEST.value()).toString());
        }
        if (!userDto.getCic().matches(Constants.REGEX_CCCD)) {
            throw new BadRequestException(new MessageResponse(Message.INVALID_CCCD, HttpStatus.BAD_REQUEST.value()).toString());
        }
        if (!userDto.getUsername().matches(Constants.REGEX_USER_NAME)) {
            throw new BadRequestException(new MessageResponse(Message.INVALID_USERNAME_C, HttpStatus.BAD_REQUEST.value()).toString());
        }
        if (!userDto.getPassword().matches(Constants.REGEX_PASSWORD)) {
            throw new BadRequestException(new MessageResponse(Message.INVALID_PASSWORD, HttpStatus.BAD_REQUEST.value()).toString());
        }
        if (option.equals("create")) {
            if (userRepository.existsByUsername(userDto.getUsername())) {
                throw new BadRequestException(new MessageResponse(Message.EXIST_USERNAME, HttpStatus.BAD_REQUEST.value()).toString());
            }
            if (userRepository.existsByEmail(userDto.getEmail())) {
                throw new BadRequestException(new MessageResponse(Message.EXIST_EMAIL, HttpStatus.BAD_REQUEST.value()).toString());
            }
            if (userRepository.getUserByCic(userDto.getCic()) != null) {
                throw new BadRequestException(new MessageResponse(Message.EXIST_CCCD, HttpStatus.BAD_REQUEST.value()).toString());
            }
            if (userRepository.getUserByPhone(userDto.getPhone()) != null) {
                throw new BadRequestException(new MessageResponse(Message.EXIST_PHONE, HttpStatus.BAD_REQUEST.value()).toString());
            }
        } else if (option.equals("update")) {
            User user = userRepository.getUserById(userDto.getId());
            if (user == null) {
                throw new ResourceNotFoundException(new MessageResponse(Message.USER_NOT_FOUND, HttpStatus.BAD_REQUEST.value()).toString());
            }
            User emailUser = userRepository.getUserByEmail(userDto.getEmail());
            User phoneUser = userRepository.getUserByPhone(userDto.getPhone());
            User cicUser = userRepository.getUserByCic(userDto.getCic());
            Optional<User> usernameUser = userRepository.findByUsername(userDto.getUsername());

            if (emailUser != null && !emailUser.equals(user)) {
                throw new BadRequestException(new MessageResponse(Message.EXIST_EMAIL, HttpStatus.BAD_REQUEST.value()).toString());
            }
            if (phoneUser != null && !phoneUser.equals(user)) {
                throw new BadRequestException(new MessageResponse(Message.EXIST_PHONE, HttpStatus.BAD_REQUEST.value()).toString());
            }
            if (cicUser != null && !cicUser.equals(user)) {
                throw new BadRequestException(new MessageResponse(Message.EXIST_CCCD, HttpStatus.BAD_REQUEST.value()).toString());
            }
            if (usernameUser.isPresent() && !usernameUser.get().equals(user)) {
                throw new BadRequestException(new MessageResponse(Message.EXIST_USERNAME, HttpStatus.BAD_REQUEST.value()).toString());
            }
        }
        return true;
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

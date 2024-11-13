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

}

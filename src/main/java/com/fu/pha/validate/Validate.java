package com.fu.pha.validate;

import com.fu.pha.dto.request.UserDto;
import com.fu.pha.dto.response.MessageResponse;
import com.fu.pha.entity.User;
import com.fu.pha.exception.Message;
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


    public boolean checkUserAge(UserDto userDto){
        LocalDate birthDate = userDto.getDob().atZone(ZoneId.systemDefault()).toLocalDate();
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        return age >= 18;
    }


    public ResponseEntity<Object> validateUser(UserDto userDto,String option){
        if (userDto.getFullName() == null || userDto.getEmail() == null || userDto.getPhone() == null ||
                userDto.getDob() == null || userDto.getAddress() == null ||
                userDto.getGender() == null || userDto.getCic() == null || userDto.getUsername() == null ||
                userDto.getPassword() == null || userDto.getRolesDto() == null || userDto.getStatus() == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Message.NULL_FILED);
        if(!checkUserAge(userDto))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Message.INVALID_AGE, HttpStatus.BAD_REQUEST.value()));
        if (!userDto.getFullName().matches(Constants.REGEX_NAME))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Message.INVALID_NAME, HttpStatus.BAD_REQUEST.value()));
        if (!userDto.getEmail().matches(Constants.REGEX_GMAIL))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Message.INVALID_GMAIL, HttpStatus.BAD_REQUEST.value()));
        if (!userDto.getPhone().matches(Constants.REGEX_PHONE))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Message.INVALID_PHONE, HttpStatus.BAD_REQUEST.value()));
        if (!userDto.getAddress().matches(Constants.REGEX_ADDRESS))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Message.INVALID_ADDRESS, HttpStatus.BAD_REQUEST.value()));
        if (!userDto.getCic().matches(Constants.REGEX_CCCD))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Message.INVALID_CCCD, HttpStatus.BAD_REQUEST.value()));
        if (!userDto.getUsername().matches(Constants.REGEX_USER_NAME))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Message.INVALID_USERNAME_C, HttpStatus.BAD_REQUEST.value()));
        if (!userDto.getPassword().matches(Constants.REGEX_PASSWORD))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Message.INVALID_PASSWORD, HttpStatus.BAD_REQUEST.value()));
        if(option.equals("create")) {
            if (userRepository.existsByUsername(userDto.getUsername()))
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Message.EXIST_USERNAME, HttpStatus.BAD_REQUEST.value()));
            if (userRepository.existsByEmail(userDto.getEmail()))
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Message.EXIST_EMAIL, HttpStatus.BAD_REQUEST.value()));
            if (userRepository.getUserByCic(userDto.getCic()) != null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Message.EXIST_CCCD, HttpStatus.BAD_REQUEST.value()));
            if (userRepository.getUserByPhone(userDto.getPhone()) != null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Message.EXIST_PHONE, HttpStatus.BAD_REQUEST.value()));
        } else if (option.equals("update")) {
            User user = userRepository.getUserById(userDto.getId());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Message.USER_NOT_FOUND, HttpStatus.BAD_REQUEST.value()));
            }
            User emailUser = userRepository.getUserByEmail(userDto.getEmail());
            User phoneUser = userRepository.getUserByPhone(userDto.getPhone());
            User cicUser = userRepository.getUserByCic(userDto.getCic());
            Optional<User> usernameUser = userRepository.findByUsername(userDto.getUsername());

            if (emailUser != null && !emailUser.equals(user))
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Message.EXIST_EMAIL, HttpStatus.BAD_REQUEST.value()));
            if (phoneUser != null && !phoneUser.equals(user))
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Message.EXIST_PHONE, HttpStatus.BAD_REQUEST.value()));
            if (cicUser != null && !cicUser.equals(user))
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Message.EXIST_CCCD, HttpStatus.BAD_REQUEST.value()));
            if (usernameUser.isPresent() && !usernameUser.get().equals(user))
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(Message.EXIST_USERNAME, HttpStatus.BAD_REQUEST.value()));
        }
        return ResponseEntity.status(HttpStatus.OK).body(Message.VALID_INFORMATION);
    }
}

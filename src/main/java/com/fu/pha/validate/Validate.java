package com.fu.pha.validate;

import com.fu.pha.dto.request.UserDto;
import org.springframework.http.HttpStatus;
import com.fu.pha.exception.ResponseMessage;
import org.springframework.http.ResponseEntity;

public class Validate {
    public ResponseEntity<Object> validateUser(UserDto userDto, String option){

        if(userDto.getFullName() == null | userDto.getEmail() == null | userDto.getPhone() == null |
        userDto.getGender() == null | userDto.getAddress() == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseMessage.NULL_FILED);

        }
        if (!userDto.getFullName().matches(Constants.REGEX_NAME))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseMessage.INVALID_NAME);
        if (!userDto.getEmail().matches(Constants.REGEX_GMAIL))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseMessage.INVALID_GMAIL);
        if (!userDto.getPhone().matches(Constants.REGEX_PHONE))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseMessage.INVALID_PHONE);
        if (!userDto.getAddress().matches(Constants.REGEX_ADDRESS))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseMessage.INVALID_ADDRESS);




        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseMessage.VALID_INFORMATION);
    }
}

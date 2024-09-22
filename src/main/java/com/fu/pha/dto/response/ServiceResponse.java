package com.fu.pha.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize
@JsonDeserialize
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceResponse<T> {
    private Integer code;
    private String message;
    private T values;

    public static <T> ServiceResponse<T> succeed(HttpStatus status, T values) {
        return new ServiceResponse<>(status.value(), "success", values);
    }
}


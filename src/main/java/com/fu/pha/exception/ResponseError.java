package com.fu.pha.exception;

public interface ResponseError {
    String getName();

    String getMessage();

    default Integer getCode() {
        return 0;
    }

}

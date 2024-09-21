package com.fu.pha.enums;

import com.fu.pha.exception.ResponseError;

public class ResponseMessage implements ResponseError {

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getMessage() {
        return null;
    }

    @Override
    public Integer getCode() {
        return ResponseError.super.getCode();
    }
}

package com.fu.pha.exception;

public class MaxUploadSizeExceededException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public MaxUploadSizeExceededException(String message) {
        super(message);
    }
}

package com.fu.pha.service;

import com.fu.pha.dto.response.SampleResponse;
import com.fu.pha.exception.BadRequestException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileS3Service {
    String uploadFile(MultipartFile multipartFile) throws IOException;

    SampleResponse getSampleResponseById(int id) throws BadRequestException;
}

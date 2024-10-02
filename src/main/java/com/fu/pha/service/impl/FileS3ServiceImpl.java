package com.fu.pha.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fu.pha.dto.response.SampleResponse;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.service.FileS3Service;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;

@Service
public class FileS3ServiceImpl implements FileS3Service {

    @Value("${aws.bucket.name}")
    private String bucketName;

    private final AmazonS3 s3Client;

    public FileS3ServiceImpl(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * Upload file to S3
     *
     * @param multipartFile file upload input
     * @return presigned URL S3 upload
     * @throws IOException when have exception occur
     */
    @Override
    public String uploadFile(MultipartFile multipartFile) throws IOException {
        // convert multipart file  to a file
        File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(multipartFile.getBytes());
        }

        // generate file name
        String fileName = generateFileName(multipartFile);

        // upload file
        PutObjectRequest request = new PutObjectRequest(bucketName, fileName, file);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("plain/" + FilenameUtils.getExtension(multipartFile.getOriginalFilename()));
        metadata.addUserMetadata("Title", "File Upload - " + fileName);
        metadata.setContentLength(file.length());
        request.setMetadata(metadata);
        s3Client.putObject(request);
        String publicUrl = s3Client.getUrl(bucketName, fileName).toString();
        // delete file
        file.delete();

        return publicUrl;
    }

    @Override
    public SampleResponse getSampleResponseById(int id) throws BadRequestException {
        if (id == 1) {
            return SampleResponse.builder().id(1).name("Sample name").message("Hello world!!!").build();
        } else if (id == 2) {
            throw new ResourceNotFoundException("Lỗi xảy ra là not found");
        } else {
            throw new BadRequestException("Lỗi xảy ra là bad request");
        }
    }

    /**
     * Generate file name by date time
     *
     * @param multiPart file upload
     * @return File name generated
     */
    private String generateFileName(MultipartFile multiPart) {
        return new Date().getTime() + "-" + Objects.requireNonNull(multiPart.getOriginalFilename()).replace(" ", "_");
    }
}

package com.fu.pha.service;

import com.cloudinary.Cloudinary;
import com.fu.pha.dto.response.CloudinaryResponse;
import com.fu.pha.util.FileUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryService {
    @Autowired
    private Cloudinary cloudinary;

    @Transactional
    public CloudinaryResponse upLoadFile(MultipartFile file, String fileName)  {

        try{
            final Map result = cloudinary.uploader().upload(file.getBytes(),
                                                                Map.of("public_id",
                                                                        "pha/user/"
                                                                                +fileName));
            final String url = (String) result.get("secure_url");
            final String publicId = (String) result.get("public_id");
            return CloudinaryResponse.builder().url(url).publicId(publicId).build();
        }catch (IOException e){
            throw new RuntimeException("Lỗi khi upload file");
        }
    }

    @Transactional
    public CloudinaryResponse upLoadFile(MultipartFile file, String fileName, String raw)  {

        try{
            final Map result = cloudinary.uploader().upload(file.getBytes(),
                    Map.of("public_id",
                            "pha/user/"
                                    +raw));
            final String url = (String) result.get("secure_url");
            final String publicId = (String) result.get("public_id");
            return CloudinaryResponse.builder().url(url).publicId(publicId).build();
        }catch (IOException e){
            throw new RuntimeException("Lỗi khi upload file");
        }
    }


}

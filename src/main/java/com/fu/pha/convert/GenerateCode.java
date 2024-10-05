package com.fu.pha.convert;

import org.springframework.stereotype.Component;

@Component
public class GenerateCode {

    public String generateNewProductCode(String lastProductCode) {
        if (lastProductCode == null || lastProductCode.length() < 2) {
            throw new IllegalArgumentException("Invalid product code");
        }
        String prefix = lastProductCode.substring(0, 2); // 'SP'
        int number = Integer.parseInt(lastProductCode.substring(2)); // Lấy phần số
        int newNumber = number + 1;
        return String.format("%s%05d", prefix, newNumber);
    }
}

package com.fu.pha.validate;

import java.time.Instant;

public class Constants {
    public static final String REGEX_NAME = "^[a-zA-ZàáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđĐ\\s]+$";
    public static final String REGEX_PHONE = "^[0-9]{10,11}$";
    public static final String REGEX_USER_NAME = "^[a-zA-Z0-9]{6,20}$";
    public static final String REGEX_PASSWORD = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";
    public static final String REGEX_GMAIL = "^[\\w.+\\-]+@gmail\\.com$";
    public static final String REGEX_ADDRESS = "^[a-zA-Z0-9àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđĐ\\s\\-]+$";
    public static final String REGEX_CCCD = "^\\d{12}$";

    public static final String REGEX_TAX = "^\\d{10}(-\\d{3})?$";


}

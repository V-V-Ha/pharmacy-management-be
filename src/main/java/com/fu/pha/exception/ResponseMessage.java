package com.fu.pha.exception;

public class ResponseMessage {
    public static final String INVALID_USERNAME = "Tên đăng nhập hoặc mật khẩu không chính xác";
    public static final String ACCOUNT_LOCKED = "Tài khoản của bạn đã bị khóa, vui lòng liên hệ bộ phận hỗ trợ";
    public static final String ACCOUNT_DISABLED = "Tài khoản của bạn đã bị vô hiệu hóa, vui lòng liên hệ bộ phận hỗ trợ";
    public static final String LOGIN_ERROR = "Đã xảy ra lỗi trong quá trình đăng nhập, vui lòng thử lại sau";
    public static final String ACCESS_DENIED = "Người dùng không có quyền truy cập";
    public static final String VALID_INFORMATION = "Thông tin hợp lệ";
    //User
    public static final String NULL_FILED = "Thông tin không được trống";
    public static final String INVALID_NAME = "Tên không hợp lệ";
    public static final String INVALID_PASSWORD = "Mật khẩu không hợp lệ, mật khẩu phải có ít nhất 8 ký tự, " +
            "và phải có 1 ký tự hoa và 1 ký tự thường";
    public static final String INVALID_CCCD = "Số căn cước công dân không hợp lệ. Yêu cầu 12 chữ số";
    public static final String INVALID_PHONE = "Số điện thoại không hợp lệ";
    public static final String INVALID_GMAIL = "Gmail không hợp lệ";
    public static final String INVALID_ADDRESS = "Địa chỉ không hợp lệ";

}

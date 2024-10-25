package com.fu.pha.exception;

public class Message {
    public static final String INVALID_USERNAME = "Tên đăng nhập hoặc mật khẩu không chính xác";
    public static final String ACCOUNT_LOCKED = "Tài khoản của bạn đã bị khóa, vui lòng liên hệ bộ phận hỗ trợ";
    public static final String ACCOUNT_DISABLED = "Tài khoản của bạn đã bị vô hiệu hóa, vui lòng liên hệ bộ phận hỗ trợ";
    public static final String OTHER_ERROR = "Đã xảy ra lỗi , vui lòng thử lại ";
    public static final String ACCESS_DENIED = "Người dùng không có quyền truy cập";
    public static final String VALID_INFORMATION = "Thông tin hợp lệ";
    //User
    public static final String NULL_FILED = "Thông tin không được trống";
    public static final String INVALID_NAME = "Tên không hợp lệ";

    public static final String CREATE_SUCCESS = "Tạo thành công";

    public static final String INVALID_PASSWORD = "Mật khẩu không hợp lệ, mật khẩu phải có ít nhất 8 ký tự, " +
            "và phải có 1 ký tự hoa và 1 ký tự thường";
    public static final String INVALID_CCCD = "Số căn cước công dân không hợp lệ. Yêu cầu 12 chữ số";
    public static final String INVALID_PHONE = "Số điện thoại không hợp lệ";
    public static final String INVALID_GMAIL = "Gmail không hợp lệ";
    public static final String INVALID_USERNAME_C = "Tên đăng nhập không hợp lệ, tài khoản phải chứa 6-20 kí tự," +
            "và chỉ chứa chữ cái và số";
    public static final String INVALID_ADDRESS = "Địa chỉ không hợp lệ";

    public static final String EXIST_USERNAME = "Tên đăng nhập đã tồn tại";
    public static final String EXIST_EMAIL = "Email đã tồn tại";
    public static final String EXIST_PHONE = "Số điện thoại đã tồn tại";
    public static final String EXIST_CCCD = "Số căn cước công dân đã tồn tại";
    public static final String INVALID_AGE = "Người dùng chưa đủ 18 tuổi";
    public static final String UPDATE_SUCCESS = "Cập nhật thành công";
    public static final String USER_NOT_FOUND = "Người dùng không tồn tại";
    public static final String ACTIVE_SUCCESS = "Kích hoạt tài khoản thành công";
    public static final String DEACTIVE_SUCCESS = "Ngưng kích hoạt tài khoản thành công";
    public static final String INVALID_TOKEN = "Token không hợp lệ";
    public static final String ROLE_NOT_FOUND = "Không tìm thấy quyền truy cập";
    public static final String STATUS_NOT_FOUND = "Không tìm thấy trạng thái";
    public static final String NOT_MATCH_PASSWORD = "Mật khẩu không khớp";
    public static final String CHANGE_PASS_SUCCESS = "Đổi mật khẩu thành công";
    public static final String INVALID_OLD_PASSWORD = "Mật khẩu cũ không chính xác";
    public static final String UPLOAD_SUCCESS = "Upload file thành công";
    public static final String EMPTY_FILE = "File không được trống";
    public static final String INVALID_FILE = "Chỉ những định dạng ảnh sau được phép: jpg,png,gif,bmp";
    public static final String INVALID_FILE_SIZE = "Kích thước file phải bé hơn 2MB";
    public static final String PRODUCT_NOT_FOUND = "Không tìm thấy sản phẩm";
    public static final String PRODUCT_SUCCESS = "Thêm sản phẩm thành công";
    public static final String EXIST_PRODUCT_CODE = "Mã sản phẩm đã tồn tại";
    public static final String EXIST_REGISTRATION_NUMBER = "Số đăng ký đã tồn tại";
    public static final String CATEGORY_NOT_FOUND = "Nhóm sản phẩm không tồn tại";
    public static final String CATEGORY_EXIST = "Nhóm sản phẩm đã tồn tại";
    public static final String MUST_FILL_USERNAME = "Tên đăng nhập không được trống";
    public static final String MUST_FILL_PASSWORD = "Mật khẩu không được trống";
    public static final String PRODUCT_UPDATE_SUCCESS = "Cập nhật sản phẩm thành công";
    public static final String UNIT_NOT_FOUND = "Đơn vị không tồn tại";
    public static final String UNIT_EXIST = "Đơn vị đã tồn tại";
    public static final String FORGOT_PASS_SUCCESS = "Vui lòng kiểm tra email để lấy lại mật khẩu";
    public static final String INVALID_ROLE_COMBINATION = "Không thể chọn cùng lúc 2 quyền truy cập này";
    public static final String UNIT_IN_USE = "Đơn vị đang được sử dụng";
    public static final String DELETE_SUCCESS = "Xóa thành công";
    public static final String EXIST_PRODUCT_NAME = "Tên sản phẩm đã tồn tại";
    public static final String PRODUCT_DELETE_SUCCESS = "Xóa sản phẩm thành công";
    public static final String SUPPLIER_EXIST = "Nhà cung cấp đã tồn tại";
    public static final String INVALID_TAX = "Mã số thuế không hợp lệ";
    public static final String SUPPLIER_NOT_FOUND = "Nhà cung cấp không tồn tại";
    public static final String QUANTITY_NOT_FOUND = "Số lượng không được trống";
    public static final String IMPORT_NOT_FOUND = "Phiếu nhập không tồn tại";

    public static final String IMPORT_SUCCESS = "Tạo phiếu nhập thành công";
    public static final String TOTAL_AMOUNT_NOT_MATCH = "Tổng tiền không khớp";
    public static final String IMPORT_ITEMS_EMPTY = "Danh sách sản phẩm không được trống";
    public static final String INVALID_CONVERSION_FACTOR = "Hệ số quy đổi không hợp lệ";
    public static final String CUSTOMER_NOT_FOUND = "Không tìm thấy khách hàng";
    public static final String EXPORT_ITEMS_EMPTY = "Danh sách sản phẩm xuất không được trống";
    public static final String NOT_ENOUGH_STOCK = "Số lượng tồn kho không đủ";
    public static final String INVALID_EXPORT_TYPE = "Loại xuất kho không hợp lệ";
    public static final String IMPORT_ITEM_NOT_FOUND_FOR_BATCH = "Không tìm thấy sản phẩm trong lô hàng nhập";
    public static final String NOT_ENOUGH_STOCK_IN_BATCH = "Số lượng trong lô hàng không đủ";
    public static final String EXPORT_SLIP_NOT_FOUND = "Không tìm thấy phiếu xuất kho";

}

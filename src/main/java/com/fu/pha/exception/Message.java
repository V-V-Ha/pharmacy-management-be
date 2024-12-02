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

    public static final String CREATE_FAILED = "Tạo thất bại";

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
    public static final String INVALID_TOKEN = "Token không hợp lệ";
    public static final String ROLE_NOT_FOUND = "Không tìm thấy quyền truy cập";
    public static final String STATUS_NOT_FOUND = "Không tìm thấy trạng thái";
    public static final String NOT_MATCH_PASSWORD = "Mật khẩu không khớp";
    public static final String CHANGE_PASS_SUCCESS = "Đổi mật khẩu thành công";
    public static final String INVALID_OLD_PASSWORD = "Mật khẩu cũ không chính xác";
    public static final String EMPTY_FILE = "File không được trống";
    public static final String INVALID_FILE = "Chỉ những định dạng ảnh sau được phép: jpg,png,gif,bmp";
    public static final String INVALID_FILE_SIZE = "Kích thước file phải bé hơn 2MB";
    public static final String PRODUCT_NOT_FOUND = "Không tìm thấy sản phẩm";
    public static final String EXIST_REGISTRATION_NUMBER = "Số đăng ký đã tồn tại";
    public static final String CATEGORY_NOT_FOUND = "Nhóm sản phẩm không tồn tại";
    public static final String CATEGORY_EXIST = "Nhóm sản phẩm đã tồn tại";
    public static final String MUST_FILL_USERNAME = "Tên đăng nhập không được trống";
    public static final String MUST_FILL_PASSWORD = "Mật khẩu không được trống";
    public static final String UNIT_NOT_FOUND = "Đơn vị không tồn tại";
    public static final String UNIT_EXIST = "Đơn vị đã tồn tại";
    public static final String FORGOT_PASS_SUCCESS = "Vui lòng kiểm tra email để lấy lại mật khẩu";
    public static final String INVALID_ROLE_COMBINATION = "Không thể chọn cùng lúc 2 quyền truy cập này";
    public static final String DELETE_SUCCESS = "Xóa thành công";
    public static final String SUPPLIER_EXIST = "Nhà cung cấp đã tồn tại";
    public static final String INVALID_TAX = "Mã số thuế không hợp lệ";
    public static final String SUPPLIER_NOT_FOUND = "Nhà cung cấp không tồn tại";
    public static final String IMPORT_NOT_FOUND = "Phiếu nhập không tồn tại";

    public static final String TOTAL_AMOUNT_NOT_MATCH = "Tổng tiền không khớp";
    public static final String IMPORT_ITEMS_EMPTY = "Danh sách sản phẩm không được trống";
    public static final String INVALID_CONVERSION_FACTOR = "Hệ số quy đổi không hợp lệ";
    public static final String CUSTOMER_NOT_FOUND = "Không tìm thấy khách hàng";
    public static final String EXPORT_ITEMS_EMPTY = "Danh sách sản phẩm xuất không được trống";
    public static final String NOT_ENOUGH_STOCK = "Số lượng tồn kho không đủ";
    public static final String INVALID_EXPORT_TYPE = "Loại xuất kho không hợp lệ";
    public static final String NOT_ENOUGH_STOCK_IN_BATCH = "Số lượng trong lô hàng không đủ";
    public static final String EXPORT_SLIP_NOT_FOUND = "Không tìm thấy phiếu xuất kho";

    public static final String DOCTOR_NOT_FOUND = "Không tìm thấy bác sĩ";
    public static final String OUT_OF_STOCK = "Sản phẩm không đủ tồn kho";
    public static final String SUPPLIER_NOT_MATCH = "Nhà cung cấp không trùng khớp";
    public static final String SALE_ORDER_NOT_FOUND = "Không tìm thấy đơn hàng";
    public static final String DATE_NOT_NULL = "Ngày tạo không được trống";
    public static final String TOTAL_AMOUNT_NOT_NULL = "Tổng tiền không được trống";
    public static final String CUSTOMER_NOT_NULL = "Khách hàng không được trống";
    public static final String USER_NOT_NULL = "Người tạo không được trống";
    public static final String SUPPLIER_NOT_NULL = "Nhà cung cấp không được trống";
    public static final String TOTAL_AMOUNT_VALID = "Tổng tiền phải lớn hơn 0";
    public static final String LIST_ITEM_NOT_NULL = "Danh sách sản phẩm không được trống";
    public static final String DOCTOR_REQUIRED = "Bác sĩ không được trống";
    public static final String INVALID_RETURN_QUANTITY = "Số lượng trả lại không hợp lệ";
    public static final String RETURN_ORDER_NOT_FOUND = "Không tìm thấy phiếu trả hàng";
    public static final String PRODUCT_NOT_IN_SALE_ORDER = "Sản phẩm không có trong đơn hàng";
    public static final String SALE_ORDER_NOT_NULL = "Thông tin đơn hàng không được trống";
    public static final String PAYMENT_METHOD_NOT_NULL = "Phương thức thanh toán không được trống";
    public static final String INVALID_YOB = "Năm sinh phải lớn hơn 1900 và không phải là năm sau.";
    public static final Object CONFIRM_SUCCESS = "Xác nhận phiếu thành công";
    public static final Object REJECT_SUCCESS = "Xác nhận từ chối phiếu thành công";
    public static final String IMAGE_IMPORT_NOT_NULL = "Ảnh phiếu nhập là bắt buộc";
    public static final String REJECT_AUTHORIZATION = "Bạn không có quyền thực hiện chức năng này";
    public static final String NOT_PENDING_IMPORT = "Phiếu nhập không ở trạng thái chờ xác nhận";

    public static final String NOT_PENDING_EXPORT = "Phiếu xuất không ở trạng thái chờ xác nhận";
    public static final String REASON_REQUIRED = "Phải nhập lý do từ chối";
    public static final String NOT_LOGIN = "Bạn chưa đăng nhập";
    public static final String NOT_UPDATE_CONFIRMED = "Không thể cập nhật phiếu đã xác nhận";
    public static final String NOT_REJECT = " Chỉ có thể từ chối phiếu ở trạng thái chờ xác nhận";
    public static final String TOTAL_AMOUNT_REQUIRED = "Tổng tiền không được trống";
    public static final String IMPORT_ITEM_NOT_FOUND = "Không tìm thấy sản phẩm trong phiếu";
    public static final String CATEGORY_INACTIVE = "Nhóm sản phẩm đã bị vô hiệu hóa";
    public static final String UNIT_INACTIVE = "Đơn vị đã bị vô hiệu hóa";
    public static final String ORDER_ALREADY_PAID = "Đơn hàng đã được thanh toán";
    public static final String PAYMENT_COMPLETED = "Thanh toán thành công";
    public static final String CANNOT_UPDATE_PAID_ORDER = "Không thể cập nhật đơn hàng đã thanh toán";
    public static final String INVALID_PRESCRIPTION_DRUG = "Hình thức bán không hợp lệ";
    public static final String SALE_ORDER_ITEM_BATCH_NOT_FOUND = "Không tìm thấy lô hàng";

}

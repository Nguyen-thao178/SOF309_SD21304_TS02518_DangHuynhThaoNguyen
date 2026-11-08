package com.cafe.utils;

import java.util.regex.Pattern;

/**
 * Lớp tiện ích kiểm tra dữ liệu (Validation)
 * Hỗ trợ các kiểu: email, phone, username, password, ...
 */
public class ValidationUtil {

    // Regex patterns
    private static final String EMAIL_PATTERN =
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    private static final String PHONE_PATTERN =
            "^(0|\\+84)[0-9]{9,10}$";

    private static final String USERNAME_PATTERN =
            "^[a-zA-Z0-9_]{3,50}$";

    private static final String PASSWORD_PATTERN =
            "^.{6,}$";

    /**
     * Kiểm tra email hợp lệ
     * @param email Địa chỉ email
     * @return true nếu hợp lệ, false nếu không
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return Pattern.matches(EMAIL_PATTERN, email.trim());
    }

    /**
     * Kiểm tra số điện thoại hợp lệ
     * @param phone Số điện thoại
     * @return true nếu hợp lệ, false nếu không
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return Pattern.matches(PHONE_PATTERN, phone.trim());
    }

    /**
     * Kiểm tra tên đăng nhập hợp lệ
     * @param username Tên đăng nhập
     * @return true nếu hợp lệ, false nếu không
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return Pattern.matches(USERNAME_PATTERN, username.trim());
    }

    /**
     * Kiểm tra mật khẩu hợp lệ (tối thiểu 6 ký tự)
     * @param password Mật khẩu
     * @return true nếu hợp lệ, false nếu không
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        return Pattern.matches(PASSWORD_PATTERN, password.trim());
    }

    /**
     * Kiểm tra mật khẩu mạnh (có chữ hoa, chữ thường, số, ký tự đặc biệt)
     * @param password Mật khẩu
     * @return true nếu mạnh, false nếu không
     */
    public static boolean isStrongPassword(String password) {
        if (!isValidPassword(password)) {
            return false;
        }
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    /**
     * Kiểm tra mật khẩu và xác nhận khớp nhau
     * @param password Mật khẩu
     * @param confirmPassword Xác nhận mật khẩu
     * @return true nếu khớp, false nếu không
     */
    public static boolean isPasswordMatch(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) {
            return false;
        }
        return password.equals(confirmPassword);
    }

    /**
     * Kiểm tra chuỗi không rỗng
     * @param value Chuỗi cần kiểm tra
     * @return true nếu không rỗng, false nếu rỗng
     */
    public static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Kiểm tra số nguyên hợp lệ (> 0)
     * @param number Số cần kiểm tra
     * @return true nếu hợp lệ, false nếu không
     */
    public static boolean isValidPositiveInt(int number) {
        return number > 0;
    }

    /**
     * Kiểm tra số thực hợp lệ (>= 0)
     * @param number Số cần kiểm tra
     * @return true nếu hợp lệ, false nếu không
     */
    public static boolean isValidNonNegativeDouble(double number) {
        return number >= 0;
    }

    /**
     * Kiểm tra ngày tháng hợp lệ (start <= end)
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return true nếu hợp lệ, false nếu không
     */
    public static boolean isValidDateRange(java.util.Date startDate, java.util.Date endDate) {
        if (startDate == null || endDate == null) {
            return false;
        }
        return !startDate.after(endDate);
    }
}
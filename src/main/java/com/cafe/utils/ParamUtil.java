package com.cafe.utils;

import jakarta.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Lớp tiện ích lấy tham số từ HttpServletRequest
 * Hỗ trợ các kiểu dữ liệu: int, long, double, float, boolean, string, date
 * Có khả năng xử lý giá trị mặc định khi tham số không tồn tại hoặc không hợp lệ
 */
public class ParamUtil {

    // ==================== STRING ====================

    /**
     * Lấy tham số kiểu String
     * @param request HttpServletRequest
     * @param name Tên tham số
     * @return Giá trị tham số, null nếu không tồn tại
     */
    public static String getString(HttpServletRequest request, String name) {
        return request.getParameter(name);
    }

    /**
     * Lấy tham số kiểu String với giá trị mặc định
     * @param request HttpServletRequest
     * @param name Tên tham số
     * @param defaultValue Giá trị mặc định
     * @return Giá trị tham số hoặc defaultValue nếu không tồn tại
     */
    public static String getString(HttpServletRequest request, String name, String defaultValue) {
        String value = request.getParameter(name);
        return (value != null && !value.trim().isEmpty()) ? value.trim() : defaultValue;
    }

    // ==================== INTEGER ====================

    /**
     * Lấy tham số kiểu int
     * @param request HttpServletRequest
     * @param name Tên tham số
     * @return Giá trị int, 0 nếu không tồn tại hoặc không hợp lệ
     */
    public static int getInt(HttpServletRequest request, String name) {
        return getInt(request, name, 0);
    }

    /**
     * Lấy tham số kiểu int với giá trị mặc định
     * @param request HttpServletRequest
     * @param name Tên tham số
     * @param defaultValue Giá trị mặc định
     * @return Giá trị int hoặc defaultValue nếu không tồn tại hoặc không hợp lệ
     */
    public static int getInt(HttpServletRequest request, String name, int defaultValue) {
        String value = request.getParameter(name);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // ==================== LONG ====================

    public static long getLong(HttpServletRequest request, String name) {
        return getLong(request, name, 0L);
    }

    public static long getLong(HttpServletRequest request, String name, long defaultValue) {
        String value = request.getParameter(name);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // ==================== DOUBLE ====================

    public static double getDouble(HttpServletRequest request, String name) {
        return getDouble(request, name, 0.0);
    }

    public static double getDouble(HttpServletRequest request, String name, double defaultValue) {
        String value = request.getParameter(name);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // ==================== FLOAT ====================

    public static float getFloat(HttpServletRequest request, String name) {
        return getFloat(request, name, 0.0f);
    }

    public static float getFloat(HttpServletRequest request, String name, float defaultValue) {
        String value = request.getParameter(name);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // ==================== BOOLEAN ====================

    /**
     * Lấy tham số kiểu boolean
     * @param request HttpServletRequest
     * @param name Tên tham số
     * @return true nếu giá trị là "true", "on", "yes", "1" (không phân biệt hoa thường)
     */
    public static boolean getBoolean(HttpServletRequest request, String name) {
        return getBoolean(request, name, false);
    }

    public static boolean getBoolean(HttpServletRequest request, String name, boolean defaultValue) {
        String value = request.getParameter(name);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        value = value.trim().toLowerCase();
        return "true".equals(value) || "on".equals(value) || "yes".equals(value) || "1".equals(value);
    }

    // ==================== DATE ====================

    /**
     * Lấy tham số kiểu Date với định dạng yyyy-MM-dd
     * @param request HttpServletRequest
     * @param name Tên tham số
     * @return Đối tượng Date, null nếu không tồn tại hoặc không hợp lệ
     */
    public static Date getDate(HttpServletRequest request, String name) {
        return getDate(request, name, "yyyy-MM-dd");
    }

    public static Date getDate(HttpServletRequest request, String name, String pattern) {
        String value = request.getParameter(name);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            sdf.setLenient(false);
            return sdf.parse(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== ARRAY ====================

    /**
     * Lấy mảng tham số kiểu String
     * @param request HttpServletRequest
     * @param name Tên tham số
     * @return Mảng String, null nếu không tồn tại
     */
    public static String[] getStringArray(HttpServletRequest request, String name) {
        return request.getParameterValues(name);
    }

    /**
     * Lấy mảng tham số kiểu int
     * @param request HttpServletRequest
     * @param name Tên tham số
     * @return Mảng int, mảng rỗng nếu không tồn tại
     */
    public static int[] getIntArray(HttpServletRequest request, String name) {
        String[] values = request.getParameterValues(name);
        if (values == null || values.length == 0) {
            return new int[0];
        }
        int[] result = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = getInt(request, name + "[" + i + "]", 0);
        }
        return result;
    }

    // ==================== UTILITY ====================

    /**
     * Kiểm tra tham số có tồn tại không
     * @param request HttpServletRequest
     * @param name Tên tham số
     * @return true nếu tồn tại và không rỗng
     */
    public static boolean hasParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value != null && !value.trim().isEmpty();
    }
}
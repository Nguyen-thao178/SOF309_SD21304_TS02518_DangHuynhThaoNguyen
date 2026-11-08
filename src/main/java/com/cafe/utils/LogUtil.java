package com.cafe.utils;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Lớp tiện ích ghi log hệ thống
 * Ghi log ra file và console
 */
public class LogUtil {

    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = "application.log";
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static {
        // Tạo thư mục logs nếu chưa tồn tại
        File dir = new File(LOG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Ghi log mức INFO
     * @param message Nội dung log
     */
    public static void info(String message) {
        log("INFO", message);
    }

    /**
     * Ghi log mức DEBUG
     * @param message Nội dung log
     */
    public static void debug(String message) {
        log("DEBUG", message);
    }

    /**
     * Ghi log mức WARN
     * @param message Nội dung log
     */
    public static void warn(String message) {
        log("WARN", message);
    }

    /**
     * Ghi log mức ERROR
     * @param message Nội dung log
     */
    public static void error(String message) {
        log("ERROR", message);
    }

    /**
     * Ghi log mức ERROR kèm exception
     * @param message Nội dung log
     * @param e Exception
     */
    public static void error(String message, Exception e) {
        log("ERROR", message + " - " + e.getMessage());
        e.printStackTrace();
    }

    /**
     * Ghi log mức INFO (có định dạng)
     * @param format Định dạng
     * @param args Tham số
     */
    public static void info(String format, Object... args) {
        info(String.format(format, args));
    }

    /**
     * Ghi log mức DEBUG (có định dạng)
     * @param format Định dạng
     * @param args Tham số
     */
    public static void debug(String format, Object... args) {
        debug(String.format(format, args));
    }

    /**
     * Ghi log ra file và console
     * @param level Mức độ log
     * @param message Nội dung log
     */
    private static void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String logEntry = String.format("[%s] [%s] %s", timestamp, level, message);

        // Ghi ra console
        System.out.println(logEntry);

        // Ghi ra file
        try (FileWriter fw = new FileWriter(LOG_DIR + File.separator + LOG_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(logEntry);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Lỗi ghi log: " + e.getMessage());
        }
    }

    /**
     * Xóa file log (dùng cho maintenance)
     */
    public static void clearLog() {
        File file = new File(LOG_DIR + File.separator + LOG_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * Lấy nội dung log
     * @return Nội dung file log
     */
    public static String getLogContent() {
        File file = new File(LOG_DIR + File.separator + LOG_FILE);
        if (!file.exists()) {
            return "Chưa có log.";
        }

        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            return "Lỗi đọc log: " + e.getMessage();
        }
        return content.toString();
    }
}
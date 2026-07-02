package guat.lxy.bigdata.smartshop.util;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件验证工具类
 * 统一处理文件格式、大小验证逻辑
 */
public final class FileValidationUtil {

    private FileValidationUtil() {
    }

    /** 支持的图片格式（不区分大小写） */
    private static final String IMAGE_PATTERN = "(?i).*\\.(jpg|jpeg|png|gif|webp)$";

    /** 默认最大文件大小：5MB */
    public static final long DEFAULT_MAX_SIZE = 5 * 1024 * 1024;

    /**
     * 验证文件是否为支持的图片格式
     */
    public static boolean isValidImage(String filename) {
        return filename != null && filename.matches(IMAGE_PATTERN);
    }

    /**
     * 验证文件大小是否在限制内
     */
    public static boolean isValidSize(MultipartFile file, long maxSize) {
        return file != null && !file.isEmpty() && file.getSize() <= maxSize;
    }

    /**
     * 验证文件大小是否在限制内（使用默认5MB限制）
     */
    public static boolean isValidSize(MultipartFile file) {
        return isValidSize(file, DEFAULT_MAX_SIZE);
    }

    /**
     * 提取文件扩展名
     */
    public static String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }
}

package guat.lxy.bigdata.smartshop.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * 文件上传工具类
 * 统一处理文件上传、验证、存储逻辑
 */
@Component
public class FileUploadUtil {

    @Value("${file.upload-dir:src/main/resources/static/uploads}")
    private String uploadDir;

    /**
     * 上传文件
     * @param file 上传的文件
     * @param subDir 子目录（如 "products", "avatars"）
     * @param fileName 文件名（不含扩展名，会自动添加原扩展名）
     * @return 可访问的URL路径
     * @throws IOException 上传失败时抛出
     */
    public String upload(MultipartFile file, String subDir, String fileName) throws IOException {
        String originalName = file.getOriginalFilename();
        String ext = FileValidationUtil.getExtension(originalName);
        String fullFileName = fileName + ext;

        String dirPath = System.getProperty("user.dir") + File.separator
                + uploadDir.replace("/", File.separator) + File.separator + subDir;
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        file.transferTo(new File(dir, fullFileName));
        return "/uploads/" + subDir + "/" + fullFileName;
    }

    /**
     * 删除指定前缀的旧文件
     * @param subDir 子目录
     * @param prefix 文件名前缀
     */
    public void deleteOldFiles(String subDir, String prefix) {
        String dirPath = System.getProperty("user.dir") + File.separator
                + uploadDir.replace("/", File.separator) + File.separator + subDir;
        File dir = new File(dirPath);
        if (dir.exists() && dir.listFiles() != null) {
            for (File f : dir.listFiles((d, n) -> n.startsWith(prefix + "."))) {
                f.delete();
            }
        }
    }
}

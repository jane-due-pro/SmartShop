package guat.lxy.bigdata.smartshop.controller;

import guat.lxy.bigdata.smartshop.entity.Product;
import guat.lxy.bigdata.smartshop.entity.User;
import guat.lxy.bigdata.smartshop.service.ProductCommentService;
import guat.lxy.bigdata.smartshop.service.ProductFavoriteService;
import guat.lxy.bigdata.smartshop.service.UserService;
import guat.lxy.bigdata.smartshop.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductFavoriteService favoriteService;

    @Autowired
    private ProductCommentService commentService;

    @Value("${file.upload-dir:src/main/resources/static/uploads}")
    private String uploadDir;

    /** 个人中心主页 */
    @GetMapping
    public String profile(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        model.addAttribute("user", user);
        return "profile";
    }

    /** 更新邮箱 */
    @PostMapping("/updateEmail")
    @ResponseBody
    public Map<String, Object> updateEmail(@RequestParam String email, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        return Result.of(userService.updateEmail(user.getId(), email), "邮箱更新成功", "更新失败");
    }

    /** 上传头像 */
    @PostMapping("/uploadAvatar")
    @ResponseBody
    public Map<String, Object> uploadAvatar(@RequestParam("file") MultipartFile file, Principal principal) {
        if (file.isEmpty()) return Result.fail("请选择文件");
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.matches("(?i).*\\.(jpg|jpeg|png|gif|webp)$")) {
            return Result.fail("仅支持 jpg/png/gif/webp 格式");
        }
        if (file.getSize() > 5 * 1024 * 1024) return Result.fail("文件不能超过 5MB");

        try {
            User user = userService.findByUsername(principal.getName());
            String ext = originalName.substring(originalName.lastIndexOf('.'));
            String fileName = user.getId() + ext;
            String dirPath = System.getProperty("user.dir") + File.separator
                    + uploadDir.replace("/", File.separator) + File.separator + "avatars";
            File dir = new File(dirPath);
            if (!dir.exists()) dir.mkdirs();

            // 删除旧头像（同用户ID不同扩展名）
            for (File f : dir.listFiles((d, n) -> n.startsWith(user.getId() + "."))) {
                f.delete();
            }

            file.transferTo(new File(dir, fileName));
            String avatarPath = "/uploads/avatars/" + fileName;
            userService.updateAvatar(user.getId(), avatarPath);
            return Result.success(avatarPath);
        } catch (IOException e) {
            return Result.fail("上传失败: " + e.getMessage());
        }
    }

    /** 我的收藏 */
    @GetMapping("/favorites")
    @ResponseBody
    public List<Product> favorites(Principal principal) {
        User user = userService.findByUsername(principal.getName());
        return favoriteService.findFavoritesByUserId(user.getId());
    }
}

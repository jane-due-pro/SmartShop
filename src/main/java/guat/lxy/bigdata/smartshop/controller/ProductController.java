package guat.lxy.bigdata.smartshop.controller;

import com.github.pagehelper.PageInfo;
import guat.lxy.bigdata.smartshop.entity.Category;
import guat.lxy.bigdata.smartshop.entity.Product;
import guat.lxy.bigdata.smartshop.entity.User;
import guat.lxy.bigdata.smartshop.service.CategoryService;
import guat.lxy.bigdata.smartshop.service.ProductCommentService;
import guat.lxy.bigdata.smartshop.service.ProductFavoriteService;
import guat.lxy.bigdata.smartshop.service.ProductService;
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
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductCommentService commentService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductFavoriteService favoriteService;

    @Value("${file.upload-dir:src/main/resources/static/uploads}")
    private String uploadDir;

    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "5") Integer pageSize,
                       @RequestParam(required = false) Integer catId,
                       @RequestParam(required = false) String name,
                       @RequestParam(required = false) Double minPrice,
                       @RequestParam(required = false) Double maxPrice,
                       Model model) {
        PageInfo<Product> pageInfo = productService.searchWithPage(catId, name, minPrice, maxPrice, pageNum, pageSize);
        List<Category> categories = categoryService.findAll();

        model.addAttribute("pageInfo", pageInfo);
        model.addAttribute("categories", categories);
        model.addAttribute("catId", catId);
        model.addAttribute("name", name);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        return "product/list";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Integer id, Model model, Principal principal) {
        Product product = productService.findById(id);
        var comments = commentService.findByProductId(id);
        User user = userService.findByUsername(principal.getName());
        commentService.fillLikeInfo(comments, user.getId());
        boolean isAdmin = userService.getUserRoles(user.getId())
                .stream().anyMatch(r -> r.getRole().equals("ROLE_admin"));
        model.addAttribute("product", product);
        model.addAttribute("comments", comments);
        model.addAttribute("commentCount", commentService.countByProductId(id));
        model.addAttribute("avgRating", commentService.avgRatingByProductId(id));
        model.addAttribute("currentUsername", principal.getName());
        model.addAttribute("isAdmin", isAdmin);
        try {
            model.addAttribute("favorited", favoriteService.isFavorited(id, user.getId()));
            model.addAttribute("favoriteCount", favoriteService.countByProductId(id));
        } catch (Exception e) {
            model.addAttribute("favorited", false);
            model.addAttribute("favoriteCount", 0);
        }
        return "product/detail";
    }

    @GetMapping("/add")
    public String addPage(Model model) {
        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);
        return "product/add";
    }

    @PostMapping("/add")
    @ResponseBody
    public Map<String, Object> add(@RequestBody Product product) {
        boolean ok = productService.save(product);
        if (ok) {
            return Map.of("success", true, "message", "添加成功", "productId", product.getId());
        }
        return Result.fail("添加失败");
    }

    @PostMapping("/uploadImage/{id}")
    @ResponseBody
    public Map<String, Object> uploadImage(@PathVariable Integer id,
                                           @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.fail("请选择文件");
        String name = file.getOriginalFilename();
        if (name == null || !name.matches("(?i).*\\.(jpg|jpeg|png|gif|webp)$")) {
            return Result.fail("仅支持 jpg/png/gif/webp 格式");
        }
        if (file.getSize() > 5 * 1024 * 1024) return Result.fail("文件不能超过 5MB");
        try {
            String ext = name.substring(name.lastIndexOf('.'));
            String fileName = id + ext;
            String dirPath = System.getProperty("user.dir") + File.separator
                    + uploadDir.replace("/", File.separator) + File.separator + "products";
            File dir = new File(dirPath);
            if (!dir.exists()) dir.mkdirs();

            // 删除旧图片（同 ID 不同扩展名）
            for (File f : dir.listFiles((d, n) -> n.startsWith(id + "."))) {
                f.delete();
            }

            file.transferTo(new File(dir, fileName));
            String photoUrl = "/uploads/products/" + fileName;
            Product p = productService.findById(id);
            p.setPhotoUrl(photoUrl);
            productService.update(p);
            return Result.success(photoUrl);
        } catch (IOException e) {
            return Result.fail("上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Integer id, Model model) {
        Product product = productService.findById(id);
        List<Category> categories = categoryService.findAll();
        model.addAttribute("product", product);
        model.addAttribute("categories", categories);
        return "product/edit";
    }

    @PostMapping("/edit")
    @ResponseBody
    public Map<String, Object> edit(@RequestBody Product product) {
        return Result.of(productService.update(product), "修改成功", "修改失败");
    }

    @PostMapping("/delete/{id}")
    @ResponseBody
    public Map<String, Object> delete(@PathVariable Integer id) {
        return Result.of(productService.deleteById(id), "删除成功", "删除失败");
    }
}
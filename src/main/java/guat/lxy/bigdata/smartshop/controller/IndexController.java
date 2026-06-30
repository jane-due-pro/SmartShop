package guat.lxy.bigdata.smartshop.controller;

import guat.lxy.bigdata.smartshop.entity.Category;
import guat.lxy.bigdata.smartshop.entity.Product;
import guat.lxy.bigdata.smartshop.service.CategoryService;
import guat.lxy.bigdata.smartshop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/")
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @GetMapping({"", "/", "/index"})
    public String index(Model model, Principal principal) {
        model.addAttribute("username", principal.getName());
        return "Index";
    }

    @GetMapping("/welcome")
    public String welcome(Model model) {
        // 走编程式 Redis 缓存：第一次查 MySQL + 写 Redis，后续命中 Redis 不再走 SQL
        List<Category> categories = categoryService.findAllWithCache();
        List<Product> products = productService.findAllWithCache();
        model.addAttribute("categories", categories);
        model.addAttribute("products", products);
        return "Welcome";
    }
}

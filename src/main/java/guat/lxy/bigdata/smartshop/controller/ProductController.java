package guat.lxy.bigdata.smartshop.controller;

import com.github.pagehelper.PageInfo;
import guat.lxy.bigdata.smartshop.entity.Category;
import guat.lxy.bigdata.smartshop.entity.Product;
import guat.lxy.bigdata.smartshop.service.CategoryService;
import guat.lxy.bigdata.smartshop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

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

    @GetMapping("/add")
    public String addPage(Model model) {
        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);
        return "product/add";
    }

    @PostMapping("/add")
    @ResponseBody
    public java.util.Map<String, Object> add(@RequestBody Product product) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        if (productService.save(product)) {
            result.put("success", true);
            result.put("message", "添加成功");
        } else {
            result.put("success", false);
            result.put("message", "添加失败");
        }
        return result;
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
    public java.util.Map<String, Object> edit(@RequestBody Product product) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        if (productService.update(product)) {
            result.put("success", true);
            result.put("message", "修改成功");
        } else {
            result.put("success", false);
            result.put("message", "修改失败");
        }
        return result;
    }

    @PostMapping("/delete/{id}")
    @ResponseBody
    public java.util.Map<String, Object> delete(@PathVariable Integer id) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        if (productService.deleteById(id)) {
            result.put("success", true);
            result.put("message", "删除成功");
        } else {
            result.put("success", false);
            result.put("message", "删除失败");
        }
        return result;
    }
}

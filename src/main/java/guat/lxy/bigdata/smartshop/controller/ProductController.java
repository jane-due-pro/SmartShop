package guat.lxy.bigdata.smartshop.controller;

import com.github.pagehelper.PageInfo;
import guat.lxy.bigdata.smartshop.entity.Category;
import guat.lxy.bigdata.smartshop.entity.Product;
import guat.lxy.bigdata.smartshop.service.CategoryService;
import guat.lxy.bigdata.smartshop.service.ProductService;
import guat.lxy.bigdata.smartshop.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    public Map<String, Object> add(@RequestBody Product product) {
        return Result.of(productService.save(product), "添加成功", "添加失败");
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
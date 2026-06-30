package guat.lxy.bigdata.smartshop.controller;

import guat.lxy.bigdata.smartshop.entity.Category;
import guat.lxy.bigdata.smartshop.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list")
    public String list(Model model) {
        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);
        return "category/list";
    }

    @GetMapping("/add")
    public String addPage() {
        return "category/add";
    }

    @PostMapping("/add")
    @ResponseBody
    public java.util.Map<String, Object> add(@RequestBody Category category) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        if (categoryService.save(category)) {
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
        Category category = categoryService.findById(id);
        model.addAttribute("category", category);
        return "category/edit";
    }

    @PostMapping("/edit")
    @ResponseBody
    public java.util.Map<String, Object> edit(@RequestBody Category category) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        if (categoryService.update(category)) {
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
        if (categoryService.deleteById(id)) {
            result.put("success", true);
            result.put("message", "删除成功");
        } else {
            result.put("success", false);
            result.put("message", "删除失败");
        }
        return result;
    }
}

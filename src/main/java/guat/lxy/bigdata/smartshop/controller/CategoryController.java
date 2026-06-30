package guat.lxy.bigdata.smartshop.controller;

import guat.lxy.bigdata.smartshop.entity.Category;
import guat.lxy.bigdata.smartshop.service.CategoryService;
import guat.lxy.bigdata.smartshop.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    public Map<String, Object> add(@RequestBody Category category) {
        return Result.of(categoryService.save(category), "添加成功", "添加失败");
    }

    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Integer id, Model model) {
        Category category = categoryService.findById(id);
        model.addAttribute("category", category);
        return "category/edit";
    }

    @PostMapping("/edit")
    @ResponseBody
    public Map<String, Object> edit(@RequestBody Category category) {
        return Result.of(categoryService.update(category), "修改成功", "修改失败");
    }

    @PostMapping("/delete/{id}")
    @ResponseBody
    public Map<String, Object> delete(@PathVariable Integer id) {
        return Result.of(categoryService.deleteById(id), "删除成功", "删除失败");
    }
}

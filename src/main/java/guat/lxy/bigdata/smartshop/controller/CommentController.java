package guat.lxy.bigdata.smartshop.controller;

import guat.lxy.bigdata.smartshop.entity.ProductComment;
import guat.lxy.bigdata.smartshop.entity.User;
import guat.lxy.bigdata.smartshop.service.ProductCommentService;
import guat.lxy.bigdata.smartshop.service.UserService;
import guat.lxy.bigdata.smartshop.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@Controller
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private ProductCommentService commentService;

    @Autowired
    private UserService userService;

    @PostMapping("/add")
    @ResponseBody
    public Map<String, Object> add(@RequestBody ProductComment comment, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        comment.setUserId(user.getId());
        return Result.of(commentService.save(comment), "评论成功", "评论失败");
    }

    @PostMapping("/delete/{id}")
    @ResponseBody
    public Map<String, Object> delete(@PathVariable Integer id, Principal principal) {
        return Result.of(commentService.deleteById(id, principal.getName()), "删除成功", "删除失败");
    }

    @PostMapping("/like/{id}")
    @ResponseBody
    public Map<String, Object> like(@PathVariable Integer id, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        boolean liked = commentService.toggleLike(id, user.getId());
        return liked ? Result.success("点赞成功") : Result.success("已取消点赞");
    }
}
